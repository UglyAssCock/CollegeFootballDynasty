package com.dynasty.cfb.domain

import com.dynasty.cfb.data.*
import com.dynasty.cfb.util.DataGenerator
import kotlin.math.roundToInt
import kotlin.random.Random

object SeasonEngine {

    fun generateRegularSeasonSchedule(colleges: List<College>, seasonYear: Int): List<ScheduledGame> {
        val rng = Random(seasonYear * 97)
        val schedule = mutableListOf<ScheduledGame>()
        var gameCounter = 1

        val byConf = colleges.groupBy { it.conferenceId }

        // Weeks 1..4 Non-Conference pairings across all 120 teams
        for (week in 1..4) {
            val shuffled = colleges.shuffled(rng)
            for (i in 0 until shuffled.size - 1 step 2) {
                val home = shuffled[i]
                val away = shuffled[i + 1]
                schedule.add(
                    ScheduledGame(
                        id = "g_${seasonYear}_${gameCounter++}",
                        week = week,
                        homeCollegeId = home.id,
                        awayCollegeId = away.id,
                        isConferenceGame = home.conferenceId == away.conferenceId,
                        titleLabel = "Week $week Non-Conf"
                    )
                )
            }
        }

        // Weeks 5..12 Conference Round-Robin (8 conference games per team)
        byConf.values.forEach { confTeams ->
            val teamList = confTeams.toList()
            val n = teamList.size
            if (n >= 2) {
                val rot = teamList.toMutableList()
                for (round in 0 until 8) {
                    val week = 5 + round
                    for (pairIdx in 0 until n / 2) {
                        val t1 = rot[pairIdx]
                        val t2 = rot[n - 1 - pairIdx]
                        val home = if ((round + pairIdx) % 2 == 0) t1 else t2
                        val away = if ((round + pairIdx) % 2 == 0) t2 else t1
                        schedule.add(
                            ScheduledGame(
                                id = "g_${seasonYear}_${gameCounter++}",
                                week = week,
                                homeCollegeId = home.id,
                                awayCollegeId = away.id,
                                isConferenceGame = true,
                                titleLabel = "Conference Action"
                            )
                        )
                    }
                    // Rotate all except first element
                    val last = rot.removeAt(rot.size - 1)
                    rot.add(1, last)
                }
            }
        }

        return schedule
    }

    fun sortConferenceStandings(colleges: List<College>, conferenceId: String): List<College> {
        return colleges
            .filter { it.conferenceId == conferenceId }
            .sortedWith(
                compareByDescending<College> { it.confWins }
                    .thenBy { it.confLosses }
                    .thenByDescending { it.wins }
                    .thenBy { it.losses }
                    .thenByDescending { it.pointsFor - it.pointsAgainst }
                    .thenByDescending { it.prestigeScore }
            )
    }

    fun recalculateTop25Poll(colleges: List<College>): List<College> {
        val rankedAll = colleges.sortedWith(
            compareByDescending<College> {
                val winPct = if (it.wins + it.losses > 0) it.wins.toDouble() / (it.wins + it.losses) else 0.5
                val margin = (it.pointsFor - it.pointsAgainst).coerceIn(-150, 250) * 0.15
                it.prestigeScore * 0.65 + (it.wins * 14.0) - (it.losses * 18.0) + (winPct * 20.0) + margin
            }
        )
        return rankedAll.mapIndexed { idx, col ->
            col.copy(pollRank = if (idx < 25) idx + 1 else 99)
        }
    }

    fun generateConferenceChampionshipGames(colleges: List<College>, conferences: List<Conference>, seasonYear: Int): List<ScheduledGame> {
        var idx = 1
        return conferences.mapNotNull { conf ->
            val standings = sortConferenceStandings(colleges, conf.id)
            if (standings.size >= 2) {
                ScheduledGame(
                    id = "cc_${seasonYear}_${idx++}",
                    week = 13,
                    homeCollegeId = standings[0].id,
                    awayCollegeId = standings[1].id,
                    isConferenceGame = true,
                    titleLabel = "${conf.shortName} Championship"
                )
            } else null
        }
    }

