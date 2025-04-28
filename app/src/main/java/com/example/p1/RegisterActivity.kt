package com.example.p1

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.p1.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseManager: FirebaseManager

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseManager = FirebaseManager()
        
        // Verificar conectividad inicial
        if (!isInternetAvailable()) {
            Toast.makeText(this, "No hay conexión a internet. Por favor, verifica tu conexión.", Toast.LENGTH_LONG).show()
        }

        // Asegurarse de que el contenedor de proveedor esté oculto inicialmente
        binding.providerFieldsContainer.visibility = View.GONE

        // Manejar cambios en el tipo de registro
        binding.registrationTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            Log.d(TAG, "Radio button seleccionado: $checkedId")
            when (checkedId) {
                R.id.providerRadioButton -> {
                    Log.d(TAG, "Mostrando campos de proveedor")
                    binding.providerFieldsContainer.visibility = View.VISIBLE
                }
                R.id.clientRadioButton -> {
                    Log.d(TAG, "Ocultando campos de proveedor")
                    binding.providerFieldsContainer.visibility = View.GONE
                }
            }
        }

        // Configurar el botón de registro
        binding.registerButton.setOnClickListener {
            if (!isInternetAvailable()) {
                Toast.makeText(this, "No hay conexión a internet. Por favor, verifica tu conexión.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Botón de registro presionado")
            
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val phone = binding.phoneEditText.text.toString()
            val id = binding.idEditText.text.toString()
            val gender = when (binding.genderGroup.checkedRadioButtonId) {
                R.id.maleRadioButton -> "Masculino"
                R.id.femaleRadioButton -> "Femenino"
                else -> ""
            }

            Log.d(TAG, "Datos ingresados: email=$email, name=$name, gender=$gender")

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Campos obligatorios faltantes")
                return@setOnClickListener
            }

            // Validar formato de email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Por favor ingrese un email válido", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Formato de email inválido")
                return@setOnClickListener
            }

            // Validar longitud de contraseña
            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Contraseña muy corta")
                return@setOnClickListener
            }

            val userData = mutableMapOf(
                "name" to name,
                "email" to email,
                "gender" to gender,
                "phone" to phone,
                "identification" to id
            )

            if (binding.registrationTypeGroup.checkedRadioButtonId == R.id.providerRadioButton) {
                val vehicleType = when (binding.vehicleTypeGroup.checkedRadioButtonId) {
                    R.id.carRadioButton -> "Carro"
                    R.id.motorcycleRadioButton -> "Moto"
                    else -> ""
                }
                val plate = binding.plateEditText.text.toString()
                val model = binding.modelEditText.text.toString()
                
                if (vehicleType.isEmpty() || plate.isEmpty() || model.isEmpty()) {
                    Toast.makeText(this, "Por favor complete todos los campos del vehículo", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Campos del vehículo incompletos")
                    return@setOnClickListener
                }
                
                userData["userType"] = "provider"
                userData["vehicleType"] = vehicleType
                userData["vehiclePlate"] = plate
                userData["vehicleModel"] = model
                Log.d(TAG, "Registrando proveedor con vehículo: type=$vehicleType, plate=$plate, model=$model")
            } else {
                userData["userType"] = "client"
                Log.d(TAG, "Registrando cliente")
            }

            // Mostrar diálogo de progreso
            Toast.makeText(this, "Registrando usuario...", Toast.LENGTH_SHORT).show()

            firebaseManager.registerUser(
                email = email,
                password = password,
                userData = userData,
                onSuccess = {
                    Log.d(TAG, "Registro exitoso, redirigiendo a LoginActivity")
                    Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                },
                onError = { e ->
                    Log.e(TAG, "Error en registro: ${e.message}", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
} 