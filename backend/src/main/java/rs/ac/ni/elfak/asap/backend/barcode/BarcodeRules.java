package rs.ac.ni.elfak.asap.backend.barcode;

import java.util.Optional;

public final class BarcodeRules {

    private BarcodeRules() {
    }

    public static Optional<String> validationCode(String value, String formatName) {
        if (formatName == null || formatName.isBlank()) {
            return Optional.of("REQUIRED");
        }

        final Barcode.Format format;
        try {
            format = Barcode.Format.valueOf(formatName);
        } catch (IllegalArgumentException exception) {
            return Optional.of("UNSUPPORTED_FORMAT");
        }

        if (value == null || value.isBlank()) {
            return Optional.of("REQUIRED");
        }
        if (!value.chars().allMatch(character -> character >= '0' && character <= '9')) {
            return Optional.of("DIGITS_ONLY");
        }
        if (value.length() != expectedLength(format)) {
            return Optional.of("INVALID_LENGTH");
        }
        if (!hasValidCheckDigit(value, format)) {
            return Optional.of("INVALID_CHECK_DIGIT");
        }
        return Optional.empty();
    }

    public static Barcode validatedBarcode(String value, String formatName) {
        Optional<String> error = validationCode(value, formatName);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }
        return new Barcode(value, Barcode.Format.valueOf(formatName));
    }

    private static int expectedLength(Barcode.Format format) {
        return switch (format) {
            case EAN_13 -> 13;
            case EAN_8, UPC_E -> 8;
            case UPC_A -> 12;
        };
    }

    private static boolean hasValidCheckDigit(String value, Barcode.Format format) {
        if (format != Barcode.Format.UPC_E) {
            return hasValidModuloTen(value);
        }

        char numberSystem = value.charAt(0);
        if (numberSystem != '0' && numberSystem != '1') {
            return false;
        }
        String digits = value.substring(1, 7);
        char checkDigit = value.charAt(7);
        char compressionDigit = digits.charAt(5);
        String body = switch (compressionDigit) {
            case '0', '1', '2' -> "" + numberSystem + digits.substring(0, 2)
                    + compressionDigit + "0000" + digits.substring(2, 5);
            case '3' -> "" + numberSystem + digits.substring(0, 3)
                    + "00000" + digits.substring(3, 5);
            case '4' -> "" + numberSystem + digits.substring(0, 4)
                    + "00000" + digits.charAt(4);
            default -> "" + numberSystem + digits.substring(0, 5)
                    + "0000" + compressionDigit;
        };
        return hasValidModuloTen(body + checkDigit);
    }

    private static boolean hasValidModuloTen(String value) {
        int sum = 0;
        int positionFromRight = 1;
        for (int index = value.length() - 2; index >= 0; index--, positionFromRight++) {
            int digit = value.charAt(index) - '0';
            sum += digit * (positionFromRight % 2 == 1 ? 3 : 1);
        }
        int expected = (10 - sum % 10) % 10;
        return expected == value.charAt(value.length() - 1) - '0';
    }
}
