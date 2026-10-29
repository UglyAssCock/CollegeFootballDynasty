package com.dynasty.cfb.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
enum class Difficulty(val label: String, val recruitBonus: Double, val aiStatMultiplier: Double, val budgetMultiplier: Double) {
    EASY("Easy", 1.25, 0.92, 1.3),
    NORMAL("Normal", 1.0, 1.0, 1.0),
    HARD("Hard", 0.85, 1.06, 0.85),
    EXPERT("Expert", 0.72, 1.12, 0.75)
}

@Serializable
enum class OffenseScheme(val label: String, val passBias: Int, val tempoBias: Int, val desc: String) {
    AIR_RAID("Air Raid", 70, 75, "High-volume vertical & spread passing attack"),
    SPREAD_OPTION("Spread Option", 45, 70, "Mobile QB read-options and RPO spacing"),
    PRO_STYLE("Pro Style", 52, 50, "Balanced play-action and tight end formations"),
    POWER_RUN("Power I / Trench", 30, 35, "Physical downhill rushing & clock control")
}

@Serializable
enum class DefenseScheme(val label: String, val blitzBias: Int, val runStopBonus: Int, val passStopBonus: Int, val desc: String) {
    BASE_4_3("4-3 Base", 45, 6, 4, "Four down linemen, balanced run and pass rush"),
    BASE_3_4("3-4 Disguise", 60, 5, 5, "Versatile linebackers bringing unpredictable pressure"),
    NICKEL_4_2_5("4-2-5 Hybrid", 50, 3, 8, "Extra hybrid safety to counter modern spread offenses"),
    STACK_3_3_5("3-3-5 Cloud", 55, 2, 10, "Speed on the field to stop Air Raid & deep shots")
}

@Serializable
enum class Position(val code: String, val group: String, val idealStarters: Int) {
    QB("QB", "Offense", 1),
    RB("RB", "Offense", 2),
    WR("WR", "Offense", 3),
    TE("TE", "Offense", 1),
    OL("OL", "Offense", 5),
    DL("DL", "Defense", 4),
    LB("LB", "Defense", 3),
    CB("CB", "Defense", 2),
    S("S", "Defense", 2),
    K("K", "Special", 1),
    P("P", "Special", 1)
}

@Serializable
enum class YearClass(val short: String, val order: Int) {
    FR("FR", 1),
    SO("SO", 2),
    JR("JR", 3),
    SR("SR", 4)
}

@Serializable
enum class LogoShape {
    SHIELD, CIRCLE, DIAMOND, HEXAGON, PENNANT
}

@Serializable
enum class PlayCallType(val label: String, val isOffense: Boolean) {
    INSIDE_RUN("Inside Zone Run", true),
    OUTSIDE_RUN("Outside Sweep", true),
    SHORT_PASS("Quick Slants / RPO", true),
    DEEP_PASS("Four Verticals Shot", true),
    SCREEN_PASS("RB Slip Screen", true),
    FIELD_GOAL("Field Goal Attempt", false),
    PUNT("Punt Deep", false),
    GO_FOR_IT("4th Down Conversion", true)
}

@Serializable
data class TacticsConfig(
    val offenseScheme: OffenseScheme = OffenseScheme.SPREAD_OPTION,
    val defenseScheme: DefenseScheme = DefenseScheme.NICKEL_4_2_5,
    val passRatioSlider: Int = 50,      // 0 (All Run) to 100 (All Pass)
    val tempoSlider: Int = 55,          // 0 (Chew Clock) to 100 (Hurried)
    val fourthDownAggression: Int = 50, // 0 (Conservative) to 100 (Analytics Max)
    val blitzFrequency: Int = 45,       // 0 to 100
    val coverageDepth: Int = 50,        // 0 (Press) to 100 (Prevent)
    val autoSubFatigueThreshold: Int = 75
)

@Serializable
data class CoachProfile(
    val name: String,
    val age: Int = 38,
    val level: Int = 1,
    val xp: Int = 0,
    val offenseRating: Int = 72,
    val defenseRating: Int = 72,
    val recruitingRating: Int = 74,
    val developmentRating: Int = 73,
    val contractYearsLeft: Int = 4,
    val annualSalaryMillions: Double = 2.4,
    val buyoutMillions: Double = 4.0,
    val requiredWinsTarget: Int = 6,
    val jobSecurity: Int = 82, // 0..100
    val careerWins: Int = 0,
    val careerLosses: Int = 0,
    val nationalTitles: Int = 0,
    val conferenceTitles: Int = 0
)

@Serializable
data class StaffMember(
    val id: String,
    val name: String,
    val role: String, // "OC", "DC", "Recruiting Coord"
    val specialty: String,
    val rating: Int,
    val salaryMillions: Double
)

@Serializable
data class Conference(
    val id: String,
    val name: String,
    val shortName: String,
    val prestige: Int, // 1..5
    val primaryColorHex: Long
)

