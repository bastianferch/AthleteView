# Athlete View Application startup
## Frontend
- navigate into the `frontend` folder
- run `npm install`
- run `npm run start` (or `ng serve`)

## Backend
### Databases
Navigate into the `backend` folder.
There is a `docker-compose.yml` which configures the databases and networks etc. needed.

To build and start the containers, use `docker-compose up --build -d`.
The databases (postgres/mongodb) have to be started for production run before the application is started!

### Application
- run `mvn clean install -DskipTests` to install all required maven dependencies
- run `mvn clean packagee` to generate a runnable JAR from the POM
- run `java -jar target/athlete-view-SNAPSHOT:1.0.0.jar` to start the backend serer

If `datagen` should be used, append `-Dspring.profiles.active=datagen` to the last command.

Alternatively, all the above mentioned steps can be executed in IntelliJ.
For this make sure to create a run configuration which specifies `AthleteViewApplication.kt` as its main class.
If needed, add `VM option` to set a spring profile and set it to `datagen` (or the desired profile).


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
