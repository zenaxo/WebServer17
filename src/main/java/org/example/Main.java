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
/*TESTer*/
    // Run the server on port 5555
    private static final int PORT = 5555;

    // Directory for HTML files
    private static final Path BASE_DIRECTORY = Paths.get("www");

    // Images
    private static final Path IMAGES_DIRECTORY = BASE_DIRECTORY.resolve("images");

    // Other assets
    private static final Path MISC_DIRECTORY = BASE_DIRECTORY.resolve("misc");

    private static final String[] SUPPORTED_CONTENT_TYPES = { "text/plain", "text/html", "image/png" };

    // debug.json info
    private static final String SERVER_NAME = "Java Webserver 1.0";
    private static final String[] OWNERS = {
            "Hannes Sjölander, (id21hsr@cs.umu.se)",
            "Napat Wattanputtakorn, (dv22nwn@cs.umu.se)"
    };
    private static final AtomicInteger requestCounter = new AtomicInteger(0);
    private static final Instant START_TIME = Instant.now();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }


    /**
     * @param clientSocket a socket responsible for the connection
     */
    private static void handleClient(Socket clientSocket) {
        try (clientSocket) {
            // Store and read the client's input
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }

            // Parse and split the input because we want the GET request
            String request = requestBuilder.toString();
            String[] requestLines = request.split("\r\n");
            String[] requestLine = requestLines[0].split(" ");
            String method = requestLine[0];
            String path = requestLine[1];

            requestCounter.incrementAndGet();

            // Assert that the request is a GET request...
            if ("GET".equals(method)) {
                handleGetRequest(clientSocket, path);
            }
            // Otherwise, return an error to the client...
            else {
                sendResponse(clientSocket, "405 Method Not Allowed", "text/plain", "Method Not Allowed".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleGetRequest(Socket clientSocket, String path) throws IOException {
        if ("/debug".equals(path)) {

            // Create the JSON object for the debug path
            long uptimeSeconds = Instant.now().getEpochSecond() - START_TIME.getEpochSecond();
            JSONObject data = new JSONObject();
            data.put("name", SERVER_NAME);
            data.put("connections", requestCounter.get());
            data.put("uptime", uptimeSeconds);
            data.put("owners", OWNERS);

            // Return the JSON object
            sendResponse(clientSocket, "200 OK", "application/json", data.toString().getBytes());

        }
        // Handle requests for images
        else if (path.startsWith("/assets/")) {
            String assetName = path.substring("/assets/".length());
            Path filePath = IMAGES_DIRECTORY.resolve(assetName);

            // Check that the image exists
            if (Files.exists(filePath)) {
                String contentType = guessContentType(filePath);
                // And that it is of a supported type...
                if (isSupportedContentType(contentType)) {
                    // Return the image
                    sendResponse(clientSocket, "200 OK", contentType, Files.readAllBytes(filePath));
                } else {
                    sendResponse(clientSocket, "415 Unsupported Media Type", "text/plain", "Unsupported file type".getBytes());
                }
            } else {
                byte[] notFoundContent = "<h1>404 Not Found</h1>".getBytes();
                sendResponse(clientSocket, "404 Not Found", "text/html", notFoundContent);
            }
        }
        // Handle requests for other assets (.txt)...
        else if (path.startsWith("/misc/")) {
            String miscFileName = path.substring("/misc/".length());
            Path filePath = MISC_DIRECTORY.resolve(miscFileName);
            if(Files.exists(filePath)) {
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
        // Return html files...
        else {
            Path filePath = getFilePath(path);
            if (Files.exists(filePath)) {
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
    }

    /**
     *
     * @param clientSocket
     * @param status
     * @param contentType
     * @param content
     * @throws IOException
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

    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        return BASE_DIRECTORY.resolve(path.substring(1));
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    private static boolean isSupportedContentType(String contentType) {
        for (String supportedType : SUPPORTED_CONTENT_TYPES) {
            if (supportedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
}
