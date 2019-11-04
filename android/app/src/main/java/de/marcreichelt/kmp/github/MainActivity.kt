package de.marcreichelt.kmp.github

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*

class MainViewModel : ViewModel() {

    val webpage: LiveData<String> = liveData {
        emit(loadGitHubWebpage())
    }

}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainText = findViewById<TextView>(R.id.main_text)
        mainText.text = createApplicationScreenMessage()

        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.webpage.observe(this) {
            mainText.text = it
        }
    }

}
