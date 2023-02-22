package common.operation;

import com.google.gson.JsonObject;
import common.NetworkHandlingThread;
import common.Operation;

import java.io.IOException;

/**
 * This class is used to handle the shutdown operation.
 * This operation is used to close the connection between the client and the server
 * to not leave open socket connections.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public class ShutdownOperation implements Operation {

    /**
     * This method is used to provide a description of the operation to the client.
     *
     * @return A description of the operation.
     */
    @Override
    public String getDescription() {
        return "This operation shuts down the connection between the client and the server.";
    }

    /**
     * This method is called when the server receives a request for this operation.
     *
     * @param request The request that was received.
     * @param out     The thread that is handling the request.
     */
    @Override
    public void handleServer( JsonObject request, NetworkHandlingThread out ) {
        try {
            out.close();
        } catch (IOException ignored) {}
    }

    /**
     * This method is called on the client side to begin the operation.
     *
     * @param out The Networking thread that is handling the request.
     *            This is used to send the request to the server.
     *            The thread will also be used to handle the response.
     */
    @Override
    public void handleClient( NetworkHandlingThread out ) {
        try {
            out.close();
        } catch (IOException ignored) {}
    }

}
