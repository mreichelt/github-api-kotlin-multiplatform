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

fun loadGitHubWebpage(): String {
    return runBlocking {
        client.get<String>("https://github.com/")
    }
}

fun loadGitHubWebpageAsync(onLoad: (String) -> Unit) {
    println("loadGitHubWebpageAsync")
    GlobalScope.apply {
        println("in global scope")
        launch(applicationDispatcher) {
            println("in launch")
            val body: String = client.get("https://github.com/")
            println("body loaded with ${body.length} characters")
            onLoad(body)
        }
    }
}
