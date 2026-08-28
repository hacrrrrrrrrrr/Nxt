import io.github.jan.supabase.auth.providers.builtin.Email
fun test() {
    Email {
        email = "a@a.com"
        password = "a"
        emailRedirectTo = "com.jod.esports://login"
    }
}
