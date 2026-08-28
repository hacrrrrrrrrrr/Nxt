import re

path = "app/src/main/java/com/example/ui/TournamentDetailsScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Fix Registration Closed logic
old_logic = 'if (System.currentTimeMillis() >= tournament.startTimestamp || tournament.status != "UPCOMING") {'
new_logic = 'if (System.currentTimeMillis() >= tournament.startTimestamp || tournament.status == "COMPLETED") {'
content = content.replace(old_logic, new_logic)

# Fix RoomIdPassCard logic
old_room_card = """@Composable
fun RoomIdPassCard(tournament: Tournament) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // light green
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ROOM DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TagGreen)
            if (tournament.room_id.isNullOrBlank()) {
                Text("Room ID & Password will be updated here before the match starts. Stay tuned!", fontSize = 14.sp, color = TextDark)
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
}"""

new_room_card = """@Composable
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
}"""

content = content.replace(old_room_card, new_room_card)

with open(path, "w") as f:
    f.write(content)
