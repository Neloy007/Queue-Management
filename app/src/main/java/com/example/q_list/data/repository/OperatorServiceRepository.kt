package com.example.q_list.data.repository


import com.example.q_list.data.model.OperatorService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class OperatorServiceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val services = firestore.collection("operatorServices")
    private val users = firestore.collection("users")

    private fun requireOperatorId(): String {
        return auth.currentUser?.uid
            ?: throw Exception("Please log in again.")
    }

    suspend fun getOrganizationName(): String {
        val operatorId = requireOperatorId()

        val profile = users.document(operatorId).get().await()

        if (!profile.exists()) {
            throw Exception("Operator profile not found.")
        }

        if (profile.getString("role") != "OPERATOR" ||
            profile.getString("approvalStatus") != "APPROVED"
        ) {
            throw Exception("Your operator account is not approved.")
        }

        return profile.getString("organizationName").orEmpty()
    }

    fun listenToServices(
        onUpdate: (List<OperatorService>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {

        val operatorId = requireOperatorId()

        return services
            .whereEqualTo("operatorId", operatorId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not load services.")
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val serviceList = snapshot.documents.mapNotNull { document ->

                    document.toObject(OperatorService::class.java)
                        ?.copy(id = document.id)
                }.sortedBy { it.name.lowercase() }

                onUpdate(serviceList)
            }
    }

    suspend fun addService(
        name: String,
        price: Double,
        estimatedMinutes: Int
    ) {

        val operatorId = requireOperatorId()
        val organizationName = getOrganizationName()

        val serviceRef = services.document()

        val serviceData = hashMapOf(
            "operatorId" to operatorId,
            "organizationName" to organizationName,
            "name" to name.trim(),
            "price" to price,
            "estimatedMinutes" to estimatedMinutes,
            "active" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        serviceRef.set(serviceData).await()
    }

    suspend fun updateService(
        service: OperatorService,
        name: String,
        price: Double,
        estimatedMinutes: Int
    ) {

        val operatorId = requireOperatorId()

        if (service.operatorId != operatorId) {
            throw Exception("You cannot edit another company's service.")
        }

        services.document(service.id).update(
            mapOf(
                "name" to name.trim(),
                "price" to price,
                "estimatedMinutes" to estimatedMinutes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun deleteService(service: OperatorService) {

        val operatorId = requireOperatorId()

        if (service.operatorId != operatorId) {
            throw Exception("You cannot delete another company's service.")
        }

        services.document(service.id).delete().await()
    }
}
