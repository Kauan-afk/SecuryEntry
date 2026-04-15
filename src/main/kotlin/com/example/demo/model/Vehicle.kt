package com.example.demo.model

data class Vehicle(
    var id: String? = null,
    var plate: String = "",
    var brand: String = "",
    var model: String = "",
    var color: String = "",
    var year: Int? = null,
    var type: String = "",
    var apartmentId: String = "",
    var visitorId: String = "",
    var notes: String = "",
    var isActive: Boolean = true
)
