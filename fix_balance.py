path = "app/src/main/java/com/example/ui/TournamentDetailsViewModel.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace(
"""                _uiState.value = _uiState.value.copy(
                    joinSuccess = "Successfully joined!",
                    joinError = null
                )""",
"""                _uiState.value = _uiState.value.copy(
                    walletBalance = newBalance,
                    joinSuccess = "Successfully joined!",
                    joinError = null
                )"""
)

with open(path, "w") as f:
    f.write(content)
