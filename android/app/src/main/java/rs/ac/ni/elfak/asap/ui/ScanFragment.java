package rs.ac.ni.elfak.asap.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rs.ac.ni.elfak.asap.MainActivity;
import rs.ac.ni.elfak.asap.R;
import rs.ac.ni.elfak.asap.ScanSession;
import rs.ac.ni.elfak.asap.history.HistoryStore;

/** Scan entry point plus the five most recent distinct products from the device history. */
public final class ScanFragment extends Fragment {

    private static final int RECENT = 5;
    private TextView status;
    private LinearLayout recentList;
    private TextView recentEmpty;
    private final ScanSession.Listener refresh = state -> {
        if (state.phase == ScanSession.Phase.LOADED) {
            renderRecent();
        } else if (state.phase == ScanSession.Phase.EMPTY_BARCODE) {
            showStatus(R.string.scan_empty);
        } else if (state.phase == ScanSession.Phase.UNSUPPORTED_BARCODE) {
            showStatus(R.string.scan_unsupported);
        }
    };

    public ScanFragment() {
        super(R.layout.fragment_scan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        status = view.findViewById(R.id.scan_status);
        recentList = view.findViewById(R.id.recent_list);
        recentEmpty = view.findViewById(R.id.recent_empty);
        MaterialButton scan = view.findViewById(R.id.scan_button);
        scan.setOnClickListener(v -> activity().startScan());
        renderRecent();
    }

    @Override
    public void onStart() {
        super.onStart();
        activity().setScanStatusListener(this::showStatus);
        activity().session().addListener(refresh);
    }

    @Override
    public void onStop() {
        activity().session().removeListener(refresh);
        activity().setScanStatusListener(null);
        super.onStop();
    }

    private MainActivity activity() {
        return (MainActivity) requireActivity();
    }

    private void showStatus(int messageRes) {
        if (status == null) {
            return;
        }
        status.setVisibility(messageRes == 0 ? View.GONE : View.VISIBLE);
        if (messageRes != 0) {
            status.setText(messageRes);
        }
    }

    private void renderRecent() {
        if (recentList == null) {
            return;
        }
        recentList.removeAllViews();
        List<HistoryStore.Entry> entries = activity().session().history().entries();
        Set<String> seen = new HashSet<>();
        for (HistoryStore.Entry e : entries) {
            if (recentList.getChildCount() == RECENT) {
                break;
            }
            if (!seen.add(e.productId) || e.barcodeValue == null) {
                continue;
            }
            MaterialButton item = new MaterialButton(requireContext(), null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            item.setText(e.brand == null ? e.name : e.name + " · " + e.brand);
            item.setAllCaps(false);
            item.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            item.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
            item.setContentDescription(getString(R.string.recent_item_description, e.name));
            item.setOnClickListener(v -> activity().session().open(e.barcodeValue, e.barcodeFormat));
            recentList.addView(item, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        recentEmpty.setVisibility(recentList.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
