package rs.ac.ni.elfak.asap;

import com.google.mlkit.common.MlKitException;

final class ScannerFailureClassifier {

    private ScannerFailureClassifier() {
    }

    static boolean isModuleUnavailable(Exception exception) {
        if (!(exception instanceof MlKitException)) {
            return false;
        }

        return isModuleUnavailableCode(((MlKitException) exception).getErrorCode());
    }

    static boolean isModuleUnavailableCode(int errorCode) {
        return errorCode == MlKitException.CODE_SCANNER_UNAVAILABLE
                || errorCode == MlKitException.UNAVAILABLE
                || errorCode == MlKitException.NETWORK_ISSUE
                || errorCode == MlKitException.NOT_ENOUGH_SPACE
                || errorCode == MlKitException.CODE_SCANNER_GOOGLE_PLAY_SERVICES_VERSION_TOO_OLD;
    }
}
