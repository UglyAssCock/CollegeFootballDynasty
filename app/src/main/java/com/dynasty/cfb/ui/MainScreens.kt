package com.dynasty.cfb.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
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
        Spacer(modifier = Modifier.height(14.dp))
        CrownVectorIcon(tint = ChampionshipGold, size = 38.dp)
        Spacer(modifier = Modifier.height(6.dp))
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
            color = TextWhite
        )
        Text(
            text = "120 Universities • Play-by-Play Engine • Recruiting & Portal",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        if (isBusy) {
            CircularProgressIndicator(color = TurfGreen, modifier = Modifier.padding(16.dp))
            Text("Generating 120 colleges & 5,280 players...", color = TextWhite)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(listOf(1, 2, 3)) { slotNum ->
                val saved = slots.find { it.slotId == slotNum }
                DynastyCard(
                    accentColor = if (saved != null) TurfGreen else BorderBlue,
                    showLeftStripe = saved != null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SAVE SLOT #$slotNum",
                                fontSize = 11.sp,
                                color = ChampionshipGold,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (saved != null) {
                                Text(saved.collegeName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                Text(
                                    "Coach ${saved.coachName} • ${saved.seasonYear} Wk ${saved.currentWeek} (${saved.recordText})",
                                    fontSize = 12.sp,
                                    color = TurfGreen
                                )
                            } else {
                                Text("Empty Dynasty Slot", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                            }
                        }
                        if (saved != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.loadSaveSlot(slotNum, onCareerLoaded) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("LOAD", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.deleteSaveSlot(slotNum) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onStartNewCareerSetup(slotNum) },
                                colors = ButtonDefaults.buttonColors(containerColor = TurfGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("NEW DYNASTY", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
        StadiumWireframeFooter()
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
    var coachName by remember { mutableStateOf("Marcus Vance") }
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
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            items(Difficulty.entries) { diff ->
                FilterChip(
                    selected = selectedDifficulty == diff,
                    onClick = { selectedDifficulty = diff },
                    label = { Text(diff.label) }
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
            items(OffenseScheme.entries) { sch ->
                FilterChip(
                    selected = selectedOffense == sch,
                    onClick = { selectedOffense = sch },
                    label = { Text(sch.label) }
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
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
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            placeholder = { Text("Search 120 Universities (${filteredColleges.size} shown)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredColleges, key = { it.id }) { college ->
                val isSelected = college.id == selectedCollege.id
                DynastyCard(
                    accentColor = if (isSelected) TurfGreen else BorderBlue,
                    showLeftStripe = isSelected,
                    modifier = Modifier.clickable { selectedCollege = college }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CollegeLogo2D(college = college, size = 46.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${college.name} ${college.mascot}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = TextWhite
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FiveStarRatingRow(stars = college.prestigeStars, starSize = 13.dp)
                                Text(
                                    "  •  Prestige ${college.prestigeScore}  •  $${fmtMillions(college.budgetMillions)}M",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = TurfGreen)
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
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
        ) {
            Text(
                if (isBusy) "BUILDING WORLD..." else "SIGN CONTRACT WITH ${selectedCollege.abbreviation}",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Rebuilt Hub Dashboard matching screenshot 1000158303_acf699.png.
 */
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Hero Team Card with Mascot Watermark
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0B2239), Color(0xFF07172B), Color(0xFF0A2E36))
                        )
                    )
                    .border(1.5.dp, TurfGreen.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            ) {
                MascotWatermarkCanvas(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterEnd)
                )
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CollegeLogo2D(college = userCol, size = 68.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val rankTag = if (userCol.pollRank <= 25) "#${userCol.pollRank} " else ""
                            Text(
                                text = "$rankTag${userCol.name}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = userCol.mascot,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.seasonYear} Season  •  Week ${state.currentWeek}  •  Record: ${userCol.wins}-${userCol.losses} (${userCol.confWins}-${userCol.confLosses})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TurfGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Coach ${state.coach.name.removePrefix("Coach ").trim()} (Lvl ${state.coach.level})",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Security: ${state.coach.jobSecurity}%",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroStatBox(
                            icon = Icons.Default.Star,
                            iconTint = TurfGreen,
                            label = "PRESTIGE",
                            modifier = Modifier.weight(1f),
                            valueNode = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${userCol.prestigeStars}★", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TurfGreen)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("(${userCol.prestigeScore})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                        )
                        HeroStatBox(
                            icon = Icons.Default.AccountBalanceWallet,
                            iconTint = CyanAccent,
                            label = "BUDGET",
                            modifier = Modifier.weight(1f),
                            valueNode = {
                                Text("$${fmtMillions(userCol.budgetMillions)}M", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            }
                        )
                        HeroStatBox(
                            icon = Icons.Default.MonetizationOn,
                            iconTint = PurpleAccent,
                            label = "NIL POOL",
                            modifier = Modifier.weight(1f),
                            valueNode = {
                                Text("$${fmtMillions(userCol.nilPoolMillions)}M", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            }
                        )
                    }
                }
            }
        }

        if (injuredStarters > 0) {
            item {
                DynastyCard(accentColor = CrimsonAlert, showLeftStripe = true, stripeColor = CrimsonAlert) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lineup Alert: $injuredStarters starter(s) unavailable.",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.autoSortDepthChart() }) {
                            Text("AUTO-FIX", color = TurfGreen, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // 2. Week Matchup & Glowing Pill Advance Button
        item {
            DynastyCard(accentColor = TurfGreen.copy(alpha = 0.65f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ChampionshipGold,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when {
                                    state.currentWeek <= 12 -> "WEEK ${state.currentWeek} MATCHUP"
                                    state.currentWeek == 13 -> "CONFERENCE CHAMPIONSHIP WEEK"
                                    state.currentWeek in 14..17 -> "POSTSEASON / PLAYOFF WEEK ${state.currentWeek}"
                                    else -> "OFFSEASON & SIGNING DAY"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = ChampionshipGold
                            )
                            if (currentGame != null) {
                                val oppId = if (currentGame.homeCollegeId == userCol.id) currentGame.awayCollegeId else currentGame.homeCollegeId
                                val opp = state.colleges.find { it.id == oppId } ?: userCol
                                val locPrefix = if (currentGame.homeCollegeId == userCol.id) "vs" else "@"
                                val oppRank = if (opp.pollRank <= 25) "#${opp.pollRank} " else ""
                                Text(
                                    text = if (currentGame.played) "Final: ${currentGame.summaryHeadline}"
                                    else "$locPrefix $oppRank${opp.name} (${opp.wins}-${opp.losses} • Prestige ${opp.prestigeScore})",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            } else {
                                Text(
                                    text = "No scheduled game this week (Bye Week / Not in bracket).",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                }

                if (currentGame != null && !currentGame.played) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.openOrStartUserWeekMatch()
                            onNavigate("match")
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, ChampionshipGold)
                    ) {
                        Icon(Icons.Default.SportsFootball, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PLAY LIVE MATCH / CALL PLAYS", color = ChampionshipGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Glowing Emerald Pill Button matching 1000158303_acf699.png
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF00E699), Color(0xFF059669))
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(27.dp))
                        .clickable(enabled = !isBusy) { viewModel.advanceWeek() }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF047857)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FastForward, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBusy) "PROCESSING WEEK..."
                                    else if (state.currentWeek >= 18) "ADVANCE TO ${state.seasonYear + 1} SEASON"
                                    else "ADVANCE WEEK",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "SIM GAMES & RECRUITING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF064E3B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        // 3. 2x4 Action Grid with Tinted Icon Boxes & Subtitles
        item {
            data class HubTile(val title: String, val subtitle: String, val route: String, val icon: ImageVector, val color: Color)
            val tiles = listOf(
                HubTile("Roster & Depth", "View your team roster", "roster", Icons.Default.People, TurfGreen),
                HubTile("Recruiting & Portal", "Scout and recruit players", "recruiting", Icons.Default.Star, TurfGreen),
                HubTile("Schedule & Polls", "Games, rankings and polls", "season", Icons.Default.DateRange, CyanAccent),
                HubTile("Tactics & Sliders", "Adjust your game plan", "tactics", Icons.Default.Tune, TurfGreen),
                HubTile("Finances, NIL & Staff", "Budget, NIL and staff", "office", Icons.Default.AccountBalance, TurfGreen),
                HubTile("Awards, Draft & History", "Season awards and draft", "history", Icons.Default.WorkspacePremium, CyanAccent),
                HubTile("Custom Editors & JSON", "Edit teams, players and more", "editor", Icons.Default.Edit, PurpleAccent),
                HubTile("Settings & Saves", "Game settings and saves", "settings", Icons.Default.Settings, CyanAccent)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in tiles.indices step 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        for (j in i..minOf(i + 1, tiles.size - 1)) {
                            val tile = tiles[j]
                            DynastyCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(tile.route) },
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(tile.color.copy(alpha = 0.18f))
                                            .border(1.dp, tile.color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(tile.icon, contentDescription = tile.title, tint = tile.color, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tile.title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite, maxLines = 1)
                                        Text(tile.subtitle, fontSize = 10.sp, color = TextMuted, maxLines = 1)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Dynasty Wire News with Stadium Thumbnails
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DYNASTY WIRE NEWS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ChampionshipGold)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigate("season") }
                ) {
                    Text("View All", fontSize = 12.sp, color = TurfGreen, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(16.dp))
                }
            }
        }

        items(state.newsFeed.take(12), key = { it.id }) { news ->
            val badgeLabel = when {
                news.category == "RECRUITING" -> "REC"
                news.category == "POLL" -> "TOP25"
                else -> "NCAA"
            }
            DynastyCard(contentPadding = PaddingValues(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NewsStadiumThumbnail(badgeText = badgeLabel)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TurfGreen.copy(alpha = 0.2f))
                                    .border(1.dp, TurfGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(news.category, fontSize = 9.sp, color = TurfGreen, fontWeight = FontWeight.ExtraBold)
                            }
                            Text("${news.season} Wk ${news.week}", fontSize = 10.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(news.headline, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = TextWhite)
                        Text(news.body, fontSize = 11.sp, color = TextMuted)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Rebuilt Roster & Depth Chart Screen matching screenshot 1000158304_acf699.png.
 */
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TurfGreen.copy(alpha = 0.15f))
                        .border(1.5.dp, TurfGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SportsFootball, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "ROSTER & DEPTH CHART (${teamPlayers.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TurfGreen.copy(alpha = 0.14f))
                    .border(1.2.dp, TurfGreen, RoundedCornerShape(10.dp))
                    .clickable { viewModel.autoSortDepthChart() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AUTO\nDEPTH", color = TurfGreen, fontWeight = FontWeight.Black, fontSize = 10.sp, lineHeight = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rounded Search Bar
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            placeholder = { Text("Search player name", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface,
                focusedBorderColor = TurfGreen,
                unfocusedBorderColor = BorderBlue
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Position Filter Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val isAll = selectedPos == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAll) TurfGreen else NavyCard)
                        .border(1.dp, if (isAll) TurfGreen else BorderBlue, RoundedCornerShape(10.dp))
                        .clickable { selectedPos = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = null,
                            tint = if (isAll) Color.Black else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("ALL", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isAll) Color.Black else TextWhite)
                    }
                }
            }
            items(Position.entries) { pos ->
                val isSelected = selectedPos == pos
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) TurfGreen else NavyCard)
                        .border(1.dp, if (isSelected) TurfGreen else BorderBlue, RoundedCornerShape(10.dp))
                        .clickable { selectedPos = pos }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SportsFootball,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(pos.code, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isSelected) Color.Black else TextWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort By Container Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NavySurface)
                .border(1.dp, BorderBlue, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sort by:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "OVR" to Icons.Default.SwapVert,
                    "POT" to Icons.Default.Star,
                    "DEPTH" to Icons.Default.Layers,
                    "GPA" to Icons.Default.School
                ).forEach { (mode, icon) ->
                    val active = sortBy == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) TurfGreen.copy(alpha = 0.2f) else NavyCard)
                            .border(1.dp, if (active) TurfGreen else BorderBlue, RoundedCornerShape(18.dp))
                            .clickable { sortBy = mode }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, tint = if (active) TurfGreen else TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(mode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) TurfGreen else TextWhite)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Player Cards
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(teamPlayers, key = { it.id }) { player ->
                val healthy = player.isAvailableToPlay
                DynastyCard(
                    accentColor = if (!healthy) CrimsonAlert else TurfGreen.copy(alpha = 0.5f),
                    showLeftStripe = true,
                    stripeColor = if (!healthy) CrimsonAlert else TurfGreen
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Position & Depth Badge (#1, #10) with diagonal bottom stripe
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySurface)
                                .border(1.2.dp, TurfGreen.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val p = Path().apply {
                                    moveTo(0f, size.height)
                                    lineTo(size.width * 0.65f, size.height * 0.78f)
                                    lineTo(size.width, size.height * 0.85f)
                                    lineTo(size.width, size.height)
                                    close()
                                }
                                drawPath(p, TurfGreen.copy(alpha = 0.35f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(player.position.code, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("#${player.depthOrder}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Center Name, Class, Stars, and 3-Column SPD | STR | AWR
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(player.fullName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                if (player.isCaptain) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("👑", fontSize = 14.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(player.yearClass.short, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("  |  ", color = BorderBlue)
                                FiveStarRatingRow(stars = player.stars)
                            }
                            HorizontalDivider(color = BorderBlue.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("SPD", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                                    Text("${player.speed}", fontSize = 15.sp, color = TextWhite, fontWeight = FontWeight.Black)
                                }
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(BorderBlue))
                                Column {
                                    Text("STR", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                                    Text("${player.strength}", fontSize = 15.sp, color = TextWhite, fontWeight = FontWeight.Black)
                                }
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(BorderBlue))
                                Column {
                                    Text("AWR", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                                    Text("${player.awareness}", fontSize = 15.sp, color = TextWhite, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Right GPA + Glowing OVR Box + Gold POT
                        Row(verticalAlignment = Alignment.Top) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 8.dp, top = 4.dp)) {
                                Text("GPA", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                                Text("${player.gpa}", fontSize = 16.sp, color = TextWhite, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(TurfGreen.copy(alpha = 0.16f))
                                        .border(1.5.dp, TurfGreen, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${player.overall}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TurfGreen)
                                        Text("OVR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("POT", fontSize = 10.sp, color = ChampionshipGold, fontWeight = FontWeight.Bold)
                                Text("${player.potential}", fontSize = 15.sp, color = ChampionshipGold, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    HorizontalDivider(color = BorderBlue.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                    // Health & Season Stats Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (player.injuryWeeks == 0) TurfGreen else CrimsonAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (player.isRedshirted) "Redshirted" else player.injuryDescription,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (player.injuryWeeks == 0 && !player.isRedshirted) TurfGreen else CrimsonAlert
                        )
                        Text("   |   ", color = BorderBlue)
                        Text(
                            text = "Stats: ${player.passYards + player.rushYards + player.receivingYards} yds, ${player.tackles} tkl",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Pill Buttons: Make Starter, Redshirt, Captain
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(TurfGreen)
                                .clickable { viewModel.promoteStarter(player.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (player.depthOrder == 1) "Starter #1" else "Make Starter",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(NavySurface)
                                .border(1.dp, BorderBlue, RoundedCornerShape(18.dp))
                                .clickable { viewModel.toggleRedshirt(player.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (player.isRedshirted) "Un-Redshirt" else "Redshirt",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(NavySurface)
                                .border(1.dp, BorderBlue, RoundedCornerShape(18.dp))
                                .clickable { viewModel.toggleCaptain(player.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CrownVectorIcon(tint = if (player.isCaptain) ChampionshipGold else CyanAccent, size = 14.dp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "Captain",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (player.isCaptain) ChampionshipGold else TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rebuilt Recruiting & Transfer Portal Screen matching screenshot 1000158305_acf699.png.
 */
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header Card with Segmented Toggle & Star Filter Pills
        DynastyCard(accentColor = BorderBlue) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TurfGreen.copy(alpha = 0.14f))
                        .border(1.5.dp, TurfGreen, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        if (showPortalOnly) "NCAA TRANSFER PORTAL BOARD" else "HIGH SCHOOL RECRUITING BOARD",
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        fontSize = 17.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Commits Signed: ", color = TextMuted, fontSize = 13.sp)
                        Text("$userCommits", color = ChampionshipGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("  •  Weekly Hours Left: ", color = TextMuted, fontSize = 13.sp)
                        Text("${state.weeklyRecruitingHoursRemaining}", color = ChampionshipGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Toggle: [ High School ] vs [ Transfer Portal ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavySurface)
                    .border(1.dp, BorderBlue, RoundedCornerShape(12.dp))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!showPortalOnly) TurfGreen else Color.Transparent)
                        .clickable { showPortalOnly = false },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = if (!showPortalOnly) Color.Black else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "High School",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (!showPortalOnly) Color.Black else TextWhite
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showPortalOnly) TurfGreen else Color.Transparent)
                        .clickable { showPortalOnly = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = if (showPortalOnly) Color.Black else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Transfer Portal",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (showPortalOnly) Color.Black else TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Star Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val starOptions = listOf(0 to "All Stars", 5 to "5★", 4 to "4★", 3 to "3★", 2 to "2★")
                starOptions.forEach { (st, label) ->
                    val selected = filterStars == st
                    Box(
                        modifier = Modifier
                            .weight(if (st == 0) 1.3f else 1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) TurfGreen.copy(alpha = 0.25f) else NavySurface)
                            .border(1.2.dp, if (selected) TurfGreen else BorderBlue, RoundedCornerShape(10.dp))
                            .clickable { filterStars = st },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = if (selected) TextWhite else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recruit List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(displayedRecruits, key = { it.id }) { rec ->
                val rivalCol = state.colleges.find { it.id == rec.topRivalCollegeId }
                val committedCol = state.colleges.find { it.id == rec.committedCollegeId }

                DynastyCard(
                    accentColor = when (rec.committedCollegeId) {
                        state.userCollegeId -> TurfGreen
                        null -> BorderBlue
                        else -> CrimsonAlert.copy(alpha = 0.5f)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Glowing Neon Position Square Badge
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySurface)
                                .border(1.8.dp, TurfGreen, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(rec.position.code, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                Text(rec.position.group.take(3).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Center Name, Stars, State / Scouted / OVR
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rec.fullName, fontSize = 17.sp, fontWeight = FontWeight.Black, color = TextWhite)
                            Spacer(modifier = Modifier.height(2.dp))
                            FiveStarRatingRow(stars = rec.stars)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "State: ${rec.state}   •   Scouted: ${rec.scoutedPercent}%   •   OVR: ${if (rec.scoutedPercent == 0) "—" else rec.displayedOverall}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        // Right COMMITTED badge or OPEN badge
                        if (committedCol != null) {
                            val isUserCommit = committedCol.id == state.userCollegeId
                            val pillColor = if (isUserCommit) TurfGreen else CrimsonAlert
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(pillColor.copy(alpha = 0.15f))
                                        .border(1.dp, pillColor, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        if (isUserCommit) "✓ SIGNED" else "! COMMITTED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = pillColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Box(
                                    modifier = Modifier
                                        .width(74.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(NavySurface)
                                        .border(1.dp, BorderBlue, RoundedCornerShape(14.dp))
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(committedCol.abbreviation, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderBlue.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                    // Bottom Points Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Your Points: ${rec.userPointsAccumulated} (${rec.userWeeklyHours} hrs/wk)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChampionshipGold
                            )
                            Text("   |   ", color = BorderBlue)
                            Text(
                                "Rival ${rivalCol?.abbreviation ?: "AI"}: ${rec.rivalPointsAccumulated} pts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChampionshipGold
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    }

                    // Action Pills for Uncommitted Recruits
                    if (rec.committedCollegeId == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            val actions = listOf(
                                Triple(if (rec.scoutedPercent >= 100) "Scouted" else "Scout 25h", rec.scoutedPercent < 100) { viewModel.scoutRecruit(rec.id) },
                                Triple(if (rec.userOfferedScholarship) "Offered" else "Offer 35h", !rec.userOfferedScholarship) { viewModel.offerScholarship(rec.id) },
                                Triple("+25h", true) { viewModel.adjustRecruitWeeklyHours(rec.id, 25) },
                                Triple("-25h", rec.userWeeklyHours >= 25) { viewModel.adjustRecruitWeeklyHours(rec.id, -25) },
                                Triple(if (rec.userScheduledVisit) "Visited" else "Visit 50h", !rec.userScheduledVisit) { viewModel.scheduleCampusVisit(rec.id) }
                            )
                            actions.forEach { (label, enabled, action) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (enabled) TurfGreen.copy(alpha = 0.16f) else NavySurface)
                                        .border(1.dp, if (enabled) TurfGreen else BorderBlue, RoundedCornerShape(8.dp))
                                        .clickable(enabled = enabled) { action() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (enabled) TurfGreen else TextMuted
                                    )
                                }
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
        DynastyCard(accentColor = TurfGreen) {
            Text(live.titleLabel, fontSize = 12.sp, color = ChampionshipGold, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CollegeLogo2D(college = awayCol, size = 42.dp)
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
                    CollegeLogo2D(college = homeCol, size = 42.dp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (live.ballOnYardLine / 100f).coerceIn(0.02f, 0.98f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
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
                        else -> BorderBlue
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
