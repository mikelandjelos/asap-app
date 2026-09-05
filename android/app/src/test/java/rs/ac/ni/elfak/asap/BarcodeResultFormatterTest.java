package rs.ac.ni.elfak.asap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class BarcodeResultFormatterTest {

    @Test
    public void selectValue_prefersRawBarcodeValue() {
        assertEquals("8601234567890", BarcodeResultFormatter.selectValue(
                "8601234567890", "display value"));
    }

    @Test
    public void selectValue_usesDisplayValueWhenRawValueIsBlank() {
        assertEquals("01234565", BarcodeResultFormatter.selectValue(" ", "01234565"));
    }

    @Test
    public void selectValue_returnsNullWhenBarcodeHasNoText() {
        assertNull(BarcodeResultFormatter.selectValue(null, ""));
    }
}
