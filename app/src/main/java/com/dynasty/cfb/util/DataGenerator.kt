package com.dynasty.cfb.util

import com.dynasty.cfb.data.*
import kotlin.random.Random

object DataGenerator {

    private val firstNames = listOf(
        "Marcus", "Jalen", "Caleb", "Bryce", "Devon", "Trey", "Darius", "Xavier", "Malik", "Cooper",
        "Garrett", "Mason", "Carson", "Bo", "Quinn", "Dillon", "Jayden", "Drake", "rome", "Luther",
        "Brock", "Ollie", "Ashton", "quinshon", "Travis", "Keon", "Kool-Aid", "Terrion", "Jeremiah", "Micah",
        "CJ", "Anthony", "Will", "Ladd", "Bucky", "Ricky", "cam", "Shedeur", "Jaxson", "Miller",
        "Aidan", "walker", "Graham", "Kaidon", "Noah", "Tetairoa", "Damien", "Tahj", "RJ", "Donovan",
        "Harold", "Abdul", "Deone", "Barrett", "Jack", "Tyleik", "Sonny", "Caleb", "Malaki", "Mykel"
    ).map { it.replaceFirstChar { c -> c.uppercase() } }

    private val lastNames = listOf(
        "Vance", "Sterling", "Thornton", "Mercer", "Calloway", "Hawkins", "Kincaid", "Lawson", "Manning", "Prescott",
        "Stroud", "Daniels", "Harrison", "Nabers", "Odunze", "Bowers", "Alt", "Latu", "Verse", "Mitchell",
        "DeJean", "Worthy", "Pearsall", "Legette", "Coleman", "Benson", "Corum", "Wright", "Brooks", "Ewers",
        "Beck", "Milroe", "Dart", "Gabriel", "Fifita", "McMillan", "Jeanty", "Judkins", "Henderson", "Gordon",
        "Hunter", "Perkins", "Carter", "Starks", "Downs", "Johnson", "Williams", "Jackson", "Robinson", "Davis",
        "Jenkins", "Montgomery", "Jefferson", "Washington", "Henderson", "Sinclair", "Garrison", "Caldwell", "ramsey", "Keller"
    ).map { it.replaceFirstChar { c -> c.uppercase() } }

    private val states = listOf(
        "TX", "FL", "CA", "GA", "OH", "AL", "LA", "PA", "NC", "SC",
        "MI", "OK", "TN", "VA", "IN", "OR", "WA", "AZ", "MS", "WI"
    )

    fun generateDefaultConferences(): List<Conference> = listOf(
        Conference("conf_sec", "Southern Gridiron Conference", "SGC", 5, 0xFF1E3A8A),
        Conference("conf_big", "Heartland Collegiate Conference", "HCC", 5, 0xFF047857),
        Conference("conf_acc", "Atlantic Coast Athletic", "ACA", 4, 0xFF7C2D12),
        Conference("conf_b12", "Great Plains 12", "GP12", 4, 0xFFB45309),
        Conference("conf_pac", "Pacific Rim Alliance", "PRA", 4, 0xFF4338CA),
        Conference("conf_aac", "American Metro Conference", "AMC", 3, 0xFF0F766E),
        Conference("conf_mwc", "Mountain Peak Conference", "MPC", 3, 0xFF6D28D9),
        Conference("conf_sbc", "Gulf Coast Belt", "GCB", 2, 0xFF9D174D),
        Conference("conf_mac", "Great Lakes Valley", "GLV", 2, 0xFF374151),
        Conference("conf_cusa", "Frontier Athletic League", "FAL", 2, 0xFF854D0E)
    )

    private data class RawSchoolSeed(
        val name: String,
        val mascot: String,
        val abbr: String,
        val state: String,
        val confId: String,
        val prestigeScore: Int,
        val c1: Long,
        val c2: Long
    )

