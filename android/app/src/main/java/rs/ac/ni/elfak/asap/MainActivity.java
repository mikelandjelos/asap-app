package rs.ac.ni.elfak.asap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

public final class MainActivity extends AppCompatActivity {

    private Button scanButton;
    private TextView statusText;
    private TextView resultText;
    private GmsBarcodeScanner scanner;

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

        scanButton.setOnClickListener(view -> startScan());
    }

    private void startScan() {
        setScanning(true);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    setScanning(false);
                    String value = BarcodeResultFormatter.selectValue(
                            barcode.getRawValue(), barcode.getDisplayValue());
                    if (value == null) {
                        statusText.setText(R.string.scan_empty);
                        resultText.setText(R.string.scan_no_value);
                    } else {
                        statusText.setText(R.string.scan_success);
                        resultText.setText(value);
                    }
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

}
