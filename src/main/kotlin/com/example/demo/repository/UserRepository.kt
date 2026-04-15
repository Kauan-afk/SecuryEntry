package com.example.demo.repository

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.example.demo.model.User
import org.springframework.stereotype.Repository

@Repository
class UserRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(user: User): String {
        val docRef = db.collection("users").document()
        user.id = docRef.id
        docRef.set(user)
        return user.id!!
    }

    fun findAll(): List<User> {
        val result = db.collection("users").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(User::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): User? {
        val doc = db.collection("users").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(User::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun findByEmail(email: String): User? {
        return findAllByEmail(email).firstOrNull()
    }

    fun findAllByEmail(email: String): List<User> {
        val result = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .get()

        return result.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.apply { id = doc.id }
        }
    }

    fun update(id: String, user: User) {
        user.id = id
        db.collection("users").document(id).set(user)
    }

    fun delete(id: String) {
        db.collection("users").document(id).delete()
    }
}