    fun generateColleges(conferences: List<Conference>): List<College> {
        val seeds = mutableListOf<RawSchoolSeed>()
        val confTemplates = mapOf(
            "conf_sec" to listOf(
                Triple("Tuscaloosa State", "Crimson Kings", "TUS"),
                Triple("Athens University", "Red Bulldogs", "ATH"),
                Triple("Baton Rouge Tech", "Bayou Tigers", "BRT"),
                Triple("Austin Republic", "Longhorn Royals", "AUS"),
                Triple("Norman Prairie", "Boomer Knights", "NOR"),
                Triple("Gainesville State", "Swamp Gators", "GNV"),
                Triple("Knoxville Volunteer U", "Smoky Pioneers", "KNX"),
                Triple("College Station A&M", "Lone Star Cadets", "CSA"),
                Triple("Auburn Plains", "Iron Eagles", "AUB"),
                Triple("Oxford Magnolia", "Rebel Admirals", "OXF"),
                Triple("Columbia Palmetto", "Garnet Roosters", "CLM"),
                Triple("Lexington Bluegrass", "Wild Stallions", "LEX")
            ),
            "conf_big" to listOf(
                Triple("Columbus Valley", "Scarlet Buckeyes", "COL"),
                Triple("Ann Arbor Northern", "Maize Wolverines", "AAN"),
                Triple("Happy Valley State", "Keystone Lions", "HVS"),
                Triple("Eugene Cascade", "Emerald Ducks", "EUG"),
                Triple("Los Angeles Heritage", "Golden Trojans", "LAH"),
                Triple("Madison Lakes", "Cardinal Badgers", "MAD"),
                Triple("Iowa City Hawkeye", "Gold Hawks", "ICH"),
                Triple("East Lansing Tech", "Bronze Spartans", "ELT"),
                Triple("Lincoln Cornbelt", "Plains Huskers", "LNC"),
                Triple("Seattle Sound U", "Purple Huskies", "SEA"),
                Triple("Twin Cities North", "Golden Gophers", "TCN"),
                Triple("Westwood Coast", "Pacific Bruins", "WWC")
            ),
            "conf_acc" to listOf(
                Triple("Clemson Foothills", "Orange Tigers", "CLF"),
                Triple("Tallahassee State", "Garnet Chiefs", "TAL"),
                Triple("Coral Gables U", "Tropical Hurricanes", "CGU"),
                Triple("Chapel Hill State", "Carolina Rams", "CHS"),
                Triple("Louisville River", "Red Cardinals", "LOU"),
                Triple("Raleigh Tech", "Red Wolfpack", "RAL"),
                Triple("Blacksburg Ridge", "Mountain Hokies", "BBR"),
                Triple("Pittsburgh Steel U", "Iron Panthers", "PSU"),
                Triple("Atlanta Metro Tech", "Gold Jackets", "AMT"),
                Triple("Syracuse Empire", "Orange Express", "SYR"),
                Triple("Durham Collegiate", "Royal Devils", "DUR"),
                Triple("Chestnut Hill U", "New England Eagles", "CHU")
            ),
            "conf_b12" to listOf(
                Triple("Manhattan Flint", "Purple Wildcats", "MNF"),
                Triple("Stillwater Plains", "Orange Cowboys", "STW"),
                Triple("Salt Lake Utes", "Crimson Peaks", "SLU"),
                Triple("Waco Brazos", "Green Bears", "WAC"),
                Triple("Fort Worth Christian", "Horned Lizards", "FWC"),
                Triple("Lubbock Red", "High Plains Raiders", "LUB"),
                Triple("Provo Wasatch", "Mountain Cougars", "PRV"),
                Triple("Lawrence Valley", "Blue Jayhawks", "LAW"),
                Triple("Ames Heartland", "Cardinal Cyclones", "AME"),
                Triple("Morgantown Ridge", "Coal Mountaineers", "MRG"),
                Triple("Tucson Desert", "Desert Cats", "TUC"),
                Triple("Tempe Sun Valley", "Sun Devils", "TSV")
            ),
            "conf_pac" to listOf(
                Triple("Boise Timber", "Blue Broncos", "BOI"),
                Triple("Corvallis Pines", "Orange Beavers", "COR"),
                Triple("Pullman Palouse", "Crimson Cougars", "PUL"),
                Triple("San Diego Bay", "Aztec Warriors", "SDB"),
                Triple("Fresno Valley", "Red Bulldogs", "FRV"),
                Triple("Palo Alto Redwood", "Cardinal Trees", "PAR"),
                Triple("Berkeley Bay", "Golden Bears", "BRK"),
                Triple("Dallas Hilltop", "Mustang Express", "DLH"),
                Triple("Fort Collins Peak", "Green Rams", "FCP"),
                Triple("Las Vegas Neon", "Silver Rebels", "LVN"),
                Triple("Reno Sierra", "Silver Pack", "RNS"),
                Triple("Honolulu Island", "Rainbow Warriors", "HNL")
            ),
            "conf_aac" to listOf(
                Triple("Memphis Bluff", "Blue Stripes", "MEM"),
                Triple("New Orleans Crescent", "Green Wave", "NOC"),
                Triple("San Antonio Alamo", "Roadrunners", "SAA"),
                Triple("Tampa Bay U", "Golden Bulls", "TBU"),
                Triple("Orlando Citrus", "Space Knights", "ORL"),
                Triple("Cincinnati Queen", "Red Bearcats", "CIN"),
                Triple("Houston Bayou", "Red Cougars", "HOU"),
                Triple("Greenville Pirates", "Purple Corsairs", "GRV"),
                Triple("Annapolis Naval", "Midshipmen", "ANN"),
                Triple("West Point Academy", "Black Cadets", "WPA"),
                Triple("Denton North", "Mean Eagles", "DNT"),
                Triple("Tulsa Hurricane", "Golden Tornado", "TUL")
            ),
            "conf_mwc" to listOf(
                Triple("Colorado Springs", "Sky Falcons", "CSP"),
                Triple("Laramie Range", "Prairie Cowboys", "LAR"),
                Triple("Albuquerque Mesa", "Silver Lobos", "ABQ"),
                Triple("Logan Cache", "Blue Aggies", "LOG"),
                Triple("San Jose Silicon", "Gold Spartans", "SJS"),
                Triple("El Paso Sun", "Copper Miners", "EPS"),
                Triple("Las Cruces Desert", "Crimson Aggies", "LCD"),
                Triple("Missoula Timber", "Silver Grizzlies", "MIS"),
                Triple("Bozeman Peak", "Gold Bobcats", "BOZ"),
                Triple("Fargo Bison U", "Dakota Herd", "FAR"),
                Triple("Brookings State", "Plains Jacks", "BRS"),
                Triple("Flagstaff Pine", "Lumberjacks", "FLG")
            ),
            "conf_sbc" to listOf(
                Triple("Boone Mountain", "Black Mountaineers", "BOO"),
                Triple("Conway Coastal", "Teal Chanticleers", "CON"),
                Triple("Troy Southern", "Cardinal Trojans", "TRY"),
                Triple("Lafayette Cajun", "Ragin Peppers", "LAF"),
                Triple("Harrisonburg Valley", "Purple Dukes", "HBV"),
                Triple("Huntington River", "Green Herd", "HUN"),
                Triple("Statesboro Eagle", "Blue Eagles", "STB"),
                Triple("Mobile Bay U", "Red Jaguars", "MOB"),
                Triple("San Marcos Hill", "Gold Bobcats", "SMH"),
                Triple("Jonesboro Ridge", "Red Wolves", "JBR"),
                Triple("Hattiesburg Pine", "Gold Eagles", "HAT"),
                Triple("Monroe Delta", "Sky Warhawks", "MNR")
            ),
            "conf_mac" to listOf(
                Triple("Toledo Glass", "Midnight Rockets", "TOL"),
                Triple("Oxford Redbrick", "Redhawks", "OXR"),
                Triple("Athens Ohio Valley", "Green Bobcats", "AOV"),
                Triple("DeKalb Prairie", "Cardinal Huskies", "DEK"),
                Triple("Kalamazoo West", "Brown Broncos", "KAL"),
                Triple("Mount Pleasant", "Maroon Chippewas", "MTP"),
                Triple("Bowling Green", "Orange Falcons", "BGF"),
                Triple("Buffalo Niagara", "Royal Bulls", "BUF"),
                Triple("Muncie Heartland", "Red Cardinals", "MUN"),
                Triple("Akron Rubber City", "Blue Zips", "AKR"),
                Triple("Kent Summit", "Gold Flashes", "KNT"),
                Triple("Ypsilanti Tech", "Green Eagles", "YPS")
            ),
            "conf_cusa" to listOf(
                Triple("Bowling Green South", "Red Hilltoppers", "BGS"),
                Triple("Lynchburg Ridge", "Flame Eagles", "LYN"),
                Triple("Murfreesboro U", "Blue Raiders", "MUR"),
                Triple("Jacksonville Hills", "Red Gamecocks", "JXH"),
                Triple("Ruston Pine", "Blue Bulldogs", "RUS"),
                Triple("Huntsville Pine", "Orange Kats", "HVP"),
                Triple("Miami International", "Gold Panthers", "MII"),
                Triple("Kennesaw Owls", "Gold Owls", "KEN"),
                Triple("weirdo Delaware", "Blue Hens", "DEL"),
                Triple("Springfield Ozark", "Maroon Bears", "SPF"),
                Triple("Chattanooga River", "Blue Mocs", "CHA"),
                Triple("Villanova Mainline", "Nova Wildcats", "VIL")
            )
        )

        val colorPairs = listOf(
            0xFF991B1BL to 0xFFFEF2F2L,
            0xFF1E3A8AL to 0xFFFBBF24L,
            0xFF065F46L to 0xFFECFDF5L,
            0xFF7C2D12L to 0xFFFDE68AL,
            0xFF4C1D95L to 0xFFF3E8FFL,
            0xFF111827L to 0xFFF59E0BL,
            0xFF0F766EL to 0xFFCCFBF1L,
            0xFF831843L to 0xFFFCE7F3L,
            0xFF1D4ED8L to 0xFFEFF6FFL,
            0xFFB45309L to 0xFFFFFBEBL
        )

        val rng = Random(2026)
        conferences.forEachIndexed { cIdx, conf ->
            val list = confTemplates[conf.id] ?: emptyList()
            list.forEachIndexed { idx, triple ->
                val basePrestige = when (conf.prestige) {
                    5 -> 88 - idx * 2
                    4 -> 82 - idx * 2
                    3 -> 74 - idx * 2
                    else -> 67 - idx * 2
                }.coerceIn(45, 96)
                val colors = colorPairs[(cIdx + idx) % colorPairs.size]
                seeds.add(
                    RawSchoolSeed(
                        name = triple.first.replace("weirdo ", ""),
                        mascot = triple.second,
                        abbr = triple.third,
                        state = states[(cIdx * 3 + idx) % states.size],
                        confId = conf.id,
                        prestigeScore = basePrestige,
                        c1 = colors.first,
                        c2 = colors.second
                    )
                )
            }
        }

        val shapes = LogoShape.entries
        val offSchemes = OffenseScheme.entries
        val defSchemes = DefenseScheme.entries

        return seeds.mapIndexed { index, seed ->
            val stars = when {
                seed.prestigeScore >= 88 -> 5
                seed.prestigeScore >= 79 -> 4
                seed.prestigeScore >= 70 -> 3
                seed.prestigeScore >= 60 -> 2
                else -> 1
            }
            val facBase = (stars * 2 - 1).coerceIn(1, 9)
            College(
                id = "col_${index + 1}",
                name = seed.name,
                mascot = seed.mascot,
                abbreviation = seed.abbr,
                state = seed.state,
                conferenceId = seed.confId,
                prestigeStars = stars,
                prestigeScore = seed.prestigeScore,
                primaryColorHex = seed.c1,
                secondaryColorHex = seed.c2,
                logoShape = shapes[index % shapes.size],
                stadiumName = "${seed.name} Memorial Stadium",
                stadiumCapacity = 22000 + stars * 16500 + (index % 5) * 2500,
                trainingFacilityLevel = facBase,
                academicFacilityLevel = (facBase + (index % 2)).coerceIn(1, 10),
                medicalFacilityLevel = facBase,
                budgetMillions = (stars * 6.5 + 4.0),
                nilPoolMillions = (stars * 2.8 + 1.0),
                weeklyRecruitingHoursMax = 300 + stars * 100,
                offenseScheme = offSchemes[(index + rng.nextInt(4)) % offSchemes.size],
                defenseScheme = defSchemes[(index + rng.nextInt(4)) % defSchemes.size],
                pollRank = if (index < 25) index + 1 else 99
            )
        }.sortedByDescending { it.prestigeScore }.mapIndexed { rankIdx, col ->
            col.copy(pollRank = if (rankIdx < 25) rankIdx + 1 else 99)
        }
    }

