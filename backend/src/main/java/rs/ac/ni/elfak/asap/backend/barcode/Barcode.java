package rs.ac.ni.elfak.asap.backend.barcode;

public record Barcode(String value, Format format) {

    public enum Format {
        EAN_13,
        EAN_8,
        UPC_A,
        UPC_E
    }
}
