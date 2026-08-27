package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onAddMoneyClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedMode by remember { mutableStateOf("BR") }
    var tournamentToJoin by remember { mutableStateOf<Tournament?>(null) }

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

    var ffName by remember { mutableStateOf("") }
    var inGameUid by remember { mutableStateOf("") }

    if (tournamentToJoin != null) {
        AlertDialog(
            onDismissRequest = { tournamentToJoin = null },
            title = { Text(text = "Confirm Join") },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Are you sure you want to join ${tournamentToJoin?.title}? It will deduct ₹${tournamentToJoin?.entryFee} from your wallet.")
                    OutlinedTextField(
                        value = ffName,
                        onValueChange = { ffName = it },
                        label = { Text("In-Game Name (FF Name)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inGameUid,
                        onValueChange = { inGameUid = it },
                        label = { Text("In-Game UID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    tournamentToJoin?.let { viewModel.joinTournament(it, ffName, inGameUid) }
                    tournamentToJoin = null
                    ffName = ""
                    inGameUid = ""
                }) {
                    Text("YES, JOIN", fontWeight = FontWeight.Bold, color = PrimaryOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    tournamentToJoin = null
                    ffName = ""
                    inGameUid = ""
                }) {
                    Text("CANCEL", color = TextGray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(walletBalance = uiState.walletBalance, onAddMoneyClick = onAddMoneyClick)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                ModeFilters(
                    selectedMode = selectedMode,
                    onModeSelected = { selectedMode = it },
                    onHackersOnlyClick = { 
                        android.widget.Toast.makeText(context, "Hackers Only mode coming soon!", android.widget.Toast.LENGTH_SHORT).show() 
                    }
                )
            }

            item {
                SectionHeader("LIVE ROOMS", "Join a tournament and compete")
            }

            items(uiState.liveTournaments.filter { it.mode == selectedMode || selectedMode == "BR" }) { tournament ->
                LiveRoomCard(tournament) {
                    tournamentToJoin = tournament
                }
            }

            item {
                SectionHeader("UPCOMING TOURNAMENTS", "")
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.upcomingTournaments) { tournament ->
                        UpcomingTournamentCard(tournament) {
                             tournamentToJoin = tournament
                        }
                    }
                }
            }

            item {
                uiState.featuredTournament?.let {
                    FeaturedTournamentCard(it) {
                        tournamentToJoin = it
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopAppBar(walletBalance: Int, onAddMoneyClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(PrimaryOrange, RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning, // Placeholder for assault rifle
                    contentDescription = "Logo",
                    tint = SurfaceWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NXT E-SPORTS",
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                fontSize = 18.sp,
                color = TextDark
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(1.dp, CircleShape)
                    .background(SurfaceWhite, CircleShape)
                    .border(1.dp, PrimaryOrange.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyRupee, // Rupee icon
                    contentDescription = "Wallet",
                    tint = PrimaryOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "₹$walletBalance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clickable { onAddMoneyClick() }) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(PrimaryOrange, CircleShape)
                        .shadow(2.dp, CircleShape, spotColor = PrimaryOrange.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Money",
                        tint = SurfaceWhite,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = "ADD MONEY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryOrange,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ModeFilters(selectedMode: String, onModeSelected: (String) -> Unit, onHackersOnlyClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeTab(
                text = "CS",
                isSelected = selectedMode == "CS",
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected("CS") }
            )
            ModeTab(
                text = "BR",
                isSelected = selectedMode == "BR",
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected("BR") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onHackersOnlyClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SurfaceWhite,
                contentColor = TextGray
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Text(text = "🔒 HACKERS ONLY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ModeTab(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) PrimaryOrange else SurfaceWhite
    val textColor = if (isSelected) SurfaceWhite else TextGray

    val customModifier = if (isSelected) {
        modifier
            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = PrimaryOrange)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    } else {
        modifier
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    }

    Box(
        modifier = customModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = (-0.5).sp,
            color = TextDark
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun LiveRoomCard(tournament: Tournament, onJoinClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray.copy(alpha=0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tournament.image_url != null) {
                AsyncImage(
                    model = tournament.image_url,
                    contentDescription = tournament.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Thumbnail Placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    // Gradient overlay
                    Box(modifier = Modifier.fillMaxSize().background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(PrimaryOrange.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.2f))
                        )
                    ))
                    Text(
                        text = tournament.mode,
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tournament.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (tournament.status == "OPEN") {
                        Text(
                            text = "OPEN",
                            color = TagGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .background(TagGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoColumn("Entry", "₹${tournament.entryFee}")
                    InfoColumn("Prize", "₹${tournament.prizePool}")
                    InfoColumn("Players", "${tournament.currentPlayers}/${tournament.maxPlayers}")
                    InfoColumn("Starts", tournament.startTimeDisplay, isTime = true)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onJoinClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(8.dp), spotColor = PrimaryOrange.copy(alpha=0.5f))
            ) {
                Text("JOIN", fontWeight = FontWeight.Black, fontSize = 12.sp, color = SurfaceWhite)
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, isTime: Boolean = false) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label.uppercase(), fontSize = 10.sp, color = TextGray.copy(alpha=0.8f), lineHeight = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isTime) PrimaryOrange else TextDark)
    }
}

@Composable
fun UpcomingTournamentCard(tournament: Tournament, onJoinClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onJoinClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            if (tournament.image_url != null) {
                AsyncImage(
                    model = tournament.image_url,
                    contentDescription = tournament.title,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Banner Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹${tournament.entryFee}\n${tournament.title}",
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "₹${tournament.entryFee} ${tournament.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoColumn("Entry", "₹${tournament.entryFee}")
                    InfoColumn("Prize", "₹${tournament.prizePool}")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = tournament.dateText,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
fun FeaturedTournamentCard(tournament: Tournament, onNotifyClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray.copy(alpha=0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(Color(0xFF0F172A))
            ) {
                if (tournament.image_url != null) {
                    AsyncImage(
                        model = tournament.image_url,
                        contentDescription = tournament.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Gradient
                    Box(modifier = Modifier.fillMaxSize().background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(PrimaryOrange.copy(alpha = 0.5f), Color.Transparent)
                        )
                    ))
                }
                
                // Overlay text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "₹${tournament.entryFee} ${tournament.title}",
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = tournament.dateText.uppercase(),
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "PRIZE POOL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                        Text(text = "₹${tournament.prizePool}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Column {
                        Text(text = "FORMAT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                        Text(text = "Squad", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
                
                Button(
                    onClick = onNotifyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("NOTIFY ME", fontWeight = FontWeight.Black, fontSize = 12.sp, color = SurfaceWhite)
                }
            }
        }
    }
}
