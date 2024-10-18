#!/bin/bash
# Compile the program
mvn compile
# Run the server with default port
mvn exec:java -Dexec.mainClass="org.example.Client"

