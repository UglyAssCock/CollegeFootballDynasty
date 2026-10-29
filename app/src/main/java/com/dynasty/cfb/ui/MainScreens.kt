package com.dynasty.cfb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynasty.cfb.data.*

@Composable
fun TitleAndSaveSlotsScreen(
    viewModel: DynastyViewModel,
    onStartNewCareerSetup: (Int) -> Unit,
    onCareerLoaded: () -> Unit
) {
    val slots by viewModel.saveSlots.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "COLLEGE FOOTBALL",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = TurfGreen
        )
        Text(
            text = "DYNASTY MANAGER",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ChampionshipGold
        )
        Text(
            text = "120 Universities • Play-by-Play Engine • Recruiting & Portal",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        if (isBusy) {
            CircularProgressIndicator(color = TurfGreen, modifier = Modifier.padding(16.dp))
            Text("Generating 120 colleges & 5,280 players...", color = TextWhite)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(listOf(1, 2, 3)) { slotNum ->
                val saved = slots.find { it.slotId == slotNum }
                DynastyCard(accentColor = if (saved != null) TurfGreen else TextMuted.copy(alpha = 0.3f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SAVE SLOT #$slotNum",
                                fontSize = 12.sp,
                                color = ChampionshipGold,
                                fontWeight = FontWeight.Bold
                            )
                            if (saved != null) {
                                Text(saved.collegeName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                Text(
                                    "Coach ${saved.coachName} • ${saved.seasonYear} Wk ${saved.currentWeek} (${saved.recordText})",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            } else {
                                Text("Empty Dynasty Slot", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                            }
                        }
                        if (saved != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.loadSaveSlot(slotNum, onCareerLoaded) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
                                ) {
                                    Text("LOAD", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(onClick = { viewModel.deleteSaveSlot(slotNum) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onStartNewCareerSetup(slotNum) },
                                colors = ButtonDefaults.buttonColors(containerColor = ChampionshipGold)
                            ) {
                                Text("NEW DYNASTY", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCareerSetupScreen(
    slotId: Int,
    viewModel: DynastyViewModel,
    onBack: () -> Unit,
    onCareerCreated: () -> Unit
) {
    var coachName by remember { mutableStateOf("Coach Marcus Vance") }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.NORMAL) }
    var selectedOffense by remember { mutableStateOf(OffenseScheme.SPREAD_OPTION) }
    var selectedDefense by remember { mutableStateOf(DefenseScheme.NICKEL_4_2_5) }
    var searchQuery by remember { mutableStateOf("") }
    val colleges = viewModel.previewColleges
    var selectedCollege by remember { mutableStateOf(colleges.first()) }
    val isBusy by viewModel.isBusy.collectAsState()

    val filteredColleges = remember(searchQuery, colleges) {
        if (searchQuery.isBlank()) colleges
        else colleges.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.mascot.contains(searchQuery, ignoreCase = true) ||
                it.state.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Text("NEW CAREER SETUP (SLOT #$slotId)", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
        }

        OutlinedTextField(
            value = coachName,
            onValueChange = { coachName = it },
            label = { Text("Head Coach Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Difficulty:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            items(Difficulty.entries) { diff ->
                FilterChip(
                    selected = selectedDifficulty == diff,
                    onClick = { selectedDifficulty = diff },
                    label = { Text(diff.label) }
                )
            }
        }

        Text("Offense & Defense Schemes:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            items(OffenseScheme.entries) { sch ->
                FilterChip(
                    selected = selectedOffense == sch,
                    onClick = { selectedOffense = sch },
                    label = { Text(sch.label) }
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            items(DefenseScheme.entries) { sch ->
                FilterChip(
                    selected = selectedDefense == sch,
                    onClick = { selectedDefense = sch },
                    label = { Text(sch.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search 120 Universities (${filteredColleges.size} shown)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredColleges, key = { it.id }) { college ->
                val isSelected = college.id == selectedCollege.id
                DynastyCard(
                    accentColor = if (isSelected) ChampionshipGold else Color.Transparent,
                    modifier = Modifier.clickable { selectedCollege = college }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CollegeLogo2D(college = college, size = 42.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${college.name} ${college.mascot}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextWhite
                            )
                            Text(
                                "${"★".repeat(college.prestigeStars)} • Prestige ${college.prestigeScore} • ${college.state} • Budget $${college.budgetMillions}M",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = ChampionshipGold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.startNewCareer(
                    slotId = slotId,
                    coachName = coachName,
                    difficulty = selectedDifficulty,
                    startingCollegeId = selectedCollege.id,
                    offenseScheme = selectedOffense,
                    defenseScheme = selectedDefense,
                    onReady = onCareerCreated
                )
            },
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
        ) {
            Text(
                if (isBusy) "BUILDING WORLD..." else "SIGN CONTRACT WITH ${selectedCollege.abbreviation}",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun DynastyHubDashboardScreen(
    state: CareerState,
    viewModel: DynastyViewModel,
    onNavigate: (String) -> Unit
) {
    val userCol = state.colleges.find { it.id == state.userCollegeId } ?: state.colleges.first()
    val isBusy by viewModel.isBusy.collectAsState()
    val currentGame = state.schedule.find {
        it.week == state.currentWeek && (it.homeCollegeId == userCol.id || it.awayCollegeId == userCol.id)
    }
    val injuredStarters = state.players.count {
        it.collegeId == userCol.id && it.depthOrder == 1 && !it.isAvailableToPlay
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DynastyCard(accentColor = Color(userCol.primaryColorHex)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CollegeLogo2D(college = userCol, size = 56.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val rankTag = if (userCol.pollRank <= 25) "#${userCol.pollRank} " else ""
                        Text(
                            text = "$rankTag${userCol.name} ${userCol.mascot}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = "${state.seasonYear} Season • Week ${state.currentWeek} • Record: ${userCol.wins}-${userCol.losses} (${userCol.confWins}-${userCol.confLosses})",
                            fontSize = 13.sp,
                            color = ChampionshipGold
                        )
                        Text(
                            text = "Coach ${state.coach.name} (Lvl ${state.coach.level}) • Security: ${state.coach.jobSecurity}%",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBadge("PRESTIGE", "${userCol.prestigeStars}★ (${userCol.prestigeScore})")
                    StatBadge("BUDGET", "$${userCol.budgetMillions}M")
                    StatBadge("NIL POOL", "$${userCol.nilPoolMillions}M")
                    StatBadge("REC HRS", "${state.weeklyRecruitingHoursRemaining}")
                }
            }
        }

        if (injuredStarters > 0) {
            item {
                DynastyCard(accentColor = CrimsonAlert) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LINEUP WARNING: $injuredStarters starter(s) unavailable due to injury/GPA/redshirt.",
                            color = TextWhite,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.autoSortDepthChart() }) {
                            Text("AUTO-FIX", color = TurfGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // This Week's Matchup + Advance Week Control
        item {
            DynastyCard(accentColor = TurfGreen) {
                Text(
                    text = if (state.currentWeek <= 12) "WEEK ${state.currentWeek} MATCHUP"
                    else if (state.currentWeek == 13) "CONFERENCE CHAMPIONSHIP WEEK"
                    else if (state.currentWeek in 14..17) "POSTSEASON / PLAYOFF WEEK ${state.currentWeek}"
                    else "OFFSEASON & SIGNING DAY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChampionshipGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (currentGame != null) {
                    val oppId = if (currentGame.homeCollegeId == userCol.id) currentGame.awayCollegeId else currentGame.homeCollegeId
                    val opp = state.colleges.find { it.id == oppId } ?: userCol
                    val locPrefix = if (currentGame.homeCollegeId == userCol.id) "vs" else "@"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CollegeLogo2D(college = opp, size = 40.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                val oppRank = if (opp.pollRank <= 25) "#${opp.pollRank} " else ""
                                Text("$locPrefix $oppRank${opp.name}", fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(
                                    "${currentGame.titleLabel} • Opp Record: ${opp.wins}-${opp.losses} (Prestige ${opp.prestigeScore})",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        if (currentGame.played) {
                            Text(
                                text = "${currentGame.homeScore}-${currentGame.awayScore}",
                                fontWeight = FontWeight.ExtraBold,
                                color = TurfGreen,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (!currentGame.played) {
                        Button(
                            onClick = {
                                viewModel.openOrStartUserWeekMatch()
                                onNavigate("match")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ChampionshipGold)
                        ) {
                            Icon(Icons.Default.SportsFootball, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PLAY LIVE MATCH / CALL PLAYS", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                } else {
                    Text(
                        text = if (state.currentWeek == 18) "Season complete! Review awards/job offers and advance to next season."
                        else "No scheduled game this week (Bye Week / Not in bracket).",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.advanceWeek() },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isBusy -> "PROCESSING WEEK..."
                            state.currentWeek >= 18 -> "ADVANCE TO ${state.seasonYear + 1} SEASON"
                            else -> "ADVANCE WEEK (SIM GAMES & RECRUITING)"
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Quick Navigation Grid
        item {
            val navButtons = listOf(
                Triple("Roster & Depth", "roster", Icons.Default.People),
                Triple("Recruiting & Portal", "recruiting", Icons.Default.Star),
                Triple("Schedule & Polls", "season", Icons.Default.EmojiEvents),
                Triple("Tactics & Sliders", "tactics", Icons.Default.Tune),
                Triple("Finances, NIL & Staff", "office", Icons.Default.AccountBalance),
                Triple("Awards, Draft & History", "history", Icons.Default.WorkspacePremium),
                Triple("Custom Editors & JSON", "editor", Icons.Default.Edit),
                Triple("Settings & Saves", "settings", Icons.Default.Settings)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in navButtons.indices step 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        for (j in i..minOf(i + 1, navButtons.size - 1)) {
                            val item = navButtons[j]
                            ElevatedButton(
                                onClick = { onNavigate(item.second) },
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.elevatedButtonColors(containerColor = NavyCard)
                            ) {
                                Icon(item.third, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.first, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // News Feed
        item {
            Text("DYNASTY WIRE NEWS", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ChampionshipGold)
        }
        items(state.newsFeed.take(12), key = { it.id }) { news ->
            DynastyCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(news.category, fontSize = 11.sp, color = TurfGreen, fontWeight = FontWeight.Bold)
                    Text("${news.season} Wk ${news.week}", fontSize = 11.sp, color = TextMuted)
                }
                Text(news.headline, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                Text(news.body, fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterAndDepthChartScreen(state: CareerState, viewModel: DynastyViewModel) {
    var selectedPos by remember { mutableStateOf<Position?>(null) }
    var sortBy by remember { mutableStateOf("OVR") }
    var search by remember { mutableStateOf("") }

    val teamPlayers = remember(state.players, state.userCollegeId, selectedPos, sortBy, search) {
        state.players
            .filter { it.collegeId == state.userCollegeId }
            .filter { selectedPos == null || it.position == selectedPos }
            .filter { search.isBlank() || it.fullName.contains(search, ignoreCase = true) }
            .let { list ->
                when (sortBy) {
                    "POT" -> list.sortedByDescending { it.potential }
                    "DEPTH" -> list.sortedWith(compareBy<Player> { it.position.ordinal }.thenBy { it.depthOrder })
                    "GPA" -> list.sortedByDescending { it.gpa }
                    else -> list.sortedByDescending { it.overall }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ROSTER & DEPTH CHART (${teamPlayers.size})", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
            Button(
                onClick = { viewModel.autoSortDepthChart() },
                colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
            ) {
                Text("AUTO DEPTH", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search player name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            item {
                FilterChip(selected = selectedPos == null, onClick = { selectedPos = null }, label = { Text("ALL") })
            }
            items(Position.entries) { pos ->
                FilterChip(selected = selectedPos == pos, onClick = { selectedPos = pos }, label = { Text(pos.code) })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            listOf("OVR", "POT", "DEPTH", "GPA").forEach { mode ->
                FilterChip(
                    selected = sortBy == mode,
                    onClick = { sortBy = mode },
                    label = { Text("Sort: $mode", fontSize = 11.sp) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(teamPlayers, key = { it.id }) { player ->
                DynastyCard(
                    accentColor = when {
                        !player.isAvailableToPlay -> CrimsonAlert
                        player.depthOrder == 1 -> TurfGreen
                        else -> Color.Transparent
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "[${player.position.code} #${player.depthOrder}] ${player.fullName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextWhite
                                )
                                if (player.isCaptain) {
                                    Text(" (C)", color = ChampionshipGold, fontWeight = FontWeight.ExtraBold)
                                }
                                if (player.isRedshirted) {
                                    Text(" [RS]", color = CrimsonAlert, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Text(
                                "${player.yearClass.short} • ${"★".repeat(player.stars)} • SPD ${player.speed} STR ${player.strength} AWR ${player.awareness} • GPA ${player.gpa}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                "Status: ${player.injuryDescription} | Stats: ${player.passYards + player.rushYards + player.receivingYards} yds, ${player.tackles} tkl",
                                fontSize = 11.sp,
                                color = if (player.injuryWeeks > 0) CrimsonAlert else TurfGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${player.overall} OVR", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TurfGreen)
                            Text("POT ${player.potential}", fontSize = 11.sp, color = ChampionshipGold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.promoteStarter(player.id) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (player.depthOrder == 1) "Starter #1" else "Make #1 Starter", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.toggleRedshirt(player.id) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (player.isRedshirted) "Remove RS" else "Redshirt", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.toggleCaptain(player.id) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (player.isCaptain) "Unset Capt" else "Captain", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitingAndPortalScreen(state: CareerState, viewModel: DynastyViewModel) {
    var showPortalOnly by remember { mutableStateOf(false) }
    var filterStars by remember { mutableStateOf(0) }

    val displayedRecruits = remember(state.recruits, showPortalOnly, filterStars) {
        state.recruits
            .filter { it.isTransferPortal == showPortalOnly }
            .filter { filterStars == 0 || it.stars == filterStars }
    }

    val userCommits = state.recruits.count { it.committedCollegeId == state.userCollegeId }

    Column(modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp)) {
        DynastyCard(accentColor = ChampionshipGold) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        if (showPortalOnly) "NCAA TRANSFER PORTAL" else "HIGH SCHOOL RECRUITING BOARD",
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        fontSize = 16.sp
                    )
                    Text(
                        "Commits Signed: $userCommits • Weekly Hours Left: ${state.weeklyRecruitingHoursRemaining}",
                        color = ChampionshipGold,
                        fontSize = 13.sp
                    )
                }
                Switch(checked = showPortalOnly, onCheckedChange = { showPortalOnly = it })
            }
            Text("Toggle switch on right to view Transfer Portal vs High School Board", fontSize = 11.sp, color = TextMuted)
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            item {
                FilterChip(selected = filterStars == 0, onClick = { filterStars = 0 }, label = { Text("All Stars") })
            }
            items(listOf(5, 4, 3, 2)) { st ->
                FilterChip(selected = filterStars == st, onClick = { filterStars = st }, label = { Text("${st}★") })
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(displayedRecruits, key = { it.id }) { rec ->
                val rivalCol = state.colleges.find { it.id == rec.topRivalCollegeId }
                val committedCol = state.colleges.find { it.id == rec.committedCollegeId }

                DynastyCard(
                    accentColor = when (rec.committedCollegeId) {
                        state.userCollegeId -> TurfGreen
                        null -> Color.Transparent
                        else -> CrimsonAlert
                    }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "[${rec.position.code}] ${rec.fullName} (${"★".repeat(rec.stars)})",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 15.sp
                            )
                            Text(
                                "State: ${rec.state} • Scouted: ${rec.scoutedPercent}% • OVR: ${rec.displayedOverall} ${if (rec.scoutedPercent == 100) "(POT ${rec.truePotential})" else ""}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                "Your Points: ${rec.userPointsAccumulated} (${rec.userWeeklyHours} hrs/wk) | Rival ${rivalCol?.abbreviation ?: "AI"}: ${rec.rivalPointsAccumulated} pts",
                                fontSize = 12.sp,
                                color = ChampionshipGold
                            )
                        }
                        if (committedCol != null) {
                            Text(
                                "COMMITTED\n${committedCol.abbreviation}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (committedCol.id == state.userCollegeId) TurfGreen else CrimsonAlert
                            )
                        }
                    }

                    if (rec.committedCollegeId == null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { viewModel.scoutRecruit(rec.id) },
                                enabled = rec.scoutedPercent < 100,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (rec.scoutedPercent >= 100) "Scouted" else "Scout (25h)", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.offerScholarship(rec.id) },
                                enabled = !rec.userOfferedScholarship,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (rec.userOfferedScholarship) "Offered" else "Offer (35h)", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.adjustRecruitWeeklyHours(rec.id, 25) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+25h", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.adjustRecruitWeeklyHours(rec.id, -25) },
                                enabled = rec.userWeeklyHours >= 25,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("-25h", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.scheduleCampusVisit(rec.id) },
                                enabled = !rec.userScheduledVisit,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (rec.userScheduledVisit) "Visited" else "Visit (50h)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveMatchScreen(
    state: CareerState,
    viewModel: DynastyViewModel,
    onBackToHub: () -> Unit
) {
    val live = state.activeMatch
    if (live == null) {
        Column(
            modifier = Modifier.fillMaxSize().background(NavyBackground).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No active live match.", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBackToHub) { Text("Return to Dynasty Hub") }
        }
        return
    }

    val homeCol = state.colleges.find { it.id == live.homeCollegeId } ?: state.colleges.first()
    val awayCol = state.colleges.find { it.id == live.awayCollegeId } ?: state.colleges.last()
    val mins = live.clockSeconds / 60
    val secs = (live.clockSeconds % 60).toString().padStart(2, '0')
    val qtrText = if (live.quarter <= 4) "Q${live.quarter}" else "OT${live.overtimePeriod}"

    Column(modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp)) {
        // Scoreboard
        DynastyCard(accentColor = TurfGreen) {
            Text(live.titleLabel, fontSize = 12.sp, color = ChampionshipGold, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CollegeLogo2D(college = awayCol, size = 38.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("${awayCol.abbreviation} ${if (!live.offenseIsHome) "🏈" else ""}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("${live.awayTotalYards} yds | ${live.awayTurnovers} TO", fontSize = 11.sp, color = TextMuted)
                    }
                }
                Text("${live.awayScore}  -  ${live.homeScore}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextWhite)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${if (live.offenseIsHome) "🏈 " else ""}${homeCol.abbreviation}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("${live.homeTotalYards} yds | ${live.homeTurnovers} TO", fontSize = 11.sp, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    CollegeLogo2D(college = homeCol, size = 38.dp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Turf Field Position Progress Bar
            LinearProgressIndicator(
                progress = { (live.ballOnYardLine / 100f).coerceIn(0.02f, 0.98f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = TurfGreen,
                trackColor = NavySurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$qtrText • $mins:$secs", fontWeight = FontWeight.Bold, color = ChampionshipGold)
                Text("Down: ${live.down} & ${live.distance}", fontWeight = FontWeight.Bold, color = TextWhite)
                Text("Ball on ${live.ballOnYardLine} yd line", color = TurfGreen, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!live.isGameOver) {
            Text("CALL PLAY OR SIMULATE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            val playButtons = listOf(
                PlayCallType.INSIDE_RUN,
                PlayCallType.OUTSIDE_RUN,
                PlayCallType.SHORT_PASS,
                PlayCallType.DEEP_PASS,
                PlayCallType.SCREEN_PASS,
                PlayCallType.FIELD_GOAL,
                PlayCallType.PUNT,
                PlayCallType.GO_FOR_IT
            )
            for (i in playButtons.indices step 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    for (j in i..minOf(i + 1, playButtons.size - 1)) {
                        val call = playButtons[j]
                        ElevatedButton(
                            onClick = { viewModel.executeLivePlay(call) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = NavyCard)
                        ) {
                            Text(call.label, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Button(
                    onClick = { viewModel.executeLivePlay(null) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
                ) { Text("SIM PLAY", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { viewModel.simMatchScope("DRIVE") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
                ) { Text("SIM DRIVE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { viewModel.simMatchScope("QUARTER") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ChampionshipGold)
                ) { Text("SIM QTR", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { viewModel.simMatchScope("GAME") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ChampionshipGold)
                ) { Text("SIM ALL", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        } else {
            Button(
                onClick = {
                    viewModel.finalizeLiveMatchAndRecord()
                    onBackToHub()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
            ) {
                Text("FINALIZE MATCH RESULT & RETURN TO HUB", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("PLAY-BY-PLAY LOG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ChampionshipGold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            items(live.playLog, key = { it.id }) { ev ->
                DynastyCard(
                    accentColor = when {
                        ev.isScoringPlay -> TurfGreen
                        ev.isTurnover -> CrimsonAlert
                        ev.isPenalty -> ChampionshipGold
                        else -> Color.Transparent
                    }
                ) {
                    Text(
                        text = "[Q${ev.quarter}] ${ev.text}",
                        fontSize = 13.sp,
                        color = if (ev.isScoringPlay) TurfGreen else if (ev.isTurnover) CrimsonAlert else TextWhite,
                        fontWeight = if (ev.isScoringPlay || ev.isTurnover) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
