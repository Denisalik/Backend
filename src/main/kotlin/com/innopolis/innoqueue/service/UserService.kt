package com.innopolis.innoqueue.service

import com.innopolis.innoqueue.controller.dto.TokenDTO
import com.innopolis.innoqueue.model.User
import com.innopolis.innoqueue.model.UserSetting
import com.innopolis.innoqueue.repository.UserRepository
import com.innopolis.innoqueue.repository.UserSettingsRepository
import com.innopolis.innoqueue.utils.StringGenerator
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

private const val TOKEN_LENGTH = 64

@Service
class UserService(
    private val userRepository: UserRepository,
    private val settingsRepository: UserSettingsRepository,
) {
    fun getUserByToken(token: String): User {
        return userRepository.findAll().firstOrNull { user -> user.token == token }
            ?: throw NoSuchElementException("No such user with token: $token")
    }

    fun getUserById(userId: Long): User? {
        return userRepository.findByIdOrNull(userId)
    }

    fun generateUserToken(userName: String, fcmToken: String): TokenDTO {
        if (userName.isEmpty()) {
            throw IllegalArgumentException("Username can't be an empty string")
        }
        val existingTokens = userRepository.findAll().map { it.token }
        val generator = StringGenerator(TOKEN_LENGTH)
        while (true) {
            val randomString = generator.generateString()
            if (!existingTokens.contains(randomString)) {
                val savedUser = createNewUser(randomString, userName, fcmToken)
                return TokenDTO(randomString, savedUser.id!!)
            }
        }
    }

    private fun createNewUser(token: String, userName: String, fcmToken: String): User {
        val newUser = User()
        newUser.name = userName
        newUser.token = token
        newUser.fcmToken = fcmToken
        val savedUser = userRepository.save(newUser)
        val settings = UserSetting()
        settings.user = newUser
        settingsRepository.save(settings)
        return savedUser
    }
}
