# Spec Sheet

This is a set of instructions and info about the backend and its dependencies, written for people who know what they're doing. While there are hyperlinks to pretty much everything, there's no hand-holding here; for that, take a look at the manual.

## Code

This backend is made with [Spring Boot](https://spring.io/projects/spring-boot) and written in 100% [Kotlin](https://kotlinlang.org/). It uses [Gradle](https://gradle.org/) for building.

## Features

### Users

The API tracks individual users via IDs. These IDs are generally 2-4 numeric digits by convention and for ease of use, but are represented internally as arbitrary-length strings ("007" and "0007" are counted as different IDs).

Users also have their name and email stored in the database. 

### Clock-In/Out

The API handles clock-in and clock-out requests, and prevents multiple consecutive clock-ins/clock-outs as well as a clock-out without a clock-in beforehand. 

### Time Tracking

A full record of all clock-ins and clock-outs for each user are stored in the database. The API uses this to calculate hours.

### Spreadsheet Output

The backend connects to Google Sheets, and outputs hour records for each person to a cells in a spreadsheet. For output to work properly, the name of each user in the spreadsheet must match their name in the user database.

### Auto-Logout

At 23:59:59 system time each day, the backend will check for any users who have logged in but did not log out. It will then log them out and send an email to their address in the database.

## The API

The full API consists of two sets of method calls: `clockapi` for timeclock terminals, and `timesheet` for displays of who is logged in at a given time. The [OpenAPI](https://www.openapis.org/) specification is in `swagger.yml` in the root of the project and contains complete documentation for all API methods.

All requests to the API must be made using [HTTPS](https://en.wikipedia.org/wiki/HTTPS).

### Authentication

Credentials are provided via [HTTP Basic Authentication](https://en.wikipedia.org/wiki/Basic_access_authentication#Security).

By default, only the `clockapi` set of methods requires authentication, though authentication can also be required for `timesheet` methods by setting the `timeclock.auth.secure-timesheet-api` property to `true`. Note that if `timesheet` authentication is enabled, `clockapi` credentials will also be accepted for `timesheet` methods.

### Password Encoding

The backend supports multiple encoding algorithms through the use of a [DelegatingPasswordEncoder](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/password/DelegatingPasswordEncoder.html). When specifying passwords in application properties, specify the algorithm ID in braces (`{}`) before the encoded form of the password like so:`{bcrypt}$2a$10$DdkI8AiFilpO2b9xKDNaVOhavN4bF.8m557X5MYoJLDD/4yo/vBz6`. The backend supports multiple hashing algorithms; however, `bcrypt` is recommended.

## Database

The backend stores data by connecting to a [MongoDB](https://www.mongodb.com/) instance through [Spring Data MongoDB](https://projects.spring.io/spring-data-mongodb/). As such, with proper configuration, it can connect to a local instance (default), a remote instance over TCP, or a cloud-based solution via MongoDB Atlas. (Consult the Spring docs for details.)

### Schema

The name of the database used is set during configuration; within that database, the backend stores all relevant data in a collection named `users`.

Each user document contains the following fields:

- `_id`: The ID of the user, as a string
- `name`: The full name of the user, as a string
- `email`: The email address of the user, as a string. This is used by the auto-logout feature.
- `_class`: This field is used internally by Spring Data. It should always be set to the string `"org.team1540.timeclock.backend.data.User"`.
- `clockEvents`: A list of clock event (clock-in or clock-out) records. Each clock event document contains the following fields:
  - `timestamp`: A signed 64-bit integer containing the system time of the clock event, in milliseconds from the Unix epoch.
  - `clockingIn`: A boolean specifying whether the event was a clock-in (`true`) or a clock-out (`false`).

## Spring Configuration Properties

The backend has [Jasypt](https://github.com/ulisesbocchio/jasypt-spring-boot) as a dependency and can use it for encrypted storage of application properties.

- `timeclock.sheets.app-name`: The "app name" to use when editing the hours spreadsheet.
- `timeclock.sheets.sheet`: The spreadsheet ID to update with hours.
- `timeclock.sheets.service-file`: A path to the service account key used to edit the timesheet.
- `timeclock.sheets.name-range`: The sheet range (e.g. A1:A20) containing the names of users.
- `timeclock.sheets.hours-col`:  The column of the spreadsheet containing the hours: the backend will update this column.
- `timeclock.sheets.hours-row-offset`: The row at which hour record cells start. This is to accomodate spreadsheets with header cells. For instance, if your sheet has two rows of header cells, then this would be 3.
- `timeclock.auth.timeclock-username`: The username used for `clockapi` API methods.
- `timeclock.auth.timeclock-password`: The password used for `clockapi` API methods. This should be encoded as described in the Authentication section.
- `timeclock.auth.secure-timesheet-api`: Whether to require authentication to access `timesheet` API methods. Defaults to `false`
- `timeclock.auth.timesheet-username`: The username used for `timesheet` API methods. Has no effect unless `secure-timesheet-api` is `true`.
- `timeclock.auth.timesheet-password`: The password used for `timesheet` API methods. Has no effect unless `secure-timesheet-api` is `true`. This should be encoded as described in the Authentication section.
- `timeclock.autologout.enabled`: Whether to enable the autologout feature. Defaults to `true`.