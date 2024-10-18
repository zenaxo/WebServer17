#!/bin/bash
# Compile the program
mvn compile

# Ask user if they want to use a default port number(5555) or specific port number
read -p "Do you want to use a specific port: (y/n): " input

if [[ "$input" == "Y" || "$input" == "y" ]]; then
  # Read user input port number
  read -p "Enter port number: " PORT
  # Run the server with the specific port
  mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="-p $PORT"
else
  # Run the server with default port
  mvn exec:java -Dexec.mainClass="org.example.Main"
fi
