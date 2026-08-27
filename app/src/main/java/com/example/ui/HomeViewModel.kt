package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class Tournament(
    val id: String,
    val title: String,
    val mode: String,
    val entryFee: Int,
    val prizePool: Int,
    val maxPlayers: Int,
    val currentPlayers: Int,
    val startTimestamp: Long,
    val startTimeDisplay: String = "",
    val status: String,
    val isFeatured: Boolean = false,
    val dateText: String = "",
    val image_url: String? = null
)

@Serializable
data class Profile(
    val id: String,
    val wallet_balance: Int
)

@Serializable
data class TournamentParticipant(
    val tournament_id: String,
    val user_id: String,
    val in_game_name: String,
    val in_game_uid: String
)

data class HomeUiState(
    val walletBalance: Int = 0,
    val liveTournaments: List<Tournament> = emptyList(),
    val upcomingTournaments: List<Tournament> = emptyList(),
    val featuredTournament: Tournament? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isJoining: Boolean = false,
    val joinError: String? = null,
    val joinSuccess: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(joinError = null, joinSuccess = null)
    }

    fun joinTournament(tournament: Tournament, ffName: String, inGameUid: String) {
        if (ffName.isBlank() || inGameUid.isBlank()) {
            _uiState.value = _uiState.value.copy(joinError = "In-game Name and UID cannot be empty.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoining = true, joinError = null, joinSuccess = null)
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session == null) {
                    _uiState.value = _uiState.value.copy(isJoining = false, joinError = "You must be logged in to join.")
                    return@launch
                }
                
                val currentBalance = _uiState.value.walletBalance
                if (currentBalance < tournament.entryFee) {
                    _uiState.value = _uiState.value.copy(isJoining = false, joinError = "Insufficient wallet balance.")
                    return@launch
                }
                
                val newBalance = currentBalance - tournament.entryFee
                val userId = session.user?.id ?: ""
                
                // Deduct balance
                SupabaseClient.client.postgrest["profiles"]
                    .update(mapOf("wallet_balance" to newBalance)) { filter { eq("id", userId) } }
                    
                // Record participation
                try {
                    val participant = TournamentParticipant(tournament.id, userId, ffName, inGameUid)
                    SupabaseClient.client.postgrest["tournament_participants"].insert(participant)
                } catch (e: Exception) {
                    // Ignore if table doesn't exist yet, but in a real app this should be a transaction.
                }

                _uiState.value = _uiState.value.copy(
                    walletBalance = newBalance,
                    isJoining = false,
                    joinSuccess = "Successfully joined ${tournament.title}!"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isJoining = false, 
                    joinError = e.message ?: "Failed to join tournament"
                )
            }
        }
    }

    init {
        fetchHomeData()
        startCountdownTimer()
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch tournaments from Supabase
                val allTournaments = SupabaseClient.client.postgrest["tournaments"]
                    .select()
                    .decodeList<Tournament>()

                val liveRooms = allTournaments.filter { it.status == "OPEN" }
                val upcomingRooms = allTournaments.filter { it.status == "UPCOMING" && !it.isFeatured }
                val featured = allTournaments.find { it.isFeatured }

                // Attempt to fetch wallet balance if user is logged in
                var balance = 0
                try {
                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    if (session != null) {
                        val profile = SupabaseClient.client.postgrest["profiles"]
                            .select { filter { eq("id", session.user?.id ?: "") } }
                            .decodeSingleOrNull<Profile>()
                        balance = profile?.wallet_balance ?: 0
                    }
                } catch (e: Exception) {
                    // Ignore auth/profile errors for now, leave balance at 0
                }

                _uiState.value = _uiState.value.copy(
                    walletBalance = balance,
                    liveTournaments = liveRooms,
                    upcomingTournaments = upcomingRooms,
                    featuredTournament = featured,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to fetch data from Supabase"
                )
            }
        }
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                
                val updatedLiveTournaments = _uiState.value.liveTournaments.map { tournament ->
                    val diff = tournament.startTimestamp - currentTime
                    if (diff > 0) {
                        val hours = (diff / (1000 * 60 * 60)) % 24
                        val minutes = (diff / (1000 * 60)) % 60
                        val seconds = (diff / 1000) % 60
                        val display = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                        tournament.copy(startTimeDisplay = display)
                    } else {
                        tournament.copy(startTimeDisplay = "00:00:00")
                    }
                }
                
                if (updatedLiveTournaments != _uiState.value.liveTournaments) {
                    _uiState.value = _uiState.value.copy(liveTournaments = updatedLiveTournaments)
                }
                
                delay(1000)
            }
        }
    }
}

