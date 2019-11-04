package de.marcreichelt.kmp.github

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect fun platformName(): String

internal expect val client: HttpClient

internal expect val applicationDispatcher: CoroutineDispatcher

expect fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T

fun createApplicationScreenMessage(): String {
    return "Kotlin Rocks on ${platformName()}"
}

suspend fun loadGitHubWebpage(): String {
    return client.get("https://github.com/")
}

fun loadGitHubWebpageAsync(onLoad: (String) -> Unit) {
    GlobalScope.apply {
        launch(applicationDispatcher) {
            onLoad(loadGitHubWebpage())
        }
    }
}
