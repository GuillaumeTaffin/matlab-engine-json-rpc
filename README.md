# MATLAB Engine JSON RPC

This project aims to create a JSON RPC adapter in front of the MATLAB Engine API.

For now, it serves the API over websocket only, and need to be run with GRADLE.

## How to use it.

1. Clone this repo
2. Find the matlabroot location you want to use. For instance, on MACOS it is something like `/Applications/MATLAB_R2024a.app`.
3. From the folder of this repo, run `./gradlew run -Pmatlabroot=/Applications/MATLAB_R2024a.app` where you can put the path of step 2.
4. Connect to MATLAB via websocket at the URL `ws://localhost:9000/connect/?engineName=<share matlab session name>`. The engineName query param is optional. If provided, it will try to connect a currently running MATLAB instance that shares its engine with that name. If not, it will start a MATLAB instance and connect to it.
5. If everything is successful, you will receive the notification `{"jsonrpc": "2.0", "method": "connected", "params": {"message" : "Successfully connected to MATLAB."}}`

## Important notes

- The app need to run on the JAVA 11 JVM. The project should automatically find a corresponding version, but if you see logs/stacktrace mentioning a Java version you might need to check your setup.
- One websocket connection connects to one MATLAB at a time.