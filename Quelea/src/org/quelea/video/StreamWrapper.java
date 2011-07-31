package org.quelea.video;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Michael
 */
public class StreamWrapper {

    private InputStream inputStream;
    private OutputStream outputStream;

    StreamWrapper(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
