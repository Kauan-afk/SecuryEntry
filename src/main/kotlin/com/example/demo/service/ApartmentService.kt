package com.example.demo.service

import com.example.demo.model.Apartment
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class ApartmentService(
    private val repository: ApartmentRepository,
    private val userRepository: UserRepository
) {

    fun create(apartment: Apartment): String {
        normalizeApartment(apartment)
        validateApartment(apartment)

        if (repository.findByNumberAndBlock(apartment.number, apartment.block) != null) {
            throw IllegalArgumentException("Ja existe um apartamento cadastrado com este numero e bloco.")
        }

        validateResidents(apartment.residentIds)
        apartment.isOccupied = apartment.residentIds.isNotEmpty()
        return repository.save(apartment)
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

    private fun normalizeApartment(apartment: Apartment) {
        apartment.number = apartment.number.trim()
        apartment.block = apartment.block.trim().uppercase()
        apartment.floor = apartment.floor.trim()
        apartment.notes = apartment.notes.trim()
        apartment.residentIds = apartment.residentIds.map { it.trim() }.filter { it.isNotBlank() }
    }
}
