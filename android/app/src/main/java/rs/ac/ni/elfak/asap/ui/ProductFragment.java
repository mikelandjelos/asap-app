package rs.ac.ni.elfak.asap.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import rs.ac.ni.elfak.asap.MainActivity;
import rs.ac.ni.elfak.asap.R;
import rs.ac.ni.elfak.asap.ScanSession;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

/** Product outcome screen. S7b renders state and a summary; S7c adds the full card and results list. */
public final class ProductFragment extends Fragment {

    private View progress;
    private TextView message;
    private View retry;
    private final ScanSession.Listener render = this::render;

    public ProductFragment() {
        super(R.layout.fragment_product);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progress = view.findViewById(R.id.product_progress);
        message = view.findViewById(R.id.product_message);
        retry = view.findViewById(R.id.product_retry);
        retry.setOnClickListener(v -> session().retry());
    }

    @Override
    public void onStart() {
        super.onStart();
        session().addListener(render);
    }

    @Override
    public void onStop() {
        session().removeListener(render);
        super.onStop();
    }

    private ScanSession session() {
        return ((MainActivity) requireActivity()).session();
    }

    private void render(ScanSession.State state) {
        progress.setVisibility(state.phase == ScanSession.Phase.LOADING ? View.VISIBLE : View.GONE);
        retry.setVisibility(state.phase == ScanSession.Phase.FAILED
                || (state.response != null && "UNAVAILABLE".equals(state.response.product.status)) ? View.VISIBLE : View.GONE);
        switch (state.phase) {
            case LOADING:
                message.setText(getString(R.string.product_loading, state.barcode));
                break;
            case FAILED:
                message.setText(failureText(state.failure));
                break;
            case LOADED:
                message.setText(summary(state));
                break;
            default:
                message.setText(R.string.product_none);
        }
    }

    private String summary(ScanSession.State state) {
        V2ApiModels.ScanQueryResponse r = state.response;
        if ("UNKNOWN".equals(r.product.status)) {
            return getString(R.string.product_unknown, state.barcode);
        }
        if ("UNAVAILABLE".equals(r.product.status)) {
            return getString(R.string.product_unavailable);
        }
        int count = r.recommendations.items == null ? 0 : r.recommendations.items.size();
        String mode = getString("PERSONALIZED_HISTORY".equals(r.recommendations.mode) ? R.string.mode_personal : R.string.mode_similar);
        return r.product.data.name + "\n" + r.product.data.theme.label + "\n\n" + getResources().getQuantityString(R.plurals.results_count, count, count, mode);
    }

    private String failureText(ScanQueryClient.Failure failure) {
        switch (failure.kind()) {
            case TRANSPORT:
                return getString(R.string.error_transport);
            case HTTP:
                return getString(R.string.error_http, failure.httpStatus());
            default:
                return getString(R.string.error_invalid);
        }
    }
}
