package de.marcreichelt.kmp.github

import io.islandtime.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.get
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect fun platformName(): String

internal expect fun httpClientEngine(): HttpClientEngine

val jsonInstance = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

val client = HttpClient(httpClientEngine()) {
    install(ContentNegotiation) {
        jsonInstance
    }
    install(Logging)
}

fun install(key: RouteScopedPlugin<ContentNegotiationConfig>, block: HttpClient.() -> Unit) {

}

internal expect val applicationDispatcher: CoroutineDispatcher

expect fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T

fun createApplicationScreenMessage(): String {
    return "Kotlin Rocks on ${platformName()}"
}

suspend fun loadGitHubWebpage(): String {
    return client.get("https://github.com/").toString()
}

fun loadGitHubWebpageAsync(onLoad: (String) -> Unit) {
    GlobalScope.apply {
        launch(applicationDispatcher) {
            onLoad(loadGitHubWebpage())
        }
    }
}

suspend fun listRepos(username: String): List<GitHubRepo> {
    val body = client.get<String>("https://api.github.com/users/$username/repos")
    return jsonInstance.parse(GitHubRepo.serializer().list, body)
}

fun listReposAsync(username: String, onLoad: (List<GitHubRepo>) -> Unit) {
    GlobalScope.apply {
        launch(applicationDispatcher) {
            onLoad(listRepos(username))
        }
    }
}

fun printMostPopularRepositoriesOrderedByFreshness(repos: List<GitHubRepo>): List<GitHubRepo> {
    return repos
        .filter { it.stargazersCount > 0 }
        .sortedByDescending { it.createdAt.secondOfUnixEpoch }
        .sortedByDescending { it.stargazersCount }
        .also { popularRepositories ->
            popularRepositories.forEach {
                println("(${it.stargazersCount}\uD83C\uDF1F) ${it.name}, created ${it.createdAt.toYearMonth()}, language ${it.language}")
            }
        }
}

@Serializable
data class GitHubRepo(

    @SerialName("name")
    val name: String,

    @SerialName("stargazers_count")
    val stargazersCount: Int,

    @SerialName("created_at")
    @Serializable(with = ZonedDateTimeSerializer::class)
    val createdAt: ZonedDateTime,

    @SerialName("language")
    val language: String?
)

@Serializer(forClass = ZonedDateTime::class)
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return decoder.decodeString()
            .toOffsetDateTime()
            .asZonedDateTime()
    }

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.toInstant().toString())
    }
}
