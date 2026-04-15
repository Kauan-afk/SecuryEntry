package com.example.demo.service

import com.example.demo.model.Vehicle
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.VehicleRepository
import com.example.demo.repository.VisitorRepository
import org.springframework.stereotype.Service

@Service
class VehicleService(
    private val repository: VehicleRepository,
    private val apartmentRepository: ApartmentRepository,
    private val visitorRepository: VisitorRepository
) {

    fun create(vehicle: Vehicle): String {
        normalizeVehicle(vehicle)
        validateVehicle(vehicle)

        if (repository.findAllByPlate(vehicle.plate).isNotEmpty()) {
            throw IllegalArgumentException("Ja existe um veiculo cadastrado com esta placa.")
        }

        val id = repository.save(vehicle)
        syncOwnership(id, vehicle, "", "")
        return id
    }

    fun getAll() = repository.findAll()

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Veiculo nao encontrado.")

    fun update(id: String, vehicle: Vehicle) {
        val existingVehicle = repository.findById(id) ?: throw NoSuchElementException("Veiculo nao encontrado.")
        normalizeVehicle(vehicle)
        validateVehicle(vehicle)

        val samePlate = existingVehicle.plate == vehicle.plate
        if (!samePlate) {
            val vehiclesWithSamePlate = repository.findAllByPlate(vehicle.plate)
            val plateAlreadyExists = vehiclesWithSamePlate.any { it.id != id }
            if (plateAlreadyExists) {
                throw IllegalArgumentException("Ja existe um veiculo cadastrado com esta placa.")
            }
        }

        repository.update(id, vehicle)
        syncOwnership(id, vehicle, existingVehicle.apartmentId, existingVehicle.visitorId)
    }

    fun delete(id: String) {
        val existingVehicle = repository.findById(id) ?: throw NoSuchElementException("Veiculo nao encontrado.")
        syncOwnership(id, Vehicle(), existingVehicle.apartmentId, existingVehicle.visitorId)
        repository.delete(id)
    }

    private fun validateVehicle(vehicle: Vehicle) {
        if (vehicle.plate.isBlank()) {
            throw IllegalArgumentException("Placa do veiculo e obrigatoria.")
        }
        if (vehicle.apartmentId.isNotBlank() && vehicle.visitorId.isNotBlank()) {
            throw IllegalArgumentException("O veiculo nao pode estar vinculado ao apartamento e ao visitante ao mesmo tempo.")
        }
        if (vehicle.apartmentId.isNotBlank()) {
            apartmentRepository.findById(vehicle.apartmentId)
                ?: throw IllegalArgumentException("Apartamento informado para o veiculo nao foi encontrado.")
        }
        if (vehicle.visitorId.isNotBlank()) {
            visitorRepository.findById(vehicle.visitorId)
                ?: throw IllegalArgumentException("Visitante informado para o veiculo nao foi encontrado.")
        }
    }

    private fun syncOwnership(vehicleId: String, vehicle: Vehicle, previousApartmentId: String, previousVisitorId: String) {
        if (previousApartmentId.isNotBlank() && previousApartmentId != vehicle.apartmentId) {
            val apartment = apartmentRepository.findById(previousApartmentId)
            if (apartment != null) {
                apartment.vehicleIds = apartment.vehicleIds.filter { it != vehicleId }
                apartmentRepository.update(previousApartmentId, apartment)
            }
        }

        if (previousVisitorId.isNotBlank() && previousVisitorId != vehicle.visitorId) {
            val visitor = visitorRepository.findById(previousVisitorId)
            if (visitor != null) {
                visitor.hasVehicle = false
                visitor.vehicleId = ""
                visitorRepository.update(previousVisitorId, visitor)
            }
        }

        if (vehicle.apartmentId.isNotBlank()) {
            val apartment = apartmentRepository.findById(vehicle.apartmentId)
                ?: throw IllegalArgumentException("Apartamento informado para o veiculo nao foi encontrado.")
            if (!apartment.vehicleIds.contains(vehicleId)) {
                apartment.vehicleIds = apartment.vehicleIds + vehicleId
                apartmentRepository.update(vehicle.apartmentId, apartment)
            }
        }

        if (vehicle.visitorId.isNotBlank()) {
            val visitor = visitorRepository.findById(vehicle.visitorId)
                ?: throw IllegalArgumentException("Visitante informado para o veiculo nao foi encontrado.")
            visitor.hasVehicle = true
            visitor.vehicleId = vehicleId
            visitorRepository.update(vehicle.visitorId, visitor)
        }
    }

    private fun normalizeVehicle(vehicle: Vehicle) {
        vehicle.plate = vehicle.plate.trim().uppercase()
        vehicle.brand = vehicle.brand.trim()
        vehicle.model = vehicle.model.trim()
        vehicle.color = vehicle.color.trim()
        vehicle.type = vehicle.type.trim().uppercase()
        vehicle.apartmentId = vehicle.apartmentId.trim()
        vehicle.visitorId = vehicle.visitorId.trim()
        vehicle.notes = vehicle.notes.trim()
    }
}
