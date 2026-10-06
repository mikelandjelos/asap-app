package rs.ac.ni.elfak.asap.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;
import java.util.TimeZone;
import rs.ac.ni.elfak.asap.MainActivity;
import rs.ac.ni.elfak.asap.R;
import rs.ac.ni.elfak.asap.ScanSession;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;
import rs.ac.ni.elfak.asap.network.V2ApiModels;
import rs.ac.ni.elfak.asap.network.V2Client;

/** Personal analytics (D-039): PCA "you vs themes" as the main chart, then status, themes, activity and sources. */
public final class AnalyticsFragment extends Fragment {

    private ThemeMapView map;
    private TextView mapSelected;
    private TextView personalTitle;
    private LinearProgressIndicator personalProgress;
    private TextView personalMessage;
    private LinearLayout themesList;
    private TextView activityTitle;
    private DailyBarsView activityBars;
    private LinearLayout sourcesList;
    private List<V2ApiModels.ThemePoint> themes;
    private final ScanSession.Listener refresh = state -> {
        if (state.phase == ScanSession.Phase.LOADED) {
            render();
        }
    };

    public AnalyticsFragment() {
        super(R.layout.fragment_analytics);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        map = view.findViewById(R.id.theme_map);
        mapSelected = view.findViewById(R.id.map_selected);
        personalTitle = view.findViewById(R.id.personal_title);
        personalProgress = view.findViewById(R.id.personal_progress);
        personalMessage = view.findViewById(R.id.personal_message);
        themesList = view.findViewById(R.id.themes_list);
        activityTitle = view.findViewById(R.id.activity_title);
        activityBars = view.findViewById(R.id.activity_bars);
        sourcesList = view.findViewById(R.id.sources_list);
        for (int id : new int[] {R.id.map_title, R.id.personal_title, R.id.themes_title, R.id.activity_title, R.id.sources_title}) {
            ViewCompat.setAccessibilityHeading(view.findViewById(id), true);
        }
        map.setOnThemeSelected(t -> mapSelected.setText(
                getResources().getQuantityString(R.plurals.analytics_map_selected, t.size, t.label, t.size)));
        loadThemes();
    }

    private void loadThemes() {
        V2Client client = new ViewModelProvider(requireActivity()).get(SessionViewModel.class).client();
        client.catalogMap(new V2Client.Callback<V2ApiModels.CatalogMap>() {
            @Override
            public void onSuccess(V2ApiModels.CatalogMap value) {
                onMain(() -> {
                    themes = value.themes;
                    render();
                });
            }

            @Override
            public void onFailure(ScanQueryClient.Failure failure) {
                onMain(() -> mapSelected.setText(R.string.analytics_map_unavailable));
            }
        });
    }

    /** Network callbacks arrive off the main thread; deliver only while the view still exists. */
    private void onMain(Runnable action) {
        View view = getView();
        if (view != null) {
            view.post(() -> {
                if (getView() != null) {
                    action.run();
                }
            });
        }
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
            if (themes == null) {
                loadThemes();
            }
        }
    }

    private ScanSession session() {
        return ((MainActivity) requireActivity()).session();
    }

    private void render() {
        if (map == null) {
            return;
        }
        ScanSession.State state = session().state();
        double[] serverYou = state.response != null && state.response.you != null && state.response.you.mapPosition != null
                ? new double[] {state.response.you.mapPosition.x, state.response.you.mapPosition.y} : null;
        AnalyticsModel m = AnalyticsModel.from(session().history().entries(), serverYou, System.currentTimeMillis(),
                TimeZone.getDefault());

        map.setData(themes, m.historyPoints, m.latest, m.you);
        map.setContentDescription(m.topThemes.isEmpty() ? getString(R.string.analytics_map_description_empty)
                : getResources().getQuantityString(R.plurals.analytics_map_description, m.scans, m.scans,
                        m.topThemes.get(0).label));
        if (m.youIsEstimate && mapSelected.getText().length() == 0) {
            mapSelected.setText(R.string.analytics_map_you_estimate);
        }

        int missing = Math.max(0, AnalyticsModel.PERSONALIZE_AFTER - m.distinctProducts);
        personalProgress.setProgressCompat(Math.min(m.distinctProducts, AnalyticsModel.PERSONALIZE_AFTER), true);
        if (m.personalized()) {
            personalTitle.setText(R.string.analytics_personal_ready);
            personalMessage.setText(getResources().getQuantityString(R.plurals.analytics_personal_ready_message,
                    Math.min(m.scans, 20), Math.min(m.scans, 20)));
        } else {
            personalTitle.setText(R.string.analytics_personal_pending);
            personalMessage.setText(getResources().getQuantityString(R.plurals.analytics_personal_pending_message, missing, missing));
        }

        bars(themesList, m.topThemes);
        bars(sourcesList, m.sources);

        int total = 0;
        StringBuilder spoken = new StringBuilder();
        for (int c : m.daily) {
            total += c;
            spoken.append(spoken.length() == 0 ? "" : ", ").append(c);
        }
        activityTitle.setText(getResources().getQuantityString(R.plurals.analytics_activity_title, total, total));
        activityBars.setData(m.daily, getString(R.string.analytics_activity_start), getString(R.string.analytics_activity_end));
        activityBars.setContentDescription(getString(R.string.analytics_activity_description, spoken));
    }

    private void bars(LinearLayout list, List<AnalyticsModel.Count> counts) {
        list.removeAllViews();
        if (counts.isEmpty()) {
            TextView none = new TextView(requireContext());
            none.setText(R.string.analytics_none);
            list.addView(none);
            return;
        }
        int max = counts.get(0).count;
        for (AnalyticsModel.Count c : counts) {
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_bar_row, list, false);
            ((TextView) row.findViewById(R.id.bar_label)).setText(getString(R.string.analytics_bar, c.label, c.count));
            LinearProgressIndicator bar = row.findViewById(R.id.bar_value);
            bar.setMax(max);
            bar.setProgressCompat(c.count, false);
            list.addView(row);
        }
    }
}