@Serializable
data class College(
    val id: String,
    val name: String,
    val mascot: String,
    val abbreviation: String,
    val state: String,
    val conferenceId: String,
    val prestigeStars: Int, // 1..5
    val prestigeScore: Int, // 40..99
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val logoShape: LogoShape,
    val stadiumName: String,
    val stadiumCapacity: Int,
    val trainingFacilityLevel: Int, // 1..10
    val academicFacilityLevel: Int, // 1..10
    val medicalFacilityLevel: Int,  // 1..10
    val budgetMillions: Double,
    val nilPoolMillions: Double,
    val weeklyRecruitingHoursMax: Int,
    val offenseScheme: OffenseScheme,
    val defenseScheme: DefenseScheme,
    val wins: Int = 0,
    val losses: Int = 0,
    val confWins: Int = 0,
    val confLosses: Int = 0,
    val pointsFor: Int = 0,
    val pointsAgainst: Int = 0,
    val pollRank: Int = 99 // 1..25 or 99 for unranked
)

@Serializable
data class Player(
    val id: String,
    val collegeId: String,
    val firstName: String,
    val lastName: String,
    val position: Position,
    val yearClass: YearClass,
    val isRedshirted: Boolean = false,
    val hasUsedRedshirt: Boolean = false,
    val gamesPlayedThisSeason: Int = 0,
    val stars: Int, // 1..5
    val potential: Int, // 60..99
    val morale: Int = 85, // 0..100
    val gpa: Double = 3.1, // 1.5..4.0 (Under 2.0 = academically ineligible)
    val injuryWeeks: Int = 0,
    val injuryDescription: String = "Healthy",
    val isCaptain: Boolean = false,
    val depthOrder: Int = 99,
    // Attributes (40..99)
    val speed: Int,
    val strength: Int,
    val awareness: Int,
    val throwPower: Int,
    val throwAccuracy: Int,
    val carrying: Int,
    val catching: Int,
    val blocking: Int,
    val tackling: Int,
    val coverage: Int,
    val kickPower: Int,
    val kickAccuracy: Int,
    // Season stats
    val passYards: Int = 0,
    val passTds: Int = 0,
    val interceptionsThrown: Int = 0,
    val rushYards: Int = 0,
    val rushTds: Int = 0,
    val receivingYards: Int = 0,
    val receivingTds: Int = 0,
    val tackles: Int = 0,
    val sacks: Int = 0,
    val interceptionsCaught: Int = 0
) {
    val fullName: String get() = "$firstName $lastName"
    val isAcademicallyEligible: Boolean get() = gpa >= 2.0
    val isAvailableToPlay: Boolean get() = injuryWeeks == 0 && isAcademicallyEligible && !isRedshirted

    val overall: Int
        get() = calculatePositionOverall(
            position, speed, strength, awareness, throwPower, throwAccuracy,
            carrying, catching, blocking, tackling, coverage, kickPower, kickAccuracy
        )

    companion object {
        fun calculatePositionOverall(
            pos: Position,
            spd: Int, str: Int, awr: Int, thp: Int, tha: Int,
            car: Int, cth: Int, blk: Int, tak: Int, cov: Int, kpw: Int, kac: Int
        ): Int {
            val raw = when (pos) {
                Position.QB -> thp * 0.32 + tha * 0.38 + awr * 0.18 + spd * 0.12
                Position.RB -> spd * 0.35 + car * 0.35 + awr * 0.15 + cth * 0.15
                Position.WR -> spd * 0.35 + cth * 0.40 + awr * 0.15 + car * 0.10
                Position.TE -> cth * 0.35 + blk * 0.30 + str * 0.20 + spd * 0.15
                Position.OL -> blk * 0.55 + str * 0.30 + awr * 0.15
                Position.DL -> str * 0.38 + tak * 0.37 + spd * 0.15 + awr * 0.10
                Position.LB -> tak * 0.40 + awr * 0.22 + spd * 0.20 + cov * 0.18
                Position.CB -> cov * 0.45 + spd * 0.38 + awr * 0.17
                Position.S -> cov * 0.35 + tak * 0.30 + spd * 0.20 + awr * 0.15
                Position.K -> kac * 0.55 + kpw * 0.35 + awr * 0.10
                Position.P -> kpw * 0.55 + kac * 0.35 + awr * 0.10
            }
            return raw.roundToInt().coerceIn(40, 99)
        }
    }
}

@Serializable
data class Recruit(
    val id: String,
    val firstName: String,
    val lastName: String,
    val state: String,
    val position: Position,
    val stars: Int, // 1..5
    val trueOverall: Int,
    val truePotential: Int,
    val scoutedPercent: Int = 0, // 0, 50, 100
    val userOfferedScholarship: Boolean = false,
    val userScheduledVisit: Boolean = false,
    val userWeeklyHours: Int = 0,
    val userPointsAccumulated: Int = 0,
    val topRivalCollegeId: String,
    val rivalPointsAccumulated: Int = 0,
    val committedCollegeId: String? = null,
    val isTransferPortal: Boolean = false,
    val previousYearClass: YearClass = YearClass.FR
) {
    val fullName: String get() = "$firstName $lastName"
    val displayedOverall: String
        get() = when {
            scoutedPercent >= 100 -> "$trueOverall"
            scoutedPercent >= 50 -> "${(trueOverall - 3).coerceAtLeast(50)}-${(trueOverall + 3).coerceAtMost(99)}"
            else -> "??"
        }
}

