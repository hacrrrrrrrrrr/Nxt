import io.github.jan.supabase.compose.auth.composable.NativeSignInResult

fun test(res: NativeSignInResult) {
    if (res is NativeSignInResult.ClosedByUser) {
        println(res.toString())
    }
}
