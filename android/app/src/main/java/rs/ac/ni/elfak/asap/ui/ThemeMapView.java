package rs.ac.ni.elfak.asap.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.material.color.MaterialColors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

/**
 * "You vs themes" PCA(2) chart (D-033, D-039): 60 theme bubbles sized by theme size, the user's history as dots,
 * the latest scan as a ring and "You" as a star. Tapping selects the nearest theme.
 */
public final class ThemeMapView extends View {

    public interface OnThemeSelected {
        void onTheme(V2ApiModels.ThemePoint theme);
    }

    private static final int LABELS_ALL = 8;
    private static final int LABELS_FOCUS = 3;
    private final Paint bubble = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selected = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint faint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint star = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path starPath = new Path();
    private List<V2ApiModels.ThemePoint> themes = Collections.emptyList();
    private List<V2ApiModels.ThemePoint> visible = Collections.emptyList();
    private List<V2ApiModels.ThemePoint> labelled = Collections.emptyList();
    private boolean focused;
    private List<double[]> history = Collections.emptyList();
    private double[] latest;
    private double[] you;
    private V2ApiModels.ThemePoint chosen;
    private OnThemeSelected listener;
    private final RectF bounds = new RectF();

    public ThemeMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int primary = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary);
        int secondary = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary);
        int onSurface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface);
        bubble.setColor(primary);
        bubble.setAlpha(46);
        bubbleStroke.setStyle(Paint.Style.STROKE);
        bubbleStroke.setColor(primary);
        bubbleStroke.setAlpha(110);
        bubbleStroke.setStrokeWidth(dp(1));
        selected.setStyle(Paint.Style.STROKE);
        selected.setColor(primary);
        selected.setStrokeWidth(dp(2.5f));
        faint.setColor(onSurface);
        faint.setAlpha(40);
        dot.setColor(secondary);
        dot.setAlpha(170);
        ring.setStyle(Paint.Style.STROKE);
        ring.setColor(secondary);
        ring.setStrokeWidth(dp(2.5f));
        star.setColor(secondary);
        label.setColor(onSurface);
        label.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11, getResources().getDisplayMetrics()));
        setClickable(true);
    }

    /**
     * @param focusLabels the user's top themes, most scanned first; when non-empty and {@code showAll} is false the
     *                    chart zooms to them, draws only them as bubbles and shows every other theme as a faint dot
     */
    public void setData(List<V2ApiModels.ThemePoint> themes, List<double[]> history, double[] latest, double[] you,
            List<String> focusLabels, boolean showAll) {
        this.themes = themes == null ? Collections.<V2ApiModels.ThemePoint>emptyList() : themes;
        this.history = history;
        this.latest = latest;
        this.you = you;
        List<V2ApiModels.ThemePoint> focus = new ArrayList<>();
        for (String l : focusLabels) {
            for (V2ApiModels.ThemePoint t : this.themes) {
                if (t.label.equals(l)) {
                    focus.add(t);
                }
            }
        }
        focused = !showAll && !focus.isEmpty();
        if (focused) {
            visible = focus;
            labelled = focus.subList(0, Math.min(LABELS_FOCUS, focus.size()));
        } else {
            visible = this.themes;
            List<V2ApiModels.ThemePoint> sorted = new ArrayList<>(this.themes);
            Collections.sort(sorted, (a, b) -> Integer.compare(b.size, a.size));
            labelled = sorted.subList(0, Math.min(LABELS_ALL, sorted.size()));
        }
        if (chosen != null && !visible.contains(chosen)) {
            chosen = null;
        }
        computeBounds();
        invalidate();
    }

    public void setOnThemeSelected(OnThemeSelected listener) {
        this.listener = listener;
    }

    private void computeBounds() {
        bounds.setEmpty();
        boolean first = true;
        List<double[]> all = new ArrayList<>(history);
        for (V2ApiModels.ThemePoint t : visible) {
            all.add(new double[] {t.x, t.y});
        }
        if (you != null) {
            all.add(you);
        }
        for (double[] p : all) {
            if (first) {
                bounds.set((float) p[0], (float) p[1], (float) p[0], (float) p[1]);
                first = false;
            } else {
                bounds.union((float) p[0], (float) p[1]);
            }
        }
        float padX = Math.max(bounds.width() * (focused ? 0.2f : 0.08f), 1e-2f);
        float padY = Math.max(bounds.height() * (focused ? 0.2f : 0.08f), 1e-2f);
        bounds.inset(-padX, -padY);
    }

    private float sx(double x) {
        return (float) (getPaddingLeft() + (x - bounds.left) / bounds.width() * (getWidth() - getPaddingLeft() - getPaddingRight()));
    }

    /** PCA y grows upwards on the chart. */
    private float sy(double y) {
        return (float) (getPaddingTop() + (bounds.bottom - y) / bounds.height() * (getHeight() - getPaddingTop() - getPaddingBottom()));
    }

    private float radius(V2ApiModels.ThemePoint t) {
        return focused ? dp(10) + dp(0.9f) * (float) Math.sqrt(t.size) : dp(6) + dp(1.2f) * (float) Math.sqrt(t.size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (themes.isEmpty()) {
            return;
        }
        if (focused) {
            for (V2ApiModels.ThemePoint t : themes) {
                if (!visible.contains(t)) {
                    canvas.drawCircle(sx(t.x), sy(t.y), dp(2.5f), faint);
                }
            }
        }
        for (V2ApiModels.ThemePoint t : visible) {
            float x = sx(t.x);
            float y = sy(t.y);
            canvas.drawCircle(x, y, radius(t), bubble);
            canvas.drawCircle(x, y, radius(t), bubbleStroke);
        }
        for (double[] p : history) {
            canvas.drawCircle(sx(p[0]), sy(p[1]), dp(3.5f), dot);
        }
        if (latest != null) {
            canvas.drawCircle(sx(latest[0]), sy(latest[1]), dp(9), ring);
        }
        for (V2ApiModels.ThemePoint t : labelled) {
            drawLabel(canvas, t);
        }
        if (chosen != null && !labelled.contains(chosen)) {
            drawLabel(canvas, chosen);
        }
        if (chosen != null) {
            canvas.drawCircle(sx(chosen.x), sy(chosen.y), radius(chosen) + dp(2), selected);
        }
        if (you != null) {
            drawStar(canvas, sx(you[0]), sy(you[1]), dp(11));
        }
    }

    private void drawLabel(Canvas canvas, V2ApiModels.ThemePoint t) {
        String text = focused || t.label.length() <= 18 ? t.label : t.label.substring(0, 17) + "…";
        float width = label.measureText(text);
        float x = Math.max(getPaddingLeft(), Math.min(sx(t.x) - width / 2, getWidth() - getPaddingRight() - width));
        canvas.drawText(text, x, sy(t.y) - radius(t) - dp(3), label);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float r) {
        starPath.reset();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            float rr = i % 2 == 0 ? r : r * 0.45f;
            float x = cx + (float) (rr * Math.cos(angle));
            float y = cy - (float) (rr * Math.sin(angle));
            if (i == 0) {
                starPath.moveTo(x, y);
            } else {
                starPath.lineTo(x, y);
            }
        }
        starPath.close();
        canvas.drawPath(starPath, star);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && !visible.isEmpty()) {
            V2ApiModels.ThemePoint best = null;
            float bestD = Float.MAX_VALUE;
            for (V2ApiModels.ThemePoint t : visible) {
                float dx = sx(t.x) - event.getX();
                float dy = sy(t.y) - event.getY();
                float d = dx * dx + dy * dy;
                if (d < bestD) {
                    bestD = d;
                    best = t;
                }
            }
            chosen = best;
            invalidate();
            if (listener != null && best != null) {
                listener.onTheme(best);
            }
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
