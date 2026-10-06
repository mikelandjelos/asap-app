package rs.ac.ni.elfak.asap.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import rs.ac.ni.elfak.asap.MainActivity;
import rs.ac.ni.elfak.asap.R;
import rs.ac.ni.elfak.asap.ScanSession;

/** Product card plus the labelled, ranked results list; every non-product state has its own message (S7c). */
public final class ProductFragment extends Fragment {

    private View stateBlock;
    private View productBlock;
    private View progress;
    private ImageView icon;
    private TextView stateTitle;
    private TextView stateMessage;
    private View retry;
    private ImageView image;
    private TextView name;
    private TextView details;
    private Chip theme;
    private TextView description;
    private TextView meta;
    private TextView resultsTitle;
    private TextView resultsSubtitle;
    private LinearLayout resultsList;
    private TextView attribution;
    private final ScanSession.Listener render = state -> render(ProductUiModel.from(state));

    public ProductFragment() {
        super(R.layout.fragment_product);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        stateBlock = view.findViewById(R.id.state_block);
        productBlock = view.findViewById(R.id.product_block);
        progress = view.findViewById(R.id.state_progress);
        icon = view.findViewById(R.id.state_icon);
        stateTitle = view.findViewById(R.id.state_title);
        stateMessage = view.findViewById(R.id.state_message);
        retry = view.findViewById(R.id.state_retry);
        image = view.findViewById(R.id.product_image);
        name = view.findViewById(R.id.product_name);
        details = view.findViewById(R.id.product_details);
        theme = view.findViewById(R.id.product_theme);
        description = view.findViewById(R.id.product_description);
        meta = view.findViewById(R.id.product_meta);
        resultsTitle = view.findViewById(R.id.results_title);
        resultsSubtitle = view.findViewById(R.id.results_subtitle);
        resultsList = view.findViewById(R.id.results_list);
        attribution = view.findViewById(R.id.attribution);
        retry.setOnClickListener(v -> session().retry());
        ViewCompat.setAccessibilityHeading(stateTitle, true);
        ViewCompat.setAccessibilityHeading(name, true);
        ViewCompat.setAccessibilityHeading(resultsTitle, true);
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

    private void render(ProductUiModel m) {
        boolean product = m.screen == ProductUiModel.Screen.PRODUCT;
        productBlock.setVisibility(product ? View.VISIBLE : View.GONE);
        stateBlock.setVisibility(product ? View.GONE : View.VISIBLE);
        if (product) {
            renderProduct(m);
        } else {
            renderState(m);
        }
    }

    private void renderState(ProductUiModel m) {
        boolean loading = m.screen == ProductUiModel.Screen.LOADING;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        icon.setVisibility(loading ? View.GONE : View.VISIBLE);
        retry.setVisibility(m.retry ? View.VISIBLE : View.GONE);
        switch (m.screen) {
            case LOADING:
                if (getView() != null) {
                    getView().scrollTo(0, 0);
                }
                set(R.string.state_loading_title, getString(R.string.state_loading_message, m.barcode), R.drawable.ic_product);
                break;
            case NOT_FOUND:
                set(R.string.state_not_found_title, getString(R.string.state_not_found_message, m.barcode), R.drawable.ic_search_off);
                break;
            case UNAVAILABLE:
                set(R.string.state_unavailable_title, getString(R.string.state_unavailable_message, m.barcode), R.drawable.ic_cloud_off);
                break;
            case OFFLINE:
                set(R.string.state_offline_title, getString(R.string.state_offline_message), R.drawable.ic_cloud_off);
                break;
            case SERVER_ERROR:
                set(R.string.state_server_title, getString(R.string.state_server_message, m.httpStatus), R.drawable.ic_cloud_off);
                break;
            case INVALID_RESPONSE:
                set(R.string.state_invalid_title, getString(R.string.state_invalid_message), R.drawable.ic_cloud_off);
                break;
            default:
                set(R.string.state_none_title, getString(R.string.state_none_message), R.drawable.ic_scan);
        }
    }

    private void set(int title, String message, int iconRes) {
        stateTitle.setText(title);
        stateMessage.setText(message);
        icon.setImageResource(iconRes);
    }

    private void renderProduct(ProductUiModel m) {
        name.setText(m.name);
        ImageLoader.get().load(m.imageUrl, image, R.drawable.ic_image_placeholder);
        showOrHide(details, m.details);
        showOrHide(description, m.description);
        theme.setText(m.theme);
        theme.setContentDescription(getString(R.string.theme_description, m.theme));
        meta.setText(getString(R.string.product_meta, m.barcode, m.source));
        attribution.setText(m.attribution);
        switch (m.resultsKind) {
            case PERSONAL:
                resultsTitle.setText(R.string.mode_personal);
                resultsSubtitle.setText(getResources().getQuantityString(
                        R.plurals.results_personal_subtitle, m.historyUsed, m.historyUsed));
                break;
            case SIMILAR:
                resultsTitle.setText(R.string.mode_similar);
                resultsSubtitle.setText(R.string.results_similar_subtitle);
                break;
            default:
                resultsTitle.setText(R.string.mode_empty);
                resultsSubtitle.setText(R.string.results_empty_subtitle);
        }
        resultsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ProductUiModel.Row row : m.rows) {
            View item = inflater.inflate(R.layout.item_result, resultsList, false);
            ((TextView) item.findViewById(R.id.result_rank)).setText(String.valueOf(row.rank));
            ((TextView) item.findViewById(R.id.result_title)).setText(row.title);
            ImageLoader.get().load(row.imageUrl, item.findViewById(R.id.result_image), R.drawable.ic_image_placeholder);
            showOrHide(item.findViewById(R.id.result_subtitle), row.subtitle);
            ((TextView) item.findViewById(R.id.result_theme)).setText(row.theme);
            item.setContentDescription(getString(R.string.result_description, row.rank, row.title, row.theme));
            if (row.barcodeValue != null) {
                item.setOnClickListener(v -> session().open(row.barcodeValue, row.barcodeFormat));
            }
            resultsList.addView(item);
        }
    }

    private static void showOrHide(View view, String text) {
        TextView tv = (TextView) view;
        tv.setVisibility(text == null ? View.GONE : View.VISIBLE);
        tv.setText(text);
    }
}
