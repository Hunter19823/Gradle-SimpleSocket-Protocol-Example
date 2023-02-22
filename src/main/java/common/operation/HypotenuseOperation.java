package common.operation;

import com.google.gson.JsonObject;
import common.NetworkHandlingThread;
import common.NetworkUtils;
import common.Operation;
import common.Util;

import java.util.Scanner;

import static common.Util.println;

/**
 * This class is responsible for handling the hypotenuse protocol on the server's side.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public class HypotenuseOperation implements Operation {

    /**
     * This method is used to provide a description of the operation to the client.
     *
     * @return A description of the operation.
     */
    @Override
    public String getDescription() {
        return "This operation calculates the hypotenuse of a right triangle.";
    }

    /**
     * This method is called when the server receives a request for this operation.
     *
     * @param request The request that was received.
     * @param out     The thread that is handling the request.
     */
    @Override
    public void handleServer( JsonObject request, NetworkHandlingThread out ) {
        if (!request.has("a") || !request.has("b")) {
            out.send(NetworkUtils.createMalformedJsonError());
            return;
        }
        if (!request.get("a").isJsonPrimitive() || !request.get("b").isJsonPrimitive()) {
            out.send(NetworkUtils.createIllegalArgumentTypeError());
            return;
        }
        if (!request.get("a").getAsJsonPrimitive().isNumber() || !request.get("b").getAsJsonPrimitive().isNumber()) {
            out.send(NetworkUtils.createIllegalArgumentTypeError());
            return;
        }
        Number a = request.get("a").getAsNumber();
        Number b = request.get("b").getAsNumber();
        out.send(createHypotenuseResponse(a, b));
    }

    /**
     * This method is called on the client side to begin the operation.
     *
     * @param networkHandlingThread The Networking thread that is handling the request.
     *                              This is used to send the request to the server.
     *                              The thread will also be used to handle the response.
     */
    @Override
    public void handleClient( NetworkHandlingThread networkHandlingThread ) {
        Number sideA;
        Number sideB;
        println("Please enter the side of one of the triangles: ");
        sideA = parseNumber(Util.getScanner());
        println("Please enter the side of the other triangle: ");
        sideB = parseNumber(Util.getScanner());

        JsonObject request = createHypotenuseRequest(sideA, sideB);

        networkHandlingThread.send(request);

        JsonObject response = networkHandlingThread.receive();

        if (response.has("error")) {
            println("Error: " + response.get("message").getAsString());
            return;
        }
        if (!response.has("result")) {
            println("Error: Malformed response received from server.");
            return;
        }

        println("The hypotenuse is: " + response.get("result").getAsDouble());
    }

    /**
     * This method is used to parse a number from the user.
     *
     * @param scanner The scanner to use to read the number.
     * @return The number that was read.
     *         If the number is invalid, this method will return null.
     */
    public static Number parseNumber( Scanner scanner ) {
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
        }
        else if (scanner.hasNextDouble()) {
            return scanner.nextDouble();
        }
        else if (scanner.hasNextLong()) {
            return scanner.nextLong();
        }
        else if (scanner.hasNextFloat()) {
            return scanner.nextFloat();
        }
        else{
            return null;
        }
    }


    /**
     * This method is used to create a hypotenuse request.
     *
     * @param a The length of one of the sides of the triangle.
     * @param b The length of the other side of the triangle.
     * @return A JsonObject that represents the hypotenuse request.
     */
    public static JsonObject createHypotenuseRequest( Number a, Number b ) {
        JsonObject request = new JsonObject();
        request.addProperty("operation", 1);
        request.addProperty("a", a);
        request.addProperty("b", b);
        return request;
    }

    /**
     * This method is used to create a hypotenuse response.
     *
     * @param a The length of one of the sides of the triangle.
     * @param b The length of the other side of the triangle.
     * @return A JsonObject that represents the hypotenuse response.
     */
    public static JsonObject createHypotenuseResponse( Number a, Number b ) {
        JsonObject response = new JsonObject();
        response.addProperty("operation", 1);
        response.addProperty("a", a);
        response.addProperty("b", b);

        double result = Math.sqrt(Math.pow(a.doubleValue(), 2) + Math.pow(b.doubleValue(), 2));
        response.addProperty("result", result);
        return response;
    }

}

