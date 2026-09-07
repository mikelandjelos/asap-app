package rs.ac.ni.elfak.asap.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.mlkit.vision.barcode.common.Barcode;
import org.junit.Test;

public class BarcodeFormatMapperTest {

    @Test
    public void mapsSupportedScannerFormatsToContractValues() {
        assertEquals("EAN_13", BarcodeFormatMapper.toContractFormat(Barcode.FORMAT_EAN_13));
        assertEquals("EAN_8", BarcodeFormatMapper.toContractFormat(Barcode.FORMAT_EAN_8));
        assertEquals("UPC_A", BarcodeFormatMapper.toContractFormat(Barcode.FORMAT_UPC_A));
        assertEquals("UPC_E", BarcodeFormatMapper.toContractFormat(Barcode.FORMAT_UPC_E));
    }

    @Test
    public void rejectsUnsupportedScannerFormat() {
        assertNull(BarcodeFormatMapper.toContractFormat(Barcode.FORMAT_QR_CODE));
    }
}
