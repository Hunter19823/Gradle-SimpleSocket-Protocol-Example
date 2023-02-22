package common;

import common.operation.HypotenuseOperation;
import common.operation.ShutdownOperation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * A class to hold utility methods for common use
 * between the client and server.
 *
 * @author Hunter Spragg
 * @version February 2022
 */
public class Util {
    // Create a singleton for the operation registry.
    private static final OperationRegistry OPERATION_REGISTRY_INSTANCE;
    // Create a singleton for the Scanner to avoid having the input stream closed
    // when the scanner is closed.
    private static Scanner SCANNER = null;
    static {
        // Create a new operation registry.
        OPERATION_REGISTRY_INSTANCE = new OperationRegistry();
        // Register all supported operations.
        OPERATION_REGISTRY_INSTANCE.registerOperation(0, new ShutdownOperation());
        OPERATION_REGISTRY_INSTANCE.registerOperation(1, new HypotenuseOperation());
    }

    /**
     * OperationRegistry is a singleton class, so this method should be used to get the instance.
     *
     * @return The singleton instance of the OperationRegistry.
     */
    public static OperationRegistry getOperationRegistry() {
        return OPERATION_REGISTRY_INSTANCE;
    }

    /**
     * Parse the given port number into an integer.
     * And verify that it is a valid port number.
     * This method will call System.exit(1) if the port is invalid.
     *
     * @param port The port number to parse.
     * @return The port number as an integer.
     */
    public static int getPort( String port) {
        if (port == null) {
            System.err.println("Port number cannot be null!");
            System.exit(1);
        }
        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber < 0 || portNumber > 65535) {
                System.err.println("Port is out of range! Port must be between 0 and 65535!");
                System.exit(1);
            }
            return portNumber;
        } catch (NumberFormatException e) {
            System.err.println("Port is not a number!");
            System.exit(1);
        }
        return -1;
    }

    /**
     * Verify that the given host name is valid.
     * As we might have been provided localhost, 1.1.1.1, or some other host name.
     * This method will call System.exit(1) if the host name is invalid.
     *
     * @param host The host name to verify.
     */
    public static void verifyHost(String host) {
        if (host == null) {
            System.err.println("Host address is null");
            System.exit(1);
        }
        try {
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.err.println("Host address is invalid!");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * This method is a helper method that prints the current thread's name and the given message.
     *
     * @param message The message to print.
     */
    public static void println( String message ) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + message);
    }

    /**
     * This method is a helper method that prints the current thread's name, the given message, formatted
     * based on the args parameter.
     *
     * @param message The message to print.
     * @param args The arguments to print.
     */
    public static void println( String message, Object... args ) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + String.format(message, args));
    }

    /**
     * This method is a helper method that prints the current thread's name, the given message, and the
     * given throwable.
     *
     * @param message The message to print.
     * @param throwable The throwable to print.
     */
    public static void println( String message, Throwable throwable ) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + message);
        throwable.printStackTrace();
    }

    /**
     * This method is a singleton method that returns a Scanner object.
     * This method will create a new Scanner object if one does not already exist.
     * This method will help prevent the input stream from being closed when the scanner is closed.
     *
     * @return The singleton Scanner object.
     */
    public static Scanner getScanner() {
        if(SCANNER == null)
            SCANNER = new Scanner(System.in);
        return SCANNER;
    }

}
