package rs.ac.ni.elfak.asap.backend.barcode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BarcodeRulesTest {

    @ParameterizedTest
    @MethodSource("validBarcodes")
    void acceptsSupportedBarcodes(String value, String format) {
        assertThat(BarcodeRules.validationCode(value, format)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidBarcodes")
    void returnsStableValidationCodes(String value, String format, String expectedCode) {
        assertThat(BarcodeRules.validationCode(value, format)).contains(expectedCode);
    }

    @Test
    void doesNotEquateUpcAAndLeadingZeroEan13() {
        Barcode upcA = BarcodeRules.validatedBarcode("036000291452", "UPC_A");
        Barcode ean13 = BarcodeRules.validatedBarcode("0036000291452", "EAN_13");

        assertThat(upcA).isNotEqualTo(ean13);
    }

    private static Stream<Arguments> validBarcodes() {
        return Stream.of(
                Arguments.of("2000000000015", "EAN_13"),
                Arguments.of("73513537", "EAN_8"),
                Arguments.of("036000291452", "UPC_A"),
                Arguments.of("01234558", "UPC_E"));
    }

    private static Stream<Arguments> invalidBarcodes() {
        return Stream.of(
                Arguments.of("2000000000014", "EAN_13", "INVALID_CHECK_DIGIT"),
                Arguments.of("200000000001", "EAN_13", "INVALID_LENGTH"),
                Arguments.of("20000000000A5", "EAN_13", "DIGITS_ONLY"),
                Arguments.of("2000000000015", "QR_CODE", "UNSUPPORTED_FORMAT"),
                Arguments.of("", "EAN_13", "REQUIRED"),
                Arguments.of("2000000000015", "", "REQUIRED"));
    }
}
