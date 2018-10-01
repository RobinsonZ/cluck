#!/usr/bin/env sh

# A simple script to deploy the app
# kills the old process on the server and copies the new one over before the server restarts it

# exit on first error, print executed lines to the console
set -ev

ssh 1540 'kill `cat ~/cluck/cluck.pid`'
sleep 3 # wait a bit for the process to shut down
rsync -v build/libs/time-server-0.0.1-SNAPSHOT.jar 1540:cluck/cluck.jar
