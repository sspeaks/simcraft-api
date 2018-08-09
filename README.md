**WIP - Simulation Craft web REST api**

`!The project development is in progress!`

Web REST API for simc project (https://github.com/simulationcraft/simc)

To build project run

    docker build -t simcraft_web_api .
    
This will build a docker image with Apache Tomcat and copy the application.  During the docker image building process 
docker will download and compile latest version of simc repository which is about 1GB+ size, so it may take time.

After building run container

    docker run -d -p 8080:8080 simcraft_web_api 

The application will be available on http://localhost:8080/simcraft-api

**Working with service**  

***Single calculation***

To launch simulation for single character with response waiting send GET query to: 

    http://localhost:8080/simcraft-api/simulate
    
or for async simulation with no waiting to:

    http://localhost:8080/simcraft-api/simulate/async

with parameters
    
    zone = REALM_ZONE - us, eu, etc.
    realm = REALM_NAME - realm name from inner Blizzard realms list, i.e. "Борейская тундра" is borean-tundra
    character = CHARACTER_NAME - name of character to cimulate
    type = html/json - type of result - in json ot html view from simc
    pawn = true/false - needed or not scaling fators (weights) for Pawn import !! IMPORTANT !! will take MUCH longer time to calculate
    iterations = NUMBER_OF_ITERATIONS - number of iteration to process in simc (could be empty, 1000 by dafault)
    
example query
    
    http://localhost:8080/simcraft-api/simulate?zone=eu&realm=borean-tundra&character=MyAwsomeCharacterE&type=html&pawn=false&iterations=1000
    
There are 2 types of execution: sync and async. While sync returns the result right after sending the query which might take a lot of time,
async implementation launches calculation in different thread which allows to continue execution without waiting for response.

Result for sync will be JSON or HTML - depends on input parameters

For async version GET, i.e.

    http://localhost:8080/simcraft-api/simulate/async?zone=eu&realm=borean-tundra&character=MyAwsomeCharacterE&type=html&pawn=false&iterations=1000
    
will return UUID of calculation, i.e.

    8ad30cf1-50f7-4239-a201-2bd164a12f9d

which should be used to retrieve calculation result. To get the result send GET query to 

    http://localhost:8080/simcraft-api/simulate/async/result
with following parameters

    uuid = UUID - unique identifier from simulation call, in this example - 8ad30cf1-50f7-4239-a201-2bd164a12f9d
    type = html/json - type of result - in json ot html view from simc
    delete = true/false - should the service delete results after response or not 
    
Note* - undeleted results will remain on server

While simulation is in progress `/result` will return `"Simulation uuid=UUID is in progress"`

When simulation results are ready - it will return JSON or HTML, depending on input `type` parameter



