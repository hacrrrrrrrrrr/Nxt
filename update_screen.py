import re

path = "app/src/main/java/com/example/ui/TournamentDetailsScreen.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Registration closing logic
old_join_logic = """                    if (uiState.isJoined) {
                        item {
                            RoomIdPassCard(tournament)
                        }
                    } else {
                        item {
                            JoinTournamentCard(
                                tournament = tournament,
                                walletBalance = uiState.walletBalance,
                                onJoinClick = { ffName, uid -> 
                                    viewModel.joinTournament(tournament, ffName, uid)
                                },
                                onAddMoneyClick = onNavigateToAddMoney
                            )
                        }
                    }"""

new_join_logic = """                    if (uiState.isJoined) {
                        item {
                            RoomIdPassCard(tournament)
                        }
                    } else {
                        item {
                            if (System.currentTimeMillis() >= tournament.startTimestamp || tournament.status != "UPCOMING") {
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
                    }"""

content = content.replace(old_join_logic, new_join_logic)

# 2. Participant item updating to show kills and prize
old_participant = """@Composable
fun ParticipantItem(participant: TournamentParticipant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
}"""

new_participant = """@Composable
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
}"""

content = content.replace(old_participant, new_participant)

with open(path, "w") as f:
    f.write(content)
