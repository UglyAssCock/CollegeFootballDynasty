package com.dynasty.cfb.domain

import com.dynasty.cfb.data.*
import kotlin.math.roundToInt
import kotlin.random.Random

object MatchEngine {

    fun calculateTeamRatings(
        college: College,
        roster: List<Player>,
        tactics: TacticsConfig? = null
    ): Triple<Int, Int, Int> {
        val active = roster.filter { it.isAvailableToPlay }.sortedBy { it.depthOrder }
        if (active.isEmpty()) {
            val fallback = college.prestigeScore
            return Triple(fallback, fallback, fallback)
        }
        val offPlayers = active.filter { it.position.group == "Offense" }.take(11)
        val defPlayers = active.filter { it.position.group == "Defense" }.take(11)
        val specPlayers = active.filter { it.position.group == "Special" }.take(2)

        val offBase = if (offPlayers.isNotEmpty()) offPlayers.map { it.overall }.average() else college.prestigeScore.toDouble()
        val defBase = if (defPlayers.isNotEmpty()) defPlayers.map { it.overall }.average() else college.prestigeScore.toDouble()
        val specBase = if (specPlayers.isNotEmpty()) specPlayers.map { it.overall }.average() else college.prestigeScore.toDouble()

        val schemeBonus = if (tactics != null) 2 else 0
        return Triple(
            (offBase + schemeBonus).roundToInt().coerceIn(50, 99),
            (defBase + schemeBonus).roundToInt().coerceIn(50, 99),
            specBase.roundToInt().coerceIn(50, 99)
        )
    }

    fun startLiveMatch(game: ScheduledGame): LiveMatchState {
        val openingEvent = PlayByPlayEvent(
            id = 1,
            quarter = 1,
            clockSeconds = 900,
            text = "Kickoff! ${game.titleLabel} is underway. Touchback to the 25-yard line."
        )
        return LiveMatchState(
            gameId = game.id,
            week = game.week,
            titleLabel = game.titleLabel,
            homeCollegeId = game.homeCollegeId,
            awayCollegeId = game.awayCollegeId,
            offenseIsHome = true,
            down = 1,
            distance = 10,
            ballOnYardLine = 25,
            playLog = listOf(openingEvent)
        )
    }

