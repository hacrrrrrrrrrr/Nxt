package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.network.SupabaseClient

data class MyTournamentsUiState(
    val joinedTournaments: List<Tournament> = emptyList(),
    val completedTournaments: List<Tournament> = emptyList(),
    val wonTournaments: List<Tournament> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MyTournamentsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyTournamentsUiState())
    val uiState: StateFlow<MyTournamentsUiState> = _uiState.asStateFlow()

    init {
        loadMyTournaments()
    }

    fun loadMyTournaments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = SupabaseClient.client.auth.currentSessionOrNull()?.user?.id
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Not logged in")
                    return@launch
                }

                // Get all participant entries for this user
                val participations = SupabaseClient.client.postgrest["tournament_participants"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<TournamentParticipant>()
                
                if (participations.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                val tournamentIds = participations.map { it.tournament_id }

                // Get the tournament details
                val tournaments = SupabaseClient.client.postgrest["tournaments"]
                    .select { filter { isIn("id", tournamentIds) } }
                    .decodeList<Tournament>()

                // Categorize
                val joined = tournaments.filter { it.status == "OPEN" || it.status == "UPCOMING" }
                val completed = tournaments.filter { it.status == "COMPLETED" }
                
                // For "Won", check if prize_won > 0
                val wonTournamentIds = participations.filter { (it.prize_won ?: 0) > 0 }.map { it.tournament_id }
                val won = tournaments.filter { it.id in wonTournamentIds }

                _uiState.value = MyTournamentsUiState(
                    joinedTournaments = joined,
                    completedTournaments = completed,
                    wonTournaments = won,
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
