package de.marcreichelt.kmp.github

import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class HttpTest2 {

    @Test
    @Ignore
    fun makeHttpCall() {
        runBlocking {
            val body = loadGitHubWebpage()
            println(body)
            assertTrue(body.isNotEmpty())
        }
    }

    @Test
    fun getMostPopularRepositoriesOrderedByFreshness() {
        runBlocking {
            val repositories = listRepos("mreichelt")
            printMostPopularRepositoriesOrderedByFreshness(repositories)
        }
    }

}
