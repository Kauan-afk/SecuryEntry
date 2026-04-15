package com.example.demo.repository

import com.example.demo.model.Apartment
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class ApartmentRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(apartment: Apartment): String {
        val docRef = db.collection("apartments").document()
        apartment.id = docRef.id
        docRef.set(apartment)
        return apartment.id!!
    }

    fun findAll(): List<Apartment> {
        val result = db.collection("apartments").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(Apartment::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): Apartment? {
        val doc = db.collection("apartments").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(Apartment::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun findByNumberAndBlock(number: String, block: String): Apartment? {
        return findAllByNumberAndBlock(number, block).firstOrNull()
    }

    fun findAllByNumberAndBlock(number: String, block: String): List<Apartment> {
        val result = db.collection("apartments")
            .whereEqualTo("number", number)
            .whereEqualTo("block", block)
            .get()
            .get()

        return result.documents.mapNotNull { doc ->
            doc.toObject(Apartment::class.java)?.apply { id = doc.id }
        }
    }

    fun update(id: String, apartment: Apartment) {
        apartment.id = id
        db.collection("apartments").document(id).set(apartment)
    }

    fun delete(id: String) {
        db.collection("apartments").document(id).delete()
    }
}
