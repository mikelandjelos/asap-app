package rs.ac.ni.elfak.asap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.mlkit.common.MlKitException;

import org.junit.Test;

public final class ScannerFailureClassifierTest {

    @Test
    public void codeScannerUnavailable_isReportedAsModuleProblem() {
        assertTrue(ScannerFailureClassifier.isModuleUnavailableCode(
                MlKitException.CODE_SCANNER_UNAVAILABLE));
    }

    @Test
    public void networkIssue_isReportedAsModuleProblem() {
        assertTrue(ScannerFailureClassifier.isModuleUnavailableCode(MlKitException.NETWORK_ISSUE));
    }

    @Test
    public void unrelatedFailure_isReportedAsGeneralProblem() {
        assertFalse(ScannerFailureClassifier.isModuleUnavailableCode(
                MlKitException.CODE_SCANNER_PIPELINE_INFERENCE_ERROR));
    }
}
