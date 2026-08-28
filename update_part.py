import re

path = "app/src/main/java/com/example/ui/HomeViewModel.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("""data class TournamentParticipant(
    val tournament_id: String,
    val user_id: String,
    val in_game_name: String,
    val in_game_uid: String
)""", """data class TournamentParticipant(
    val tournament_id: String,
    val user_id: String,
    val in_game_name: String,
    val in_game_uid: String,
    val kills: Int? = 0,
    val prize_won: Int? = 0
)""")

with open(path, "w") as f:
    f.write(content)
