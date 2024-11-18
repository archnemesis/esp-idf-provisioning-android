package com.espressif.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class ColorChooser extends View {
    protected Paint paint;
    protected int color;

    public ColorChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(0xFFFF0000);
        color = 0xFFFF0000;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0.0f);
        canvas.drawCircle(
                getWidth() / 2.0f,
                getHeight() / 2.0f,
                (getWidth() - 4.0f) / 2.0f,
                paint);

        paint.setColor(0xFF000000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        canvas.drawCircle(
                getWidth() / 2.0f,
                getHeight() / 2.0f,
                (getWidth() - 4.0f) / 2.0f,
                paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(240, 240);
    }
}