    fun simulateSinglePlay(
        state: LiveMatchState,
        homeCollege: College,
        awayCollege: College,
        homeRoster: List<Player>,
        awayRoster: List<Player>,
        userPlayCall: PlayCallType?,
        userTactics: TacticsConfig,
        difficulty: Difficulty,
        rng: Random = Random.Default
    ): LiveMatchState {
        if (state.isGameOver) return state

        val (homeOff, homeDef, homeSpec) = calculateTeamRatings(homeCollege, homeRoster, userTactics)
        val (awayOff, awayDef, awaySpec) = calculateTeamRatings(awayCollege, awayRoster, null)

        val offTeamAbbr = if (state.offenseIsHome) homeCollege.abbreviation else awayCollege.abbreviation
        val defTeamAbbr = if (state.offenseIsHome) awayCollege.abbreviation else homeCollege.abbreviation

        val rawOffRating = if (state.offenseIsHome) homeOff else (awayOff * difficulty.aiStatMultiplier).roundToInt()
        val rawDefRating = if (state.offenseIsHome) (awayDef * difficulty.aiStatMultiplier).roundToInt() else homeDef
        val specRating = if (state.offenseIsHome) homeSpec else awaySpec

        val offFatiguePenalty = (if (state.offenseIsHome) state.homeFatigue else state.awayFatigue) / 15
        val defFatiguePenalty = (if (state.offenseIsHome) state.awayFatigue else state.homeFatigue) / 15

        val effectiveDelta = (rawOffRating - offFatiguePenalty) - (rawDefRating - defFatiguePenalty)

        val chosenCall = userPlayCall ?: chooseAiPlayCall(state, userTactics, rng)
        val nextEventId = (state.playLog.firstOrNull()?.id ?: 0) + 1

        // Clock runoff
        val tempoFactor = (100 - userTactics.tempoSlider) / 5 // 0..20 sec extra
        val playSeconds = if (state.quarter <= 4) (18 + tempoFactor + rng.nextInt(0, 12)) else 0

        var newHomeScore = state.homeScore
        var newAwayScore = state.awayScore
        var newOffenseIsHome = state.offenseIsHome
        var newDown = state.down
        var newDistance = state.distance
        var newYardLine = state.ballOnYardLine
        var newHomeYards = state.homeTotalYards
        var newAwayYards = state.awayTotalYards
        var newHomeTurnovers = state.homeTurnovers
        var newAwayTurnovers = state.awayTurnovers
        var isScoring = false
        var isTurnover = false
        var isPenalty = false
        var playDescription: String

        when (chosenCall) {
            PlayCallType.PUNT -> {
                val puntDist = (36 + (specRating - 65) / 3 + rng.nextInt(-6, 10)).coerceIn(28, 58)
                val targetSpot = 100 - (state.ballOnYardLine + puntDist)
                newYardLine = if (targetSpot <= 0) 20 else targetSpot.coerceIn(5, 80)
                newOffenseIsHome = !state.offenseIsHome
                newDown = 1
                newDistance = 10
                playDescription = "$offTeamAbbr punts $puntDist yards. $defTeamAbbr takes over at their own $newYardLine."
            }

            PlayCallType.FIELD_GOAL -> {
                val kickDistance = (100 - state.ballOnYardLine) + 17
                val makeProb = (88 - (kickDistance - 30) * 1.6 + (specRating - 72) * 0.8).coerceIn(10.0, 98.0)
                val made = rng.nextDouble(100.0) < makeProb
                if (made) {
                    isScoring = true
                    if (state.offenseIsHome) newHomeScore += 3 else newAwayScore += 3
                    playDescription = "GOOD! $offTeamAbbr drills a $kickDistance-yard Field Goal! (+3 pts)"
                    newOffenseIsHome = !state.offenseIsHome
                    newDown = 1
                    newDistance = 10
                    newYardLine = 25
                } else {
                    playDescription = "NO GOOD! $offTeamAbbr misses the $kickDistance-yard Field Goal wide right."
                    newOffenseIsHome = !state.offenseIsHome
                    newDown = 1
                    newDistance = 10
                    newYardLine = (100 - state.ballOnYardLine).coerceIn(20, 80)
                }
            }

            else -> {
                // Check penalty (6% chance)
                val penaltyRoll = rng.nextInt(100)
                if (penaltyRoll < 6) {
                    isPenalty = true
                    val isOffensePenalty = rng.nextBoolean()
                    if (isOffensePenalty) {
                        newYardLine = (state.ballOnYardLine - 10).coerceAtLeast(1)
                        newDistance += 10
                        playDescription = "FLAG: Holding on $offTeamAbbr offense. 10-yard penalty, repeat ${ordinalDown(newDown)}."
                    } else {
                        val gained = 5
                        newYardLine = (state.ballOnYardLine + gained).coerceAtMost(99)
                        if (gained >= state.distance) {
                            newDown = 1
                            newDistance = (100 - newYardLine).coerceAtMost(10)
                            playDescription = "FLAG: Offsides on $defTeamAbbr defense (+5 yds). Automatic 1st down!"
                        } else {
                            newDistance -= gained
                            playDescription = "FLAG: Offsides on $defTeamAbbr defense (+5 yds). Repeat ${ordinalDown(newDown)}."
                        }
                    }
                } else {
                    // Normal offensive snap
                    val isPass = chosenCall in listOf(PlayCallType.SHORT_PASS, PlayCallType.DEEP_PASS, PlayCallType.SCREEN_PASS)
                    val turnoverChance = when (chosenCall) {
                        PlayCallType.DEEP_PASS -> 6
                        PlayCallType.SHORT_PASS, PlayCallType.SCREEN_PASS -> 3
                        else -> 2
                    }

                    if (rng.nextInt(100) < turnoverChance) {
                        isTurnover = true
                        if (state.offenseIsHome) newHomeTurnovers++ else newAwayTurnovers++
                        val typeStr = if (isPass) "INTERCEPTED" else "FUMBLE RECOVERED"
                        newOffenseIsHome = !state.offenseIsHome
                        newDown = 1
                        newDistance = 10
                        newYardLine = (100 - state.ballOnYardLine).coerceIn(10, 85)
                        playDescription = "TURNOVER! $offTeamAbbr play is $typeStr by $defTeamAbbr at the $newYardLine-yard line!"
                    } else {
                        val yardsGained = computeYardsGained(chosenCall, effectiveDelta, rng)
                        if (state.offenseIsHome) newHomeYards += yardsGained else newAwayYards += yardsGained

                        val targetYard = state.ballOnYardLine + yardsGained
                        if (targetYard >= 100) {
                            // Touchdown + PAT
                            isScoring = true
                            val patPoints = if (rng.nextInt(100) < 96) 7 else 6
                            if (state.offenseIsHome) newHomeScore += patPoints else newAwayScore += patPoints
                            val tdYards = 100 - state.ballOnYardLine
                            playDescription = "TOUCHDOWN $offTeamAbbr! ${chosenCall.label} bursts in from $tdYards yards out! (+$patPoints pts)"
                            newOffenseIsHome = !state.offenseIsHome
                            newDown = 1
                            newDistance = 10
                            newYardLine = 25
                        } else if (targetYard <= 0) {
                            // Safety
                            isScoring = true
                            if (state.offenseIsHome) newAwayScore += 2 else newHomeScore += 2
                            playDescription = "SAFETY! $defTeamAbbr tackles the ball carrier in the endzone! (+2 pts)"
                            newOffenseIsHome = !state.offenseIsHome
                            newDown = 1
                            newDistance = 10
                            newYardLine = 35
                        } else {
                            newYardLine = targetYard
                            if (yardsGained >= state.distance) {
                                newDown = 1
                                newDistance = (100 - newYardLine).coerceAtMost(10)
                                playDescription = "$offTeamAbbr (${chosenCall.label}): Gain of $yardsGained yds to the ${formatYardLine(newYardLine)}. FIRST DOWN!"
                            } else {
                                if (state.down >= 4) {
                                    isTurnover = true
                                    newOffenseIsHome = !state.offenseIsHome
                                    newDown = 1
                                    newDistance = 10
                                    newYardLine = (100 - targetYard).coerceIn(1, 99)
                                    playDescription = "TURNOVER ON DOWNS! $offTeamAbbr gains $yardsGained yds, short of the line to gain."
                                } else {
                                    newDown = state.down + 1
                                    newDistance = state.distance - yardsGained
                                    playDescription = "$offTeamAbbr (${chosenCall.label}): Gain of $yardsGained yds. Now ${ordinalDown(newDown)} & $newDistance at ${formatYardLine(newYardLine)}."
                                }
                            }
                        }
                    }
                }
            }
        }

        // Advance clock & quarter / overtime
        var newQuarter = state.quarter
        var newClock = state.clockSeconds - playSeconds
        var newIsGameOver = false
        var newOtPeriod = state.overtimePeriod

        if (state.quarter <= 4) {
            if (newClock <= 0) {
                if (state.quarter < 4) {
                    newQuarter = state.quarter + 1
                    newClock = 900
                } else {
                    if (newHomeScore == newAwayScore) {
                        newQuarter = 5
                        newOtPeriod = 1
                        newClock = 0
                        newDown = 1
                        newDistance = 10
                        newYardLine = 75
                        playDescription += " | End of Regulation TIED! Heading to College Overtime from the 25-yard line!"
                    } else {
                        newClock = 0
                        newIsGameOver = true
                        playDescription += " | FINAL WHISTLE! ${homeCollege.abbreviation} $newHomeScore - ${awayCollege.abbreviation} $newAwayScore"
                    }
                }
            }
        } else {
            // Overtime sudden resolution after scoring or turnover
            if (isScoring || isTurnover) {
                if (newHomeScore != newAwayScore && (!newOffenseIsHome)) {
                    newIsGameOver = true
                    playDescription += " | OVERTIME WINNER! Final: ${homeCollege.abbreviation} $newHomeScore - ${awayCollege.abbreviation} $newAwayScore"
                } else {
                    newOtPeriod++
                    newYardLine = 75
                    newDown = 1
                    newDistance = 10
                }
            }
        }

        val event = PlayByPlayEvent(
            id = nextEventId,
            quarter = newQuarter,
            clockSeconds = newClock.coerceAtLeast(0),
            text = playDescription,
            isScoringPlay = isScoring,
            isTurnover = isTurnover,
            isPenalty = isPenalty
        )

        return state.copy(
            homeScore = newHomeScore,
            awayScore = newAwayScore,
            quarter = newQuarter,
            clockSeconds = newClock.coerceAtLeast(0),
            offenseIsHome = newOffenseIsHome,
            down = newDown,
            distance = newDistance,
            ballOnYardLine = newYardLine,
            homeTotalYards = newHomeYards,
            awayTotalYards = newAwayYards,
            homeTurnovers = newHomeTurnovers,
            awayTurnovers = newAwayTurnovers,
            homeFatigue = (state.homeFatigue + 1).coerceAtMost(85),
            awayFatigue = (state.awayFatigue + 1).coerceAtMost(85),
            isGameOver = newIsGameOver,
            overtimePeriod = newOtPeriod,
            playLog = (listOf(event) + state.playLog).take(60)
        )
    }

