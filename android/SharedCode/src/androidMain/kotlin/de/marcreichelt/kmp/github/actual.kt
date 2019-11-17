package de.marcreichelt.kmp.github

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal actual fun httpClientEngine(): HttpClientEngine = OkHttp.create()

internal actual val applicationDispatcher: CoroutineDispatcher = Dispatchers.Default

actual fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = kotlinx.coroutines.runBlocking(context, block)


actual fun platformName(): String {
    return "Android"
}
