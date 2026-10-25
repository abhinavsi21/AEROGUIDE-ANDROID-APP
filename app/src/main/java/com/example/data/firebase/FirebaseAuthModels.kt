package com.example.data.firebase

data class FirebaseUser(
    val uid: String,
    val email: String,
    val fullName: String? = null,
    val identityType: String = "demo_passenger",
    val emailVerified: Boolean = false
)

data class FirebaseSession(
    val accessToken: String,
    val refreshToken: String,
    val user: FirebaseUser
)

data class FirebaseConfig(
    val apiKey: String = "",
    val projectId: String = "",
    val appId: String = ""
) {
    val isValid: Boolean get() = apiKey.isNotBlank() && projectId.isNotBlank()
}

sealed class FirebaseAuthResult {
    data class Success(
        val user: FirebaseUser,
        val session: FirebaseSession,
        val message: String = "Login successful"
    ) : FirebaseAuthResult()

    data class Error(val message: String) : FirebaseAuthResult()
}
