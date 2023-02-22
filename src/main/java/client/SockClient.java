package client;

import common.NetworkHandlingThread;
import common.Util;

import java.net.Socket;
import java.util.Scanner;

import static common.Util.println;

/**
 * A class to demonstrate a simple client-server connection using sockets.
 *
 * @author Hunter Spragg
 * Software Engineering
 * @version February 2022
 */
class SockClient {

    public static void main( String[] args ) {
        // Declare the local variables we will need
        // We will use these to store the host and port
        // As well as the operation we want to perform
        int port;
        String host;
        int operation;

        // Check for the correct number of arguments
        if(args.length != 2){
            println("See the README.md for usage instructions");
            System.exit(1);
        }
        // Parse the port number
        port = Util.getPort(args[0]);
        host = args[1];
        // Verify the host.
        Util.verifyHost(host);

        // Create the socket using the host and port.
        // We use a try-with-resources block to ensure the socket is closed
        // when the program exits.
        // This is a good practice to follow.
        try (Socket sock = new Socket(host, port);
             // Create a new NetworkHandlingThread to handle the socket
             // This thread will handle all the communication with the server
             // and will be responsible for closing the socket when it is done.
             // We will use this thread to send and receive messages from the server.
             NetworkHandlingThread client = new NetworkHandlingThread(sock);
        ) {
            // Start the thread
            client.start();
            do {
                // Prompt the user for an operation to perform
                operation = promptForOperation();
                if(Util.getOperationRegistry().hasOperation(operation)){
                    Util.getOperationRegistry().getOperation(operation).handleClient(client);
                }
            } while(operation != -1 && client.isRunning());

        } catch (Exception e) {
            println("An exception occurred communicating with the server", e);
        }
    }


    private static int promptForOperation() {
        int operation = -1;
        println("Please enter the corresponding number for the operation you would like to perform: ");
        while (!Util.getOperationRegistry().hasOperation(operation)) {
            println("=============\n"+Util.getOperationRegistry().listOperations());
            if (Util.getScanner().hasNextInt()) {
                operation = Util.getScanner().nextInt();
                if (Util.getOperationRegistry().hasOperation(operation)) return operation;
                println("Invalid operation number. Please try again.");
            }
            else {
                println("Please enter a valid number: ");
            }
        }
        return operation;
    }
}