    fun generatePostseasonWeekGames(
        week: Int,
        colleges: List<College>,
        existingSchedule: List<ScheduledGame>,
        playoffSize: Int,
        bowlPrefix: String,
        seasonYear: Int
    ): List<ScheduledGame> {
        val ranked = colleges.sortedWith(
            compareBy<College> { it.pollRank }.thenByDescending { it.wins }.thenByDescending { it.prestigeScore }
        )

        val games = mutableListOf<ScheduledGame>()
        if (week == 14) {
            // Opening Playoff Round + Bowl games for teams outside playoff
            val seedsCount = playoffSize.coerceIn(4, 16)
            val playoffTeams = ranked.take(seedsCount)
            val half = playoffTeams.size / 2
            for (i in 0 until half) {
                val highSeed = playoffTeams[i]
                val lowSeed = playoffTeams[playoffTeams.size - 1 - i]
                games.add(
                    ScheduledGame(
                        id = "cfp_${seasonYear}_w14_$i",
                        week = 14,
                        homeCollegeId = highSeed.id,
                        awayCollegeId = lowSeed.id,
                        isConferenceGame = false,
                        titleLabel = "CFP Round 1 (#${i + 1} vs #${playoffTeams.size - i})"
                    )
                )
            }
            // Add 8 showcase Bowl Games for non-playoff top teams
            val bowlTeams = ranked.drop(seedsCount).take(16)
            for (b in 0 until bowlTeams.size - 1 step 2) {
                games.add(
                    ScheduledGame(
                        id = "bowl_${seasonYear}_$b",
                        week = 14,
                        homeCollegeId = bowlTeams[b].id,
                        awayCollegeId = bowlTeams[b + 1].id,
                        isConferenceGame = false,
                        titleLabel = "$bowlPrefix Classic"
                    )
                )
            }
        } else if (week in 15..17) {
            val prevPlayoffGames = existingSchedule.filter { it.week == week - 1 && it.id.startsWith("cfp_") && it.played }
            val winners = prevPlayoffGames.map { g ->
                if (g.homeScore >= g.awayScore) g.homeCollegeId else g.awayCollegeId
            }
            if (winners.size >= 2) {
                val stageTitle = if (winners.size == 2) "CFP National Championship" else "CFP Quarterfinal/Semifinal"
                for (i in 0 until winners.size - 1 step 2) {
                    games.add(
                        ScheduledGame(
                            id = "cfp_${seasonYear}_w${week}_$i",
                            week = week,
                            homeCollegeId = winners[i],
                            awayCollegeId = winners[i + 1],
                            isConferenceGame = false,
                            titleLabel = stageTitle
                        )
                    )
                }
            }
        }
        return games
    }

