package de.marcreichelt.kmp.github

expect fun platformName(): String

fun createApplicationScreenMessage(): String {
    return "Kotlin Rocks on ${platformName()}"
}
