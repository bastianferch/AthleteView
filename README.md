# Introduction
This is a software project for the course "Advanced Software Engineering" at the TU Wien. 
The goal of this project is to develop a web application for athletes to track their training progress and to schedule their activities with the constraints of the trainer and athlete. 
The application is developed in a team of 7 students. More information about the project and its development is stored in the [wiki](https://github.com/bastianferch/AthleteView/wiki).

# Contributors
- Drucker Florian
- Ferch Bastian
- Dmytro Kondrashov
- [Stöger Stephan](https://github.com/stoger)
- [Strasser Michael](https://github.com/Megalokom)
- [Watzinger Sebastian](https://github.com/Nyzabes)
- Winkler Michael

# Athlete View Application startup

## Frontend
- navigate into the `frontend` folder
- run `npm install`
- run `npm run start` (or `ng serve`)

## Garmin Mock Api
- navigate into the `garmin-mock` folder
- run `npm install`
- run `node api.js`

## Backend
### Databases
Navigate into the `backend` folder.
There is a `docker-compose.yml` which configures the databases and networks etc. needed.

To build and start the containers, use `docker-compose up --build -d`.
The databases (postgres/mongodb) have to be started for production run before the application is started!

### Worker
Navigate into the `worker` folder.
Run the following command: `./bin/build.sh` to build the image.
Activate the `local-worker` profile in IntelliJ (like `datagen`) or append the profile to your application startup command.
This will automatically start 1 worker with a working configuration for you on backend startup.
Please note that your docker has to have the option 'Expose daemon on tcp://localhost:2375 without TLS' enabled in the settings for this to work.
The configuration can be changed in `backend/src/main/resources/application.yml`

### Application
- run `mvn clean install -DskipTests` to install all required maven dependencies
- run `mvn clean packagee` to generate a runnable JAR from the POM
- run `java -jar target/athlete-view-SNAPSHOT:1.0.0.jar` to start the backend serer

If `datagen` should be used, append `-Dspring.profiles.active=datagen` to the last command.

Alternatively, all the above mentioned steps can be executed in IntelliJ.
For this make sure to create a run configuration which specifies `AthleteViewApplication.kt` as its main class.
If needed, add `VM option` to set a spring profile and set it to `datagen` (or the desired profile).

### Email Service
You have to provide an email service and include the information in the corresponding file. The current keys in the file are not valid!


# Known issues
## docker-compose on windows
- use WSL instead of powershell to run `docker-compose up`!
In powershell, the database-init script is not run properly thus leading to authentication issues with mongodb.
If an AuthenticationError is returned by mongo (and shown in the backend's logs), then run the following:
- `docker ps -a` and find `mongo`-container in the output (specifically its `id`)
- `docker exec -it <mongo-container-id> mongosh`
- run `use athlete_view;` in mongo shell
- paste the contents of `backend/db_init/mongo/mongo-init.js` into the console and hit enter
Now the user should be created, the command should return `{ 'ok': 1 }`.

If another error occurs, i.e. the user does not have rights to create a new user, go ahead and stop the containers before deleting th docker volumes using `docker volume prune`
Afterwards, try restarting the containers - make sure to specify `--build` and hope it now works :)

## `no main attribute in manifest`
Currently, `mvn clean package` does not compile a runnable JAR.
Therefore, at the moment, the backend has to be started from IntelliJ (see above for "HowTo")
