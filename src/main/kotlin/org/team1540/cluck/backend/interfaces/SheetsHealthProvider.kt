package org.team1540.cluck.backend.interfaces

import org.springframework.boot.actuate.health.Health

interface SheetsHealthProvider {
    /**
     * Return an indication of health.
     * @return the health for
     */
    fun getSheetsHealth(): Health
}