    fun generateAllRosters(colleges: List<College>, seed: Int = 2026): List<Player> {
        val rng = Random(seed)
        val allPlayers = mutableListOf<Player>()
        var pid = 1

        // 44 players per college * 120 colleges = 5,280 players with full attributes
        val positionDistribution = listOf(
            Position.QB to 3,
            Position.RB to 4,
            Position.WR to 6,
            Position.TE to 3,
            Position.OL to 8,
            Position.DL to 6,
            Position.LB to 5,
            Position.CB to 4,
            Position.S to 3,
            Position.K to 1,
            Position.P to 1
        )

        val classes = YearClass.entries

        colleges.forEach { college ->
            val teamBase = college.prestigeScore - 6
            positionDistribution.forEach { (pos, count) ->
                val generatedForPos = mutableListOf<Player>()
                for (i in 0 until count) {
                    val yr = classes[(i + rng.nextInt(4)) % classes.size]
                    val yrBoost = yr.order * 2
                    val targetOvr = (teamBase + yrBoost + rng.nextInt(-8, 9)).coerceIn(52, 97)
                    val p = createPlayerWithTarget(
                        id = "p_${pid++}",
                        collegeId = college.id,
                        pos = pos,
                        yearClass = yr,
                        targetBase = targetOvr,
                        rng = rng
                    )
                    generatedForPos.add(p)
                }
                generatedForPos.sortByDescending { it.overall }
                generatedForPos.forEachIndexed { depthIdx, player ->
                    allPlayers.add(
                        player.copy(
                            depthOrder = depthIdx + 1,
                            isCaptain = (pos == Position.QB || pos == Position.LB) && depthIdx == 0
                        )
                    )
                }
            }
        }
        return allPlayers
    }