    private fun chooseAiPlayCall(state: LiveMatchState, tactics: TacticsConfig, rng: Random): PlayCallType {
        if (state.down == 4) {
            val yardsToGoal = 100 - state.ballOnYardLine
            if (state.distance <= 2 && state.ballOnYardLine >= 55 && rng.nextInt(100) < tactics.fourthDownAggression) {
                return PlayCallType.INSIDE_RUN
            }
            return if (yardsToGoal <= 37) PlayCallType.FIELD_GOAL else PlayCallType.PUNT
        }
        val passRoll = rng.nextInt(100) < tactics.passRatioSlider
        return if (passRoll) {
            when (rng.nextInt(3)) {
                0 -> PlayCallType.SHORT_PASS
                1 -> PlayCallType.DEEP_PASS
                else -> PlayCallType.SCREEN_PASS
            }
        } else {
            if (rng.nextBoolean()) PlayCallType.INSIDE_RUN else PlayCallType.OUTSIDE_RUN
        }
    }

    private fun computeYardsGained(call: PlayCallType, ratingDelta: Int, rng: Random): Int {
        val ratingBonus = (ratingDelta / 5)
        return when (call) {
            PlayCallType.INSIDE_RUN -> (rng.nextInt(-2, 9) + ratingBonus).coerceIn(-4, 32)
            PlayCallType.OUTSIDE_RUN -> {
                val burst = if (rng.nextInt(100) < 12) rng.nextInt(12, 35) else rng.nextInt(-4, 10)
                (burst + ratingBonus).coerceIn(-6, 65)
            }
            PlayCallType.SHORT_PASS -> {
                if (rng.nextInt(100) < (66 + ratingBonus)) (rng.nextInt(3, 14) + ratingBonus / 2).coerceIn(1, 28)
                else 0 // Incomplete
            }
            PlayCallType.SCREEN_PASS -> {
                if (rng.nextInt(100) < 72) (rng.nextInt(-1, 16) + ratingBonus).coerceIn(-3, 40)
                else 0
            }
            PlayCallType.DEEP_PASS -> {
                if (rng.nextInt(100) < (38 + ratingBonus)) (rng.nextInt(18, 48) + ratingBonus).coerceIn(15, 80)
                else if (rng.nextInt(100) < 14) -rng.nextInt(4, 9) // Sack
                else 0
            }
            else -> rng.nextInt(1, 7)
        }
    }

