package com.dynasty.cfb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynasty.cfb.data.*
import com.dynasty.cfb.domain.SeasonEngine
import com.dynasty.cfb.editor.EditorValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonStandingsAndPollsScreen(state: CareerState) {
    var tabIndex by remember { mutableStateOf(0) }
    var selectedConfId by remember { mutableStateOf(state.conferences.first().id) }

    Column(modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            listOf("Top 25 Poll", "Conf Standings", "My Schedule", "Playoff / Bowls").forEachIndexed { idx, label ->
                FilterChip(
                    selected = tabIndex == idx,
                    onClick = { tabIndex = idx },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        when (tabIndex) {
            0 -> {
                val top25 = remember(state.colleges) {
                    state.colleges.filter { it.pollRank <= 25 }.sortedBy { it.pollRank }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(top25, key = { it.id }) { col ->
                        DynastyCard(accentColor = if (col.id == state.userCollegeId) TurfGreen else Color.Transparent) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${col.pollRank}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ChampionshipGold, modifier = Modifier.width(42.dp))
                                CollegeLogo2D(college = col, size = 36.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${col.name} ${col.mascot}", fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text("Prestige ${col.prestigeScore} • PF ${col.pointsFor} / PA ${col.pointsAgainst}", fontSize = 12.sp, color = TextMuted)
                                }
                                Text("${col.wins}-${col.losses}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TurfGreen)
                            }
                        }
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(standings, key = { it.id }) { col ->
                        DynastyCard(accentColor = if (col.id == state.userCollegeId) TurfGreen else Color.Transparent) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CollegeLogo2D(college = col, size = 34.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(col.name, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text("Diff: ${col.pointsFor - col.pointsAgainst}", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Text("Conf: ${col.confWins}-${col.confLosses} | All: ${col.wins}-${col.losses}", fontWeight = FontWeight.Bold, color = ChampionshipGold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            2 -> {
                val userGames = remember(state.schedule, state.userCollegeId) {
                    state.schedule.filter { it.homeCollegeId == state.userCollegeId || it.awayCollegeId == state.userCollegeId }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(userGames, key = { it.id }) { g ->
                        val isHome = g.homeCollegeId == state.userCollegeId
                        val oppId = if (isHome) g.awayCollegeId else g.homeCollegeId
                        val opp = state.colleges.find { it.id == oppId }
                        DynastyCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Week ${g.week} • ${g.titleLabel}", fontSize = 11.sp, color = ChampionshipGold)
                                    Text("${if (isHome) "vs" else "@"} ${opp?.name ?: "Opponent"} (${opp?.wins ?: 0}-${opp?.losses ?: 0})", fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                                if (g.played) {
                                    val userWon = (isHome && g.homeScore >= g.awayScore) || (!isHome && g.awayScore > g.homeScore)
                                    Text(
                                        "${if (userWon) "W" else "L"} ${g.homeScore}-${g.awayScore}",
                                        fontWeight = FontWeight.ExtraBold,
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (postGames.isEmpty()) {
                    DynastyCard {
                        Text("Conference Championships (Week 13) and the ${state.settings.playoffTeamsCount}-Team Playoff & Bowls (Weeks 14-17) are seeded automatically after Week 12.", color = TextMuted)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(postGames, key = { it.id }) { g ->
                            val h = state.colleges.find { it.id == g.homeCollegeId }
                            val a = state.colleges.find { it.id == g.awayCollegeId }
                            DynastyCard(accentColor = ChampionshipGold) {
                                Text("Week ${g.week} • ${g.titleLabel}", fontSize = 12.sp, color = ChampionshipGold, fontWeight = FontWeight.Bold)
                                Text("${a?.name ?: "Away"} vs ${h?.name ?: "Home"}", fontWeight = FontWeight.Bold, color = TextWhite)
                                if (g.played) {
                                    Text("Final: ${a?.abbreviation} ${g.awayScore} - ${h?.abbreviation} ${g.homeScore}", color = TurfGreen, fontWeight = FontWeight.Bold)
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
            Text("TACTICS, SCHEMES & GAMEPLAN SLIDERS", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreen)
                ) {
                    Text("SAVE GAMEPLAN TACTICS", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun FinancesStaffAndOffersScreen(state: CareerState, viewModel: DynastyViewModel) {
    val userCol = state.colleges.find { it.id == state.userCollegeId } ?: state.colleges.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DynastyCard(accentColor = TurfGreen) {
                Text("ATHLETIC DEPARTMENT & FACILITIES", fontWeight = FontWeight.ExtraBold, color = TextWhite, fontSize = 16.sp)
                Text("Available Budget: $${userCol.budgetMillions}M • NIL Collective: $${userCol.nilPoolMillions}M", color = ChampionshipGold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { viewModel.upgradeFacility("TRAINING") }, modifier = Modifier.weight(1f)) {
                        Text("Training Lvl ${userCol.trainingFacilityLevel}\n($3.5M)", fontSize = 11.sp, color = Color.Black)
                    }
                    Button(onClick = { viewModel.upgradeFacility("ACADEMIC") }, modifier = Modifier.weight(1f)) {
                        Text("Academic Lvl ${userCol.academicFacilityLevel}\n($3.5M)", fontSize = 11.sp, color = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { viewModel.upgradeFacility("MEDICAL") }, modifier = Modifier.weight(1f)) {
                        Text("Medical Lvl ${userCol.medicalFacilityLevel}\n($3.5M)", fontSize = 11.sp, color = Color.Black)
                    }
                    Button(onClick = { viewModel.upgradeFacility("NIL") }, modifier = Modifier.weight(1f)) {
                        Text("Boost NIL +Hrs\n($3.5M)", fontSize = 11.sp, color = Color.Black)
                    }
                }
            }
        }

        item {
            Text("COACHING STAFF", fontWeight = FontWeight.ExtraBold, color = ChampionshipGold)
        }
        items(state.staff, key = { it.id }) { member ->
            DynastyCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${member.role}: ${member.name}", fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("${member.specialty} • Rating: ${member.rating} • Salary: $${member.salaryMillions}M", fontSize = 12.sp, color = TextMuted)
                    }
                    OutlinedButton(onClick = { viewModel.upgradeStaffMember(member.id) }) {
                        Text("Train ($1.5M)", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text("COACHING CAROUSEL JOB OFFERS", fontWeight = FontWeight.ExtraBold, color = ChampionshipGold)
        }
        if (state.pendingJobOffers.isEmpty()) {
            item {
                DynastyCard {
                    Text("Job offers from other universities arrive at the end of the Postseason (Week 18 Offseason).", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(state.pendingJobOffers, key = { it.collegeId }) { offer ->
                DynastyCard(accentColor = ChampionshipGold) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("${offer.collegeName} (${"★".repeat(offer.prestigeStars)})", fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("$${offer.salaryMillions}M/yr (${offer.contractYears} yrs) • NIL Pool $${offer.nilPoolMillions}M", fontSize = 12.sp, color = TextMuted)
                        }
                        Button(onClick = { viewModel.acceptJobOffer(offer) }) {
                            Text("ACCEPT", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
            Text("TROPHY ROOM & ACHIEVEMENTS", fontWeight = FontWeight.ExtraBold, color = ChampionshipGold, fontSize = 16.sp)
        }
        items(state.achievements, key = { it.id }) { ach ->
            DynastyCard(accentColor = if (ach.unlocked) TurfGreen else Color.Transparent) {
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
            Text("DYNASTY YEAR-BY-YEAR HISTORY", fontWeight = FontWeight.ExtraBold, color = ChampionshipGold, fontSize = 16.sp)
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
            Text("AWARDS & 1ST ROUND NFL DRAFT PICKS", fontWeight = FontWeight.ExtraBold, color = ChampionshipGold, fontSize = 16.sp)
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

    // College Editor state
    var colName by remember(userCol) { mutableStateOf(userCol.name) }
    var colMascot by remember(userCol) { mutableStateOf(userCol.mascot) }
    var colAbbr by remember(userCol) { mutableStateOf(userCol.abbreviation) }
    var colPrestige by remember(userCol) { mutableStateOf(userCol.prestigeScore.toString()) }

    // Player Editor state (edits top player on user team)
    val topPlayer = remember(state.players, state.userCollegeId) {
        state.players.filter { it.collegeId == state.userCollegeId }.maxByOrNull { it.overall } ?: state.players.first()
    }
    var pFirst by remember(topPlayer) { mutableStateOf(topPlayer.firstName) }
    var pLast by remember(topPlayer) { mutableStateOf(topPlayer.lastName) }
    var pGpa by remember(topPlayer) { mutableStateOf(topPlayer.gpa.toString()) }
    var pPot by remember(topPlayer) { mutableStateOf(topPlayer.potential.toString()) }

    // Conference Editor state
    val userConf = state.conferences.find { it.id == userCol.conferenceId } ?: state.conferences.first()
    var confName by remember(userConf) { mutableStateOf(userConf.name) }
    var confShort by remember(userConf) { mutableStateOf(userConf.shortName) }
    var confStars by remember(userConf) { mutableStateOf(userConf.prestige.toString()) }

    // JSON Import/Export state
    var jsonBuffer by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(NavyBackground).padding(12.dp)) {
        Text("IN-GAME EDITORS & JSON IMPORT/EXPORT", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
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
                Text("COMPETITION & GAMEPLAY SETTINGS", fontWeight = FontWeight.ExtraBold, color = TurfGreen)
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
                Text("MANUAL SAVE TO SLOTS", fontWeight = FontWeight.ExtraBold, color = ChampionshipGold)
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
