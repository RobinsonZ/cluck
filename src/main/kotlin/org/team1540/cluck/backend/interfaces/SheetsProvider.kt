package org.team1540.cluck.backend.interfaces

import com.google.api.services.sheets.v4.Sheets

interface SheetsProvider {
    val sheets: Sheets
}
