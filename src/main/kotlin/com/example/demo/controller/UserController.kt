package com.example.demo.controller

import com.example.demo.model.User
import com.example.demo.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val service: UserService) {

    @PostMapping
    fun create(@RequestBody user: User) = service.create(user)

    @GetMapping
    fun getAll() = service.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody user: User) {
        service.update(id, user)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        service.delete(id)
    }
}