    fun advanceSingleWeek(state: CareerState): CareerState {
        if (state.currentWeek >= 18) return advanceToNextSeason(state)

        val rng = Random(state.seasonYear * 100 + state.currentWeek)
        val collegeMap = state.colleges.associateBy { it.id }.toMutableMap()
        val rostersByTeam = state.players.groupBy { it.collegeId }.toMutableMap()
        val updatedSchedule = state.schedule.toMutableList()
        val newNews = mutableListOf<NewsItem>()

        // 1. Simulate all unplayed games in currentWeek
        for (i in updatedSchedule.indices) {
            val g = updatedSchedule[i]
            if (g.week == state.currentWeek && !g.played) {
                val home = collegeMap[g.homeCollegeId] ?: continue
                val away = collegeMap[g.awayCollegeId] ?: continue
                val hRoster = rostersByTeam[home.id] ?: emptyList()
                val aRoster = rostersByTeam[away.id] ?: emptyList()

                val simmed = MatchEngine.simulateFullMatchInstant(
                    game = g,
                    homeCollege = home,
                    awayCollege = away,
                    homeRoster = hRoster,
                    awayRoster = aRoster,
                    tactics = state.tactics,
                    difficulty = state.difficulty,
                    rng = rng
                )
                updatedSchedule[i] = simmed
                applyGameResultToCollegesAndStats(simmed, collegeMap, rostersByTeam, rng)

                if (home.id == state.userCollegeId || away.id == state.userCollegeId || home.pollRank <= 10 || away.pollRank <= 10) {
                    newNews.add(
                        NewsItem(
                            id = "news_${state.seasonYear}_${state.currentWeek}_${g.id}",
                            season = state.seasonYear,
                            week = state.currentWeek,
                            category = "GAME",
                            headline = simmed.summaryHeadline,
                            body = "${g.titleLabel}: ${home.abbreviation} (${simmed.homeScore}) vs ${away.abbreviation} (${simmed.awayScore})"
                        )
                    )
                }
            }
        }

        // 2. Process Recruiting Board
        val userCol = collegeMap[state.userCollegeId]!!
        val recBonus = state.difficulty.recruitBonus * (1.0 + (state.coach.recruitingRating - 70) * 0.015)
        val updatedRecruits = state.recruits.map { rec ->
            if (rec.committedCollegeId != null) rec
            else {
                val userGain = ((rec.userWeeklyHours * 0.45 + (if (rec.userOfferedScholarship) 12 else 0) + (if (rec.userScheduledVisit) 20 else 0)) * recBonus).roundToInt()
                val rivalGain = rng.nextInt(8, 28)
                val newUserPts = rec.userPointsAccumulated + userGain
                val newRivalPts = rec.rivalPointsAccumulated + rivalGain
                val newScout = if (rec.userWeeklyHours >= 25) (rec.scoutedPercent + 50).coerceAtMost(100) else rec.scoutedPercent

                var committed: String? = null
                if (newUserPts >= 220 && newUserPts > newRivalPts + 15 && rec.userOfferedScholarship) {
                    committed = state.userCollegeId
                    newNews.add(
                        NewsItem(
                            id = "rec_commit_${rec.id}",
                            season = state.seasonYear,
                            week = state.currentWeek,
                            category = "RECRUITING",
                            headline = "${rec.stars}★ ${rec.position.code} ${rec.fullName} commits to ${userCol.name}!",
                            body = "A huge recruiting victory over rival programs."
                        )
                    )
                } else if (newRivalPts >= 240 && newRivalPts > newUserPts + 25) {
                    committed = rec.topRivalCollegeId
                }

                rec.copy(
                    scoutedPercent = newScout,
                    userPointsAccumulated = newUserPts,
                    rivalPointsAccumulated = newRivalPts,
                    userScheduledVisit = false,
                    committedCollegeId = committed
                )
            }
        }

        // 3. Process Player Training, Injuries Recovery & New Minor Injuries
        val devBoostProb = 8 + state.coach.developmentRating / 10 + userCol.trainingFacilityLevel
        val updatedPlayers = rostersByTeam.values.flatten().map { p ->
            var nextInjuryWeeks = (p.injuryWeeks - 1).coerceAtLeast(0)
            var nextDesc = if (nextInjuryWeeks == 0) "Healthy" else p.injuryDescription

            if (state.settings.injuriesEnabled && p.collegeId == state.userCollegeId && nextInjuryWeeks == 0 && p.depthOrder == 1) {
                if (rng.nextInt(1000) < (18 - userCol.medicalFacilityLevel)) {
                    nextInjuryWeeks = rng.nextInt(1, 4)
                    nextDesc = listOf("Sprained Ankle", "Bruised Shoulder", "Hamstring Strain")[rng.nextInt(3)]
                }
            }

            // Weekly in-season skill tick for user team
            val boost = if (p.collegeId == state.userCollegeId && p.overall < p.potential && rng.nextInt(100) < devBoostProb) 1 else 0
            p.copy(
                injuryWeeks = nextInjuryWeeks,
                injuryDescription = nextDesc,
                awareness = (p.awareness + boost).coerceAtMost(99)
            )
        }

        // 4. Recalculate Top 25 Poll
        val reRankedColleges = recalculateTop25Poll(collegeMap.values.toList())
        val nextWeek = state.currentWeek + 1

        // 5. Generate Postseason games if entering Week 13, 14, 15, 16, or 17
        if (nextWeek == 13) {
            updatedSchedule.addAll(generateConferenceChampionshipGames(reRankedColleges, state.conferences, state.seasonYear))
        } else if (nextWeek in 14..17) {
            updatedSchedule.addAll(
                generatePostseasonWeekGames(
                    week = nextWeek,
                    colleges = reRankedColleges,
                    existingSchedule = updatedSchedule,
                    playoffSize = state.settings.playoffTeamsCount,
                    bowlPrefix = state.settings.bowlGamesTitlePrefix,
                    seasonYear = state.seasonYear
                )
            )
        }

        // 6. Generate Season Awards & Job Offers at end of Postseason (entering Week 18 Offseason)
        val updatedAwards = state.awardsHistory.toMutableList()
        val updatedDraft = state.draftHistory.toMutableList()
        var jobOffers = state.pendingJobOffers

        if (nextWeek == 18) {
            val topPlayer = updatedPlayers.maxByOrNull {
                it.passYards * 0.04 + it.passTds * 4 + it.rushYards * 0.08 + it.rushTds * 6 + it.receivingYards * 0.08 + it.tackles * 1.2 + it.sacks * 5 + it.overall * 1.5
            }
            if (topPlayer != null) {
                val schoolName = reRankedColleges.find { it.id == topPlayer.collegeId }?.name ?: "University"
                updatedAwards.add(
                    0,
                    AwardWinner(
                        season = state.seasonYear,
                        awardName = "National Player of the Year",
                        playerName = topPlayer.fullName,
                        position = topPlayer.position,
                        collegeName = schoolName,
                        statLine = "${topPlayer.overall} OVR | ${topPlayer.passYards + topPlayer.rushYards + topPlayer.receivingYards} Total Yds"
                    )
                )
            }

            // NFL Draft top 10 seniors/juniors
            val draftEligible = updatedPlayers
                .filter { it.yearClass == YearClass.SR || (it.yearClass == YearClass.JR && it.overall >= 88) }
                .sortedByDescending { it.overall }
                .take(10)
            draftEligible.forEachIndexed { dIdx, dp ->
                val cName = reRankedColleges.find { it.id == dp.collegeId }?.name ?: "College"
                updatedDraft.add(
                    0,
                    DraftPick(
                        season = state.seasonYear,
                        round = 1,
                        pickNumber = dIdx + 1,
                        playerName = dp.fullName,
                        position = dp.position,
                        collegeName = cName,
                        overall = dp.overall
                    )
                )
            }

            // Coaching Carousel Job Offers
            jobOffers = reRankedColleges
                .filter { it.id != state.userCollegeId && it.prestigeScore <= userCol.prestigeScore + 12 }
                .shuffled(rng)
                .take(4)
                .map { col ->
                    JobOffer(
                        collegeId = col.id,
                        collegeName = col.name,
                        prestigeStars = col.prestigeStars,
                        salaryMillions = ((col.prestigeStars * 1.35) * 10).roundToInt() / 10.0,
                        contractYears = 4,
                        nilPoolMillions = col.nilPoolMillions
                    )
                }
        }

        // 7. Update Coach Profile & Achievements
        val refreshedUserCol = reRankedColleges.find { it.id == state.userCollegeId } ?: userCol
        val securityDelta = (refreshedUserCol.wins - refreshedUserCol.losses) * 2
        val updatedCoach = state.coach.copy(
            jobSecurity = (state.coach.jobSecurity + securityDelta).coerceIn(15, 100),
            xp = state.coach.xp + 120
        )

        val updatedAchievements = evaluateAchievements(
            state.achievements,
            refreshedUserCol,
            updatedRecruits,
            updatedAwards,
            state.seasonYear
        )

        return state.copy(
            currentWeek = nextWeek,
            colleges = reRankedColleges,
            players = updatedPlayers,
            recruits = updatedRecruits,
            schedule = updatedSchedule,
            activeMatch = null,
            coach = updatedCoach,
            newsFeed = (newNews + state.newsFeed).take(80),
            awardsHistory = updatedAwards,
            draftHistory = updatedDraft,
            achievements = updatedAchievements,
            pendingJobOffers = jobOffers,
            weeklyRecruitingHoursRemaining = refreshedUserCol.weeklyRecruitingHoursMax
        )
    }

