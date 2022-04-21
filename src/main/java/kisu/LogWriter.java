package kisu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;

public class LogWriter extends Writer {
    private final Logger logger;
    private boolean closed = false;

    private final StringBuilder builder = new StringBuilder();

    public LogWriter(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Writer already closed");
        }
        synchronized (builder) {
            for (int i = off; i < off + len; i++) {
                char c = cbuf[i];
                if (c == '\n') {
                    flush();
                } else {
                    builder.append(c);
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Writer already closed");
        }
        synchronized (builder) {
            if (builder.isEmpty()) {
                return;
            }
            logger.info(builder.toString());
            builder.setLength(0);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            flush();
            closed = true;
        }
    }
}
