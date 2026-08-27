package test
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserUpdateBuilder
import kotlinx.serialization.json.buildJsonObject
fun test() {
    val b = UserUpdateBuilder()
    b.data = buildJsonObject {}
}
