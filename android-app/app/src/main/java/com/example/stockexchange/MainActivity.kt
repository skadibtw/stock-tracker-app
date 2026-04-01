package com.example.stockexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePrefs.applySaved(this)
        setContentView(R.layout.activity_main)

        val loginEditText = findViewById<EditText>(R.id.loginEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val openProfileButton = findViewById<Button>(R.id.openProfileButton)

        openProfileButton.setOnClickListener {
            val login = loginEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                val intent = Intent(this, SecondActivity::class.java).apply {
                    putExtra("username", login)
                }
                startActivity(intent)
            } else {
                // Можно добавить Toast/ошибку, пока просто не пускаем дальше
            }
        }
    }
}