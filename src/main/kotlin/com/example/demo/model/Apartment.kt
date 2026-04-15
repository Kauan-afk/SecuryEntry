package com.example.demo.model

data class Apartment(
    var id: String? = null,
    var number: String = "",
    var block: String = "",
    var floor: String = "",
    var residentIds: List<String> = emptyList(),
    var maxResidents: Int = 0,
    var parkingSpotCount: Int = 0,
    var availableParkingSpots: Int = 0,
    var isOccupied: Boolean = false,
    var isActive: Boolean = true,
    var notes: String = ""
)
