# CLUCK Backend Manual

This is a start-to-finish manual on how to set up the CLUCK backend. It is assumed that you have some basic knowledge of networking and CLI usage.

CLUCK is OS-independent and runs anywhere Java and MongoDB run, but many commands presented will only work on *NIX systems. For Windows, you're on your own for command-line tools although the rest should work.

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
      file: logs/cluck.log
    ```

    This will log all events to the `logs/` directory. Spring will auto-compress logs for each day by default resulting in a large number of ZIP files, so it is recommended to log events to a separate directory.

3. Configure admin password (required):

    Add to `application.yml`:

    ```yaml
    cluck:
      auth:
        admin-password: {bcrypt}$2y$12$/mCPS60bUoyo7.C2JrjpWOxQCBYDOhBW/Y2qAA0B2Wc8NCj5Trpgm
    ```

    These credentials will be necessary for the admin account to log in and create timeclock and timesheet accounts.

    Note that the password is not actually the password but an encoded form. See the Configuring Passwords section of this manual for more info.

4. Disable scheduled log-outs (if desired)

    By default, at 11:59:59 PM system time, CLUCK will log out any logged-in users and send them an email notifying them that they forgot to log out. This can be disabled if desired.

    Under `cluck`, add:

    ```yaml
    autologout.enabled: false
    ```

### Configuring Passwords

For security reasons, the backend does not store passwords; it instead stores an encoded form. When setting the admin password, you should encode it with BCrypt. Go to [this website](https://bcrypt-generator.com/) and enter your desired password into the "String to encrypt" box. Add `{bcrypt}` to the beginning of the resulting string of gibberish and paste it where you'd put the password.
### Configuring SSL/HTTPS

It is **strongly** recommended to configure SSL/HTTPS so that your credentials are encrypted. SSL and HTTPS require a certificate to prove that you are who you say you are. For now, we'll use a self-signed cert; if you want to use a Let's Encrypt certificate a guide can be found [here](https://dzone.com/articles/spring-boot-secured-by-lets-encrypt).

#### Using a Self-Signed Certificate

Self-signed certificates are much simpler but may cause problems in your client tools (usually these can be fixed by turning off certificate verification). 

1. Generate a certificate using the `keytool` program (this is installed with the JRE and should be on any computer with Java installed):

   ```bash
   keytool -genkey -alias cluck -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
   ```

   This should be run in the same directory as your CLUCK backend JAR. You'll be asked for a keystore password, so enter one. You'll then be presented with a series of prompts; just hit enter until you get to one asking if the information you entered is correct. Then type `yes` and hit enter.

1. Under the `server` section in your `application.yml`, add these options:

   ```yaml
   ssl:
     key-store: keystore.p12
     key-store-password: password
     key-store-type: PKCS12
     key-alias: cluck
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
       key-alias: cluck
   
   # other options
   ```

## Installing and Configuring Dependencies

### MongoDB

The main dependency of the backend is MongoDB, which is required for data storage and persistence. It is recommended to simply install the Community Edition on your computer. Simply follow the instructions [here](https://docs.mongodb.com/manual/administration/install-community/) for your OS.

If you install and run MongoDB with no authentication (the default setting) on the same computer as the cluck backend is running on, configuration is fairly simple. In your `application.yml`, add this:

```yaml
spring:
  data.mongodb:
    database: cluck
```

The database name can be whatever you want.

### Google Sheets

The backend uses Google Sheets to let users easily view their hour counts.

#### Setting Up a Google Sheet

1. instructions on how to get a service account key go here
2. Once you have a service account key file (with a name that looks something like `foo-c2dcf0a2fba7.json`), copy it into the same directory with the jar. 
3. Create a new Google Sheet. Note the long string of numbers and letters in the URL; this is the spreadsheet ID. For example, if the URL is `https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit#gid=0`, the ID is `1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms`.
4. Make sure that you have one column containing all of your user's names (exactly as they are when you enter them into the backend). This column can also contain headers and such, as long as the names are in one block. Make a note of the spreadsheet range (e.g. A1:A20) occupied by the names. 
5. Figure out your row offset–the row number where the names start.
6. Decide which column you want the backend to spit out hour counts into, and note its column letter. Note that the backend puts number in un-rounded, so you'll want to set up Google Sheets to display the numbers in that column to only a few decimal places.

Add the following lines under the `cluck` section in your `application.yml`:

```yaml
cluck:
  # <auth configuration>
  sheets:
    appName: Timeclock # pretty sure this bit does nothing
    serviceFile: <name of service key file from step 2>
    sheet: <sheet ID from step 3>
    nameRange: <spreadsheet range from step 4>
    hoursRowOffset: <row offset from step 5>
    hoursCol: <column letter from step 6>
```

### SMTP Server

Unless you disabled the autologout feature in Configuring the Server step 4, the backend requires an SMTP server to be configured to send mail from. 

Create an email account with a hosting provider of your choice. Consult their documentation (google "\<provider name\> SMTP server") and figure out the address/host, port, and whether TLS is required (if in doubt, it probably is).

Then add the following entries to your `application.yml` under the `spring` section like so:

```yaml
spring:
  # <data configuration>
  mail:
    host: <your host, e.g. smtp.gmail.com>
    port: <your port, e.g. 587>
    username: <your account username>
    password: <your account password>
    properties:
      mail.smtp:
        auth: true
        starttls.enable: <true if TLS is required, false otherwise>
```

When everything is said and done, your `application.yml` should look something like this:

```yaml
server:
  port: 8443
  ssl:
    key-store: keystore.p12
    key-store-password: password
    key-store-type: PKCS12
    key-alias: cluck
    
logging:
  file: logs/cluck.log
  
spring:
  data.mongodb:
    database: cluck
  mail:
    host: <your host, e.g. smtp.gmail.com>
    port: <your port, e.g. 587>
    username: <your account username>
    password: <your account password>
    properties:
      mail.smtp:
        auth: true
        starttls.enable: <true if TLS is required, false otherwise>
cluck:
  auth:
    admin-password: {bcrypt}$2y$12$/mCPS60bUoyo7.C2JrjpWOxQCBYDOhBW/Y2qAA0B2Wc8NCj5Trpgm
  sheets:
    appName: Timeclock # pretty sure this bit does nothing
    serviceFile: <name of service key file from step 2>
    sheet: <sheet ID from step 3>
    nameRange: <spreadsheet range from step 4>
    hoursRowOffset: <row offset from step 5>
    hoursCol: <column letter from step 6>
```

## Starting the Backend

To start the backend, open a terminal, `cd` to the directory containing the JAR, and type:

```
java -jar backend.jar
```

If you see a message saying "Started TimeClockBackendKt in X seconds (JVM running for Y)", you've successfully configured the backend! Now you can open up the admin client and configure users and credentials.
