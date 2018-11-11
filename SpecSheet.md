# Spec Sheet

This is a set of instructions and info about the CLUCK backend and its dependencies, written for people who know what they're doing. While there are hyperlinks to pretty much everything, there's no hand-holding here; for that, take a look at the manual.

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

Additionally, an additional sheet on the spreadsheet is used as a display of who is logged in at any given time.

### Auto-Logout

At 23:59:59 system time each day, the backend will check for any users who have logged in but did not log out. It will then log them out and send an email to their address in the database.

### Administration

The backend can be configured via API calls; these calls can add/remove users, add/remove sets of valid credentials for admin/timeclock/timesheet APIs, and reset hours on a per-user or global basis.

## The API

The full API consists of three sets of method calls: `admin` for administraion, `clockapi` for timeclock terminals, and `timesheet` for displays of who is logged in at a given time. The [OpenAPI](https://www.openapis.org/) specification is in `swagger.yml` in the root of the project and contains complete documentation for all API methods.

All requests to the API must be made using [HTTPS](https://en.wikipedia.org/wiki/HTTPS).

### Authentication

Credentials are provided via [HTTP Basic Authentication](https://en.wikipedia.org/wiki/Basic_access_authentication#Security). Accounts with the `ADMIN` access level can create other accounts. An account with username `admin` and a configurable password is the only account out-of-the-box; this set of credentials must be used to create additional logins for timeclocks and timesheets. If this account is deleted (by another account it created with `ADMIN` access) it will be recreated on server restart. As the API is entirely stateless, multiple clients may use the same set of credentials. 

#### Access Levels

The API has three access levels: `ADMIN`, `TIMECLOCK`, and `TIMESHEET`. These correspond to the three sets of methods. Accounts with the `ADMIN` access level may access all API methods; `TIMECLOCK` accounts may access `clockapi` and `timesheet` methods, and `TIMESHEET` accounts can only access `timesheet` methods.

### Password Encoding

The backend supports multiple encoding algorithms through the use of a [DelegatingPasswordEncoder](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/password/DelegatingPasswordEncoder.html). When specifying passwords in application properties, specify the algorithm ID in braces (`{}`) before the encoded form of the password like so:`{bcrypt}$2a$10$DdkI8AiFilpO2b9xKDNaVOhavN4bF.8m557X5MYoJLDD/4yo/vBz6`. The backend supports multiple hashing algorithms; however, `bcrypt` is recommended.

## Database

The backend stores data by connecting to a [MongoDB](https://www.mongodb.com/) instance through [Spring Data MongoDB](https://projects.spring.io/spring-data-mongodb/). As such, with proper configuration, it can connect to a local instance (default), a remote instance over TCP, or a cloud-based solution via MongoDB Atlas. (Consult the Spring docs for details.)

The name of the database used is set during configuration.

### Schema

#### User Info

User info is stored in a collection called `users`.

Each user document contains the following fields:

- `_id`: The ID of the user, as a string
- `name`: The full name of the user, as a string
- `email`: The email address of the user, as a string. This is used by the auto-logout feature.
- `_class`: This field is used internally by Spring Data. It should always be set to the string `"org.team1540.cluck.backend.data.User"`.
- `clockEvents`: A list of clock event (clock-in or clock-out) records. Each clock event document contains the following fields:
  - `timestamp`: A signed 64-bit integer containing the system time of the clock event, in milliseconds from the Unix epoch.
  - `clockingIn`: A boolean specifying whether the event was a clock-in (`true`) or a clock-out (`false`).
- `lastEvent`: The time of the last clock event. This is stored for caching purposes; if you are editing the user object manually, delete it to avoid errors.
- `inNow`: Whether the user is currently clocked in. This is stored for caching purposes; if you are editing the user object manually, delete it to avoid errors.

#### Credential Info

Credentials (accounts) are stored in a collection called `credentials`.

Each credential document contains the following fields:

* `_id`: The username of the credential.
* `password`: The encoded (hashed) password of the credential.
* `accessLevel`: The access level of the credential (`TIMECLOCK`, `TIMESHEET`, or `ADMIN`).
* `_class`:This field is used internally by Spring Data. It should always be set to the string `"org.team1540.cluck.backend.data.Credential"`.

#### Analytics

The backend stores an event history in a collection called `analyticsEvents`, if you're into that sort of thing.

Each analytics event contains the following fields:

- `timestamp`: The time of the event, in milliseconds from the UNIX epoch.
- `user`: The username (i.e. credential) causing this event; i.e. the username used to authenticate to the API when making the logged request. Can be blank if the record is of an automated action (e.g. `outstanding_login`)
- `description`: A description of the event. Current descriptions are as follows:
  - `clock_in`: A user clocked in.
  - `clock_in_failed_not_found`: A user attempted to clock in, but they were not found in the user database/entered their ID incorrectly.
  - `clock_in_failed_repeat_clock`: A user attempted to clock in, but was already clocked in beforehand.
  - `clock_out`: A user clocked out.
  - `clock_out_failed_not_found`: A user attempted to clock out, but they were not found in the user database/entered their ID incorrectly.
  - `clock_out_failed_repeat_clock`: A user attempted to clock out, but was already clocked out beforehand/never clocked in in the first place.
  - `outstanding_login`: A user failed to clock out and was clocked out automatically by the auto-logout system.

### Time Cache

The backend uses an additional collection called `timecache` to cache certain data. All you need to know is  that you shouldn't touch it.

## Spring Configuration Properties

The backend has [Jasypt](https://github.com/ulisesbocchio/jasypt-spring-boot) as a dependency and can use it for encrypted storage of application properties.

- `cluck.sheets.app-name`: The "app name" to use when editing the hours spreadsheet.
- `cluck.sheets.sheet`: The spreadsheet ID to update with hours.
- `cluck.sheets.service-file`: A path to the service account key used to edit the timesheet.
- `cluck.sheets.name-range`: The sheet range (e.g. A1:A20) containing the names of users.
- `cluck.sheets.hours-col`:  The column of the spreadsheet containing the hours: the backend will update this column.
- `cluck.sheets.hours-row-offset`: The row at which hour record cells start. This is to accomodate spreadsheets with header cells. For instance, if your sheet has two rows of header cells, then this would be 3.
- `cluck.sheets.logged-in-sheet-name`: The name of the spreadsheet sheet to put logged-in user data on.
- `cluck.auth.admin-password`: The password for the root admin account. This should be encoded as described in the Authentication section.
- `cluck.autologout.enabled`: Whether to enable the autologout feature. Defaults to `true`.
- `cluck.email.email-from`: The email that autologout emails should be sent from. Defaults to `"CLUCK" <cluck@example.com>`.
- `cluck.email.email-reply-to`: The reply-to email for autologout emails. Defaults to `cluck@example.com`.
