# A simple Java Web Server
A simple Java Web Server capable of handling GET-requests
Can serve HTML, images, JSON and text files

## How to run the server?
1. Run **chmod +x silent.sh** to give access to the bash file
2. Start the server with the command **./silent.sh or ./silent.sh -p PORT**

## How do I test the connection to the server?
1. Run **curl localhost:PORT/debug**

## How to stop the server?
1. Locate the process with the following command **ps aux | grep java**
2. Output will be similar to *user* **12345** *0.5 5.0 123456 12345 ...*
3. Kill the process with the command **kill 12345**