    fun applyGameResultToCollegesAndStats(
        game: ScheduledGame,
        collegeMap: MutableMap<String, College>,
        rostersByTeam: MutableMap<String, List<Player>>,
        rng: Random
    ) {
        val home = collegeMap[game.homeCollegeId] ?: return
        val away = collegeMap[game.awayCollegeId] ?: return
        val homeWon = game.homeScore >= game.awayScore

        collegeMap[home.id] = home.copy(
            wins = home.wins + if (homeWon) 1 else 0,
            losses = home.losses + if (homeWon) 0 else 1,
            confWins = home.confWins + if (game.isConferenceGame && homeWon) 1 else 0,
            confLosses = home.confLosses + if (game.isConferenceGame && !homeWon) 1 else 0,
            pointsFor = home.pointsFor + game.homeScore,
            pointsAgainst = home.pointsAgainst + game.awayScore
        )
        collegeMap[away.id] = away.copy(
            wins = away.wins + if (!homeWon) 1 else 0,
            losses = away.losses + if (!homeWon) 0 else 1,
            confWins = away.confWins + if (game.isConferenceGame && !homeWon) 1 else 0,
            confLosses = away.confLosses + if (game.isConferenceGame && homeWon) 1 else 0,
            pointsFor = away.pointsFor + game.awayScore,
            pointsAgainst = away.pointsAgainst + game.homeScore
        )

        // Credit individual player stats for starters
        listOf(home.id to game.homeScore, away.id to game.awayScore).forEach { (cid, pts) ->
            val teamRoster = rostersByTeam[cid] ?: return@forEach
            val updated = teamRoster.map { p ->
                if (!p.isAvailableToPlay || p.depthOrder > 2) p
                else {
                    when (p.position) {
                        Position.QB -> if (p.depthOrder == 1) p.copy(
                            gamesPlayedThisSeason = p.gamesPlayedThisSeason + 1,
                            passYards = p.passYards + rng.nextInt(160, 340),
                            passTds = p.passTds + (pts / 10).coerceAtLeast(1)
                        ) else p
                        Position.RB -> if (p.depthOrder == 1) p.copy(
                            gamesPlayedThisSeason = p.gamesPlayedThisSeason + 1,
                            rushYards = p.rushYards + rng.nextInt(55, 145),
                            rushTds = p.rushTds + (pts / 14)
                        ) else p
                        Position.WR, Position.TE -> if (p.depthOrder <= 2) p.copy(
                            gamesPlayedThisSeason = p.gamesPlayedThisSeason + 1,
                            receivingYards = p.receivingYards + rng.nextInt(40, 120),
                            receivingTds = p.receivingTds + rng.nextInt(0, 2)
                        ) else p
                        Position.DL, Position.LB, Position.CB, Position.S -> if (p.depthOrder == 1) p.copy(
                            gamesPlayedThisSeason = p.gamesPlayedThisSeason + 1,
                            tackles = p.tackles + rng.nextInt(3, 11),
                            sacks = p.sacks + if (rng.nextInt(100) < 25) 1 else 0
                        ) else p
                        else -> p.copy(gamesPlayedThisSeason = p.gamesPlayedThisSeason + 1)
                    }
                }
            }
            rostersByTeam[cid] = updated
        }
    }

