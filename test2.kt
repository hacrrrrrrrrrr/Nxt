import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import androidx.compose.runtime.Composable

@Composable
fun Test() {
    val action = io.github.jan.supabase.compose.auth.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
        },
        fallbackToBrowser = false
    )
}
