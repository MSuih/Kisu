package kisu.exceptions;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringBuilder builder = new StringBuilder();
        addTitle(builder, t);
        addException(builder, e, true);
        for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause())
            addException(builder, cause, false);

        String message = builder.toString();
        logger.error(message);
        showMessageDialog(message);

        System.exit(1);
    }

    private void addTitle(StringBuilder builder, Thread t) {
        builder.append("Uncaught exception thrown in thread: ");
        builder.append(t.getName());
        builder.append(" (");
        builder.append(t.getId());
        builder.append(")");
    }

    private void addException(StringBuilder builder, Throwable e, boolean isPrimary) {
        builder.append('\n');
        if (!isPrimary)
            builder.append("Caused by: ");
        builder.append(e.getClass().getCanonicalName());
        builder.append(": ");
        builder.append(e.getMessage());
        builder.append('\n');

        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length == 0) {
            builder.append("\tNo stacktrace\n");
        } else for (StackTraceElement traceElement : stackTrace) {
            builder.append('\t');
            builder.append(traceElement.getClassName());
            builder.append("::");
            builder.append(traceElement.getMethodName());
            if (traceElement.getLineNumber() >= 0) {
                builder.append(':');
                builder.append(traceElement.getLineNumber());
            }
            builder.append("\n");
        }
    }

    private void showMessageDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Uncaught Exception");
        alert.setContentText("Unexpected error has occurred and the application must be closed");

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }
}
