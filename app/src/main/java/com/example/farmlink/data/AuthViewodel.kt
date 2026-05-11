package com.example.farmlink.data

import androidx.lifecycle.ViewModel
import com.example.farmlink.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signup(
        username: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        // Validate input
        if (username.isBlank() || email.isBlank() || phone.isBlank() ||
            password.isBlank() || confirmPassword.isBlank()
        ) {
            onResult(false, "Please fill all the fields")
            return
        }

        if (password != confirmPassword) {
            onResult(false, "Passwords do not match")
            return
        }

        // Firebase signup
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        onResult(false, "Failed to get user ID")
                        return@addOnCompleteListener
                    }

                    val user = UserModel(
                        username = username,
                        email = email,
                        phone = phone,
                        userId = userId
                    )

                    saveUserToDatabase(user, onResult)

                } else {
                    onResult(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun saveUserToDatabase(
        user: UserModel,
        onResult: (Boolean, String) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference("Users/${user.userId}")
            .setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "User registered successfully")
                } else {
                    onResult(false, task.exception?.message ?: "Failed to save user")
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email and Password required")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login successful")
                } else {
                    onResult(false, task.exception?.message ?: "Login failed")
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}