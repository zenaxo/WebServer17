# A simple Java Web Server
This is a basic Java Web Server capable of handling GET-requests.
The server can serve HTML, images, JSON, and text files.
Includes a `/debug` endpoint to return server information in JSON format.

## How to run the server?
To start the server, use the **./run_server.sh** script. This scipt complies the code and asks whether you would like to specify a port or use default port(5555).
1. Make the script executable by running the command: **chmod +x run_server.sh**.
2. Start the server with the command **./run_server.sh**
3. The script will ask **Do you want to use a specific port: (y/n):**

   3.1. To run the default port, simple answer "n" or "N".

   3.2. To specify a custom port, answer "y" and provide the port number.

## How to run the server?
To start the client and connect to the server, use the run_client.sh script. 
1. Make the script executable by running the command: **chmod +x run_client.sh**.
2. Start the server with the command **./run_client.sh**

## How do I test the connection to the server?
1. Run **curl localhost:PORT/debug** to check the server's debug information.
   Replace `PORT` with the actual port number (5555 by default or your specified port).

## How to stop the server?
1. Locate the process with the following command **ps aux | grep java**
2. Output will be similar to *user* **12345** *0.5 5.0 123456 12345 ...*
3. Kill the process with the command **kill 12345**

