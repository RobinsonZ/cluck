package org.team1540.timeclock.backend

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.convertToISODate(): String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault()))