    fun advanceToNextSeason(state: CareerState): CareerState {
        val nextYear = state.seasonYear + 1
        val rng = Random(nextYear)
        val userCol = state.colleges.find { it.id == state.userCollegeId } ?: state.colleges.first()

        // 1. Record Season History
        val natChampGame = state.schedule.lastOrNull { it.titleLabel.contains("National Championship") && it.played }
        val champId = if (natChampGame != null) {
            if (natChampGame.homeScore >= natChampGame.awayScore) natChampGame.homeCollegeId else natChampGame.awayCollegeId
        } else {
            state.colleges.minByOrNull { it.pollRank }?.id ?: userCol.id
        }
        val champName = state.colleges.find { it.id == champId }?.name ?: userCol.name
        val heisman = state.awardsHistory.firstOrNull { it.season == state.seasonYear }?.playerName ?: "N/A"
        val userCommitsCount = state.recruits.count { it.committedCollegeId == state.userCollegeId }

        val historyEntry = SeasonHistoryEntry(
            seasonYear = state.seasonYear,
            userCollegeName = userCol.name,
            userRecord = "${userCol.wins}-${userCol.losses}",
            userFinalRank = userCol.pollRank,
            nationalChampionName = champName,
            heismanWinner = heisman,
            recruitingClassRank = (25 - userCommitsCount * 2).coerceIn(1, 60)
        )

        // 2. Graduate Seniors & Develop Returning Players
        val returningPlayers = state.players.mapNotNull { p ->
            if (p.yearClass == YearClass.SR && !p.isRedshirted) {
                null // Graduated
            } else {
                val nextClass = if (p.isRedshirted) p.yearClass else when (p.yearClass) {
                    YearClass.FR -> YearClass.SO
                    YearClass.SO -> YearClass.JR
                    YearClass.JR -> YearClass.SR
                    YearClass.SR -> YearClass.SR
                }
                val growth = rng.nextInt(2, 6)
                p.copy(
                    yearClass = nextClass,
                    isRedshirted = false,
                    hasUsedRedshirt = p.hasUsedRedshirt || p.isRedshirted,
                    gamesPlayedThisSeason = 0,
                    injuryWeeks = 0,
                    injuryDescription = "Healthy",
                    speed = (p.speed + growth / 2).coerceAtMost(p.potential),
                    strength = (p.strength + growth).coerceAtMost(p.potential),
                    awareness = (p.awareness + growth).coerceAtMost(p.potential),
                    throwPower = (p.throwPower + growth).coerceAtMost(p.potential),
                    throwAccuracy = (p.throwAccuracy + growth).coerceAtMost(p.potential),
                    carrying = (p.carrying + growth).coerceAtMost(p.potential),
                    catching = (p.catching + growth).coerceAtMost(p.potential),
                    blocking = (p.blocking + growth).coerceAtMost(p.potential),
                    tackling = (p.tackling + growth).coerceAtMost(p.potential),
                    coverage = (p.coverage + growth).coerceAtMost(p.potential),
                    passYards = 0, passTds = 0, interceptionsThrown = 0,
                    rushYards = 0, rushTds = 0, receivingYards = 0, receivingTds = 0,
                    tackles = 0, sacks = 0, interceptionsCaught = 0
                )
            }
        }.toMutableList()

        // 3. Enroll Committed Recruits & Replenish Rosters
        var newPlayerCounter = nextYear * 10000
        state.recruits.forEach { rec ->
            val targetTeam = rec.committedCollegeId ?: rec.topRivalCollegeId
            returningPlayers.add(
                DataGenerator.createPlayerWithTarget(
                    id = "p_${newPlayerCounter++}",
                    collegeId = targetTeam,
                    pos = rec.position,
                    yearClass = rec.previousYearClass,
                    targetBase = rec.trueOverall,
                    rng = rng
                ).copy(
                    firstName = rec.firstName,
                    lastName = rec.lastName,
                    stars = rec.stars,
                    potential = rec.truePotential
                )
            )
        }

        // Auto-sort depth charts per college
        val balancedRosters = returningPlayers
            .groupBy { it.collegeId to it.position }
            .values
            .flatMap { group ->
                group.sortedByDescending { it.overall }.mapIndexed { idx, pl ->
                    pl.copy(depthOrder = idx + 1)
                }
            }

        // 4. Reset College Season Records & Generate New Schedule
        val resetColleges = state.colleges.map { c ->
            val revenueGain = (c.wins * 0.45)
            c.copy(
                wins = 0,
                losses = 0,
                confWins = 0,
                confLosses = 0,
                pointsFor = 0,
                pointsAgainst = 0,
                budgetMillions = ((c.budgetMillions + revenueGain) * 10).roundToInt() / 10.0
            )
        }

        val newSchedule = generateRegularSeasonSchedule(resetColleges, nextYear)
        val newRecruits = DataGenerator.generateRecruitClass(resetColleges, nextYear)

        val updatedCoach = state.coach.copy(
            age = state.coach.age + 1,
            level = state.coach.level + 1,
            offenseRating = (state.coach.offenseRating + 1).coerceAtMost(99),
            defenseRating = (state.coach.defenseRating + 1).coerceAtMost(99),
            recruitingRating = (state.coach.recruitingRating + 1).coerceAtMost(99),
            developmentRating = (state.coach.developmentRating + 1).coerceAtMost(99),
            careerWins = state.coach.careerWins + userCol.wins,
            careerLosses = state.coach.careerLosses + userCol.losses,
            nationalTitles = state.coach.nationalTitles + if (champId == state.userCollegeId) 1 else 0,
            contractYearsLeft = (state.coach.contractYearsLeft - 1).coerceAtLeast(2)
        )

        return state.copy(
            seasonYear = nextYear,
            currentWeek = 1,
            coach = updatedCoach,
            colleges = resetColleges,
            players = balancedRosters,
            recruits = newRecruits,
            schedule = newSchedule,
            activeMatch = null,
            seasonHistory = listOf(historyEntry) + state.seasonHistory,
            pendingJobOffers = emptyList(),
            weeklyRecruitingHoursRemaining = userCol.weeklyRecruitingHoursMax,
            newsFeed = listOf(
                NewsItem(
                    id = "news_new_season_$nextYear",
                    season = nextYear,
                    week = 1,
                    category = "POLL",
                    headline = "Welcome to the $nextYear College Football Season!",
                    body = "Fresh recruits have arrived on campus and Week 1 kicks off now."
                )
            ) + state.newsFeed.take(40)
        )
    }

    private fun evaluateAchievements(
        existing: List<Achievement>,
        userCol: College,
        recruits: List<Recruit>,
        awards: List<AwardWinner>,
        seasonYear: Int
    ): List<Achievement> {
        return existing.map { ach ->
            if (ach.unlocked) ach
            else {
                val shouldUnlock = when (ach.id) {
                    "ach_first_win" -> userCol.wins >= 1
                    "ach_top_25" -> userCol.pollRank <= 25
                    "ach_5star" -> recruits.any { it.committedCollegeId == userCol.id && it.stars == 5 }
                    "ach_conf_champ" -> userCol.confWins >= 7
                    "ach_playoff" -> userCol.pollRank <= 12 && userCol.wins >= 9
                    "ach_natty" -> userCol.pollRank == 1 && userCol.wins >= 12
                    "ach_heisman" -> awards.any { it.collegeName == userCol.name }
                    else -> false
                }
                if (shouldUnlock) ach.copy(unlocked = true, unlockedSeason = seasonYear) else ach
            }
        }
    }
}
