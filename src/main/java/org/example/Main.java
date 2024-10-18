package org.example;

import org.json.JSONObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    /** A port number for running a server */
    //private static final int PORT = 42069;
    /** Base directory where HTML files are located */
    private static final Path BASE_DIRECTORY = Paths.get("www");
    /** The directory for image files */
    private static final Path IMAGES_DIRECTORY = BASE_DIRECTORY.resolve("images");
    /** The directory for miscellaneous files */
    private static final Path MISC_DIRECTORY = BASE_DIRECTORY.resolve("misc");
    /** Supported content types for the server */
    private static final String[] SUPPORTED_CONTENT_TYPES = { "text/plain", "text/html", "image/png" };
    /** Server name for the debug information */
    private static final String SERVER_NAME = "Java Webserver 1.0";
    /** Owners of the server, using for the debug */
    private static final String[] OWNERS = {
            "Hannes Sjölander, (id21hsr@cs.umu.se)",
            "Napat Wattanputtakorn, (dv22nwn@cs.umu.se)"
    };
    /** Counter to track the number of client requests received */
    private static final AtomicInteger requestCounter = new AtomicInteger(0);
    /** The start time of the server */
    private static final Instant START_TIME = Instant.now();

    public static void main(String[] args){
	// Set a default port if not port is passed an argument
	int port = 5555;

	for (int i = 0; i < args.length; i++) {
	    if ("-p".equals(args[i]) && i + 1 < args.length) {
		try {
		   port = Integer.parseInt(args[i + 1]);
		} catch (NumberFormatException e) {
		   System.out.println("Invalid port number after -p. Using default port " + port);
		}
	    }
	}

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }


    /**
     * The method handles a client connection.
     * It reads the HTTP request and checks if it's a GET request.
     * If the request is not GET, returns an error.
     *
     * @param clientSocket  A socket responsible for the connection
     */
    private static void handleClient(Socket clientSocket) {
        try (clientSocket) {
            /* Store and read the client's input */
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }
            String request = requestBuilder.toString();

            /* Parse the request line because we want the GET request */
            /* Ex. GET /index.html HTTP/1.1, Host: localhost, Connection: keep-alive,... */
            String[] requestLines = request.split("\r\n");
            /* Split request line into method, path, and HTTP version*/
            /* GET, /index.html, HTTP/1.1 */
            String[] requestLine = requestLines[0].split(" ");

            String method = requestLine[0]; /* e.g., GET */
            String path = requestLine[1];   /* e.g., /index.html*/

            /* Increase the number of received request */
            requestCounter.incrementAndGet();

            /* Assert that the request is a GET request and path is not empty */
            if ("GET".equals(method) && path != null) {
                handleGetRequest(clientSocket, path);
            }
            else {    /* Otherwise, return an error to the client... */
                sendResponse(clientSocket, "405 Method Not Allowed", "text/plain", "Method Not Allowed".getBytes());
            }
        } catch (IOException e) {
            System.out.println("Client handler error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The method serves the files (HTML, images, or other assets), including server debug information.
     *
     * @param clientSocket  A socket responsible for the connection
     * @param path          The requested path from the HTTP GET request
     * @throws IOException  If an I/O error occurs while handling the request.
     */
    private static void handleGetRequest(Socket clientSocket, String path) throws IOException {

        if ("/debug".equals(path)) {

            /* Create the JSON object for the debug path */
            long uptimeSeconds = Instant.now().getEpochSecond() - START_TIME.getEpochSecond();
            JSONObject data = new JSONObject();
            data.put("name", SERVER_NAME);
            data.put("connections", requestCounter.get());
            data.put("uptime", uptimeSeconds);
            data.put("owners", OWNERS);

            /* Return the JSON object */
            sendResponse(clientSocket, "200 OK", "application/json", data.toString().getBytes());
        }
        /* Handle requests for images */
        else if (path.endsWith(".png")) {
            /*Extracts the file */
            String assetName = path.substring("/assets/".length()); /* Ex. extract image.png */
            Path filePath = IMAGES_DIRECTORY.resolve(assetName);    /* Resolves the full path to /misc/text.txt */
            handleFileRequest(clientSocket, filePath);
        }
        /* Handle requests for other assets (.txt) */
        else if (path.endsWith(".txt")) {
            String miscFileName = path.substring("/misc/".length());
            Path filePath = MISC_DIRECTORY.resolve(miscFileName);
            handleFileRequest(clientSocket, filePath);
        }
        /* Handle html files or specific path that lead to files */
        else {
            Path filePath = getFilePath(path);
            handleFileRequest(clientSocket, filePath);
        }
    }

    /**
     * The method handles file requests from the client by checking if the requested file exists,
     * determining its content type, and sending the appropriate response.
     *
     * @param clientSocket  A socket responsible for the connection
     * @param filePath      A path to the requested file
     * @throws IOException  If an I/O error occurs while handling the request.
     */
    private static void handleFileRequest(Socket clientSocket, Path filePath) throws IOException {
        /* Check if file is exist and not a directory*/
        if(Files.exists(filePath) && !Files.isDirectory(filePath)) {
            String contentType = guessContentType(filePath);
            if (isSupportedContentType(contentType)) {
                sendResponse(clientSocket, "200 OK", contentType, Files.readAllBytes(filePath));
            } else {
                sendResponse(clientSocket, "415 Unsupported Media Type", "text/plain", "Unsupported file type".getBytes());
            }
        } else {
            byte[] notFoundContent = "<h1>404 Not Found</h1>".getBytes();
            sendResponse(clientSocket, "404 Not Found", "text/html", notFoundContent);
        }
    }

    /**
     * Sends an HTTP response to the client with the given status, content type and content.
     *
     * @param clientSocket  A socket responsible for the connection
     * @param status        AN HTTP status code and message
     * @param contentType   A type of the content
     * @param content       A byte array representing the response body
     * @throws IOException  If an I/O error occurs while writing the response
     */
    private static void sendResponse(Socket clientSocket, String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        clientOutput.write(("Content-Type: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
    }

    /**
     * Resolves the requested path to a file on the server. If the root path "/" is
     * requested, it defaults to serving "/index.html".
     *
     * @param path      The requested path form the HTTP request
     * @return          A path to the corresponding file
     */
    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        return BASE_DIRECTORY.resolve(path.substring(1));
    }

    /**
     * The method is used to guess the content type of the requested file.
     *
     * @param filePath      A path to the file
     * @return              The guessed content type as a string
     * @throws IOException  If an I/O error occurs while determining the content type
     */
    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    /**
     * Checks if the given content type is supported by the server.
     *
     * @param contentType   A type of the file
     * @return              True if the server support content type, otherwise false
     */
    private static boolean isSupportedContentType(String contentType) {
        for (String supportedType : SUPPORTED_CONTENT_TYPES) {
            if (supportedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
}
