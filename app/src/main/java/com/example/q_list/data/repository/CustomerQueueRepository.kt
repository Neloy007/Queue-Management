package com.example.q_list.data.repository


import com.example.q_list.data.model.CustomerOrganization
import com.example.q_list.data.model.CustomerQueueInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class CustomerQueueRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun createBooking(
        organization: CustomerOrganization,
        selectedQuantities: Map<String, Int>
    ): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please log in first.")

        require(selectedQuantities.isNotEmpty()) {
            "Please select at least one service."
        }

        val queueRef = firestore.collection("queues")
            .document(organization.queueId)

        val userRef = firestore.collection("users").document(uid)

        val serviceRefs = selectedQuantities.keys.associateWith { serviceId ->
            firestore.collection("operatorServices").document(serviceId)
        }

        val tokenRef = firestore.collection("tokens").document()

        return firestore.runTransaction { transaction ->

            // All transaction reads happen before any writes.
            val queueSnapshot = transaction.get(queueRef)
            val userSnapshot = transaction.get(userRef)

            check(queueSnapshot.exists()) {
                "This queue no longer exists."
            }

            check(userSnapshot.exists()) {
                "Customer profile not found."
            }

            check(userSnapshot.getString("role") == "CUSTOMER") {
                "Only customer accounts can book."
            }

            check(queueSnapshot.getString("status") == "OPEN") {
                "This organization has closed its queue."
            }

            check(queueSnapshot.getString("operatorId") == organization.operatorId) {
                "Invalid organization."
            }

            val customerName = userSnapshot.getString("name")
                ?: "Customer"

            val serviceSnapshots = serviceRefs.mapValues { (_, ref) ->
                transaction.get(ref)
            }

            val serviceItems = mutableListOf<Map<String, Any>>()
            var totalPrice = 0.0
            var totalEstimatedMinutes = 0

            selectedQuantities.forEach { (serviceId, quantity) ->

                require(quantity in 1..20) {
                    "Invalid service quantity."
                }

                val snapshot = serviceSnapshots[serviceId]
                    ?: throw IllegalStateException("Service not found.")

                check(snapshot.exists()) {
                    "A selected service is no longer available."
                }

                check(snapshot.getString("operatorId") == organization.operatorId) {
                    "A selected service belongs to another organization."
                }

                check(snapshot.getBoolean("active") == true) {
                    "A selected service is no longer active."
                }

                val name = snapshot.getString("name").orEmpty()
                val price = snapshot.getDouble("price") ?: 0.0
                val minutes = snapshot.getLong("estimatedMinutes")?.toInt() ?: 1

                totalPrice += price * quantity
                totalEstimatedMinutes += minutes * quantity

                serviceItems.add(
                    mapOf(
                        "serviceId" to serviceId,
                        "name" to name,
                        "price" to price,
                        "estimatedMinutes" to minutes,
                        "quantity" to quantity
                    )
                )
            }

            val lastTokenNumber =
                queueSnapshot.getLong("lastTokenNumber") ?: 0L

            val nextTokenNumber = lastTokenNumber + 1L

            val waitingTokenIds =
                (queueSnapshot.get("waitingTokenIds") as? List<*>)
                    ?.mapNotNull { it as? String }
                    .orEmpty()

            val tokenData = hashMapOf<String, Any>(
                "queueId" to organization.queueId,
                "operatorId" to organization.operatorId,
                "customerId" to uid,
                "customerName" to customerName,
                "tokenNumber" to nextTokenNumber,
                "status" to "WAITING",
                "serviceItems" to serviceItems,
                "totalPrice" to totalPrice,
                "estimatedMinutes" to totalEstimatedMinutes,
                "createdAt" to FieldValue.serverTimestamp()
            )

            transaction.set(tokenRef, tokenData)

            transaction.update(
                queueRef,
                mapOf(
                    "lastTokenNumber" to nextTokenNumber,
                    "waitingTokenIds" to (waitingTokenIds + tokenRef.id),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            tokenRef.id

        }.await()
    }

    fun listenToMyQueue(
        tokenId: String,
        queueId: String,
        onUpdate: (CustomerQueueInfo) -> Unit,
        onError: (String) -> Unit
    ): List<ListenerRegistration> {

        val tokenRef = firestore.collection("tokens").document(tokenId)
        val queueRef = firestore.collection("queues").document(queueId)

        var tokenData: Map<String, Any>? = null
        var queueData: Map<String, Any>? = null

        fun publish() {
            val token = tokenData ?: return
            val queue = queueData ?: return

            val waitingIds = (queue["waitingTokenIds"] as? List<*>)
                ?.mapNotNull { it as? String }
                .orEmpty()

            val status = token["status"] as? String ?: "WAITING"
            val currentTokenId = queue["currentTokenId"] as? String

            val index = waitingIds.indexOf(tokenId)

            val position = when {
                status == "COMPLETED" -> 0
                status == "SERVING" || currentTokenId == tokenId -> 0
                index >= 0 -> index + 1
                else -> 0
            }

            val averageMinutes =
                (queue["averageMinutesPerUnit"] as? Number)?.toInt() ?: 10

            val tokensAhead = when {
                position == 0 -> 0
                else -> index + if (currentTokenId != null) 1 else 0
            }

            onUpdate(
                CustomerQueueInfo(
                    tokenId = tokenId,
                    queueId = queueId,
                    tokenNumber =
                        (token["tokenNumber"] as? Number)?.toLong() ?: 0L,
                    status = status,
                    organizationName =
                        queue["organizationName"] as? String ?: "",
                    location = queue["location"] as? String ?: "",
                    position = position,
                    estimatedWaitMinutes = tokensAhead * averageMinutes,
                    totalPrice =
                        (token["totalPrice"] as? Number)?.toDouble() ?: 0.0,
                    customerName =
                        token["customerName"] as? String ?: "Customer"
                )
            )
        }

        val tokenListener = tokenRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error.message ?: "Could not load your token.")
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                onError("Your booking could not be found.")
                return@addSnapshotListener
            }

            tokenData = snapshot.data
            publish()
        }

        val queueListener = queueRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error.message ?: "Could not update queue status.")
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                onError("Queue information is unavailable.")
                return@addSnapshotListener
            }

            queueData = snapshot.data
            publish()
        }

        return listOf(tokenListener, queueListener)
    }

    fun listenToMyActiveQueues(
        onUpdate: (List<CustomerQueueInfo>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please log in first.")

        val activeTokens = mutableMapOf<String, Map<String, Any>>()
        val queueData = mutableMapOf<String, Map<String, Any>>()
        val queueListeners = mutableMapOf<String, ListenerRegistration>()

        var isRemoved = false

        fun publish() {
            if (isRemoved) return

            val result = activeTokens.mapNotNull { (tokenId, token) ->
                val queueId = token["queueId"] as? String ?: return@mapNotNull null
                val queue = queueData[queueId] ?: return@mapNotNull null

                val status = token["status"] as? String ?: "WAITING"
                if (status != "WAITING" && status != "SERVING") {
                    return@mapNotNull null
                }

                val waitingIds = (queue["waitingTokenIds"] as? List<*>)
                    .orEmpty()
                    .filterIsInstance<String>()

                val currentTokenId = queue["currentTokenId"] as? String
                val index = waitingIds.indexOf(tokenId)

                val position = when {
                    status == "SERVING" || currentTokenId == tokenId -> 0
                    index >= 0 -> index + 1
                    else -> 0
                }

                val averageMinutes =
                    (queue["averageMinutesPerUnit"] as? Number)?.toInt() ?: 10

                val tokensAhead = if (position == 0) {
                    0
                } else {
                    index + if (currentTokenId != null) 1 else 0
                }

                CustomerQueueInfo(
                    tokenId = tokenId,
                    queueId = queueId,
                    tokenNumber = (token["tokenNumber"] as? Number)?.toLong() ?: 0L,
                    status = status,
                    organizationName = queue["organizationName"] as? String ?: "",
                    location = queue["location"] as? String ?: "",
                    position = position,
                    estimatedWaitMinutes = tokensAhead * averageMinutes,
                    totalPrice = (token["totalPrice"] as? Number)?.toDouble() ?: 0.0,
                    customerName = token["customerName"] as? String ?: "Customer"
                )
            }.sortedWith(
                compareBy<CustomerQueueInfo> { it.status != "SERVING" }
                    .thenBy { it.tokenNumber }
            )

            onUpdate(result)
        }

        val tokenListener = firestore.collection("tokens")
            .whereEqualTo("customerId", uid)
            .whereIn("status", listOf("WAITING", "SERVING"))
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not load your active bookings.")
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                activeTokens.clear()

                snapshot.documents.forEach { document ->
                    document.data?.let { data ->
                        activeTokens[document.id] = data
                    }
                }

                // Attach listeners for queues that contain this customer's active tokens.
                val requiredQueueIds = activeTokens.values
                    .mapNotNull { it["queueId"] as? String }
                    .toSet()

                // Remove listeners for queues no longer needed.
                val removedQueueIds = queueListeners.keys - requiredQueueIds

                removedQueueIds.forEach { queueId ->
                    queueListeners.remove(queueId)?.remove()
                    queueData.remove(queueId)
                }

                // Listen to each relevant queue.
                requiredQueueIds.forEach { queueId ->
                    if (queueId !in queueListeners) {
                        val listener = firestore.collection("queues")
                            .document(queueId)
                            .addSnapshotListener { queueSnapshot, queueError ->

                                if (queueError != null) {
                                    onError(
                                        queueError.message
                                            ?: "Could not update queue information."
                                    )
                                    return@addSnapshotListener
                                }

                                if (queueSnapshot == null || !queueSnapshot.exists()) {
                                    queueData.remove(queueId)
                                } else {
                                    queueData[queueId] = queueSnapshot.data.orEmpty()
                                }

                                publish()
                            }

                        queueListeners[queueId] = listener
                    }
                }

                publish()
            }

        return object : ListenerRegistration {
            override fun remove() {
                isRemoved = true
                tokenListener.remove()

                queueListeners.values.forEach { it.remove() }
                queueListeners.clear()

                activeTokens.clear()
                queueData.clear()
            }
        }
    }
}
