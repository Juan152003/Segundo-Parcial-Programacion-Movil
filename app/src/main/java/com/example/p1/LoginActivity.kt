package com.example.p1

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.p1.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseManager = FirebaseManager()
        auth = FirebaseAuth.getInstance()

        // Forzar cierre de sesión al iniciar
        auth.signOut()

        // Verificar conectividad
        if (!isInternetAvailable()) {
            showError("No hay conexión a internet. Por favor, verifica tu conexión.")
        }

        // Verificar si ya hay una sesión activa
        auth.currentUser?.let { user ->
            Log.d(TAG, "Usuario ya está logueado: ${user.email}")
            navigateToMainActivity()
            return@let
        }

        binding.btnLogin.setOnClickListener {
            // Ocultar mensaje de error anterior
            hideError()
            
            if (!isInternetAvailable()) {
                showError("No hay conexión a internet. Por favor, verifica tu conexión.")
                return@setOnClickListener
            }

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showError("Por favor ingrese email y contraseña")
                return@setOnClickListener
            }

            // Mostrar progreso
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Iniciando sesión..."

            Log.d(TAG, "Intentando iniciar sesión con email: $email")
            
            firebaseManager.loginUser(
                email = email,
                password = password,
                onSuccess = { userId ->
                    Log.d(TAG, "Login exitoso, obteniendo datos del usuario...")
                    hideError()
                    firebaseManager.getUserData(
                        userId = userId,
                        onSuccess = { userData ->
                            Log.d(TAG, "Datos de usuario obtenidos: $userData")
                            val userType = userData["userType"] as? String
                            navigateToMainActivity(userType)
                        },
                        onError = { e ->
                            Log.e(TAG, "Error al obtener datos: ${e.message}", e)
                            showError(e.message ?: "Error al obtener datos del usuario")
                            resetLoginButton()
                        }
                    )
                },
                onError = { e ->
                    Log.e(TAG, "Error en login: ${e.message}", e)
                    showError(e.message ?: "Error desconocido")
                    resetLoginButton()
                }
            )
        }

        binding.btnRegister.setOnClickListener {
            hideError()
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun resetLoginButton() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Iniciar Sesión"
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network == null) {
                Log.d(TAG, "No hay red activa")
                return false
            }
            
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities == null) {
                Log.d(TAG, "No hay capacidades de red")
                return false
            }

            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.d(TAG, "Conectado a WiFi")
                    true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.d(TAG, "Conectado a datos móviles")
                    true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.d(TAG, "Conectado a Ethernet")
                    true
                }
                else -> {
                    Log.d(TAG, "Sin conexión a internet")
                    false
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun navigateToMainActivity(userType: String? = null) {
        val intent = when (userType) {
            "provider" -> {
                Log.d(TAG, "Navegando a ProviderMainActivity")
                Intent(this, ProviderMainActivity::class.java)
            }
            else -> {
                Log.d(TAG, "Navegando a ClientMainActivity")
                Intent(this, ClientMainActivity::class.java)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
} 