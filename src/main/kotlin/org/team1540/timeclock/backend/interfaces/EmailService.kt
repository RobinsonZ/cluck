package org.team1540.timeclock.backend.interfaces

import org.team1540.timeclock.backend.data.SimpleEmail

interface EmailService {
    /**
     * 53ND IT
     */
    fun send(vararg emails: SimpleEmail)
}
