package com.lukas.hackassembler.emit;

import java.io.Closeable;

/**
 * Implementations should be able to emit the results of a parser into specific outputs (eg. a log, a file).
 */
public interface Emitter extends Closeable {
    /**
     * Emits a string of parser output followed by a new line characher.
     *
     * @param line a line of text to be outputted
     */
    void emitLine(String line);

    /**
     * Emits a string of the parser output as is.
     *
     * @param str a string to be outputted
     */
    void emitString(String str);

    /**
     * Gets the full output of the parser till that point.
     *
     * @return output of the parser till this point.
     */
    String getOutput();
}
