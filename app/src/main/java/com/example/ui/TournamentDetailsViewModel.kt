package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

data class TournamentDetailsUiState(
    val tournament: Tournament? = null,
    val participants: List<TournamentParticipant> = emptyList(),
    val isLoading: Boolean = true,
    val isJoined: Boolean = false,
    val error: String? = null,
    val joinSuccess: String? = null,
    val joinError: String? = null,
    val walletBalance: Int = 0
)

class TournamentDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TournamentDetailsUiState())
    val uiState: StateFlow<TournamentDetailsUiState> = _uiState.asStateFlow()

    fun loadTournament(tournamentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch tournament
                val tournament = SupabaseClient.client.postgrest["tournaments"]
                    .select { filter { eq("id", tournamentId) } }
                    .decodeSingleOrNull<Tournament>()
                
                // Fetch participants
                val participants = SupabaseClient.client.postgrest["tournament_participants"]
                    .select { filter { eq("tournament_id", tournamentId) } }
                    .decodeList<TournamentParticipant>()
                
                // Check if user is joined
                val userId = SupabaseClient.client.auth.currentSessionOrNull()?.user?.id
                val isJoined = participants.any { it.user_id == userId }

                // Fetch wallet balance
                var balance = 0
                if (userId != null) {
                    val profile = SupabaseClient.client.postgrest["profiles"]
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<Profile>()
                    balance = profile?.wallet_balance ?: 0
                }

                _uiState.value = _uiState.value.copy(
                    tournament = tournament,
                    participants = participants,
                    isJoined = isJoined,
                    walletBalance = balance,
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun joinTournament(tournament: Tournament, ffName: String, inGameUid: String) {
        if (ffName.isBlank() || inGameUid.isBlank()) {
            _uiState.value = _uiState.value.copy(joinError = "Please enter both In-Game Name and UID.")
            return
        }

        val currentBalance = _uiState.value.walletBalance
        if (currentBalance < tournament.entryFee) {
            _uiState.value = _uiState.value.copy(joinError = "Insufficient wallet balance. Please add money.")
            return
        }

        viewModelScope.launch {
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val userId = session?.user?.id ?: ""
                
                // Deduct balance
                val newBalance = currentBalance - tournament.entryFee
                SupabaseClient.client.postgrest["profiles"]
                    .update(mapOf("wallet_balance" to newBalance)) { filter { eq("id", userId) } }
                    
                // Record participation
                val participant = TournamentParticipant(tournament.id, userId, ffName, inGameUid)
                SupabaseClient.client.postgrest["tournament_participants"].insert(participant)
                
                // Increment the currentPlayers count dynamically
                val newCurrentPlayers = tournament.currentPlayers + 1
                SupabaseClient.client.postgrest["tournaments"]
                    .update(mapOf("currentPlayers" to newCurrentPlayers)) { filter { eq("id", tournament.id) } }

                _uiState.value = _uiState.value.copy(
                    joinSuccess = "Successfully joined!",
                    joinError = null
                )
                // Reload to see the changes
                loadTournament(tournament.id)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(joinError = "Failed to join: ${e.message}")
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(joinSuccess = null, joinError = null)
    }
}
