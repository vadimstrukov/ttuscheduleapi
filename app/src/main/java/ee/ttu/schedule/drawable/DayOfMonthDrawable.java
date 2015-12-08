package ee.ttu.schedule.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.vadimstrukov.ttuschedule.R;

public class DayOfMonthDrawable extends Drawable {
    private String dayOfMonth = "1";
    private final Paint paint;
    private final Rect textBounds = new Rect();

    public DayOfMonthDrawable(Context context) {
        float textSize = context.getResources().getDimension(R.dimen.today_icon_text_size);
        paint = new Paint();
        paint.setAlpha(255);
        paint.setColor(ContextCompat.getColor(context, android.R.color.white));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public void draw(Canvas canvas) {
        paint.getTextBounds(dayOfMonth, 0, dayOfMonth.length(), textBounds);
        Rect bounds = getBounds();
        canvas.drawText(dayOfMonth, bounds.right/2, (float) (0.5*(bounds.bottom + textBounds.bottom - textBounds.top + 5)), paint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
    public void setDayOfMonth(int day) {
        dayOfMonth = Integer.toString(day);
        invalidateSelf();
    }

}
