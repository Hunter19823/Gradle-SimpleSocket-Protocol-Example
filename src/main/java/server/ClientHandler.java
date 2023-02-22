package server;

import com.google.gson.JsonObject;
import common.NetworkHandlingThread;
import common.NetworkUtils;
import common.Util;
import jdk.net.ExtendedSocketOptions;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static common.Util.println;

/**
 * This class handles a single client connection.
 * This class is a thread that will run until the client disconnects.
 * This class will use {@link Util#getOperationRegistry()} to find the
 * operation that the client requested and then call the handleServer method
 * on that operation.
 * This class also implements {@link Closeable} so that it can be used in a
 * try-with-resources statement.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public class ClientHandler extends Thread implements Closeable {

    // The NetworkHandlingThread that is used to safely handle the communication with the client.
    private final NetworkHandlingThread networkHandlingThread;

    /**
     * This constructor is used to create a new ClientHandler.
     * The thread will not start until the start() method is called.
     * The thread will run until the client disconnects or the close() method is called.
     *
     * @param socket The socket of the client.
     */
    public ClientHandler( Socket socket ) {
        super("ClientHandler#" + socket.getInetAddress().getHostAddress());
        // The socket of the client.
        this.networkHandlingThread = new NetworkHandlingThread(socket);
    }

    @Override
    public void run() {
        // Start the network handling thread.
        networkHandlingThread.start();
        try {
            println("Server connected to client");
            while(networkHandlingThread.isRunning()) {
                // Wait for the client to send a message.
                // In a real application, you would probably want to have a timeout here.
                // This is just a simple example, we'll just wait forever instead.
                JsonObject request = networkHandlingThread.receive();

                // Verify that the request is valid.
                // See protocol.md for more information on the protocol.
                if (!request.has("operation")) {
                    networkHandlingThread.send(NetworkUtils.createMissingRequiredArgumentError());
                    return;
                }
                if (!request.get("operation").isJsonPrimitive() || !request.get("operation").getAsJsonPrimitive().isNumber()) {
                    networkHandlingThread.send(NetworkUtils.createIllegalArgumentTypeError());
                    return;
                }
                int operation = request.get("operation").getAsInt();

                if (Util.getOperationRegistry().hasOperation(operation)) {
                    Util.getOperationRegistry().getOperation(operation).handleServer(request, networkHandlingThread);
                }
                else {
                    networkHandlingThread.send(NetworkUtils.createUnsupportedOperationError());
                }
            }

        } catch (Exception e) {
            networkHandlingThread.send(NetworkUtils.createInternalError());
            println("Client Handler Encountered an Internal Error: ",e);
        } finally {
            try {
                networkHandlingThread.close();
            } catch (IOException ignored) {}
        }
    }


    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        // Close the network handling thread
        networkHandlingThread.close();
        try {
            networkHandlingThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.interrupt();
    }

}
