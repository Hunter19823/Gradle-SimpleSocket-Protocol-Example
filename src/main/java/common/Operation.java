package common;

import com.google.gson.JsonObject;

/**
 * This interface is used to define the operations that can be performed by the
 * server.
 * All classes that implement this interface should be added to the
 * OperationRegistry so that they can be used by the server.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public interface Operation {

    /**
     * This method is used to provide a description of the operation to the client.
     *
     * @return A description of the operation.
     */
    public String getDescription();

    /**
     * This method is called when the server receives a request for this operation.
     *
     * @param request The request that was received.
     * @param out The thread that is handling the request.
     */
    public void handleServer( JsonObject request, NetworkHandlingThread out );

    /**
     * This method is called on the client side to begin the operation.
     *
     * @param out The Networking thread that is handling the request.
     *            This is used to send the request to the server.
     *            The thread will also be used to handle the response.
     */
    public void handleClient( NetworkHandlingThread out );



}
