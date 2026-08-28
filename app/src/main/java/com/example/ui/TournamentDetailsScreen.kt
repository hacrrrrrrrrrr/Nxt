package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailsScreen(
    tournamentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddMoney: () -> Unit,
    viewModel: TournamentDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(tournamentId) {
        viewModel.loadTournament(tournamentId)
    }

    LaunchedEffect(uiState.joinSuccess, uiState.joinError) {
        if (uiState.joinSuccess != null) {
            android.widget.Toast.makeText(context, uiState.joinSuccess, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        if (uiState.joinError != null) {
            android.widget.Toast.makeText(context, uiState.joinError, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tournament Details", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Unknown error", color = Color.Red)
            }
        } else {
            val tournament = uiState.tournament
            if (tournament != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        TournamentInfoHeader(tournament)
                    }

                    if (uiState.isJoined) {
                        item {
                            RoomIdPassCard(tournament)
                        }
                    } else {
                        item {
                            if (System.currentTimeMillis() >= tournament.startTimestamp || tournament.status == "COMPLETED") {
                                Card(
                                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text("REGISTRATION CLOSED", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("This tournament has already started or ended.", fontSize = 14.sp, color = TextGray)
                                    }
                                }
                            } else {
                                JoinTournamentCard(
                                    tournament = tournament,
                                    walletBalance = uiState.walletBalance,
                                    onJoinClick = { ffName, uid -> 
                                        viewModel.joinTournament(tournament, ffName, uid)
                                    },
                                    onAddMoneyClick = onNavigateToAddMoney
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "PLAYERS JOINED (${tournament.currentPlayers}/${tournament.maxPlayers})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }

                    items(uiState.participants) { participant ->
                        ParticipantItem(participant)
                    }
                }
            }
        }
    }
}

@Composable
fun TournamentInfoHeader(tournament: Tournament) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(tournament.title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextDark)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoColumn("Entry Fee", "₹${tournament.entryFee}")
                InfoColumn("Prize Pool", "₹${tournament.prizePool}")
                InfoColumn("Mode", tournament.mode)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoColumn("Spots Left", "${tournament.maxPlayers - tournament.currentPlayers}")
                InfoColumn("Starts At", tournament.startTimeDisplay, isTime = true)
            }
        }
    }
}

@Composable
fun RoomIdPassCard(tournament: Tournament) {
    val showDetails = System.currentTimeMillis() >= (tournament.startTimestamp - (10 * 60 * 1000))
    
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // light green
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ROOM DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TagGreen)
            if (!showDetails) {
                Text("Room ID & Password will be revealed 10 minutes before the match starts.", fontSize = 14.sp, color = TextDark)
            } else if (tournament.room_id.isNullOrBlank()) {
                Text("Room ID & Password will be updated here shortly. Stay tuned!", fontSize = 14.sp, color = TextDark)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Room ID", fontSize = 10.sp, color = TextGray)
                        Text(tournament.room_id, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextDark)
                    }
                    Column {
                        Text("Password", fontSize = 10.sp, color = TextGray)
                        Text(tournament.room_pass ?: "None", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextDark)
                    }
                }
            }
        }
    }
}

@Composable
fun JoinTournamentCard(
    tournament: Tournament, 
    walletBalance: Int, 
    onJoinClick: (String, String) -> Unit,
    onAddMoneyClick: () -> Unit
) {
    var ffName by remember { mutableStateOf("") }
    var inGameUid by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("JOIN TOURNAMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryOrange)
            
            OutlinedTextField(
                value = ffName,
                onValueChange = { ffName = it },
                label = { Text("In-Game Name (FF Name)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = inGameUid,
                onValueChange = { inGameUid = it },
                label = { Text("In-Game UID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (walletBalance < tournament.entryFee) {
                Text("Insufficient balance (₹$walletBalance). You need ₹${tournament.entryFee}.", color = Color.Red, fontSize = 12.sp)
                Button(
                    onClick = onAddMoneyClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ADD MONEY", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { onJoinClick(ffName, inGameUid) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("PAY ₹${tournament.entryFee} & JOIN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ParticipantItem(participant: TournamentParticipant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryOrange.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.in_game_name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(participant.in_game_name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                Text("UID: ${participant.in_game_uid}", fontSize = 12.sp, color = TextGray)
            }
        }
        
        if ((participant.kills ?: 0) > 0 || (participant.prize_won ?: 0) > 0) {
            Column(horizontalAlignment = Alignment.End) {
                if ((participant.kills ?: 0) > 0) {
                    Text("${participant.kills} Kills", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                if ((participant.prize_won ?: 0) > 0) {
                    Text("Won ₹${participant.prize_won}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TagGreen)
                }
            }
        }
    }
}
