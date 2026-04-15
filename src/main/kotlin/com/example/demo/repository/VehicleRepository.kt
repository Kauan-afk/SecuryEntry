package com.example.demo.repository

import com.example.demo.model.Vehicle
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class VehicleRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(vehicle: Vehicle): String {
        val docRef = db.collection("vehicles").document()
        vehicle.id = docRef.id
        docRef.set(vehicle)
        return vehicle.id!!
    }

    fun findAll(): List<Vehicle> {
        val result = db.collection("vehicles").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): Vehicle? {
        val doc = db.collection("vehicles").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(Vehicle::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun findByPlate(plate: String): Vehicle? {
        return findAllByPlate(plate).firstOrNull()
    }

    fun findAllByPlate(plate: String): List<Vehicle> {
        val result = db.collection("vehicles")
            .whereEqualTo("plate", plate)
            .get()
            .get()

        return result.documents.mapNotNull { doc ->
            doc.toObject(Vehicle::class.java)?.apply { id = doc.id }
        }
    }

    fun update(id: String, vehicle: Vehicle) {
        vehicle.id = id
        db.collection("vehicles").document(id).set(vehicle)
    }

    fun delete(id: String) {
        db.collection("vehicles").document(id).delete()
    }
}
