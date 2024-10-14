#!/bin/bash

# Default port (if none is provided)
PORT=5555

# Function to check if the provided port is a valid integer between 1000 and 99999
validate_port() {
  if [[ "$1" =~ ^[0-9]{4,5}$ ]] && [ "$1" -ge 1000 ] && [ "$1" -le 99999 ]; then
    return 0  # Valid port
  else
    return 1  # Invalid port
  fi
}

# Parse command line arguments
while getopts ":p:" opt; do
  case $opt in
    p) 
      if validate_port "$OPTARG"; then
        PORT="$OPTARG"
      else
        echo "Invalid port number. Please provide a port number between 1000 and 99999."
        exit 1
      fi
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
        echo "Usage: ./run -p <port>"
        exit 1
    ;;
    :) echo "Option -$OPTARG requires an argument." >&2
       echo "Usage: ./run -p <port>"
       exit 1
    ;;
  esac
done

# Compile the project
mvn compile

# Run the Java application with the specified port
mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="-p $PORT" > output.log 2>&1 &

# Inform the user that the server has started with the given port
echo "Server started on port $PORT, logs can be found in output.log"

