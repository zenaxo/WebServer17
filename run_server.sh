#!/bin/bash
# Compile the program
mvn compile

read -p "Do you want to use a specific port: (y/n)" input

if [[ "$input" == "Y" || "$input" == "y" ]]; then
  read -p "Enter port number: " PORT
  mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="-p $PORT"
else
  # Run the server with default port
  mvn exec:java -Dexec.mainClass="org.example.Main"
fi
