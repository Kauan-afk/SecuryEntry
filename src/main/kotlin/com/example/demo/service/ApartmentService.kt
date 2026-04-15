package com.example.demo.service

import com.example.demo.model.Apartment
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.UserRepository
import com.example.demo.repository.VehicleRepository
import org.springframework.stereotype.Service

@Service
class ApartmentService(
    private val repository: ApartmentRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) {

    fun create(apartment: Apartment): String {
        normalizeApartment(apartment)
        validateApartment(apartment)

        if (repository.findByNumberAndBlock(apartment.number, apartment.block) != null) {
            throw IllegalArgumentException("Ja existe um apartamento cadastrado com este numero e bloco.")
        }

        validateResidents(apartment.residentIds)
        apartment.isOccupied = apartment.residentIds.isNotEmpty()
        val id = repository.save(apartment)
        syncVehicles(id, apartment.vehicleIds, emptyList())
        return id
    }

    fun getAll() = repository.findAll()

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Apartamento nao encontrado.")

    fun update(id: String, apartment: Apartment) {
        val existingApartment = repository.findById(id)
            ?: throw NoSuchElementException("Apartamento nao encontrado.")

        normalizeApartment(apartment)
        validateApartment(apartment)

        val sameUnit = apartment.number == existingApartment.number && apartment.block == existingApartment.block
        if (!sameUnit) {
            val apartmentsWithSameUnit = repository.findAllByNumberAndBlock(apartment.number, apartment.block)
            val unitAlreadyExists = apartmentsWithSameUnit.any { it.id != id }
            if (unitAlreadyExists) {
                throw IllegalArgumentException("Ja existe um apartamento cadastrado com este numero e bloco.")
            }
        }

        validateResidents(apartment.residentIds)
        apartment.isOccupied = apartment.residentIds.isNotEmpty()
        repository.update(id, apartment)
        syncVehicles(id, apartment.vehicleIds, existingApartment.vehicleIds)
    }

    fun delete(id: String) {
        repository.findById(id) ?: throw NoSuchElementException("Apartamento nao encontrado.")
        repository.delete(id)
    }

    private fun validateApartment(apartment: Apartment) {
        if (apartment.number.isBlank()) {
            throw IllegalArgumentException("Numero do apartamento e obrigatorio.")
        }
        if (apartment.block.isBlank()) {
            throw IllegalArgumentException("Bloco e obrigatorio.")
        }
        if (apartment.maxResidents < 0) {
            throw IllegalArgumentException("A capacidade maxima nao pode ser negativa.")
        }
        if (apartment.maxResidents > 0 && apartment.residentIds.size > apartment.maxResidents) {
            throw IllegalArgumentException("A quantidade de moradores excede a capacidade maxima do apartamento.")
        }
        if (apartment.vehicleIds.size > apartment.parkingSpotCount) {
            throw IllegalArgumentException("A quantidade de veiculos nao pode ser maior que o total de vagas.")
        }
        if (apartment.parkingSpotCount < 0) {
            throw IllegalArgumentException("A quantidade de vagas nao pode ser negativa.")
        }
        if (apartment.availableParkingSpots < 0) {
            throw IllegalArgumentException("A quantidade de vagas disponiveis nao pode ser negativa.")
        }
        if (apartment.availableParkingSpots > apartment.parkingSpotCount) {
            throw IllegalArgumentException("As vagas disponiveis nao podem ser maiores que o total de vagas.")
        }
    }

    private fun validateResidents(residentIds: List<String>) {
        val ids = residentIds.map { it.trim() }.filter { it.isNotBlank() }
        if (ids.size != ids.distinct().size) {
            throw IllegalArgumentException("Nao e permitido repetir moradores no apartamento.")
        }

        ids.forEach { residentId ->
            userRepository.findById(residentId)
                ?: throw IllegalArgumentException("Morador informado nao foi encontrado: $residentId")
        }
    }

    private fun validateVehicles(vehicleIds: List<String>, apartmentId: String? = null) {
        val ids = vehicleIds.map { it.trim() }.filter { it.isNotBlank() }
        if (ids.size != ids.distinct().size) {
            throw IllegalArgumentException("Nao e permitido repetir veiculos no apartamento.")
        }

        ids.forEach { vehicleId ->
            val vehicle = vehicleRepository.findById(vehicleId)
                ?: throw IllegalArgumentException("Veiculo informado nao foi encontrado: $vehicleId")

            if (vehicle.visitorId.isNotBlank()) {
                throw IllegalArgumentException("O veiculo $vehicleId esta vinculado a um visitante e nao pode ser associado ao apartamento.")
            }

            if (vehicle.apartmentId.isNotBlank() && vehicle.apartmentId != apartmentId) {
                throw IllegalArgumentException("O veiculo $vehicleId ja esta vinculado a outro apartamento.")
            }
        }
    }

    private fun syncVehicles(apartmentId: String, currentVehicleIds: List<String>, previousVehicleIds: List<String>) {
        val currentIds = currentVehicleIds.distinct()
        val previousIds = previousVehicleIds.distinct()

        previousIds.filter { it !in currentIds }.forEach { vehicleId ->
            val vehicle = vehicleRepository.findById(vehicleId)
            if (vehicle != null && vehicle.apartmentId == apartmentId) {
                vehicle.apartmentId = ""
                vehicleRepository.update(vehicleId, vehicle)
            }
        }

        currentIds.forEach { vehicleId ->
            val vehicle = vehicleRepository.findById(vehicleId)
                ?: throw IllegalArgumentException("Veiculo informado nao foi encontrado: $vehicleId")
            if (vehicle.apartmentId != apartmentId) {
                vehicle.apartmentId = apartmentId
                vehicle.visitorId = ""
                vehicleRepository.update(vehicleId, vehicle)
            }
        }
    }

    private fun normalizeApartment(apartment: Apartment) {
        apartment.number = apartment.number.trim()
        apartment.block = apartment.block.trim().uppercase()
        apartment.floor = apartment.floor.trim()
        apartment.notes = apartment.notes.trim()
        apartment.residentIds = apartment.residentIds.map { it.trim() }.filter { it.isNotBlank() }
        apartment.vehicleIds = apartment.vehicleIds.map { it.trim() }.filter { it.isNotBlank() }
        validateVehicles(apartment.vehicleIds, apartment.id)
    }
}