    fun createPlayerWithTarget(
        id: String,
        collegeId: String,
        pos: Position,
        yearClass: YearClass,
        targetBase: Int,
        rng: Random
    ): Player {
        fun attr(primary: Boolean): Int {
            val base = if (primary) targetBase else (targetBase - 18).coerceAtLeast(42)
            return (base + rng.nextInt(-6, 7)).coerceIn(40, 99)
        }

        val spd = attr(pos in listOf(Position.RB, Position.WR, Position.CB, Position.S, Position.QB))
        val str = attr(pos in listOf(Position.OL, Position.DL, Position.LB, Position.TE))
        val awr = (targetBase + rng.nextInt(-5, 6)).coerceIn(45, 99)
        val thp = attr(pos == Position.QB)
        val tha = attr(pos == Position.QB)
        val car = attr(pos in listOf(Position.RB, Position.WR, Position.QB))
        val cth = attr(pos in listOf(Position.WR, Position.TE, Position.RB, Position.CB))
        val blk = attr(pos in listOf(Position.OL, Position.TE))
        val tak = attr(pos in listOf(Position.DL, Position.LB, Position.S, Position.CB))
        val cov = attr(pos in listOf(Position.CB, Position.S, Position.LB))
        val kpw = attr(pos in listOf(Position.K, Position.P))
        val kac = attr(pos in listOf(Position.K, Position.P))

        val stars = when {
            targetBase >= 86 -> 5
            targetBase >= 79 -> 4
            targetBase >= 71 -> 3
            targetBase >= 64 -> 2
            else -> 1
        }

        return Player(
            id = id,
            collegeId = collegeId,
            firstName = firstNames[rng.nextInt(firstNames.size)],
            lastName = lastNames[rng.nextInt(lastNames.size)],
            position = pos,
            yearClass = yearClass,
            stars = stars,
            potential = (targetBase + rng.nextInt(3, 15)).coerceIn(60, 99),
            gpa = (21 + rng.nextInt(19)) / 10.0,
            speed = spd,
            strength = str,
            awareness = awr,
            throwPower = thp,
            throwAccuracy = tha,
            carrying = car,
            catching = cth,
            blocking = blk,
            tackling = tak,
            coverage = cov,
            kickPower = kpw,
            kickAccuracy = kac
        )
    }

