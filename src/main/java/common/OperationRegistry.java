package common;

import java.util.Map;
import java.util.TreeMap;

/**
 * A registry of operations that can be performed by the server.
 * This class is meant to be used as a singleton.
 *
 * @author Hunter Spragg
 * @version February 2023
 */
public class OperationRegistry {
    // The map of operations that can be performed by the server.
    private final Map<Integer, Operation> OPERATION_MAP;

    /**
     * Creates a new OperationRegistry with an empty map.
     *
     * @implSpec This constructor is equivalent to calling {@link #OperationRegistry(Map)} with a new {@link TreeMap}.
     */
    public OperationRegistry() {
        // Create a new tree map to store the operations by default.
        this(new TreeMap<>());
    }

    /**
     * Create a new operation registry with the given map.
     *
     * @param operationMap The map to use to store the operations.
     */
    public OperationRegistry( Map<Integer, Operation> operationMap ) {
        OPERATION_MAP = operationMap;
    }

    /**
     * Get the operation with the given id.
     *
     * @param operationId The id of the operation to get.
     * @return The operation with the given id, or null if no operation with the given id is registered.
     */
    public Operation getOperation( int operationId ) {
        return OPERATION_MAP.get(operationId);
    }

    /**
     * Check if an operation with the given id is registered.
     *
     * @param operationId The id of the operation to check.
     * @return True if an operation with the given id is registered, false otherwise.
     */
    public boolean hasOperation( int operationId ) {
        return OPERATION_MAP.containsKey(operationId);
    }

    /**
     * Register an operation with the given id.
     * Will overwrite any existing operation with the same id.
     *
     * @param operationId The id of the operation to register.
     * @param operation The operation to register.
     */
    public void registerOperation( int operationId, Operation operation ) {
        OPERATION_MAP.put(operationId, operation);
    }

    /**
     * Unregister the operation with the given id.
     *
     * @param operationId The id of the operation to unregister.
     *                    If no operation with the given id is registered, this method does nothing.
     */
    public void unregisterOperation( int operationId ) {
        OPERATION_MAP.remove(operationId);
    }

    /**
     * Unregister the given operation.
     *
     * @param operation The operation to unregister.
     *                  If the given operation is not registered, this method does nothing.
     */
    public void unregisterOperation( Operation operation ) {
        OPERATION_MAP.values().remove(operation);
    }

    /**
     * Get the number of operations registered.
     *
     * @return The number of operations registered.
     */
    public int size() {
        return OPERATION_MAP.size();
    }

    /**
     * Check if this registry is empty.
     *
     * @return True if this registry is empty, false otherwise.
     */
    public boolean isEmpty() {
        return OPERATION_MAP.isEmpty();
    }

    /**
     * List all the operations registered.
     *
     * @return A string containing a list of all the operations registered.
     */
    public String listOperations() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Operation> entry : OPERATION_MAP.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue().getDescription()).append("\n");
        }
        return sb.toString();
    }
}
