package com.example.p1

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val userNameText = findViewById<TextView>(R.id.userNameText)
        val clientContainer = findViewById<CardView>(R.id.clientContainer)
        val providerContainer = findViewById<CardView>(R.id.providerContainer)

        // Obtener el tipo de usuario del intent
        val isProvider = intent.getBooleanExtra("isProvider", false)
        val userName = intent.getStringExtra("userName") ?: "Usuario"

        // Actualizar el nombre del usuario
        userNameText.text = userName

        // Mostrar el contenedor correspondiente según el tipo de usuario
        if (isProvider) {
            providerContainer.visibility = View.VISIBLE
            clientContainer.visibility = View.GONE
            setupProviderActions()
        } else {
            clientContainer.visibility = View.VISIBLE
            providerContainer.visibility = View.GONE
            setupClientActions()
        }
    }

    private fun setupClientActions() {
        findViewById<View>(R.id.takeRideButton).setOnClickListener {
            // Aquí iría la navegación a la pantalla de búsqueda de viajes
            Toast.makeText(this, "Buscando viajes disponibles...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupProviderActions() {
        findViewById<View>(R.id.publishRideButton).setOnClickListener {
            // Aquí iría la navegación a la pantalla de publicación de viajes
            Toast.makeText(this, "Crear nuevo viaje...", Toast.LENGTH_SHORT).show()
        }
    }
} 