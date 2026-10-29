package com.dynasty.cfb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dynasty.cfb.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DynastyTheme {
                DynastyAppRoot()
            }
        }
    }
}

@Composable
fun DynastyAppRoot(viewModel: DynastyViewModel = viewModel()) {
    val navController = rememberNavController()
    val careerState by viewModel.careerState.collectAsState()
    val banner by viewModel.statusBanner.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "title"

    val showBottomBar = careerState != null && currentRoute !in listOf("title", "new_career/{slotId}")

    Scaffold(
        containerColor = NavyBackground,
        topBar = {
            if (banner != null) {
                TopStatusBanner(
                    message = banner ?: "",
                    onDismiss = { viewModel.clearBanner() }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                Column(modifier = Modifier.fillMaxWidth().background(NavySurface)) {
                    HorizontalDivider(color = BorderBlue, thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val items = listOf(
                            Triple("hub", "Hub", Icons.Default.Home),
                            Triple("roster", "Roster", Icons.Default.People),
                            Triple("recruiting", "Recruit", Icons.Default.Star),
                            Triple("season", "Season", Icons.Default.EmojiEvents),
                            Triple("office", "Office", Icons.Default.AccountBalance)
                        )
                        items.forEach { (route, label, icon) ->
                            val selected = currentRoute == route
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                        }
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) TurfGreen else TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (selected) TurfGreen else TextMuted
                                )
                                // Bottom glowing emerald underline bar matching screenshots
                                Box(
                                    modifier = Modifier
                                        .width(42.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (selected) TurfGreen else androidx.compose.ui.graphics.Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "title",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("title") {
                TitleAndSaveSlotsScreen(
                    viewModel = viewModel,
                    onStartNewCareerSetup = { slotId ->
                        navController.navigate("new_career/$slotId")
                    },
                    onCareerLoaded = {
                        navController.navigate("hub") {
                            popUpTo("title") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "new_career/{slotId}",
                arguments = listOf(navArgument("slotId") { type = NavType.IntType })
            ) { backStackEntry ->
                val slotId = backStackEntry.arguments?.getInt("slotId") ?: 1
                NewCareerSetupScreen(
                    slotId = slotId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCareerCreated = {
                        navController.navigate("hub") {
                            popUpTo("title") { inclusive = true }
                        }
                    }
                )
            }

            composable("hub") {
                val st = careerState
                if (st != null) {
                    DynastyHubDashboardScreen(
                        state = st,
                        viewModel = viewModel,
                        onNavigate = { target -> navController.navigate(target) }
                    )
                }
            }

            composable("roster") {
                careerState?.let { RosterAndDepthChartScreen(it, viewModel) }
            }

            composable("recruiting") {
                careerState?.let { RecruitingAndPortalScreen(it, viewModel) }
            }

            composable("match") {
                careerState?.let {
                    LiveMatchScreen(
                        state = it,
                        viewModel = viewModel,
                        onBackToHub = { navController.popBackStack() }
                    )
                }
            }

            composable("season") {
                careerState?.let { SeasonStandingsAndPollsScreen(it) }
            }

            composable("tactics") {
                careerState?.let { TacticsAndSlidersScreen(it, viewModel) }
            }

            composable("office") {
                careerState?.let { FinancesStaffAndOffersScreen(it, viewModel) }
            }

            composable("history") {
                careerState?.let { AwardsDraftAndHistoryScreen(it) }
            }

            composable("editor") {
                careerState?.let { CustomEditorsScreen(it, viewModel) }
            }

            composable("settings") {
                careerState?.let {
                    SettingsAndSavesScreen(
                        state = it,
                        viewModel = viewModel,
                        onExitToTitle = {
                            navController.navigate("title") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
