package org.team1540.timeclock.backend.services

open class AdminToolsException(message: String) : Exception(message)
class NoSuchUserException : AdminToolsException("not_found")
class UserAlreadyExistsException : AdminToolsException("already_exists")
open class ClockInOutException(message: String) : Exception(message)
class UserNotFoundException : ClockInOutException("not_found")
class AlreadyClockedInOrOutException : ClockInOutException("repeat_clock")
class NeverClockedInException : ClockInOutException("never_clocked")
