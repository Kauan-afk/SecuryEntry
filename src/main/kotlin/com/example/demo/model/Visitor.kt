package com.example.demo.model

data class Visitor(
    var id: String? = null,
    var name: String = "",
    var cpf: String = "",
    var phone: String = "",
    var apartmentId: String = "",
    var visitDate: String = "",
    var freeAccess: Boolean = false,
    var hasVehicle: Boolean = false,
    var vehicleId: String = "",
    var authorizedBy: String = "",
    var notes: String = "",
    var isActive: Boolean = true
)
