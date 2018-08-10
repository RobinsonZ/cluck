package org.team1540.cluck.backend.interfaces

import org.team1540.cluck.backend.data.SimpleEmail

/**
 * A service that can send simple emails.
 */
interface EmailService {
    /**
     * 53ND IT
     */
    fun send(vararg emails: SimpleEmail)
}
