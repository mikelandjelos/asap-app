package rs.ac.ni.elfak.asap.network;

import com.google.mlkit.vision.barcode.common.Barcode;

public final class BarcodeFormatMapper {

    private BarcodeFormatMapper() {
    }

    public static String toContractFormat(int scannerFormat) {
        switch (scannerFormat) {
            case Barcode.FORMAT_EAN_13:
                return "EAN_13";
            case Barcode.FORMAT_EAN_8:
                return "EAN_8";
            case Barcode.FORMAT_UPC_A:
                return "UPC_A";
            case Barcode.FORMAT_UPC_E:
                return "UPC_E";
            default:
                return null;
        }
    }
}
