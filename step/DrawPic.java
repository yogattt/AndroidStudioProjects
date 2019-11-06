package com.xmobileapp.android.step;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DrawPic extends View{
    public class fPoint{
        int x;
        float y;
        fPoint(int xx,float yy)
        {
            x=xx;
            y=yy;
        }
    }
    private List PointList = new ArrayList();
    public Paint mPoint = new Paint();

    public DrawPic(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawPic(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPoint.setColor(Color.BLUE);
        mPoint.setStrokeWidth(4.0f);
        mPoint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (PointList.size() >= 2) {
            for (int k = 0; k < PointList.size()-1; k++) {
                canvas.drawLine(
                        ((fPoint) PointList.get(k)).x,
                        ((fPoint) PointList.get(k)).y,
                        ((fPoint) PointList.get(k + 1)).x,
                        ((fPoint) PointList.get(k + 1)).y, mPoint);
            }
        }
    }

    public final void ClearList() {
        PointList.clear();
    }
    public void AddPointToList(float X) {
        fPoint Point1=new fPoint(0,X+50);
        int i = PointList.size();
        if(i>=420)
            PointList.remove(0);
        int j = 0;
        while (j < PointList.size()) {
            fPoint localPoint = (fPoint) PointList.get(j);
            localPoint.x = (localPoint.x+5);
            j++;
        }
        PointList.add(Point1);
        invalidate();
    }
}