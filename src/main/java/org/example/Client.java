package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        /* Ask the user to input the resource path */
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the host (ex. localhost, itchy.cs.umu.se, etc): ");
        String host = scanner.nextLine();
        System.out.print("Enter port: ");
        int port = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter the resource path (ex. /debug, /images/image.png, /misc/textfile.txt): ");
        String resourcePath = scanner.nextLine();

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            /*Send GET request for the specified resource*/
            out.println("GET " + resourcePath + " HTTP/1.1");
            out.println("Host: localhost");
            out.println("Connection: close");
            out.println();

            /* Read and print response from server */
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
