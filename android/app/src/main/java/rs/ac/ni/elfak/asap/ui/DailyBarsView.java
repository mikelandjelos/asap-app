package rs.ac.ni.elfak.asap.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.google.android.material.color.MaterialColors;

/** Scans per day for the last {@link AnalyticsModel#DAYS} days; today is the rightmost bar. */
public final class DailyBarsView extends View {

    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint today = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint base = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private int[] counts = new int[AnalyticsModel.DAYS];
    private String startLabel = "";
    private String endLabel = "";

    public DailyBarsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bar.setColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary));
        bar.setAlpha(150);
        today.setColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary));
        base.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline));
        text.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant));
        text.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11, getResources().getDisplayMetrics()));
    }

    public void setData(int[] counts, String startLabel, String endLabel) {
        this.counts = counts.clone();
        this.startLabel = startLabel;
        this.endLabel = endLabel;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float density = getResources().getDisplayMetrics().density;
        float textH = text.getTextSize() + 4 * density;
        float chartH = getHeight() - getPaddingTop() - getPaddingBottom() - textH;
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        int max = 1;
        for (int c : counts) {
            max = Math.max(max, c);
        }
        float slot = width / counts.length;
        float bottom = getPaddingTop() + chartH;
        for (int i = 0; i < counts.length; i++) {
            float h = counts[i] == 0 ? 2 * density : chartH * counts[i] / max;
            float left = getPaddingLeft() + i * slot + slot * 0.18f;
            rect.set(left, bottom - h, left + slot * 0.64f, bottom);
            canvas.drawRoundRect(rect, 3 * density, 3 * density, i == counts.length - 1 ? today : bar);
        }
        canvas.drawRect(getPaddingLeft(), bottom, getPaddingLeft() + width, bottom + density, base);
        canvas.drawText(startLabel, getPaddingLeft(), bottom + textH, text);
        canvas.drawText(endLabel, getPaddingLeft() + width - text.measureText(endLabel), bottom + textH, text);
    }
}
