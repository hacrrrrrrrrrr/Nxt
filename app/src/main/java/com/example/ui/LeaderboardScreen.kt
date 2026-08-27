package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class LeaderboardEntry(val rank: Int, val username: String, val winnings: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen() {
    val topPlayers = emptyList<LeaderboardEntry>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Leaderboard", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
        )

        if (topPlayers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No rankings yet.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(topPlayers) { index, player ->
                    LeaderboardRow(player)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(player: LeaderboardEntry) {
    val isTop3 = player.rank <= 3
    val rankColor = when (player.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> TextGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isTop3) rankColor.copy(alpha = 0.1f) else Color.Transparent)
                        .border(1.dp, if (isTop3) rankColor else BorderGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTop3) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = rankColor, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = "#${player.rank}",
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Avatar Placeholder & Username
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = player.username,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 14.sp
                )
            }

            // Winnings
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "WON", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Text(text = "₹${player.winnings}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = PrimaryOrange)
            }
        }
    }
}
