package rs.ac.ni.elfak.asap.ui;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import rs.ac.ni.elfak.asap.MainActivity;
import rs.ac.ni.elfak.asap.R;
import rs.ac.ni.elfak.asap.ScanSession;
import rs.ac.ni.elfak.asap.history.HistoryStore;

/** Device-only history (D-038): privacy notice, clear action with confirmation, newest-first list. */
public final class HistoryFragment extends Fragment {

    private TextView title;
    private TextView empty;
    private LinearLayout list;
    private View clear;
    private final ScanSession.Listener refresh = state -> {
        if (state.phase == ScanSession.Phase.LOADED) {
            render();
        }
    };

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        title = view.findViewById(R.id.history_title);
        empty = view.findViewById(R.id.history_empty);
        list = view.findViewById(R.id.history_list);
        clear = view.findViewById(R.id.history_clear);
        ViewCompat.setAccessibilityHeading(view.findViewById(R.id.privacy_title), true);
        ViewCompat.setAccessibilityHeading(title, true);
        clear.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.history_clear_confirm_title)
                .setMessage(R.string.history_clear_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.history_clear_confirm, (d, w) -> {
                    session().history().clear();
                    render();
                })
                .show());
    }

    @Override
    public void onStart() {
        super.onStart();
        session().addListener(refresh);
        render();
    }

    @Override
    public void onStop() {
        session().removeListener(refresh);
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            render();
        }
    }

    private ScanSession session() {
        return ((MainActivity) requireActivity()).session();
    }

    private void render() {
        if (list == null) {
            return;
        }
        List<HistoryStore.Entry> entries = session().history().entries();
        title.setText(getResources().getQuantityString(R.plurals.history_title, entries.size(), entries.size()));
        empty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        clear.setEnabled(!entries.isEmpty());
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        long now = System.currentTimeMillis();
        for (HistoryStore.Entry e : entries) {
            View item = inflater.inflate(R.layout.item_result, list, false);
            item.findViewById(R.id.result_rank).setVisibility(View.GONE);
            ((TextView) item.findViewById(R.id.result_title)).setText(e.name);
            CharSequence when = DateUtils.getRelativeTimeSpanString(e.occurredAtMs, now, DateUtils.MINUTE_IN_MILLIS);
            ((TextView) item.findViewById(R.id.result_subtitle)).setText(e.brand == null ? when : e.brand + " · " + when);
            ((TextView) item.findViewById(R.id.result_theme)).setText(e.themeLabel);
            ImageLoader.get().load(e.imageUrl, item.findViewById(R.id.result_image), R.drawable.ic_image_placeholder);
            item.setContentDescription(getString(R.string.history_item_description, e.name, e.themeLabel, when));
            if (e.barcodeValue != null) {
                item.setOnClickListener(v -> session().open(e.barcodeValue, e.barcodeFormat));
            }
            list.addView(item);
        }
    }
}
