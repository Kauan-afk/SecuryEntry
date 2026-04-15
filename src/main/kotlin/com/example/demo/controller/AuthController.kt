package com.example.demo.controller

import com.example.demo.dto.LoginRequest
import com.example.demo.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val service: UserService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest) = service.login(request)
}
