package de.marcreichelt.kmp.github

import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

@Ignore("Run manually only (runs against real backend)")
class HttpTest {

    @Test
    fun makeHttpCall() {
        runBlocking {
            val body = loadGitHubWebpage()
            println(body)
            assertTrue(body.isNotEmpty())
        }
    }

}