    private fun ordinalDown(down: Int): String = when (down) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "4th"
    }

    private fun formatYardLine(yard: Int): String {
        return when {
            yard == 50 -> "50"
            yard < 50 -> "Own $yard"
            else -> "Opp ${100 - yard}"
        }
    }

    fun simulateFullMatchInstant(
        game: ScheduledGame,
        homeCollege: College,
        awayCollege: College,
        homeRoster: List<Player>,
        awayRoster: List<Player>,
        tactics: TacticsConfig,
        difficulty: Difficulty,
        rng: Random = Random.Default
    ): ScheduledGame {
        var live = startLiveMatch(game)
        var safetyCounter = 0
        while (!live.isGameOver && safetyCounter < 240) {
            live = simulateSinglePlay(live, homeCollege, awayCollege, homeRoster, awayRoster, null, tactics, difficulty, rng)
            safetyCounter++
        }
        if (live.homeScore == live.awayScore) {
            live = live.copy(homeScore = live.homeScore + 3, overtimePeriod = 1)
        }
        val winner = if (live.homeScore > live.awayScore) homeCollege.name else awayCollege.name
        val loser = if (live.homeScore > live.awayScore) awayCollege.name else homeCollege.name
        val winPts = maxOf(live.homeScore, live.awayScore)
        val losePts = minOf(live.homeScore, live.awayScore)

        return game.copy(
            played = true,
            homeScore = live.homeScore,
            awayScore = live.awayScore,
            overtimePeriods = live.overtimePeriod,
            summaryHeadline = "$winner defeats $loser $winPts-$losePts"
        )
    }
}
