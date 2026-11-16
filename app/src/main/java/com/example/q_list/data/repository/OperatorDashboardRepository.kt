package com.example.q_list.data.repository


import com.example.q_list.data.model.QueueSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class OperatorDashboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val users = firestore.collection("users")
    private val queues = firestore.collection("queues")
    private val bookingCodes = firestore.collection("bookingCodes")

    private fun requireOperatorId(): String {
        return auth.currentUser?.uid
            ?: throw Exception("Please log in again.")
    }

    fun listenToDashboard(
        onUpdate: (
            operatorName: String,
            organizationName: String,
            location: String,
            session: QueueSession?
        ) -> Unit,
        onError: (String) -> Unit
    ): List<ListenerRegistration> {

        val operatorId = requireOperatorId()

        var operatorName = ""
        var organizationName = ""
        var location = ""
        var activeSession: QueueSession? = null

        fun publish() {
            onUpdate(
                operatorName,
                organizationName,
                location,
                activeSession
            )
        }

        val profileListener = users.document(operatorId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not load operator profile.")
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    onError("Operator profile not found.")
                    return@addSnapshotListener
                }

                operatorName = snapshot.getString("name").orEmpty()
                organizationName =
                    snapshot.getString("organizationName").orEmpty()
                location = snapshot.getString("location").orEmpty()

                publish()
            }

        val queueListener = queues
            .whereEqualTo("operatorId", operatorId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Could not load shop session.")
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                activeSession = snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(QueueSession::class.java)
                            ?.copy(id = document.id)
                    }
                    .filter { it.status == "OPEN" }
                    .maxByOrNull { it.sessionDate }

                publish()
            }

        return listOf(profileListener, queueListener)
    }

    suspend fun openShop(): String {
        val operatorId = requireOperatorId()

        val profile = users.document(operatorId).get().await()

        if (!profile.exists()) {
            throw Exception("Operator profile not found.")
        }

        val organizationName =
            profile.getString("organizationName").orEmpty()

        val location =
            profile.getString("location").orEmpty()

        if (organizationName.isBlank()) {
            throw Exception("Please complete your organization details.")
        }

        val existingSession = queues
            .whereEqualTo("operatorId", operatorId)
            .get()
            .await()
            .documents
            .firstOrNull { it.getString("status") == "OPEN" }

        if (existingSession != null) {
            throw Exception("Your shop is already open.")
        }

        val sessionRef = queues.document()
        val sessionDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())

        val sessionData = hashMapOf<String, Any?>(
            "operatorId" to operatorId,
            "organizationName" to organizationName,
            "location" to location,
            "bookingCode" to "",
            "sessionDate" to sessionDate,
            "status" to "OPEN",
            "lastTokenNumber" to 0L,
            "currentTokenId" to null,
            "waitingTokenIds" to emptyList<String>(),
            "averageMinutesPerUnit" to 10,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        // Retry if a randomly generated code already exists.
        repeat(10) {
            val code = Random.nextInt(100000, 1000000).toString()
            val codeRef = bookingCodes.document(code)

            try {
                firestore.runTransaction { transaction ->

                    val codeSnapshot = transaction.get(codeRef)

                    if (codeSnapshot.exists()) {
                        throw Exception("CODE_ALREADY_EXISTS")
                    }

                    sessionData["bookingCode"] = code

                    transaction.set(sessionRef, sessionData)

                    transaction.set(
                        codeRef,
                        mapOf(
                            "queueId" to sessionRef.id,
                            "operatorId" to operatorId,
                            "sessionDate" to sessionDate,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )

                    null
                }.await()

                return sessionRef.id

            } catch (exception: Exception) {
                if (exception.message?.contains("CODE_ALREADY_EXISTS") == true) {
                    return@repeat
                }
                throw exception
            }
        }

        throw Exception("Could not generate a booking code. Please try again.")
    }

    suspend fun closeShop(queueId: String) {
        val operatorId = requireOperatorId()
        val sessionRef = queues.document(queueId)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(sessionRef)

            if (!snapshot.exists()) {
                throw Exception("Shop session not found.")
            }

            if (snapshot.getString("operatorId") != operatorId) {
                throw Exception("You cannot close another operator's shop.")
            }

            if (snapshot.getString("status") != "OPEN") {
                throw Exception("This shop session is already closed.")
            }

            transaction.update(
                sessionRef,
                mapOf(
                    "status" to "CLOSED",
                    "closedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            null
        }.await()
    }
}
