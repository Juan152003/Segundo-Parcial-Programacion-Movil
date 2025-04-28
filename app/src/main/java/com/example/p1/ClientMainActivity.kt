package com.example.p1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button

class ClientMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main)

        // Verificar si el usuario está autenticado
        if (FirebaseAuth.getInstance().currentUser == null) {
            // Si no hay usuario autenticado, redirigir al login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Cerrar sesión
            FirebaseAuth.getInstance().signOut()
            // Redirigir al login
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }
} 