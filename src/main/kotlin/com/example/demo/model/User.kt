package com.example.demo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class User (
    var id: String? = null,
    var name: String = "",
    var email: String = "",
    @field:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String = "",
    var phone: String = "",
    var apartment: String = "",
    var block: String = "",
    var residentType: String = "",
    var birthDate: String = "",
    var cpf: String = "",
    var isActive: Boolean = true
)
