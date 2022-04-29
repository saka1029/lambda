package saka1029.lambda;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * コードポイントを返すReaderのフィルターです。
 */
public class CodePointReader {

    Reader in;

    public CodePointReader(String source) {
        this.in = new StringReader(source);
    }

    public CodePointReader(Reader in) {
        this.in = in;
    }

    public int read() throws IOException {
        int first = in.read();
        if (first == -1)
            return -1;
        if (!Character.isHighSurrogate((char)first))
            return first;
        int second = in.read();
        if (second == -1)
            throw new IOException("low surrogate expected after %d".formatted(first));
        if (!Character.isLowSurrogate((char)second))
            throw new IOException("invalid surrogate pair (%d, %d)".formatted(first, second));
        return Character.toCodePoint((char)first, (char)second);
    }
}
