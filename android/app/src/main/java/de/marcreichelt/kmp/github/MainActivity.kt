package de.marcreichelt.kmp.github

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainText = findViewById<TextView>(R.id.main_text)
        mainText.text = createApplicationScreenMessage()

        loadGitHubWebpageAsync {
            runOnUiThread {
                mainText.text = it
            }
        }
    }

}
