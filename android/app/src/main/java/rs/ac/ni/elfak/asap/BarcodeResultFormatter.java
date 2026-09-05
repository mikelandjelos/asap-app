package rs.ac.ni.elfak.asap;

final class BarcodeResultFormatter {

    private BarcodeResultFormatter() {
    }

    static String selectValue(String rawValue, String displayValue) {
        if (rawValue != null && !rawValue.trim().isEmpty()) {
            return rawValue;
        }
        if (displayValue != null && !displayValue.trim().isEmpty()) {
            return displayValue;
        }
        return null;
    }
}
