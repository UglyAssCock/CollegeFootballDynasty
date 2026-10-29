package com.dynasty.cfb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynasty.cfb.data.*
import com.dynasty.cfb.domain.SeasonEngine
import com.dynasty.cfb.editor.EditorValidator

/**
 * Rebuilt Season Standings & Top 25 Poll Screen matching screenshot 1000158306_acf699.png.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonStandingsAndPollsScreen(state: CareerState) {
    var tabIndex by remember { mutableStateOf(0) }
    var selectedConfId by remember { mutableStateOf(state.conferences.first().id) }
    val userCol = state.colleges.find { it.id == state.userCollegeId } ?: state.colleges.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top Icon Pill Tabs
        val tabs = listOf(
            Triple(0, "Top 25 Poll", Icons.Default.Assessment),
            Triple(1, "Conf Standings", Icons.Default.EmojiEvents),
            Triple(2, "My Schedule", Icons.Default.DateRange),
            Triple(3, "Playoff", Icons.Default.Star)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 10.dp)) {
            items(tabs) { (idx, label, icon) ->
                val active = tabIndex == idx
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (active) Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                            else Brush.horizontalGradient(listOf(NavyCard, NavyCard))
                        )
                        .border(1.2.dp, if (active) TurfGreen else BorderBlue, RoundedCornerShape(12.dp))
                        .clickable { tabIndex = idx }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, tint = if (active) Color.White else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (active) Color.White else TextMuted)
                    }
                }
            }
        }

        when (tabIndex) {
            0 -> {
                val top25 = remember(state.colleges) {
                    state.colleges.filter { it.pollRank <= 25 }.sortedBy { it.pollRank }
                }

                // National Rankings Summary Hero Card
                DynastyCard(accentColor = BorderBlue) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.4f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SportsFootball,
                                    contentDescription = null,
                                    tint = TurfGreen,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("WEEK ${state.currentWeek}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TurfGreen)
                                        Text("  •  NATIONAL RANKINGS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Top 25 Poll", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { (state.currentWeek / 18f).coerceIn(0.1f, 1f) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = TurfGreen,
                                    trackColor = NavySurface
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("25 / 25", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Top 25 teams this week", fontSize = 12.sp, color = TextMuted)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .width(1.dp)
                                .height(86.dp)
                                .background(BorderBlue)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Season", fontSize = 10.sp, color = TextMuted)
                                    Text("Week ${state.currentWeek}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Next Poll", fontSize = 10.sp, color = TextMuted)
                                    Text("Week ${(state.currentWeek + 1).coerceAtMost(18)}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Your Rank", fontSize = 10.sp, color = TextMuted)
                                    Text(if (userCol.pollRank <= 25) "#${userCol.pollRank}" else "NR", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(top25, key = { it.id }) { col ->
                        val isFirst = col.pollRank == 1
                        val isUser = col.id == state.userCollegeId
                        DynastyCard(
                            accentColor = if (isFirst || isUser) TurfGreen else BorderBlue
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Rank Badge Box (#1 with crown, #2, #3...)
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1F2421))
                                        .border(
                                            1.5.dp,
                                            if (isFirst) ChampionshipGold else ChampionshipGold.copy(alpha = 0.4f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "#${col.pollRank}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 19.sp,
                                            color = ChampionshipGold
                                        )
                                        if (isFirst) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            CrownVectorIcon(tint = ChampionshipGold, size = 14.dp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Center Logo + Glowing Green Record Pill underneath
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CollegeLogo2D(college = col, size = 56.dp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(TurfGreen)
                                            .padding(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${col.wins}-${col.losses}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // School Name, Mascot, Prestige, PF/PA
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(col.name, fontWeight = FontWeight.Black, fontSize = 17.sp, color = TextWhite)
                                    Text(col.mascot, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextWhite)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Prestige ${col.prestigeScore}   •   PF ${col.pointsFor} / PA ${col.pointsAgainst}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                            }
                        }
                    }
                    item {
                        StadiumWireframeFooter(showTagline = true)
                    }
                }
            }

            1 -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    items(state.conferences) { conf ->
                        FilterChip(
                            selected = selectedConfId == conf.id,
                            onClick = { selectedConfId = conf.id },
                            label = { Text(conf.shortName) }
                        )
                    }
                }
                val standings = remember(state.colleges, selectedConfId) {
                    SeasonEngine.sortConferenceStandings(state.colleges, selectedConfId)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(standings, key = { it.id }) { col ->
                        DynastyCard(accentColor = if (col.id == state.userCollegeId) TurfGreen else BorderBlue) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CollegeLogo2D(college = col, size = 40.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("${col.name} ${col.mascot}", fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text("PF ${col.pointsFor} / PA ${col.pointsAgainst} (Diff: ${col.pointsFor - col.pointsAgainst})", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Conf: ${col.confWins}-${col.confLosses}", fontWeight = FontWeight.Black, color = TurfGreen, fontSize = 13.sp)
                                    Text("Overall: ${col.wins}-${col.losses}", fontWeight = FontWeight.SemiBold, color = ChampionshipGold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val userGames = remember(state.schedule, state.userCollegeId) {
                    state.schedule.filter { it.homeCollegeId == state.userCollegeId || it.awayCollegeId == state.userCollegeId }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(userGames, key = { it.id }) { g ->
                        val isHome = g.homeCollegeId == state.userCollegeId
                        val oppId = if (isHome) g.awayCollegeId else g.homeCollegeId
                        val opp = state.colleges.find { it.id == oppId }
                        DynastyCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (opp != null) {
                                        CollegeLogo2D(college = opp, size = 40.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    Column {
                                        Text("Week ${g.week} • ${g.titleLabel}", fontSize = 11.sp, color = ChampionshipGold, fontWeight = FontWeight.Bold)
                                        Text("${if (isHome) "vs" else "@"} ${opp?.name ?: "Opponent"} (${opp?.wins ?: 0}-${opp?.losses ?: 0})", fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                    }
                                }
                                if (g.played) {
                                    val userWon = (isHome && g.homeScore >= g.awayScore) || (!isHome && g.awayScore > g.homeScore)
                                    Text(
                                        "${if (userWon) "W" else "L"} ${g.homeScore}-${g.awayScore}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = if (userWon) TurfGreen else CrimsonAlert
                                    )
                                } else {
                                    Text("Upcoming", fontSize = 12.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                val postGames = remember(state.schedule) {
                    state.schedule.filter { it.week >= 13 }
                }
                Text(
                    "Configured Playoff Size: ${state.settings.playoffTeamsCount}-Team Bracket",
                    color = ChampionshipGold,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (postGames.isEmpty()) {
                    DynastyCard {
                        Text(
                            "Conference Championships (Week 13) and the ${state.settings.playoffTeamsCount}-Team Playoff & Bowls (Weeks 14-17) are seeded automatically after Week 12.",
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(postGames, key = { it.id }) { g ->
                            val h = state.colleges.find { it.id == g.homeCollegeId }
                            val a = state.colleges.find { it.id == g.awayCollegeId }
                            DynastyCard(accentColor = ChampionshipGold) {
                                Text("Week ${g.week} • ${g.titleLabel}", fontSize = 12.sp, color = ChampionshipGold, fontWeight = FontWeight.Bold)
                                Text("${a?.name ?: "Away"} vs ${h?.name ?: "Home"}", fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                if (g.played) {
                                    Text("Final: ${a?.abbreviation} ${g.awayScore} - ${h?.abbreviation} ${g.homeScore}", color = TurfGreen, fontWeight = FontWeight.Black)
                                }
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
fun TacticsAndSlidersScreen(state: CareerState, viewModel: DynastyViewModel) {
    var offScheme by remember(state.tactics) { mutableStateOf(state.tactics.offenseScheme) }
    var defScheme by remember(state.tactics) { mutableStateOf(state.tactics.defenseScheme) }
    var passRatio by remember(state.tactics) { mutableFloatStateOf(state.tactics.passRatioSlider.toFloat()) }
    var tempo by remember(state.tactics) { mutableFloatStateOf(state.tactics.tempoSlider.toFloat()) }
    var fourthAgg by remember(state.tactics) { mutableFloatStateOf(state.tactics.fourthDownAggression.toFloat()) }
    var blitzFreq by remember(state.tactics) { mutableFloatStateOf(state.tactics.blitzFrequency.toFloat()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("TACTICS, SCHEMES & GAMEPLAN SLIDERS", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextWhite)
        }
        item {
            DynastyCard {
                Text("Offensive Scheme: ${offScheme.label}", fontWeight = FontWeight.Bold, color = TurfGreen)
                Text(offScheme.desc, fontSize = 12.sp, color = TextMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    items(OffenseScheme.entries) { s ->
                        FilterChip(selected = offScheme == s, onClick = { offScheme = s }, label = { Text(s.label) })
                    }
                }
            }
        }
        item {
            DynastyCard {
                Text("Defensive Scheme: ${defScheme.label}", fontWeight = FontWeight.Bold, color = ChampionshipGold)
                Text(defScheme.desc, fontSize = 12.sp, color = TextMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    items(DefenseScheme.entries) { s ->
                        FilterChip(selected = defScheme == s, onClick = { defScheme = s }, label = { Text(s.label) })
                    }
                }
            }
        }
        item {
            DynastyCard {
                Text("Pass / Run Ratio: ${passRatio.toInt()}% Pass", color = TextWhite, fontWeight = FontWeight.SemiBold)
                Slider(value = passRatio, onValueChange = { passRatio = it }, valueRange = 10f..90f)

                Text("Offensive Tempo: ${tempo.toInt()}%", color = TextWhite, fontWeight = FontWeight.SemiBold)
                Slider(value = tempo, onValueChange = { tempo = it }, valueRange = 10f..100f)

                Text("4th Down Aggressiveness: ${fourthAgg.toInt()}%", color = TextWhite, fontWeight = FontWeight.SemiBold)
                Slider(value = fourthAgg, onValueChange = { fourthAgg = it }, valueRange = 0f..100f)

                Text("Defensive Blitz Frequency: ${blitzFreq.toInt()}%", color = TextWhite, fontWeight = FontWeight.SemiBold)
                Slider(value = blitzFreq, onValueChange = { blitzFreq = it }, valueRange = 0f..100f)

                Button(
                    onClick = {
                        viewModel.updateTactics(
                            state.tactics.copy(
                                offenseScheme = offScheme,
                                defenseScheme = defScheme,
                                passRatioSlider = passRatio.toInt(),
                                tempoSlider = tempo.toInt(),
                                fourthDownAggression = fourthAgg.toInt(),
                                blitzFrequency = blitzFreq.toInt()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
                ) {
                    Text("SAVE GAMEPLAN TACTICS", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/**
 * Rebuilt Athletic Department & Facilities (Office) Screen matching screenshot 1000158307_acf699.png.
 */
