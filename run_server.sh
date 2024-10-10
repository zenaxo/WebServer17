#!/bin/bash
# Compile the program
mvn compile

# Run the server
mvn exec:java -Dexec.mainClass="org.example.Main"