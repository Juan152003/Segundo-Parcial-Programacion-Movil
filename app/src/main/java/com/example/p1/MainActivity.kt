package com.example.p1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailEditText = findViewById<TextInputEditText>(R.id.emailEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Aquí iría la validación real de credenciales
                // Por ahora, simulamos un login exitoso
                val intent = Intent(this, DashboardActivity::class.java).apply {
                    // Por ahora, determinamos el tipo de usuario basado en el email
                    // En una implementación real, esto vendría de la base de datos
                    putExtra("isProvider", email.contains("provider"))
                    putExtra("userName", "Usuario de Prueba")
                }
                startActivity(intent)
                finish() // Cerramos la actividad de login
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordTextView.setOnClickListener {
            // Aquí iría la navegación a la pantalla de recuperación de contraseña
            Toast.makeText(this, "Recuperar contraseña", Toast.LENGTH_SHORT).show()
        }
    }
}