@Composable
fun FinancesStaffAndOffersScreen(state: CareerState, viewModel: DynastyViewModel) {
    val userCol = state.colleges.find { it.id == state.userCollegeId } ?: state.colleges.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header with Temple Icon & Subtitle
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = TurfGreen,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ATHLETIC DEPARTMENT & FACILITIES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = "Build your program. Develop your players. Win championships.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // 2. Two Side-by-Side Budget & NIL Collective Cards
        item {
            DynastyCard(accentColor = BorderBlue, contentPadding = PaddingValues(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurface)
                            .border(1.dp, BorderBlue, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("AVAILABLE BUDGET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text(
                                    "$${fmtMillions(userCol.budgetMillions)}M",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TurfGreen
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurface)
                            .border(1.dp, BorderBlue, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("NIL COLLECTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text(
                                    "$${fmtMillions(userCol.nilPoolMillions)}M",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TurfGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Facility Upgrades 4-Column Tile Grid
        item {
            DynastyCard(accentColor = BorderBlue) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FACILITY UPGRADES", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ChampionshipGold)
                }
                Spacer(modifier = Modifier.height(10.dp))

                data class FacilityTile(val code: String, val title: String, val levelText: String, val icon: ImageVector)
                val facTiles = listOf(
                    FacilityTile("TRAINING", "TRAINING", "Lvl ${userCol.trainingFacilityLevel}", Icons.Default.FitnessCenter),
                    FacilityTile("ACADEMIC", "ACADEMIC", "Lvl ${userCol.academicFacilityLevel}", Icons.Default.School),
                    FacilityTile("MEDICAL", "MEDICAL", "Lvl ${userCol.medicalFacilityLevel}", Icons.Default.LocalHospital),
                    FacilityTile("NIL", "BOOST NIL", "+Hrs", Icons.Default.MonetizationOn)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    facTiles.forEach { tile ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySurface)
                                .border(1.2.dp, TurfGreen.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.upgradeFacility(tile.code) }
                                .padding(horizontal = 6.dp, vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(tile.icon, contentDescription = tile.title, tint = TurfGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${tile.title} ${tile.levelText}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextWhite,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                                ) {
                                    Text("$3.5M", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Coaching Staff Section with Headset Avatars
        item {
            DynastyCard(accentColor = BorderBlue) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("COACHING STAFF", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ChampionshipGold)
                }
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.staff.forEach { member ->
                        val shortRole = when {
                            member.role.contains("Recruit", ignoreCase = true) -> "RC"
                            else -> member.role.take(2).uppercase()
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NavySurface)
                                .border(1.dp, BorderBlue, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoachHeadsetAvatar(roleCode = shortRole, size = 62.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TurfGreen.copy(alpha = 0.16f))
                                            .border(1.dp, TurfGreen, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 1.dp)
                                    ) {
                                        Text(shortRole, fontSize = 10.sp, fontWeight = FontWeight.Black, color = TurfGreen)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(member.name, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                    Text(member.specialty, fontSize = 12.sp, color = CyanAccent.copy(alpha = 0.85f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rating ", fontSize = 11.sp, color = TextMuted)
                                        Text("${member.rating}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                        Text("   |   ", color = BorderBlue)
                                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Salary ", fontSize = 11.sp, color = TextMuted)
                                        Text("$${String.format(java.util.Locale.US, "%.2f", member.salaryMillions)}M", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                    }
                                }

                                // Right Emerald Outlined Action Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(TurfGreen.copy(alpha = 0.14f))
                                        .border(1.2.dp, TurfGreen, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.upgradeStaffMember(member.id) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (shortRole == "RC") Icons.Default.Description else Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = TurfGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            if (shortRole == "RC") "CONTRACT" else "TRAINING",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhite
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TurfGreen, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Coaching Carousel Job Offers Card
        item {
            DynastyCard(accentColor = BorderBlue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanAccent.copy(alpha = 0.14f))
                            .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("COACHING CAROUSEL JOB OFFERS", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ChampionshipGold)
                        if (state.pendingJobOffers.isEmpty()) {
                            Text(
                                "No active offers right now. The offseason will bring new opportunities for your coaching staff.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        } else {
                            Text("${state.pendingJobOffers.size} active head coaching offers available below!", fontSize = 12.sp, color = TurfGreen)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurface)
                            .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanAccent)
                    }
                }
            }
        }

        if (state.pendingJobOffers.isNotEmpty()) {
            items(state.pendingJobOffers, key = { it.collegeId }) { offer ->
                DynastyCard(accentColor = ChampionshipGold) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${offer.collegeName} (${"★".repeat(offer.prestigeStars)})", fontWeight = FontWeight.Black, color = TextWhite)
                            Text("$${fmtMillions(offer.salaryMillions)}M/yr (${offer.contractYears} yrs) • NIL Pool $${fmtMillions(offer.nilPoolMillions)}M", fontSize = 12.sp, color = TextMuted)
                        }
                        Button(onClick = { viewModel.acceptJobOffer(offer) }) {
                            Text("ACCEPT", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        item {
            StadiumWireframeFooter(showTagline = false)
        }
    }
}

@Composable
fun AwardsDraftAndHistoryScreen(state: CareerState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("TROPHY ROOM & ACHIEVEMENTS", fontWeight = FontWeight.Black, color = ChampionshipGold, fontSize = 16.sp)
        }
        items(state.achievements, key = { it.id }) { ach ->
            DynastyCard(accentColor = if (ach.unlocked) TurfGreen else BorderBlue, showLeftStripe = ach.unlocked) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ach.title, fontWeight = FontWeight.Bold, color = if (ach.unlocked) TurfGreen else TextWhite)
                        Text(ach.description, fontSize = 12.sp, color = TextMuted)
                    }
                    Text(if (ach.unlocked) "UNLOCKED (${ach.unlockedSeason})" else "LOCKED", fontSize = 11.sp, color = ChampionshipGold, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text("DYNASTY YEAR-BY-YEAR HISTORY", fontWeight = FontWeight.Black, color = ChampionshipGold, fontSize = 16.sp)
        }
        if (state.seasonHistory.isEmpty()) {
            item { DynastyCard { Text("Complete your first season to log dynasty history.", color = TextMuted) } }
        } else {
            items(state.seasonHistory) { h ->
                DynastyCard {
                    Text("${h.seasonYear}: ${h.userCollegeName} (${h.userRecord}) - Rank #${h.userFinalRank}", fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("National Champ: ${h.nationalChampionName} | POTY: ${h.heismanWinner}", fontSize = 12.sp, color = TextMuted)
                }
            }
        }

        item {
            Text("AWARDS & 1ST ROUND NFL DRAFT PICKS", fontWeight = FontWeight.Black, color = ChampionshipGold, fontSize = 16.sp)
        }
        items(state.awardsHistory.take(8)) { aw ->
            DynastyCard {
                Text("${aw.season} ${aw.awardName}: ${aw.playerName} (${aw.position.code} - ${aw.collegeName})", fontWeight = FontWeight.Bold, color = TurfGreen)
                Text(aw.statLine, fontSize = 12.sp, color = TextMuted)
            }
        }
        items(state.draftHistory.take(10)) { dp ->
            DynastyCard {
                Text("${dp.season} Pick #${dp.pickNumber}: ${dp.playerName} (${dp.position.code}, ${dp.overall} OVR) - ${dp.collegeName}", fontSize = 13.sp, color = TextWhite)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEditorsScreen(state: CareerState, viewModel: DynastyViewModel) {
    var mode by remember { mutableStateOf(0) }
    val userCol = state.colleges.find { it.id == state.userCollegeId } ?: state.colleges.first()

    var colName by remember(userCol) { mutableStateOf(userCol.name) }
    var colMascot by remember(userCol) { mutableStateOf(userCol.mascot) }
    var colAbbr by remember(userCol) { mutableStateOf(userCol.abbreviation) }
    var colPrestige by remember(userCol) { mutableStateOf(userCol.prestigeScore.toString()) }

    val topPlayer = remember(state.players, state.userCollegeId) {
        state.players.filter { it.collegeId == state.userCollegeId }.maxByOrNull { it.overall } ?: state.players.first()
    }
    var pFirst by remember(topPlayer) { mutableStateOf(topPlayer.firstName) }
    var pLast by remember(topPlayer) { mutableStateOf(topPlayer.lastName) }
    var pGpa by remember(topPlayer) { mutableStateOf(topPlayer.gpa.toString()) }
    var pPot by remember(topPlayer) { mutableStateOf(topPlayer.potential.toString()) }

    val userConf = state.conferences.find { it.id == userCol.conferenceId } ?: state.conferences.first()
    var confName by remember(userConf) { mutableStateOf(userConf.name) }
    var confShort by remember(userConf) { mutableStateOf(userConf.shortName) }
    var confStars by remember(userConf) { mutableStateOf(userConf.prestige.toString()) }

    var jsonBuffer by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp)) {
        Text("IN-GAME EDITORS & JSON IMPORT/EXPORT", fontSize = 17.sp, fontWeight = FontWeight.Black, color = TextWhite)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("College", "Player", "Conference", "JSON Data").forEachIndexed { idx, label ->
                FilterChip(selected = mode == idx, onClick = { mode = idx }, label = { Text(label, fontSize = 11.sp) })
            }
        }

        when (mode) {
            0 -> DynastyCard {
                Text("Edit Your University (${userCol.name})", fontWeight = FontWeight.Bold, color = TurfGreen)
                OutlinedTextField(value = colName, onValueChange = { colName = it }, label = { Text("College Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = colMascot, onValueChange = { colMascot = it }, label = { Text("Mascot") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = colAbbr, onValueChange = { colAbbr = it }, label = { Text("Abbreviation (2-4 chars)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = colPrestige, onValueChange = { colPrestige = it }, label = { Text("Prestige Score (40-99)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.saveEditedCollege(userCol.id, colName, colMascot, colAbbr, colPrestige.toIntOrNull() ?: 75)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("VALIDATE & SAVE COLLEGE", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
            1 -> DynastyCard {
                Text("Edit Star Player (${topPlayer.fullName} - ${topPlayer.position.code})", fontWeight = FontWeight.Bold, color = TurfGreen)
                OutlinedTextField(value = pFirst, onValueChange = { pFirst = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pLast, onValueChange = { pLast = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pGpa, onValueChange = { pGpa = it }, label = { Text("GPA (1.0 - 4.0)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pPot, onValueChange = { pPot = it }, label = { Text("Potential (50 - 99)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.saveEditedPlayer(topPlayer.id, pFirst, pLast, pGpa.toDoubleOrNull() ?: 3.0, pPot.toIntOrNull() ?: 88, speedBoost = 2)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("VALIDATE & SAVE PLAYER (+2 SPD/AWR)", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
            2 -> DynastyCard {
                Text("Edit Conference (${userConf.name})", fontWeight = FontWeight.Bold, color = TurfGreen)
                OutlinedTextField(value = confName, onValueChange = { confName = it }, label = { Text("Conference Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = confShort, onValueChange = { confShort = it }, label = { Text("Short Code (2-6 chars)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = confStars, onValueChange = { confStars = it }, label = { Text("Prestige Stars (1-5)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.saveEditedConference(userConf.id, confName, confShort, confStars.toIntOrNull() ?: 4)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("VALIDATE & SAVE CONFERENCE", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
            3 -> DynastyCard {
                Text("JSON Dynasty Export & Validated Import", fontWeight = FontWeight.Bold, color = ChampionshipGold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Button(
                        onClick = { jsonBuffer = EditorValidator.exportCareerToJson(state) },
                        modifier = Modifier.weight(1f)
                    ) { Text("GENERATE EXPORT JSON", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { viewModel.importJsonToCurrentCareer(jsonBuffer) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ChampionshipGold)
                    ) { Text("VALIDATE & IMPORT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                OutlinedTextField(
                    value = jsonBuffer,
                    onValueChange = { jsonBuffer = it },
                    label = { Text("Dynasty JSON Payload") },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndSavesScreen(
    state: CareerState,
    viewModel: DynastyViewModel,
    onExitToTitle: () -> Unit
) {
    var playoffSize by remember(state.settings) { mutableStateOf(state.settings.playoffTeamsCount) }
    var autoSave by remember(state.settings) { mutableStateOf(state.settings.autoSaveEnabled) }
    var injuries by remember(state.settings) { mutableStateOf(state.settings.injuriesEnabled) }
    var bowlPrefix by remember(state.settings) { mutableStateOf(state.settings.bowlGamesTitlePrefix) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DynastyCard {
                Text("COMPETITION & GAMEPLAY SETTINGS", fontWeight = FontWeight.Black, color = TurfGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Text("College Football Playoff Size:", fontSize = 13.sp, color = TextWhite)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                    listOf(4, 8, 12, 16).forEach { sz ->
                        FilterChip(
                            selected = playoffSize == sz,
                            onClick = { playoffSize = sz },
                            label = { Text("$sz Teams") }
                        )
                    }
                }
                OutlinedTextField(
                    value = bowlPrefix,
                    onValueChange = { bowlPrefix = it },
                    label = { Text("Custom Bowl Series Prefix") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Autosave on Week Advance", color = TextWhite)
                    Switch(checked = autoSave, onCheckedChange = { autoSave = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Player Injuries Enabled", color = TextWhite)
                    Switch(checked = injuries, onCheckedChange = { injuries = it })
                }
                Button(
                    onClick = {
                        viewModel.updateSettings(
                            state.settings.copy(
                                playoffTeamsCount = playoffSize,
                                autoSaveEnabled = autoSave,
                                injuriesEnabled = injuries,
                                bowlGamesTitlePrefix = bowlPrefix.ifBlank { "Dynasty Bowl" }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("APPLY SETTINGS", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        item {
            DynastyCard(accentColor = ChampionshipGold) {
                Text("MANUAL SAVE TO SLOTS", fontWeight = FontWeight.Black, color = ChampionshipGold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(1, 2, 3).forEach { slot ->
                        Button(
                            onClick = { viewModel.manualSaveCurrentCareer(slot) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Slot #$slot", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onExitToTitle, modifier = Modifier.fillMaxWidth()) {
                    Text("EXIT TO MAIN TITLE / SWITCH SAVE SLOT", color = CrimsonAlert, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
