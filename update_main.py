import re

path = "app/src/main/java/com/example/MainActivity.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("1 -> TournamentsScreen()", "1 -> TournamentsScreen(onNavigateToTournamentDetails = onNavigateToTournamentDetails)")

with open(path, "w") as f:
    f.write(content)
