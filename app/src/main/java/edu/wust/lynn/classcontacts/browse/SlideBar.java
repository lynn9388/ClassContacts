package edu.wust.lynn.classcontacts.browse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by lynn on 14-9-9.
 */
public class SlideBar extends View {
    private Paint paint = new Paint();
    private OnTouchLetterChangeListenner listenner;

    private boolean showBg = false;
    private int choose = -1;
    public static String[] letters = { "#", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z" };

    public SlideBar(Context context) {
        super(context);
    }

    public SlideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight() - 30;
        int singleHeight = height / letters.length;
        if (showBg) {
            canvas.drawColor(Color.parseColor("#55000000"));
        }

        for (int i = 0; i < letters.length; i++) {
            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(20f);

            if (i == choose) {
                paint.setColor(Color.parseColor("#F88701"));
                paint.setFakeBoldText(true);
            }
            // 要画的字母的x,y坐标
            float posX = width / 2 - paint.measureText(letters[i]) / 2;
            float posY = i * singleHeight + singleHeight;
            // 画出字母
            canvas.drawText(letters[i], posX, posY, paint);
            // 重新设置画笔
            paint.reset();
        }
    }

    /**
     * 处理SlideBar的状态
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float y = event.getY();
        // 算出点击的字母的索引
        final int index = (int) (y / getHeight() * letters.length);
        // 保存上次点击的字母的索引到oldChoose
        final int oldChoose = choose;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showBg = true;
                if (oldChoose != index && listenner != null && index > 0
                        && index < letters.length) {
                    choose = index;
                    listenner.onTouchLetterChange(event, letters[index]);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (oldChoose != index && listenner != null && index > 0
                        && index < letters.length) {
                    choose = index;
                    listenner.onTouchLetterChange(event, letters[index]);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                showBg = false;
                choose = -1;
                if (listenner != null && index > 0 && index < letters.length)
                    listenner.onTouchLetterChange(event, letters[index]);
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 回调方法，注册监听器
     *
     * @param listenner
     */
    public void setOnTouchLetterChangeListenner(
            OnTouchLetterChangeListenner listenner) {
        this.listenner = listenner;
    }

    /**
     * SlideBar 的监听器接口
     *
     * @author Folyd
     *
     */
    public interface OnTouchLetterChangeListenner {

        void onTouchLetterChange(MotionEvent event, String s);
    }

}
