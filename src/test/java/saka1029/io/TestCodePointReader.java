package saka1029.io;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

public class TestCodePointReader {

    static String SURROG = "a𩸽b"; // サロゲート文字はvscodeで編集すると化けるので注意する。

    @Test
    public void testRead() throws IOException {
        try (CodePointReader reader = new CodePointReader(new StringReader(SURROG))) {
            assertEquals('a', reader.read());
            assertEquals(SURROG.codePointAt(1), reader.read());
            assertEquals('b', reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testRead2() throws IOException {
        try (CodePointReader reader = new CodePointReader(SURROG)) {
            assertEquals('a', reader.read());
            assertEquals(SURROG.codePointAt(1), reader.read());
            assertEquals('b', reader.read());
            assertEquals(-1, reader.read());
        }
    }
}
