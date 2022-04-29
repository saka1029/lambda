package saka1029.lambda.io;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;
import saka1029.lambda.CodePointReader;

public class TestCodePointReader {

    static String SURROG = "a𩸽b"; // サロゲート文字はvscodeで編集すると化けるので注意する。

    @Test
    public void testRead() throws IOException {
        CodePointReader reader = new CodePointReader(new StringReader(SURROG));
        assertEquals('a', reader.read());
        assertEquals(SURROG.codePointAt(1), reader.read());
        assertEquals('b', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    public void testRead2() throws IOException {
        CodePointReader reader = new CodePointReader(SURROG);
        assertEquals('a', reader.read());
        assertEquals(SURROG.codePointAt(1), reader.read());
        assertEquals('b', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    public void testCodePointReader() throws IOException {
        String s = "😅😆";
        CodePointReader reader = new CodePointReader(new StringReader(s));
        assertEquals(s.codePointAt(0), reader.read());
        assertEquals(s.codePointAt(2), reader.read());
        assertEquals(-1, reader.read());
    }
}
