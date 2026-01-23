package com.example.mind_detox.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mind_detox.MainActivity
import com.example.mind_detox.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedPref = getSharedPreferences("mind_detox_prefs", Context.MODE_PRIVATE)
        val savedPin = sharedPref.getString("user_pin", null)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedPin == null) {
            binding.tvSubtitle.text = "Set a 4-digit PIN to secure your focus"
            binding.btnLogin.text = "Set PIN"
        }

        binding.btnLogin.setOnClickListener {
            val pin = binding.etPin.text.toString()
            if (pin.length == 4) {
                if (savedPin == null) {
                    sharedPref.edit().putString("user_pin", pin).apply()
                    navigateToMain()
                } else {
                    if (pin == savedPin) {
                        navigateToMain()
                    } else {
                        Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
