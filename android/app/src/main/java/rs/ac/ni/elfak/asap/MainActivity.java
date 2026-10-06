package rs.ac.ni.elfak.asap;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import rs.ac.ni.elfak.asap.ui.AnalyticsFragment;
import rs.ac.ni.elfak.asap.ui.HistoryFragment;
import rs.ac.ni.elfak.asap.ui.ProductFragment;
import rs.ac.ni.elfak.asap.ui.ScanFragment;
import rs.ac.ni.elfak.asap.ui.SessionViewModel;

/** Single-Activity shell with bottom navigation (D-038); screens are fragments sharing one {@link ScanSession}. */
public final class MainActivity extends AppCompatActivity {

    /** Scanner outcome shown on the Scan screen when no request is made. */
    public interface ScanStatusListener {
        void onScanStatus(int messageRes);
    }

    private GmsBarcodeScanner scanner;
    private ScanSession session;
    private BottomNavigationView nav;
    private ScanStatusListener scanStatusListener;
    private final ScanSession.Listener autoOpenProduct = state -> {
        if (state.phase == ScanSession.Phase.LOADING && nav != null && nav.getSelectedItemId() != R.id.nav_product) {
            nav.setSelectedItemId(R.id.nav_product);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new ViewModelProvider(this).get(SessionViewModel.class).session();

        // The toolbar is dark green in light mode and light green in dark mode: status-bar icons must contrast.
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(night);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });

        scanner = GmsBarcodeScanning.getClient(this, new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
                .enableAutoZoom()
                .build());

        nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            show(item.getItemId());
            return true;
        });
        if (savedInstanceState == null) {
            show(R.id.nav_scan);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        session.addListener(autoOpenProduct);
    }

    @Override
    protected void onStop() {
        session.removeListener(autoOpenProduct);
        super.onStop();
    }

    public ScanSession session() {
        return session;
    }

    public void setScanStatusListener(ScanStatusListener listener) {
        scanStatusListener = listener;
    }

    public void startScan() {
        status(R.string.scan_in_progress);
        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    status(0);
                    session.onScanned(barcode.getRawValue(), barcode.getDisplayValue(), barcode.getFormat());
                })
                .addOnCanceledListener(() -> status(R.string.scan_cancelled))
                .addOnFailureListener(e -> status(ScannerFailureClassifier.isModuleUnavailable(e)
                        ? R.string.scan_module_unavailable : R.string.scan_failed));
    }

    private void status(int messageRes) {
        if (scanStatusListener != null) {
            scanStatusListener.onScanStatus(messageRes);
        }
    }

    /** Shows the tab's fragment, creating it once and keeping the others alive but hidden. */
    private void show(int itemId) {
        FragmentManager fm = getSupportFragmentManager();
        String tag = "tab_" + itemId;
        var tx = fm.beginTransaction().setReorderingAllowed(true);
        for (Fragment f : fm.getFragments()) {
            if (!tag.equals(f.getTag())) {
                tx.hide(f);
            }
        }
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) {
            target = itemId == R.id.nav_scan ? new ScanFragment()
                    : itemId == R.id.nav_product ? new ProductFragment()
                    : itemId == R.id.nav_analytics ? new AnalyticsFragment()
                    : new HistoryFragment();
            tx.add(R.id.content, target, tag);
        } else {
            tx.show(target);
        }
        tx.commit();
    }
}
