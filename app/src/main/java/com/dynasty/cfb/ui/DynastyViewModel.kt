package com.dynasty.cfb.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dynasty.cfb.data.*
import com.dynasty.cfb.domain.MatchEngine
import com.dynasty.cfb.domain.SeasonEngine
import com.dynasty.cfb.editor.EditorValidator
import com.dynasty.cfb.util.DataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DynastyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DynastyDatabase.getInstance(application)
    private val dao = db.saveSlotDao()
    private val prefs = PreferencesManager(application)

    val saveSlots: StateFlow<List<SaveSlotEntity>> = dao.observeAllSaveSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _careerState = MutableStateFlow<CareerState?>(null)
    val careerState: StateFlow<CareerState?> = _careerState.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    // Preview list of 120 colleges for New Career Picker
    val previewColleges: List<College> by lazy {
        val confs = DataGenerator.generateDefaultConferences()
        DataGenerator.generateColleges(confs)
    }

    private val advanceMutex = Mutex()

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun showBanner(message: String) {
        _statusBanner.value = message
    }

    fun startNewCareer(
        slotId: Int,
        coachName: String,
        difficulty: Difficulty,
        startingCollegeId: String,
        offenseScheme: OffenseScheme,
        defenseScheme: DefenseScheme,
        onReady: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val newState = withContext(Dispatchers.Default) {
                val confs = DataGenerator.generateDefaultConferences()
                val colleges = DataGenerator.generateColleges(confs)
                val userCol = colleges.find { it.id == startingCollegeId } ?: colleges.first()
                val rosters = DataGenerator.generateAllRosters(colleges, seed = 2026)
                val recruits = DataGenerator.generateRecruitClass(colleges, seasonYear = 2026)
                val schedule = SeasonEngine.generateRegularSeasonSchedule(colleges, seasonYear = 2026)
                val coach = CoachProfile(
                    name = coachName.ifBlank { "Coach Dynasty" },
                    annualSalaryMillions = (userCol.prestigeStars * 1.1).coerceAtLeast(1.2),
                    buyoutMillions = (userCol.prestigeStars * 2.0),
                    requiredWinsTarget = (userCol.prestigeStars + 3).coerceIn(4, 10)
                )
                CareerState(
                    saveSlotId = slotId,
                    careerName = coach.name,
                    seasonYear = 2026,
                    currentWeek = 1,
                    difficulty = difficulty,
                    userCollegeId = userCol.id,
                    coach = coach,
                    tactics = TacticsConfig(offenseScheme = offenseScheme, defenseScheme = defenseScheme),
                    staff = DataGenerator.generateInitialStaff(),
                    conferences = confs,
                    colleges = colleges,
                    players = rosters,
                    recruits = recruits,
                    schedule = schedule,
                    achievements = DataGenerator.generateDefaultAchievements(),
                    weeklyRecruitingHoursRemaining = userCol.weeklyRecruitingHoursMax,
                    newsFeed = listOf(
                        NewsItem(
                            id = "welcome_1",
                            season = 2026,
                            week = 1,
                            category = "POLL",
                            headline = "${coach.name} named Head Coach at ${userCol.name}!",
                            body = "Fans in ${userCol.state} are excited for the new ${offenseScheme.label} era."
                        )
                    )
                )
            }
            _careerState.value = newState
            persistCareerToRoom(newState)
            prefs.setLastActiveSlot(slotId)
            _isBusy.value = false
            onReady()
        }
    }

    fun loadSaveSlot(slotId: Int, onLoaded: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            val entity = dao.getSaveSlot(slotId)
            if (entity != null) {
                val res = withContext(Dispatchers.Default) {
                    EditorValidator.importCareerFromJson(entity.serializedCareerJson)
                }
                res.onSuccess { loaded ->
                    _careerState.value = loaded
                    prefs.setLastActiveSlot(slotId)
                    _isBusy.value = false
                    onLoaded()
                }.onFailure { err ->
                    _statusBanner.value = "Failed to load slot $slotId: ${err.message}"
                    _isBusy.value = false
                }
            } else {
                _statusBanner.value = "Save slot $slotId is empty."
                _isBusy.value = false
            }
        }
    }

    fun deleteSaveSlot(slotId: Int) {
        viewModelScope.launch {
            dao.deleteSaveSlot(slotId)
            _statusBanner.value = "Deleted Save Slot $slotId."
        }
    }

    fun manualSaveCurrentCareer(targetSlotId: Int? = null) {
        val current = _careerState.value ?: return
        val slot = targetSlotId ?: current.saveSlotId
        viewModelScope.launch {
            val updated = current.copy(saveSlotId = slot)
            _careerState.value = updated
            persistCareerToRoom(updated)
            _statusBanner.value = "Saved Dynasty to Slot #$slot."
        }
    }

    private suspend fun persistCareerToRoom(state: CareerState) {
        withContext(Dispatchers.IO) {
            val userCol = state.colleges.find { it.id == state.userCollegeId }
            val json = EditorValidator.exportCareerToJson(state)
            val entity = SaveSlotEntity(
                slotId = state.saveSlotId,
                coachName = state.coach.name,
                collegeName = userCol?.name ?: "University",
                seasonYear = state.seasonYear,
                currentWeek = state.currentWeek,
                recordText = "${userCol?.wins ?: 0}-${userCol?.losses ?: 0}",
                updatedAtMillis = System.currentTimeMillis(),
                serializedCareerJson = json
            )
            dao.upsertSaveSlot(entity)
        }
    }

    // Protected against double taps with Mutex + _isBusy check
    fun advanceWeek() {
        if (_isBusy.value) return
        viewModelScope.launch {
            advanceMutex.withLock {
                if (_isBusy.value) return@withLock
                val current = _careerState.value ?: return@withLock
                _isBusy.value = true
                val nextState = withContext(Dispatchers.Default) {
                    SeasonEngine.advanceSingleWeek(current)
                }
                _careerState.value = nextState
                if (nextState.settings.autoSaveEnabled) {
                    persistCareerToRoom(nextState)
                }
                _statusBanner.value = if (nextState.currentWeek == 1 && nextState.seasonYear > current.seasonYear) {
                    "Entered ${nextState.seasonYear} Season!"
                } else {
                    "Advanced to Week ${nextState.currentWeek}."
                }
                _isBusy.value = false
            }
        }
    }

    // Roster & Depth Chart Management
    fun autoSortDepthChart() {
        val current = _careerState.value ?: return
        val updatedPlayers = current.players
            .groupBy { it.collegeId to it.position }
            .values
            .flatMap { group ->
                if (group.first().collegeId == current.userCollegeId) {
                    group.sortedWith(
                        compareByDescending<Player> { it.isAvailableToPlay }
                            .thenByDescending { it.overall }
                    ).mapIndexed { idx, p -> p.copy(depthOrder = idx + 1) }
                } else group
            }
        _careerState.value = current.copy(players = updatedPlayers)
        _statusBanner.value = "Depth chart automatically optimized by Overall & Availability."
    }

    fun promoteStarter(playerId: String) {
        val current = _careerState.value ?: return
        val target = current.players.find { it.id == playerId } ?: return
        val updated = current.players.map { p ->
            if (p.collegeId == target.collegeId && p.position == target.position) {
                when {
                    p.id == target.id -> p.copy(depthOrder = 1)
                    p.depthOrder == 1 -> p.copy(depthOrder = target.depthOrder)
                    else -> p
                }
            } else p
        }
        _careerState.value = current.copy(players = updated)
        _statusBanner.value = "${target.fullName} set as #1 Starter at ${target.position.code}."
    }

    fun toggleRedshirt(playerId: String) {
        val current = _careerState.value ?: return
        val target = current.players.find { it.id == playerId } ?: return
        if (target.hasUsedRedshirt) {
            _statusBanner.value = "${target.fullName} has already used a redshirt season."
            return
        }
        if (target.gamesPlayedThisSeason > 4) {
            _statusBanner.value = "Cannot redshirt ${target.fullName}: played more than 4 games this season."
            return
        }
        val updated = current.players.map {
            if (it.id == playerId) it.copy(isRedshirted = !it.isRedshirted) else it
        }
        _careerState.value = current.copy(players = updated)
    }

    fun toggleCaptain(playerId: String) {
        val current = _careerState.value ?: return
        val updated = current.players.map {
            if (it.id == playerId) it.copy(isCaptain = !it.isCaptain) else it
        }
        _careerState.value = current.copy(players = updated)
    }

    // Recruiting Actions
    fun scoutRecruit(recruitId: String) {
        val current = _careerState.value ?: return
        val cost = 25
        if (current.weeklyRecruitingHoursRemaining < cost) {
            _statusBanner.value = "Need $cost Recruiting Hours to scout."
            return
        }
        val target = current.recruits.find { it.id == recruitId } ?: return
        if (target.scoutedPercent >= 100) {
            _statusBanner.value = "${target.fullName} is already 100% scouted."
            return
        }
        val updatedRecruits = current.recruits.map {
            if (it.id == recruitId) it.copy(scoutedPercent = (it.scoutedPercent + 50).coerceAtMost(100)) else it
        }
        _careerState.value = current.copy(
            recruits = updatedRecruits,
            weeklyRecruitingHoursRemaining = current.weeklyRecruitingHoursRemaining - cost
        )
    }

    fun offerScholarship(recruitId: String) {
        val current = _careerState.value ?: return
        val cost = 35
        if (current.weeklyRecruitingHoursRemaining < cost) {
            _statusBanner.value = "Need $cost Recruiting Hours to offer a scholarship."
            return
        }
        val updatedRecruits = current.recruits.map {
            if (it.id == recruitId && !it.userOfferedScholarship) {
                it.copy(userOfferedScholarship = true, userPointsAccumulated = it.userPointsAccumulated + 20)
            } else it
        }
        _careerState.value = current.copy(
            recruits = updatedRecruits,
            weeklyRecruitingHoursRemaining = current.weeklyRecruitingHoursRemaining - cost
        )
        _statusBanner.value = "Scholarship offered!"
    }

    fun adjustRecruitWeeklyHours(recruitId: String, delta: Int) {
        val current = _careerState.value ?: return
        val target = current.recruits.find { it.id == recruitId } ?: return
        val newHours = (target.userWeeklyHours + delta).coerceIn(0, 100)
        val actualDelta = newHours - target.userWeeklyHours
        if (actualDelta > current.weeklyRecruitingHoursRemaining) {
            _statusBanner.value = "Not enough weekly recruiting hours left."
            return
        }
        val updatedRecruits = current.recruits.map {
            if (it.id == recruitId) it.copy(userWeeklyHours = newHours) else it
        }
        _careerState.value = current.copy(
            recruits = updatedRecruits,
            weeklyRecruitingHoursRemaining = current.weeklyRecruitingHoursRemaining - actualDelta
        )
    }

    fun scheduleCampusVisit(recruitId: String) {
        val current = _careerState.value ?: return
        val cost = 50
        if (current.weeklyRecruitingHoursRemaining < cost) {
            _statusBanner.value = "Need $cost Hours to schedule an Official Campus Visit."
            return
        }
        val updatedRecruits = current.recruits.map {
            if (it.id == recruitId) it.copy(userScheduledVisit = true) else it
        }
        _careerState.value = current.copy(
            recruits = updatedRecruits,
            weeklyRecruitingHoursRemaining = current.weeklyRecruitingHoursRemaining - cost
        )
        _statusBanner.value = "Official Campus Visit scheduled for this week (+20 Bonus Pts)!"
    }

    // Live Match Controls
    fun openOrStartUserWeekMatch() {
        val current = _careerState.value ?: return
        if (current.activeMatch != null) return
        val userGame = current.schedule.find {
            it.week == current.currentWeek && !it.played &&
                (it.homeCollegeId == current.userCollegeId || it.awayCollegeId == current.userCollegeId)
        }
        if (userGame == null) {
            _statusBanner.value = "No unplayed game for your team this week (Bye Week or already played)."
            return
        }
        _careerState.value = current.copy(activeMatch = MatchEngine.startLiveMatch(userGame))
    }

    fun executeLivePlay(playCall: PlayCallType?) {
        val current = _careerState.value ?: return
        val live = current.activeMatch ?: return
        if (live.isGameOver) return

        val homeCol = current.colleges.find { it.id == live.homeCollegeId } ?: return
        val awayCol = current.colleges.find { it.id == live.awayCollegeId } ?: return
        val hRoster = current.players.filter { it.collegeId == homeCol.id }
        val aRoster = current.players.filter { it.collegeId == awayCol.id }

        val nextLive = MatchEngine.simulateSinglePlay(
            state = live,
            homeCollege = homeCol,
            awayCollege = awayCol,
            homeRoster = hRoster,
            awayRoster = aRoster,
            userPlayCall = playCall,
            userTactics = current.tactics,
            difficulty = current.difficulty
        )
        _careerState.value = current.copy(activeMatch = nextLive)
    }

    fun simMatchScope(scope: String) {
        val current = _careerState.value ?: return
        var live = current.activeMatch ?: return
        if (live.isGameOver) return

        val homeCol = current.colleges.find { it.id == live.homeCollegeId } ?: return
        val awayCol = current.colleges.find { it.id == live.awayCollegeId } ?: return
        val hRoster = current.players.filter { it.collegeId == homeCol.id }
        val aRoster = current.players.filter { it.collegeId == awayCol.id }

        val startOffense = live.offenseIsHome
        val startQuarter = live.quarter
        var guard = 0
        while (!live.isGameOver && guard < 220) {
            live = MatchEngine.simulateSinglePlay(
                state = live,
                homeCollege = homeCol,
                awayCollege = awayCol,
                homeRoster = hRoster,
                awayRoster = aRoster,
                userPlayCall = null,
                userTactics = current.tactics,
                difficulty = current.difficulty
            )
            guard++
            if (scope == "DRIVE" && live.offenseIsHome != startOffense) break
            if (scope == "QUARTER" && live.quarter != startQuarter) break
        }
        _careerState.value = current.copy(activeMatch = live)
    }

    fun finalizeLiveMatchAndRecord() {
        val current = _careerState.value ?: return
        val live = current.activeMatch ?: return
        if (!live.isGameOver) {
            _statusBanner.value = "Finish the game first before finalizing."
            return
        }
        val collegeMap = current.colleges.associateBy { it.id }.toMutableMap()
        val rostersByTeam = current.players.groupBy { it.collegeId }.toMutableMap()
        val homeName = collegeMap[live.homeCollegeId]?.name ?: "Home"
        val awayName = collegeMap[live.awayCollegeId]?.name ?: "Away"

        val updatedSchedule = current.schedule.map { g ->
            if (g.id == live.gameId) {
                val finished = g.copy(
                    played = true,
                    homeScore = live.homeScore,
                    awayScore = live.awayScore,
                    overtimePeriods = live.overtimePeriod,
                    summaryHeadline = "$homeName ${live.homeScore}, $awayName ${live.awayScore}"
                )
                SeasonEngine.applyGameResultToCollegesAndStats(finished, collegeMap, rostersByTeam, kotlin.random.Random.Default)
                finished
            } else g
        }

        _careerState.value = current.copy(
            colleges = collegeMap.values.toList(),
            players = rostersByTeam.values.flatten(),
            schedule = updatedSchedule,
            activeMatch = null
        )
        _statusBanner.value = "Match result recorded! Tap ADVANCE WEEK when ready."
    }

    // Tactics, Finances, NIL, Staff & Job Offers
    fun updateTactics(newTactics: TacticsConfig) {
        val current = _careerState.value ?: return
        _careerState.value = current.copy(tactics = newTactics)
        _statusBanner.value = "Tactics & Scheme Sliders saved."
    }

    fun upgradeFacility(type: String) {
        val current = _careerState.value ?: return
        val userCol = current.colleges.find { it.id == current.userCollegeId } ?: return
        val cost = 3.5
        if (userCol.budgetMillions < cost) {
            _statusBanner.value = "Insufficient budget ($${userCol.budgetMillions}M). Need $${cost}M."
            return
        }
        val updatedCol = when (type) {
            "TRAINING" -> {
                if (userCol.trainingFacilityLevel >= 10) {
                    _statusBanner.value = "Training Facility is already at Max Level 10."; return
                }
                userCol.copy(trainingFacilityLevel = userCol.trainingFacilityLevel + 1, budgetMillions = userCol.budgetMillions - cost)
            }
            "ACADEMIC" -> {
                if (userCol.academicFacilityLevel >= 10) {
                    _statusBanner.value = "Academic Center is already at Max Level 10."; return
                }
                userCol.copy(academicFacilityLevel = userCol.academicFacilityLevel + 1, budgetMillions = userCol.budgetMillions - cost)
            }
            "MEDICAL" -> {
                if (userCol.medicalFacilityLevel >= 10) {
                    _statusBanner.value = "Medical Lab is already at Max Level 10."; return
                }
                userCol.copy(medicalFacilityLevel = userCol.medicalFacilityLevel + 1, budgetMillions = userCol.budgetMillions - cost)
            }
            "NIL" -> {
                userCol.copy(
                    nilPoolMillions = userCol.nilPoolMillions + 2.0,
                    weeklyRecruitingHoursMax = userCol.weeklyRecruitingHoursMax + 40,
                    budgetMillions = userCol.budgetMillions - cost
                )
            }
            else -> userCol
        }
        val cols = current.colleges.map { if (it.id == userCol.id) updatedCol else it }
        _careerState.value = current.copy(colleges = cols)
        _statusBanner.value = "Upgraded $type successfully!"
    }

    fun upgradeStaffMember(staffId: String) {
        val current = _careerState.value ?: return
        val userCol = current.colleges.find { it.id == current.userCollegeId } ?: return
        val cost = 1.5
        if (userCol.budgetMillions < cost) {
            _statusBanner.value = "Need $${cost}M athletic budget to upgrade coordinator."
            return
        }
        val updatedStaff = current.staff.map {
            if (it.id == staffId && it.rating < 99) {
                it.copy(rating = (it.rating + 4).coerceAtMost(99), salaryMillions = it.salaryMillions + 0.2)
            } else it
        }
        val updatedCols = current.colleges.map {
            if (it.id == userCol.id) it.copy(budgetMillions = it.budgetMillions - cost) else it
        }
        _careerState.value = current.copy(staff = updatedStaff, colleges = updatedCols)
        _statusBanner.value = "Coordinator upgraded (+4 Rating)!"
    }

    fun acceptJobOffer(offer: JobOffer) {
        val current = _careerState.value ?: return
        val updatedCoach = current.coach.copy(
            annualSalaryMillions = offer.salaryMillions,
            contractYearsLeft = offer.contractYears,
            jobSecurity = 90
        )
        _careerState.value = current.copy(
            userCollegeId = offer.collegeId,
            coach = updatedCoach,
            pendingJobOffers = emptyList()
        )
        _statusBanner.value = "Accepted Head Coach contract at ${offer.collegeName}!"
    }

    // Editors & Settings
    fun updateSettings(newSettings: GameSettings) {
        val current = _careerState.value ?: return
        _careerState.value = current.copy(settings = newSettings)
        _statusBanner.value = "Settings updated."
    }

    fun saveEditedCollege(collegeId: String, name: String, mascot: String, abbr: String, prestigeScore: Int) {
        val current = _careerState.value ?: return
        when (val res = EditorValidator.validateCollegeEdit(name, mascot, abbr, prestigeScore)) {
            is EditorValidator.ValidationResult.Invalid -> _statusBanner.value = res.reason
            EditorValidator.ValidationResult.Valid -> {
                val stars = (prestigeScore / 18).coerceIn(1, 5)
                val updated = current.colleges.map {
                    if (it.id == collegeId) it.copy(name = name, mascot = mascot, abbreviation = abbr.uppercase(), prestigeScore = prestigeScore, prestigeStars = stars) else it
                }
                _careerState.value = current.copy(colleges = updated)
                _statusBanner.value = "Saved changes to $name."
            }
        }
    }

    fun saveEditedPlayer(playerId: String, firstName: String, lastName: String, gpa: Double, potential: Int, speedBoost: Int) {
        val current = _careerState.value ?: return
        when (val res = EditorValidator.validatePlayerEdit(firstName, lastName, gpa, potential)) {
            is EditorValidator.ValidationResult.Invalid -> _statusBanner.value = res.reason
            EditorValidator.ValidationResult.Valid -> {
                val updated = current.players.map {
                    if (it.id == playerId) {
                        it.copy(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            gpa = gpa,
                            potential = potential,
                            speed = (it.speed + speedBoost).coerceIn(40, 99),
                            awareness = (it.awareness + speedBoost).coerceIn(40, 99)
                        )
                    } else it
                }
                _careerState.value = current.copy(players = updated)
                _statusBanner.value = "Saved edits to $firstName $lastName."
            }
        }
    }

    fun saveEditedConference(confId: String, name: String, shortName: String, prestige: Int) {
        val current = _careerState.value ?: return
        when (val res = EditorValidator.validateConferenceEdit(name, shortName, prestige)) {
            is EditorValidator.ValidationResult.Invalid -> _statusBanner.value = res.reason
            EditorValidator.ValidationResult.Valid -> {
                val updated = current.conferences.map {
                    if (it.id == confId) it.copy(name = name.trim(), shortName = shortName.trim().uppercase(), prestige = prestige) else it
                }
                _careerState.value = current.copy(conferences = updated)
                _statusBanner.value = "Updated Conference $shortName."
            }
        }
    }

    fun importJsonToCurrentCareer(rawJson: String) {
        val res = EditorValidator.importCareerFromJson(rawJson)
        res.onSuccess { imported ->
            _careerState.value = imported
            viewModelScope.launch { persistCareerToRoom(imported) }
            _statusBanner.value = "Successfully imported Dynasty JSON!"
        }.onFailure { err ->
            _statusBanner.value = err.message ?: "Import failed."
        }
    }
}
