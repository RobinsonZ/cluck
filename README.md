# ChickenClock Backend

The backend is the brains of the ChickenClock system. It is intended to have one instance running on a central server used by all timeclocks. This central server can be on-premises or hosted in the cloud (though marginally more security configuration may be necessary if the server is to be exposed to the open internet).

## Running The Backend

The backend is packaged as a .jar file which can be executed on the server. It requires several additional pieces of configuration. You should run the jar with a file called `application.yml` in your working directory looking something like this (with different passwords and such, of course):

```yaml
server:
  port: 443 # Port for web requests. 443 is the default port for HTTPS
  ssl:
    key-store: keystore.p12
    key-store-password: robots
    key-store-type: PKCS12
    key-alias: timekey

spring:
  mail: # These properties may be different depending on your mail server.
    host: smtp.gmail.com
    port: 587
    username: mail@gmail.com
    password: robots
    properties:
      mail.smtp:
        auth: true
        starttls.enable: true
  data:
    mongodb: # may look very different depending on your authentication configuration and where your database is running
      database: timeclockdb
      authentication-database: timeclockdb 
      username: timeclock
      password: robots

timeclock:
  sheets:
    appName: My App
    sheet: 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms
    serviceFile: timeclock-c2dcf0a2fba7.json
    nameRange: A:A
    hoursCol: B
    hoursRowOffset: 1
  auth: # Usernames/passwords for accessing the server's API.
    timeclockUsername: timeclock
    timeclockPassword: d34nk4m3n4evr
    secureTimesheetApi: true # if true, the timesheet API will require authentication as well as the timeclock API. Set to true if you're running this in the cloud, or just don't want anyone to know who's logged in.
    timesheetUsername: timesheet
    timesheetPassword: d413y0cum
```



### Configuration

#### SSL

SSL must be configured with a certificate. See [this guide](https://drissamri.be/blog/java/enable-https-in-spring-boot/) up until step 2. Since you should be executing this as a .jar the keystore file won't be on the classpath but instead a regular file.

#### External Service Settings
The backend connects to several different web services to perform its functions, each of which need their own credentials set via application properties.

* Google Sheets (via a service account)
  * `timeclock.sheets.appname`: The name of the Google Sheets app. No idea what this does but you probably want to set it.
  * `timeclock.sheets.sheet`: The sheet ID of the sheet you want to update with people's hours. You can find this from the sheet URL: for example, the URL `https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit#gid=0` translates to a sheet id of `1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms`.
  * `timeclock.sheets.serviceFile`: The JSON file containing the details for your service account. 
  * `timeclock.sheets.nameRange`: The Sheets range with the "name" column of the google sheet. A range looks like `A:A` or `B2:B35`.
  * `timeclock.sheets.hoursCol`: The column number where the app should put the hours figures.
  * `timeclock.sheets.hoursRowOffset`: How many rows down the "hours" area starts, plus one. For example, if your spreadsheet has two header cells before the hours figures start, the offset should be 3.
* An email server (via SMTP)
  * Authentication must be configured in the `spring.mail` properties (see [this guide](http://www.baeldung.com/spring-email))

### MongoDB

This app requires a MongoDB Community Edition server, which can be downloaded [here](https://www.mongodb.com/download-center?jmp=nav#community). See the [documentation](https://docs.mongodb.com/manual/) for how to install and run the server.

Spring Data MongoDB must be configured: see [this guide](https://springframework.guru/configuring-spring-boot-for-mongo/) (specifically the section "Configuration Properties").

## Known Issues

* If the server recieves more than one clock-out request for the same user in the span of a few milliseconds, it is possible for two clock-out records to be written to the database a few ms apart. Low-priority issue as there's almost no way this would happen short of a DDoS attack from an authenticated timeclock somehow, and other components of the infrastructure handle it gracefully and will take the earlier record anyway.