    fun generateRecruitClass(colleges: List<College>, seasonYear: Int, count: Int = 260): List<Recruit> {
        val rng = Random(seasonYear * 31 + 7)
        val positions = Position.entries
        val topColleges = colleges.take(40)
        return (1..count).map { idx ->
            val isPortal = idx > (count - 45)
            val stars = when (rng.nextInt(100)) {
                in 0..4 -> 5
                in 5..24 -> 4
                in 25..69 -> 3
                in 70..89 -> 2
                else -> 1
            }
            val baseOvr = when (stars) {
                5 -> rng.nextInt(79, 86)
                4 -> rng.nextInt(73, 80)
                3 -> rng.nextInt(66, 74)
                2 -> rng.nextInt(60, 67)
                else -> rng.nextInt(54, 61)
            } + if (isPortal) 5 else 0

            val rival = topColleges[rng.nextInt(topColleges.size)]
            Recruit(
                id = "rec_${seasonYear}_$idx",
                firstName = firstNames[rng.nextInt(firstNames.size)],
                lastName = lastNames[rng.nextInt(lastNames.size)],
                state = states[rng.nextInt(states.size)],
                position = positions[rng.nextInt(positions.size)],
                stars = stars,
                trueOverall = baseOvr.coerceIn(54, 92),
                truePotential = (baseOvr + rng.nextInt(6, 17)).coerceIn(65, 99),
                topRivalCollegeId = rival.id,
                rivalPointsAccumulated = rng.nextInt(20, 90),
                isTransferPortal = isPortal,
                previousYearClass = if (isPortal) YearClass.SO else YearClass.FR
            )
        }.sortedWith(compareByDescending<Recruit> { it.stars }.thenByDescending { it.trueOverall })
    }

