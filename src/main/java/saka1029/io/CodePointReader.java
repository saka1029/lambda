package saka1029.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * コードポイントを返すReaderのフィルターです。
 * read()を読んだ時だけcharではなくコードポイントを返します。
 * read(char[])などは通常のReaderと同じです。
 */
public class CodePointReader extends FilterReader {

    public CodePointReader(String in) {
        super(new StringReader(in));
    }

    public CodePointReader(Reader in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int first = super.read();
        if (first == -1)
            return -1;
        if (!Character.isHighSurrogate((char)first))
            return first;
        int second = super.read();
        if (second == -1)
            return -1;
        if (!Character.isLowSurrogate((char)second))
            throw new IOException("invalid surrogate pair (%d, %d)".formatted(first, second));
        return Character.toCodePoint((char)first, (char)second);
    }
}
