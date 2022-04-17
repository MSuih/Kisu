package kisu.exceptions;

public class ConnectFailedException extends Exception {
    public ConnectFailedException(String message) {
        super(message);
    }

    public ConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
