package com.example.demo.service

import com.example.demo.model.Visitor
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.VehicleRepository
import com.example.demo.repository.VisitorRepository
import org.springframework.stereotype.Service

@Service
class VisitorService(
    private val repository: VisitorRepository,
    private val apartmentRepository: ApartmentRepository,
    private val vehicleRepository: VehicleRepository
) {

    fun create(visitor: Visitor): String {
        normalizeVisitor(visitor)
        validateVisitor(visitor)
        val id = repository.save(visitor)
        syncVehicle(id, visitor.vehicleId, "")
        return id
    }

    fun getAll() = repository.findAll()

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Visitante nao encontrado.")

    fun update(id: String, visitor: Visitor) {
        val existingVisitor = repository.findById(id) ?: throw NoSuchElementException("Visitante nao encontrado.")
        normalizeVisitor(visitor)
        validateVisitor(visitor)
        repository.update(id, visitor)
        syncVehicle(id, visitor.vehicleId, existingVisitor.vehicleId)
    }

    fun delete(id: String) {
        repository.findById(id) ?: throw NoSuchElementException("Visitante nao encontrado.")
        repository.delete(id)
    }

    private fun validateVisitor(visitor: Visitor) {
        if (visitor.name.isBlank()) {
            throw IllegalArgumentException("Nome do visitante e obrigatorio.")
        }
        if (visitor.cpf.isBlank()) {
            throw IllegalArgumentException("CPF do visitante e obrigatorio.")
        }
        if (visitor.apartmentId.isBlank()) {
            throw IllegalArgumentException("Apartamento do visitante e obrigatorio.")
        }
        apartmentRepository.findById(visitor.apartmentId)
            ?: throw IllegalArgumentException("Apartamento informado nao foi encontrado.")

        if (!visitor.freeAccess && visitor.visitDate.isBlank()) {
            throw IllegalArgumentException("A data da visita e obrigatoria para visitantes sem acesso livre.")
        }
        if (visitor.hasVehicle && visitor.vehicleId.isBlank()) {
            throw IllegalArgumentException("O veiculo do visitante e obrigatorio quando a entrada for com veiculo.")
        }
        if (visitor.vehicleId.isNotBlank()) {
            val vehicle = vehicleRepository.findById(visitor.vehicleId)
                ?: throw IllegalArgumentException("Veiculo informado para o visitante nao foi encontrado.")

            if (vehicle.apartmentId.isNotBlank()) {
                throw IllegalArgumentException("O veiculo informado ja esta vinculado a um apartamento.")
            }
        }
    }

    private fun syncVehicle(visitorId: String, currentVehicleId: String, previousVehicleId: String) {
        if (previousVehicleId.isNotBlank() && previousVehicleId != currentVehicleId) {
            val previousVehicle = vehicleRepository.findById(previousVehicleId)
            if (previousVehicle != null && previousVehicle.visitorId == visitorId) {
                previousVehicle.visitorId = ""
                vehicleRepository.update(previousVehicleId, previousVehicle)
            }
        }

        if (currentVehicleId.isNotBlank()) {
            val vehicle = vehicleRepository.findById(currentVehicleId)
                ?: throw IllegalArgumentException("Veiculo informado para o visitante nao foi encontrado.")
            if (vehicle.visitorId != visitorId) {
                vehicle.visitorId = visitorId
                vehicle.apartmentId = ""
                vehicleRepository.update(currentVehicleId, vehicle)
            }
        }
    }

    private fun normalizeVisitor(visitor: Visitor) {
        visitor.name = visitor.name.trim()
        visitor.cpf = visitor.cpf.trim()
        visitor.phone = visitor.phone.trim()
        visitor.apartmentId = visitor.apartmentId.trim()
        visitor.visitDate = if (visitor.freeAccess) "" else visitor.visitDate.trim()
        visitor.vehicleId = if (visitor.hasVehicle) visitor.vehicleId.trim() else ""
        visitor.authorizedBy = visitor.authorizedBy.trim()
        visitor.notes = visitor.notes.trim()
    }
}
