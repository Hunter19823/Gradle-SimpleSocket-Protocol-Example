package common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * This class contains a number of utility methods for creating
 * and parsing JSON objects using the protocol described in
 * the README.md file.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public class NetworkUtils {
    // The Gson object used to parse JSON objects.
    // This object is thread safe.
    // This object will also be used to pretty-print JSON objects.
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * This method is used to create a new JsonObject that represents
     * an internal client/server error.
     *
     * @return A JsonObject that represents an internal client/server error.
     */
    public static JsonObject createInternalError() {
        JsonObject response = new JsonObject();
        response.addProperty("error", -1);
        response.addProperty("message", "Internal Server Error");
        return response;
    }

    /**
     * This method is used to create a new JsonObject that represents
     * a malformed JSON error.
     *
     * @return A JsonObject that represents a malformed JSON error.
     */
    public static JsonObject createMalformedJsonError() {
        JsonObject response = new JsonObject();
        response.addProperty("error", 0);
        response.addProperty("message", "Malformed Json");
        return response;
    }

    /**
     * This method is used to create a new JsonObject that represents
     * an unsupported operation error.
     *
     * @return A JsonObject that represents an unsupported operation error.
     */
    public static JsonObject createUnsupportedOperationError() {
        JsonObject response = new JsonObject();
        response.addProperty("error", 1);
        response.addProperty("message", "Unsupported Operation");
        return response;
    }

    /**
     * This method is used to create a new JsonObject that represents
     * an illegal argument error.
     *
     * @return A JsonObject that represents an illegal argument error.
     */
    public static JsonObject createIllegalArgumentTypeError() {
        JsonObject response = new JsonObject();
        response.addProperty("error", 2);
        response.addProperty("message", "Illegal Argument Type");
        return response;
    }

    /**
     * This method is used to create a new JsonObject that represents
     * a missing required argument error.
     *
     * @return A JsonObject that represents a missing required argument error.
     */
    public static JsonObject createMissingRequiredArgumentError() {
        JsonObject response = new JsonObject();
        response.addProperty("error", 3);
        response.addProperty("message", "Missing Required Argument");
        return response;
    }

    /**
     * This method is used to create a new JsonObject from
     * an InputStream.
     * The format of the data being read from the InputStream
     * should be the following:
     *    <int: length of JSON string><JSON string>
     *        - The length of the JSON string is stored as an int.
     *        - The JSON string is stored as a UTF-8 encoded string.
     *
     * @param in The InputStream to read from.
     * @return A JsonObject that was read from the InputStream.
     * @throws IOException If an error occurs while reading from the InputStream.
     */
    public static JsonObject fromStream( InputStream in ) throws IOException {
        // Allocate a byte array to store the length of the JSON string.
        byte[] lengthBytes = new byte[ 4 ];

        // Copy the input stream into the byte array.
        in.read(lengthBytes);

        // Convert the length bytes to an int.
        // Use the ByteBuffer class to convert the bytes to an int.
        int length = ByteBuffer.wrap(lengthBytes).getInt();

        // Verify that the length is greater than 2.
        // If the length is less than 2, then the JSON string is empty.
        // A valid JSON String either looks like "{}" or "[]".
        if(length < 2) {
            throw new IOException("Invalid length");
        }

        // Read the rest of the message
        byte[] messageBytes = ByteBuffer.allocate(length).array();

        // Copy the input stream into the byte array.
        in.read(messageBytes);

        // Convert the message bytes to a String.
        String message = new String(messageBytes);

        // Debug print message for testing
//        System.out.println("Received: " + message);

        // Parse the JSON string into a JsonObject.
        JsonObject object = GSON.fromJson(message, JsonObject.class);

        // Debug print resulting object for testing
//        System.out.println("Received JSON: " + GSON.toJson(object));

        // Return the JsonObject.
        return object;
    }

    /**
     * This method is used to write a JsonObject to an OutputStream.
     * The format of the data being written to the OutputStream
     * should be the following:
     *   <int: length of JSON string><JSON string>
     *       - The length of the JSON string is stored as an int.
     *       - The JSON string is stored as a UTF-8 encoded string.
     *
     * @param json The JsonObject to write to the OutputStream.
     * @param out The OutputStream to write to.
     *            This method will not close the OutputStream.
     * @throws IOException If an error occurs while writing to the OutputStream.
     */
    public static void toStream( JsonObject json, OutputStream out ) throws IOException {
        // Convert the JsonObject to a JSON string.
        String jsonStr = GSON.toJson(json);

        // Debug print message for testing
//        System.out.println("Sending: " + jsonStr);

        // Convert the JSON string to a byte array.
        byte[] jsonBytes = jsonStr.getBytes();

        // Convert the length of the JSON string to a byte array.
        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(jsonBytes.length).array();

        // Write the length bytes to the OutputStream.
        out.write(lengthBytes);

        // Write the JSON bytes to the OutputStream.
        out.write(jsonBytes);

        // Flush the OutputStream to ensure that the data is sent.
        out.flush();
    }

    public static JsonObject createShutdownResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("operation", 0);
        return response;

    }

}