@Serializable
data class ScheduledGame(
    val id: String,
    val week: Int, // 1..12 Regular, 13 Conf Champ, 14..17 Playoff / Bowls
    val homeCollegeId: String,
    val awayCollegeId: String,
    val isConferenceGame: Boolean,
    val titleLabel: String = "Regular Season",
    val played: Boolean = false,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val overtimePeriods: Int = 0,
    val summaryHeadline: String = ""
)

@Serializable
data class PlayByPlayEvent(
    val id: Int,
    val quarter: Int,
    val clockSeconds: Int,
    val text: String,
    val isScoringPlay: Boolean = false,
    val isTurnover: Boolean = false,
    val isPenalty: Boolean = false
)

@Serializable
data class LiveMatchState(
    val gameId: String,
    val week: Int,
    val titleLabel: String,
    val homeCollegeId: String,
    val awayCollegeId: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val quarter: Int = 1, // 1..4, 5+ = OT
    val clockSeconds: Int = 900, // 15:00
    val offenseIsHome: Boolean = true,
    val down: Int = 1,
    val distance: Int = 10,
    val ballOnYardLine: Int = 25, // 1..99 (yards from own endzone toward opponent endzone)
    val homeTimeouts: Int = 3,
    val awayTimeouts: Int = 3,
    val homeTotalYards: Int = 0,
    val awayTotalYards: Int = 0,
    val homeTurnovers: Int = 0,
    val awayTurnovers: Int = 0,
    val homeFatigue: Int = 10, // 0..100
    val awayFatigue: Int = 10,
    val isGameOver: Boolean = false,
    val overtimePeriod: Int = 0,
    val playLog: List<PlayByPlayEvent> = emptyList()
)

@Serializable
data class NewsItem(
    val id: String,
    val season: Int,
    val week: Int,
    val category: String, // "GAME", "RECRUITING", "POLL", "AWARD", "INJURY"
    val headline: String,
    val body: String
)

@Serializable
data class AwardWinner(
    val season: Int,
    val awardName: String,
    val playerName: String,
    val position: Position,
    val collegeName: String,
    val statLine: String
)

@Serializable
data class DraftPick(
    val season: Int,
    val round: Int,
    val pickNumber: Int,
    val playerName: String,
    val position: Position,
    val collegeName: String,
    val overall: Int
)

@Serializable
data class SeasonHistoryEntry(
    val seasonYear: Int,
    val userCollegeName: String,
    val userRecord: String,
    val userFinalRank: Int,
    val nationalChampionName: String,
    val heismanWinner: String,
    val recruitingClassRank: Int
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockedSeason: Int? = null
)

@Serializable
data class JobOffer(
    val collegeId: String,
    val collegeName: String,
    val prestigeStars: Int,
    val salaryMillions: Double,
    val contractYears: Int,
    val nilPoolMillions: Double
)

@Serializable
data class GameSettings(
    val playoffTeamsCount: Int = 12, // 4, 8, 12, or 16
    val autoSaveEnabled: Boolean = true,
    val injuriesEnabled: Boolean = true,
    val academicEligibilityEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val bowlGamesTitlePrefix: String = "Dynasty Bowl"
)

@Serializable
data class CareerState(
    val saveSlotId: Int = 1,
    val careerName: String = "Coach Dynasty",
    val seasonYear: Int = 2026,
    val currentWeek: Int = 1, // 1..12 Reg, 13 Conf Champ, 14..17 Playoff/Bowls, 18 Offseason
    val difficulty: Difficulty = Difficulty.NORMAL,
    val userCollegeId: String,
    val coach: CoachProfile,
    val tactics: TacticsConfig = TacticsConfig(),
    val settings: GameSettings = GameSettings(),
    val staff: List<StaffMember> = emptyList(),
    val conferences: List<Conference> = emptyList(),
    val colleges: List<College> = emptyList(),
    val players: List<Player> = emptyList(),
    val recruits: List<Recruit> = emptyList(),
    val schedule: List<ScheduledGame> = emptyList(),
    val activeMatch: LiveMatchState? = null,
    val newsFeed: List<NewsItem> = emptyList(),
    val awardsHistory: List<AwardWinner> = emptyList(),
    val draftHistory: List<DraftPick> = emptyList(),
    val seasonHistory: List<SeasonHistoryEntry> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val pendingJobOffers: List<JobOffer> = emptyList(),
    val weeklyRecruitingHoursRemaining: Int = 500
)
