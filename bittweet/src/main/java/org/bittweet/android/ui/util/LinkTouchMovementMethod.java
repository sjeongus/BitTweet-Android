package org.bittweet.android.ui.util;

import android.graphics.Color;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.bittweet.android.ui.util.TweetFormatter;

/**
 * Created by soomin on 9/1/2014.
 */
public class LinkTouchMovementMethod extends LinkMovementMethod {
    private TweetFormatter.TouchableSpan mPressedSpan;

    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        View grandparentView = (View) textView.getParent().getParent();
        View parentView = (View) textView.getParent();
        Rect grandpaRect = new Rect();
        grandparentView.getHitRect(grandpaRect);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null) {
                mPressedSpan.setPressed(true);
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan));
            } else {
                //grandparentView.setBackgroundColor(Color.LTGRAY);
                parentView.onTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            TweetFormatter.TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                mPressedSpan.setPressed(false);
                mPressedSpan = null;
                Selection.removeSelection(spannable);
                //grandparentView.setBackgroundColor(Color.WHITE);
                /*if (!grandpaRect.contains((int)event.getX(), (int)event.getY())) {
                    grandparentView.setBackgroundColor(Color.WHITE);
                    mPressedSpan.setPressed(false);
                }*/
            }
        } else {
            if (mPressedSpan != null) {
                mPressedSpan.setPressed(false);
                super.onTouchEvent(textView, spannable, event);
            } else {
                //grandparentView.setBackgroundColor(Color.WHITE);
                parentView.onTouchEvent(event);
            }
            mPressedSpan = null;
            Selection.removeSelection(spannable);
            //grandparentView.setBackgroundColor(Color.WHITE);
        }
        return false;
        //return super.onTouchEvent(textView, spannable, event);
    }

    private TweetFormatter.TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        TweetFormatter.TouchableSpan[] link = spannable.getSpans(off, off, TweetFormatter.TouchableSpan.class);
        TweetFormatter.TouchableSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }

}