package com.example.demo.repository

import com.example.demo.model.Visitor
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class VisitorRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(visitor: Visitor): String {
        val docRef = db.collection("visitors").document()
        visitor.id = docRef.id
        docRef.set(visitor)
        return visitor.id!!
    }

    fun findAll(): List<Visitor> {
        val result = db.collection("visitors").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(Visitor::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): Visitor? {
        val doc = db.collection("visitors").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(Visitor::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun update(id: String, visitor: Visitor) {
        visitor.id = id
        db.collection("visitors").document(id).set(visitor)
    }

    fun delete(id: String) {
        db.collection("visitors").document(id).delete()
    }
}
