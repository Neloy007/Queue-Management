
package com.example.q_list.data.repository

import com.example.q_list.data.model.CustomerOrganization
import com.example.q_list.data.model.OperatorService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CustomerExploreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val queues = firestore.collection("queues")
    private val services = firestore.collection("operatorServices")

    fun listenToOrganizations(
        onUpdate: (List<CustomerOrganization>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return queues
            .whereEqualTo("status", "OPEN")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not load organizations.")
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val organizations = snapshot.documents.mapNotNull { document ->
                    val operatorId =
                        document.getString("operatorId") ?: return@mapNotNull null

                    CustomerOrganization(
                        queueId = document.id,
                        operatorId = operatorId,
                        organizationName =
                            document.getString("organizationName").orEmpty(),
                        location = document.getString("location").orEmpty(),
                        bookingCode =
                            document.getString("bookingCode").orEmpty(),
                        sessionDate =
                            document.getString("sessionDate").orEmpty()
                    )
                }.sortedBy { it.organizationName.lowercase() }

                onUpdate(organizations)
            }
    }

    /**
     * Listens to active services across all operators.
     * Used only to discover which shops offer a searched service.
     */
    fun listenToAllActiveServices(
        onUpdate: (List<OperatorService>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return services
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not search services.")
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

    /**
     * Loads services belonging to the selected organization.
     */
    fun listenToServices(
        operatorId: String,
        onUpdate: (List<OperatorService>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return services
            .whereEqualTo("operatorId", operatorId)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not load company services.")
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
}
