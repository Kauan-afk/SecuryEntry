package com.example.demo.service

import com.example.demo.dto.LoginRequest
import com.example.demo.dto.LoginResponse
import com.example.demo.model.User
import com.example.demo.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(private val repository: UserRepository) {

    private val passwordEncoder = BCryptPasswordEncoder()

    fun create(user: User): String {
        val email = user.email.trim().lowercase()
        validateUser(user)

        if (repository.findAllByEmail(email).isNotEmpty()) {
            throw IllegalArgumentException("Ja existe um usuario cadastrado com este email.")
        }

        user.email = email
        user.password = hash(user.password)
        return repository.save(user)
    }

    fun getAll() = repository.findAll()

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Usuario nao encontrado.")

    fun update(id: String, user: User) {
        val existingUser = repository.findById(id) ?: throw NoSuchElementException("Usuario nao encontrado.")
        val email = user.email.trim().lowercase()
        val currentEmail = existingUser.email.trim().lowercase()
        validateUser(user, isUpdate = true)

        user.email = email
        user.password = if (user.password.isBlank()) {
            existingUser.password
        } else {
            hash(user.password)
        }

        if (email == currentEmail) {
            repository.update(id, user)
            return
        }

        val usersWithSameEmail = repository.findAllByEmail(email)
        val emailBelongsToAnotherUser = usersWithSameEmail.any { it.id != id }
        if (emailBelongsToAnotherUser) {
            throw IllegalArgumentException("Ja existe um usuario cadastrado com este email.")
        }

        repository.update(id, user)
    }

    fun delete(id: String) = repository.delete(id)

    fun login(request: LoginRequest): LoginResponse {
        val email = request.email.trim().lowercase()
        val password = request.password.trim()

        if (email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Email e senha sao obrigatorios.")
        }

        val user = repository.findByEmail(email)
            ?: throw NoSuchElementException("Usuario nao encontrado.")

        if (!user.isActive) {
            throw IllegalAccessException("Usuario sem acesso liberado.")
        }

        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("Senha invalida.")
        }

        return LoginResponse(
            message = "Login realizado com sucesso.",
            userId = user.id.orEmpty(),
            name = user.name,
            email = user.email
        )
    }

    private fun validateUser(user: User, isUpdate: Boolean = false) {
        if (user.name.isBlank()) {
            throw IllegalArgumentException("Nome e obrigatorio.")
        }
        if (user.email.isBlank()) {
            throw IllegalArgumentException("Email e obrigatorio.")
        }
        if (!user.email.contains("@")) {
            throw IllegalArgumentException("Email invalido.")
        }
        if (!isUpdate && user.password.isBlank()) {
            throw IllegalArgumentException("Senha e obrigatoria.")
        }
        if (user.password.isNotBlank() && user.password.length < 6) {
            throw IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.")
        }
        if (user.apartment.isBlank()) {
            throw IllegalArgumentException("Apartamento e obrigatorio.")
        }
        if (user.cpf.isBlank()) {
            throw IllegalArgumentException("CPF e obrigatorio.")
        }
    }

    private fun hash(value: String): String {
        return requireNotNull(passwordEncoder.encode(value))
    }
}
