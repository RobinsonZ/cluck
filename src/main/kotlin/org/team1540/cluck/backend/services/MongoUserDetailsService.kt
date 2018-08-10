package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.config.AuthenticationConfig
import org.team1540.cluck.backend.data.AccessLevel
import org.team1540.cluck.backend.data.Credential
import org.team1540.cluck.backend.data.CredentialRepository
import javax.annotation.PostConstruct

@Service
class MongoUserDetailsService : UserDetailsService {
    private val logger = KotlinLogging.logger { }
    @Autowired
    private lateinit var credentialRepository: CredentialRepository

    @Autowired
    private lateinit var authConfig: AuthenticationConfig

    override fun loadUserByUsername(username: String?): UserDetails {
        val credential = credentialRepository.findById(username
                ?: throw UsernameNotFoundException("")).orElseThrow { UsernameNotFoundException("") }

        return User.builder().username(credential.username).password(credential.password).roles(*credential.accessLevel.roleNames).build()
    }

    @PostConstruct
    fun init() {
        val lastCredential: Credential? = credentialRepository.findById("admin").orElse(null)
        if (authConfig.adminPassword != lastCredential?.password) {
            logger.info { "Admin password has changed, updating" }
            credentialRepository.save(Credential(AccessLevel.ADMIN, "admin", authConfig.adminPassword))
        }
    }
}
