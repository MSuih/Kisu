package kisu.exceptions;

public class CompilationFailedException extends Exception {
    public CompilationFailedException() {
    }

    public CompilationFailedException(String message) {
        super(message);
    }

    public CompilationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