    fun generateInitialStaff(seed: Int = 1): List<StaffMember> {
        val rng = Random(seed)
        return listOf(
            StaffMember("st_oc", "${firstNames[rng.nextInt(firstNames.size)]} ${lastNames[rng.nextInt(lastNames.size)]}", "OC", "Quarterback Guru", 75, 0.85),
            StaffMember("st_dc", "${firstNames[rng.nextInt(firstNames.size)]} ${lastNames[rng.nextInt(lastNames.size)]}", "DC", "Blitz Architect", 74, 0.80),
            StaffMember("st_rc", "${firstNames[rng.nextInt(firstNames.size)]} ${lastNames[rng.nextInt(lastNames.size)]}", "Recruiting Coord", "National Pipeline", 76, 0.55)
        )
    }

    fun generateDefaultAchievements(): List<Achievement> = listOf(
        Achievement("ach_first_win", "First Whistle Victory", "Win your very first game as a college head coach."),
        Achievement("ach_top_25", "Ranked Contender", "Lead your program into the Top 25 National Poll."),
        Achievement("ach_5star", "Blue-Chip Closer", "Sign a 5-Star prospect to your recruiting class."),
        Achievement("ach_conf_champ", "Conference Crown", "Win a Conference Championship game in Week 13."),
        Achievement("ach_playoff", "Playoff Bound", "Earn a berth in the College Football Playoff."),
        Achievement("ach_natty", "Dynasty Immortal", "Win the National Championship Trophy."),
        Achievement("ach_heisman", "Trophy Sculptor", "Coach a player who wins National Player of the Year.")
    )
}
