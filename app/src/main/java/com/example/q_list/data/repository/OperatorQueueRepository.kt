package com.example.q_list.data.repository


import com.example.q_list.data.model.QueueToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class OperatorQueueRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val queues = firestore.collection("queues")
    private val tokens = firestore.collection("tokens")

    private fun requireOperatorId(): String {
        return auth.currentUser?.uid
            ?: throw Exception("Please log in again.")
    }

    // REAL-TIME QUEUE LISTENER
    fun listenToQueue(
        queueId: String,
        onUpdate: (List<QueueToken>, QueueToken?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {

        return tokens
            .whereEqualTo("queueId", queueId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not load queue.")
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val allTokens = snapshot.documents.mapNotNull { document ->
                    document.toObject(QueueToken::class.java)?.copy(
                        id = document.id
                    )
                }

                val waitingTokens = allTokens
                    .filter { it.status == "WAITING" }
                    .sortedBy { it.tokenNumber }

                val servingToken = allTokens.firstOrNull {
                    it.status == "SERVING"
                }

                onUpdate(waitingTokens, servingToken)
            }
    }

    // CALL NEXT CUSTOMER
    suspend fun callNextCustomer(queueId: String) {
        val operatorId = requireOperatorId()
        val queueRef = queues.document(queueId)

        firestore.runTransaction { transaction ->

            val queueSnapshot = transaction.get(queueRef)

            if (!queueSnapshot.exists()) {
                throw Exception("Queue not found.")
            }

            if (queueSnapshot.getString("operatorId") != operatorId) {
                throw Exception("You are not authorized to manage this queue.")
            }

            if (queueSnapshot.getString("status") != "OPEN") {
                throw Exception("This queue is currently closed.")
            }

            val currentTokenId =
                queueSnapshot.getString("currentTokenId")

            if (!currentTokenId.isNullOrBlank()) {
                throw Exception("Complete the current customer first.")
            }

            val waitingIds =
                queueSnapshot.get("waitingTokenIds") as? List<*>
                    ?: emptyList<Any>()

            if (waitingIds.isEmpty()) {
                throw Exception("There are no waiting customers.")
            }

            val nextTokenId = waitingIds.first().toString()
            val tokenRef = tokens.document(nextTokenId)
            val tokenSnapshot = transaction.get(tokenRef)

            if (!tokenSnapshot.exists()) {
                throw Exception("The next token could not be found.")
            }

            if (tokenSnapshot.getString("status") != "WAITING") {
                throw Exception("The next token is no longer waiting.")
            }

            transaction.update(
                tokenRef,
                mapOf(
                    "status" to "SERVING",
                    "operatorId" to operatorId,
                    "calledAt" to FieldValue.serverTimestamp()
                )
            )

            transaction.update(
                queueRef,
                mapOf(
                    "waitingTokenIds" to waitingIds.drop(1),
                    "currentTokenId" to nextTokenId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            null
        }.await()
    }

    // COMPLETE CURRENT SERVICE
    suspend fun completeService(queueId: String) {
        val operatorId = requireOperatorId()
        val queueRef = queues.document(queueId)

        firestore.runTransaction { transaction ->

            val queueSnapshot = transaction.get(queueRef)

            if (!queueSnapshot.exists()) {
                throw Exception("Queue not found.")
            }

            if (queueSnapshot.getString("operatorId") != operatorId) {
                throw Exception("You are not authorized to manage this queue.")
            }

            val currentTokenId =
                queueSnapshot.getString("currentTokenId")
                    ?: throw Exception("There is no active customer.")

            val tokenRef = tokens.document(currentTokenId)
            val tokenSnapshot = transaction.get(tokenRef)

            if (!tokenSnapshot.exists()) {
                throw Exception("Current token not found.")
            }

            if (tokenSnapshot.getString("status") != "SERVING") {
                throw Exception("This token is not being served.")
            }

            transaction.update(
                tokenRef,
                mapOf(
                    "status" to "COMPLETED",
                    "completedAt" to FieldValue.serverTimestamp()
                )
            )

            transaction.update(
                queueRef,
                mapOf(
                    "currentTokenId" to null,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            null
        }.await()
    }
}
