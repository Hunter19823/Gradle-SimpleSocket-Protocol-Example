package common;

import com.google.gson.JsonObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static common.Util.println;

/**
 * This class is used to handle the network communication of a single socket.
 * It is used by both the server and the client to handle the communication
 * in a separate thread allowing the main thread to continue with other tasks
 * while waiting for a response.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public class NetworkHandlingThread extends Thread implements Closeable {
    // The socket that is being handled by this thread.
    private final Socket socket;
    // A blocking queue that is used to store the messages that are to be sent.
    private final BlockingQueue<JsonObject> REQUEST_QUEUE;
    // A blocking queue that is used to store the messages that have been received.
    private final BlockingQueue<JsonObject> RECEIVED_QUEUE;
    // A flag that is used to indicate if the thread should continue running.
    // This boolean needs to be atomic because it is might be modified by a different thread.
    private final AtomicBoolean isRunning;


    /**
     * This constructor is used to create a new NetworkHandlingThread.
     * The thread will not start until the start() method is called.
     *
     * @param socket The socket that is to be handled by this thread.
     */
    public NetworkHandlingThread( Socket socket ) {
        super("NetworkHandlingThread#" + socket.getInetAddress().getHostAddress());
        this.socket = socket;
        this.REQUEST_QUEUE = new LinkedBlockingQueue<>();
        this.RECEIVED_QUEUE = new LinkedBlockingQueue<>();
        this.isRunning = new AtomicBoolean(true);
    }

    /**
     * This is like the main method of a thread.
     * Whenever you call the start() method on a thread, it will call this method.
     * This method will run until the thread is interrupted or the close() method is called.
     */
    @Override
    public void run() {
        // Set the isRunning flag to true to indicate that the thread is running.
        this.isRunning.set(true);
        // Try with resources will automatically close the streams when the try block is finished.
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream();
        ) {
            // While the socket is connected and the thread is running.
            while(isRunning() && isConnected(socket)) {
                // While the socket is connected, there is no data to read, the thread is running, and there is no data to send.
                // We will sleep for 100 milliseconds to prevent this loop from using too much CPU.
                while(isRunning() && isConnected(socket) && !hasData(in)) {
                    try {
                        // Sleep for 100 milliseconds.
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // If the thread is interrupted, print the stack trace.
                        println("Network Handler has been Interrupted", e);
                    }
                }
                if(isConnected(socket)) {
                    try {
                        // If there is any data to write, write it to the output stream.
                        // Send a queued response
                        if (!this.REQUEST_QUEUE.isEmpty()) {
                            // Get the next response from the queue.
                            JsonObject request = this.REQUEST_QUEUE.take();
                            // Use the NetworkUtils class to write the JSON object to the output stream.
                            // See the NetworkUtils class for more information.
                            println("Sent: " + request);
                            NetworkUtils.toStream(request, out);
                        }
                        // If there is data to read from the InputStream, read it.
                        if (in.available() > 0) {
                            // Read the data from the InputStream in a json object.
                            // Use the NetworkUtils class to read the JSON object from the input stream.
                            JsonObject request = NetworkUtils.fromStream(in);
                            println("Received: " + request);

                            // Add the json object to the received queue.
                            this.RECEIVED_QUEUE.add(request);
                        }
                    } catch (Exception e) {
                        // If there is an exception, print the stack trace.
                        println("Error sending/receiving data from socket streams", e);
                        // Send an internal error response.
                        NetworkUtils.toStream(NetworkUtils.createInternalError(), out);
                    }
                }
            }
            // If the socket is still connected, send the remaining responses.
            if(isConnected(socket)) {
                // Send all queued responses
                try {
                    for (JsonObject request : this.REQUEST_QUEUE) {
                        NetworkUtils.toStream(request, out);
                    }
                    NetworkUtils.toStream(NetworkUtils.createShutdownResponse(), out);
                } catch (SocketException ignored) {}
            }else {
                // If the socket is not connected, and we still have responses to send,
                // print all the responses that were not sent.
                if(!this.REQUEST_QUEUE.isEmpty()) {
                    println("Socket is not connected, but there are still requests to send.");
                    println("Requests: " + this.REQUEST_QUEUE.size());
                    for(JsonObject request : this.REQUEST_QUEUE) {
                        println("Queued Request: %s", request);
                    }
                }
            }
        } catch (Exception e) {
            // If there is an exception, print the stack trace.
            println("Exception in NetworkHandlingThread", e);
        } finally {
            // Set the isRunning flag to false, indicating that the thread is no longer running.
            // And close the socket if it is still open.
            this.isRunning.set(false);
            try {
                this.socket.close();
                println("Socket Closed!");
            } catch (IOException ignored) {}
        }
    }

    /**
     * This method can be used to queue a request to be sent
     * to the socket's output stream.
     * The request will be sent as soon as possible and will be sent
     * all at once.
     *
     * @param request The request to be sent.
     */
    public void send( JsonObject request ) {
        this.REQUEST_QUEUE.add(request);
    }

    /**
     * This method can be used to get the next received request.
     * If there is no request in the queue, this method will block
     * until a request is received.
     * If this is not intended, use the hasReceived() method to check
     * if there is a request in the queue.
     *
     * @return JsonObject The next received request.
     */
    public synchronized JsonObject receive() {
        try {
            return this.RECEIVED_QUEUE.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method can be used to check if there is a request in the queue.
     *
     * @return True if there is a request in the queue, false otherwise.
     */
    public boolean hasReceived() {
        return !this.RECEIVED_QUEUE.isEmpty();
    }

    /**
     * This method is a helper method to check if a socket is still connected.
     *
     * @param socket The socket to check.
     * @return True if the socket is connected, false otherwise.
     */
    private static boolean isConnected( Socket socket) {
        return !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    /**
     * This method is a helper method for checking if the input stream has data to read
     * or if the output stream has data to write.
     *
     * @param in The inputStream to check.
     */
    private boolean hasData( InputStream in ) throws IOException {
        return in.available() != 0 || !REQUEST_QUEUE.isEmpty();
    }

    /**
     * This method is a helper method to check if the thread is running.
     */
    public boolean isRunning() {
        return this.isRunning.get();
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
        println("Closing NetworkHandlerThread...");
        // Set the isRunning flag to false, indicating that the thread is no longer running.
        send(NetworkUtils.createShutdownResponse());
        this.isRunning.set(false);
        try {
            // Wait for the thread to finish running.
            // This will allow the thread to finish sending any queued requests.
            this.join();
        } catch (InterruptedException ignored) {} finally {
            try{
                // Finally, close the socket.
                this.socket.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

}
