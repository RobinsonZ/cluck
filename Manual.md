# Timeclock Backend Manual

This is a start-to-finish manual on how to set up the timeclock backend. It is assumed that you have some basic knowledge of networking and CLI usage.

The backend is OS-independent and runs anywhere Java and MongoDB run, but many commands presented will only work on *NIX systems. For Windows, you're on your own for command-line tools although the rest should work.

## Installing the Backend

1. Download the latest JAR from the releases page and place it in a directory.
2. Create a text file in that directory named `application.yml`. This will be used for configuration.

### Configuring the Server

1. Configure the server port (required).

    Add these options in your `application.yml`:

    ```yaml
    server:
      port: 8443
    ```
    `port` can be anything as long as you don't conflict with other services.

2. Configure logging (optional).

    Add this in your `application.yml`:

    ```yaml
    logging:
      file: logs/timeclock.log
    ```

    This will log all events to the `logs/` directory. Spring will auto-compress logs for each day by default resulting in a large number of ZIP files, so it is recommended to log events to a separate directory.

3. Configure timeclock authentication (required):

    Add to `application.yml`:

    ```yaml
    timeclock:
      auth:
        timeclock-username: timeclock
        timeclock-password: {bcrypt}$2y$12$/mCPS60bUoyo7.C2JrjpWOxQCBYDOhBW/Y2qAA0B2Wc8NCj5Trpgm
    ```

    These credentials will be necessary for timeclock clients (anyone accessing API methods under ``/clockapi/``) to log in.

    Note that the password is not actually the password but an encoded form. See the Configuring Passwords section of this manual for more info.

4. Configure timesheet authentication (optional, recomended for publicly exposed servers)

    If you don't want people to be able to access data about who is logged in without a password, that can be configured here. Under the `auth` section, add these options:

    ```yaml
    secure-timesheet-api: true
    timesheet-username: timesheet
    timesheet-password: {bcrypt}$2y$12$/mCPS60bUoyo7.C2JrjpWOxQCBYDOhBW/Y2qAA0B2Wc8NCj5Trpgm
    ```

    If configured, timesheet clients (anyone accessing API methods under `/timesheet/`) will need to authenticate with those credentials.

5. Disable scheduled log-outs (if desired)

    By default, at 11:59:59 PM local time, the timeclock will log out any logged-in users and send them an email notifying them that they forgot to log out. This can be disabled if desired.

    Under `timeclock`, add:

    ```yaml
    autologout.enabled: false
    ```

### Configuring SSL/HTTPS

It is **strongly** recommended to configure SSL/HTTPS so that the timeclock credentials are encrypted. SSL and HTTPS require a certificate to prove that you are who you say you are. For now, we'll use a self-signed cert; if you want to use a Let's Encrypt certificate a guide can be found [here](https://dzone.com/articles/spring-boot-secured-by-lets-encrypt).

#### Using a Self-Signed Certificate

Self-signed certificates are much simpler but may cause problems in your client tools (usually these can be fixed by turning off certificate verification). 

1. Generate a certificate using the `keytool` program (this is installed with the JRE and should be on any computer with Java installed):

   ```bash
   keytool -genkey -alias timeclock -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
   ```

   This should be run in the same directory as your timeclock JAR. You'll be asked for a keystore password, so enter one. You'll then be presented with a series of prompts; just hit enter until you get to one asking if the information you entered is correct. Then type `yes` and hit enter.

1. Under the `server` section in your `application.yml`, add these options:

   ```yaml
   ssl:
     key-store: keystore.p12
     key-store-password: password
     key-store-type: PKCS12
     key-alias: timeclock
   ```

   `key-store-password` should be the password you entered in the previous step.

1. Your `application.yml` should now look something like this, plus several other configuration options:

   ```yaml
   server:
     port: 8443
     ssl:
       key-store: keystore.p12
       key-store-password: password
       key-store-type: PKCS12
       key-alias: timeclock
   
   # other options
   ```

## Installing and/or Configuring Dependencies

### MongoDB

The main dependency of the backend is MongoDB, which is required for data storage and persistence. It is recommended to simply install the Community Edition on your computer. Simply follow the instructions [here](https://docs.mongodb.com/manual/administration/install-community/) for your OS.

If you install and run MongoDB with no authentication (the default setting) on the same computer as the timeclock backend is running on, configuration is fairly simple. In your `application.yml`, add this:

```yaml
spring:
  data.mongodb:
    database: timeclock
```

The database can be whatever you want, you'll just need it to configure users.