package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import com.example.ui.theme.TagGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsScreen(
    viewModel: MyTournamentsViewModel = viewModel(),
    onNavigateToTournamentDetails: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Joined", "Completed", "Won")

    LaunchedEffect(Unit) {
        viewModel.loadMyTournaments()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("My Tournaments", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceWhite,
            contentColor = PrimaryOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                         Text(
                            text = title, 
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) PrimaryOrange else TextGray
                        ) 
                    }
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = PrimaryOrange,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val listToShow = when (selectedTab) {
                    0 -> uiState.joinedTournaments
                    1 -> uiState.completedTournaments
                    2 -> uiState.wonTournaments
                    else -> emptyList()
                }

                if (listToShow.isEmpty()) {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        when (selectedTab) {
                            0 -> EmptyStateMessage("You haven't joined any active tournaments yet.")
                            1 -> EmptyStateMessage("No completed tournaments.")
                            2 -> EmptyStateMessage("Keep playing to win your first tournament!")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(listToShow) { tournament ->
                            MyTournamentCard(
                                tournament = tournament,
                                isCompleted = selectedTab == 1 || selectedTab == 2,
                                onClick = { onNavigateToTournamentDetails(tournament.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyTournamentCard(tournament: Tournament, isCompleted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(tournament.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                if (isCompleted) {
                    Text("RESULTS OUT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PrimaryOrange)
                } else {
                    Text("VIEW ROOM", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TagGreen)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mode: ${tournament.mode}", fontSize = 12.sp, color = TextGray)
                Text(tournament.startTimeDisplay, fontSize = 12.sp, color = TextGray)
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
        Text(
            text = "NOTHING HERE",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = TextDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = message,
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
