package com.example.p1

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FirebaseManager {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        // Habilitar persistencia offline
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestoreSettings = settings
    }
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "FirebaseManager"
    }

    fun registerUser(
        email: String,
        password: String,
        userData: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d(TAG, "Iniciando registro de usuario con email: $email")
        
        // Crear usuario en Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "Usuario creado exitosamente en Authentication")
                val userId = authResult.user?.uid
                if (userId == null) {
                    Log.e(TAG, "Error: userId es null después de crear usuario")
                    onError(Exception("Error al obtener el ID del usuario"))
                    return@addOnSuccessListener
                }

                val userDataWithId = userData.toMutableMap().apply {
                    put("userId", userId)
                    put("email", email)
                    put("createdAt", com.google.firebase.Timestamp.now())
                }

                Log.d(TAG, "Guardando datos en Firestore: $userDataWithId")

                // Guardar en Firestore
                db.collection("users")
                    .document(userId)
                    .set(userDataWithId)
                    .addOnSuccessListener {
                        Log.d(TAG, "Datos guardados exitosamente en Firestore")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al guardar en Firestore: ${e.message}", e)
                        // Si falla al guardar en Firestore, eliminamos el usuario de Authentication
                        auth.currentUser?.delete()
                        onError(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al crear usuario en Authentication: ${e.message}", e)
                onError(e)
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d(TAG, "Iniciando inicio de sesión con email: $email")
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "Inicio de sesión exitoso en Authentication")
                val userId = authResult.user?.uid
                if (userId == null) {
                    Log.e(TAG, "Error: userId es null después de iniciar sesión")
                    onError(Exception("Error al obtener el ID del usuario"))
                    return@addOnSuccessListener
                }

                // Verificar que el usuario exista en Firestore
                db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            Log.d(TAG, "Usuario encontrado en Firestore: ${document.data}")
                            onSuccess(userId)
                        } else {
                            Log.e(TAG, "Usuario no encontrado en Firestore")
                            onError(Exception("Usuario no encontrado en la base de datos"))
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al verificar usuario en Firestore: ${e.message}", e)
                        onError(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error en inicio de sesión: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("password") == true -> 
                        "Contraseña incorrecta"
                    e.message?.contains("no user record") == true || 
                    e.message?.contains("There is no user record") == true -> 
                        "El usuario no se encuentra registrado"
                    e.message?.contains("network") == true -> 
                        "Error de conexión. Por favor verifica tu internet."
                    e.message?.contains("The supplied auth credential is incorrect") == true ||
                    e.message?.contains("malformed or has expired") == true ->
                        "Las credenciales de autenticación son incorrectas o han expirado"
                    else -> "Error: ${e.message}"
                }
                onError(Exception(errorMessage))
            }
    }

    fun getUserData(
        userId: String,
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d(TAG, "Obteniendo datos del usuario: $userId")
        
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, "Datos del usuario obtenidos: ${document.data}")
                    onSuccess(document.data ?: mapOf())
                } else {
                    Log.e(TAG, "No se encontraron datos para el usuario: $userId")
                    onError(Exception("No se encontraron datos del usuario"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener datos del usuario: ${e.message}", e)
                onError(e)
            }
    }

    fun updateUserData(
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d(TAG, "Actualizando datos del usuario: $userId")
        
        db.collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Datos del usuario actualizados exitosamente")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al actualizar datos del usuario: ${e.message}", e)
                onError(e)
            }
    }
} 