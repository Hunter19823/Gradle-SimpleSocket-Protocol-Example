package server;

import common.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import static common.Util.println;

/**
 * A class to demonstrate a multithreaded server using sockets.
 * This server will accept multiple connections and handle them concurrently.
 * This server can also support multiple requests from a single client if the
 * ClientHandler is modified to handle multiple requests.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public class SockServer {

    public static void main( String[] args ) {
        // The first thing we should always do is verify that the program is being run with the correct number of arguments
        if (args.length != 1) {
            println("See the README.md for usage instructions");
            System.exit(1);
        }

        // Next, we should parse the port number from the command line arguments
        // We should also verify that the port number is valid
        // We will use the Util class to do this as it is a common task for
        // both the client and server
        int port = Util.getPort(args[ 0 ]);

        // Create a linked list of clients to keep track of all the clients
        // that are connected to the server
        // This is a thread safe data structure
        // We will clear this list when the server is shutting down.
        List<ClientHandler> clients = new LinkedList<>();

        // Create a try-with-resources block to make sure the server socket is closed
        // A try-with-resources block is a try block that automatically closes any
        // resources that are created in the try block.
        // In this case it's a server socket.
        // But it could be a database connection, a file, or anything else that
        // implements the AutoCloseable or Closeable interface.
        try (ServerSocket serv = new ServerSocket(port)) {
            // create server socket on port 8888
            println("Server ready for connections");
            // Loop forever to continuously accept new connections.
            // This is the main loop of the server.
            while (true) {

                // Print a message to the console to let the user know the server is waiting for a new connection
                println("Server waiting for a new connection");

                try {
                    // By calling serv.accept() we are waiting for a new connection.
                    // This method will block the main thread until a new connection is made.
                    // Once a new connection is made, it will return a new socket that is then passed
                    // to the ClientHandler constructor so that the ClientHandler can communicate with the client.
                    ClientHandler clientHandler = new ClientHandler(serv.accept());

                    // Add the new client to the list of clients
                    clients.add(clientHandler);

                    // Start the client handler thread.
                    // This will allow the client handler to start communicating with the client.
                    // The client handler will run in parallel with the main thread.
                    clientHandler.start();
                } catch (Exception e) {
                    // If an exception is thrown, print the stack trace to the console.
                    // This is not a good way to handle exceptions, but it is sufficient for this example.
                    // In a real application, we would want to handle the exception in a more meaningful way.
                    // For example, we could log the exception to a file.
                    // We could also send a message to the client to let them know something went wrong.
                    // But for this example, we will just print the stack trace.
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // If an exception is thrown when creating the server socket, print the stack trace to the console.
            println("Failed to create server socket", e);
        }
        finally {
            // Print a message to the console to let the user know the server is closing.
            println("Server closed");
            // Interrupt all the client threads.
            clients.stream().parallel().forEach(ClientHandler::interrupt);
        }
    }

}