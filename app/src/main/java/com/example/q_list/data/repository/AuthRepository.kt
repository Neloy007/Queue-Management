
package com.example.q_list.data.repository

import com.example.q_list.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection("users")

    // CUSTOMER REGISTRATION
    suspend fun registerCustomer(
        name: String,
        email: String,
        password: String
    ) {
        val authResult = auth.createUserWithEmailAndPassword(
            email,
            password
        ).await()

        val user = authResult.user
            ?: throw Exception("Account creation failed.")

        val userData = hashMapOf(
            "uid" to user.uid,
            "name" to name,
            "email" to email,
            "role" to UserRole.CUSTOMER.name,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            usersCollection.document(user.uid)
                .set(userData)
                .await()

        } catch (e: Exception) {
            try {
                user.delete().await()
            } catch (_: Exception) {
                auth.signOut()
            }

            throw Exception(
                "Could not save customer profile. ${e.message}"
            )
        }
    }

    // OPERATOR REGISTRATION
    suspend fun registerOperator(
        name: String,
        organizationName: String,
        email: String,
        location: String,
        password: String
    ) {
        val authResult = auth.createUserWithEmailAndPassword(
            email,
            password
        ).await()

        val user = authResult.user
            ?: throw Exception("Account creation failed.")

        val userData = hashMapOf(
            "uid" to user.uid,
            "name" to name,
            "email" to email,
            "role" to UserRole.OPERATOR.name,
            "organizationName" to organizationName,
            "location" to location,
            "approvalStatus" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            usersCollection.document(user.uid)
                .set(userData)
                .await()

            // Do not keep a pending operator signed in.
            auth.signOut()

        } catch (e: Exception) {
            try {
                user.delete().await()
            } catch (_: Exception) {
                auth.signOut()
            }

            throw Exception(
                "Could not complete operator registration. ${e.message}"
            )
        }
    }

    // CUSTOMER AND OPERATOR LOGIN
    suspend fun login(
        email: String,
        password: String,
        expectedRole: UserRole
    ): UserRole {

        val authResult = auth.signInWithEmailAndPassword(
            email,
            password
        ).await()

        val user = authResult.user
            ?: throw Exception("Login failed.")

        try {
            val document = usersCollection
                .document(user.uid)
                .get()
                .await()

            if (!document.exists()) {
                throw Exception("User profile not found.")
            }

            val storedRole = document.getString("role")
                ?: throw Exception("User role is missing.")

            val actualRole = try {
                UserRole.valueOf(storedRole)
            } catch (_: IllegalArgumentException) {
                throw Exception("Invalid user role.")
            }

            if (actualRole != expectedRole) {
                throw Exception(
                    "This account is not registered as a ${expectedRole.name.lowercase()}."
                )
            }

            // Operator approval check
            if (actualRole == UserRole.OPERATOR) {
                when (document.getString("approvalStatus")) {

                    "APPROVED" -> {
                        // Allow login
                    }

                    "PENDING" -> {
                        throw Exception(
                            "Your operator account is awaiting approval."
                        )
                    }

                    "REJECTED" -> {
                        throw Exception(
                            "Your operator registration was not approved."
                        )
                    }

                    else -> {
                        throw Exception(
                            "Your operator account has not been approved."
                        )
                    }
                }
            }

            return actualRole

        } catch (e: Exception) {
            auth.signOut()
            throw e
        }
    }

    // FORGOT PASSWORD
    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    // LOGOUT
    fun logout() {
        auth.signOut()
    }

    // CURRENT USER ID
    fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    // CHECK LOGIN STATUS
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // CHECK EXISTING SESSION
    // CHECK EXISTING SESSION
    fun getCurrentUserRole(
        onResult: (UserRole?) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onResult(null)
            return
        }

        usersCollection.document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    auth.signOut()
                    onResult(null)
                    return@addOnSuccessListener
                }

                val roleString = document.getString("role")

                val role = try {
                    roleString?.let {
                        UserRole.valueOf(it)
                    }
                } catch (_: IllegalArgumentException) {
                    null
                }

                if (role == null) {
                    auth.signOut()
                    onResult(null)
                    return@addOnSuccessListener
                }

                if (role == UserRole.OPERATOR) {
                    val approvalStatus =
                        document.getString("approvalStatus")

                    if (approvalStatus != "APPROVED") {
                        auth.signOut()
                        onResult(null)
                        return@addOnSuccessListener
                    }
                }

                onResult(role)
            }
            .addOnFailureListener {
                // A network failure is not proof that the user is logged out.
                // Do not sign out; let the caller handle the unavailable result.
                onResult(null)
            }
    }
}
