package rs.ac.ni.elfak.asap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import rs.ac.ni.elfak.asap.network.ApiClientFactory;
import rs.ac.ni.elfak.asap.network.I1ApiModels;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;

public final class MainActivity extends AppCompatActivity {

    private Button scanButton;
    private TextView statusText;
    private TextView resultText;
    private GmsBarcodeScanner scanner;
    private ScanQueryCoordinator queryCoordinator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scan_button);
        statusText = findViewById(R.id.status_text);
        resultText = findViewById(R.id.result_text);

        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E)
                .enableAutoZoom()
                .build();
        scanner = GmsBarcodeScanning.getClient(this, options);
        queryCoordinator = new ScanQueryCoordinator(
                ApiClientFactory.createDefault(),
                this::runOnUiThread,
                new QueryView());

        scanButton.setOnClickListener(view -> startScan());
    }

    private void startScan() {
        queryCoordinator.onScanStarted();
        setScanning(true);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    setScanning(false);
                    queryCoordinator.onScanSucceeded(
                            barcode.getRawValue(),
                            barcode.getDisplayValue(),
                            barcode.getFormat());
                })
                .addOnCanceledListener(() -> {
                    setScanning(false);
                    statusText.setText(R.string.scan_cancelled);
                })
                .addOnFailureListener(exception -> {
                    setScanning(false);
                    if (ScannerFailureClassifier.isModuleUnavailable(exception)) {
                        statusText.setText(R.string.scan_module_unavailable);
                    } else {
                        statusText.setText(R.string.scan_failed);
                    }
                });
    }

    private void setScanning(boolean scanning) {
        scanButton.setEnabled(!scanning);
        if (scanning) {
            statusText.setText(R.string.scan_in_progress);
        }
    }

    @Override
    protected void onDestroy() {
        queryCoordinator.close();
        super.onDestroy();
    }

    private final class QueryView implements ScanQueryCoordinator.View {

        @Override
        public void showEmptyBarcode() {
            statusText.setText(R.string.scan_empty);
            resultText.setText(R.string.scan_no_value);
        }

        @Override
        public void showUnsupportedBarcode(String value) {
            statusText.setText(R.string.scan_unsupported);
            resultText.setText(value);
        }

        @Override
        public void showLoading(String value) {
            statusText.setText(R.string.api_loading);
            resultText.setText(value);
        }

        @Override
        public void showResponse(String value, I1ApiModels.ScanQueryResponse response) {
            statusText.setText(R.string.api_response_received);
            resultText.setText(value);
        }

        @Override
        public void showFailure(String value, ScanQueryClient.Failure failure) {
            switch (failure.kind()) {
                case TRANSPORT:
                    statusText.setText(R.string.api_transport_failure);
                    break;
                case HTTP:
                    statusText.setText(getString(
                            R.string.api_http_failure,
                            failure.httpStatus() == null ? 0 : failure.httpStatus()));
                    break;
                case INVALID_RESPONSE:
                default:
                    statusText.setText(R.string.api_invalid_response);
                    break;
            }
            resultText.setText(value);
        }
    }

}
