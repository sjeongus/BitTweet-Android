package org.bittweet.android.ui.util;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class LinkTouchMovementMethod extends LinkMovementMethod {
    private TweetFormatter.TouchableSpan mPressedSpan;
    private boolean wantClick;

    public LinkTouchMovementMethod(boolean wantClick) {
        this.wantClick = wantClick;
    }

    // Change the color of links and mentions in a tweet
    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        View parentView = (View) textView.getParent();
        View grandparentView = (View) parentView.getParent();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null) {
                mPressedSpan.setPressed(true);
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan));
            } else if (wantClick) {
                grandparentView.onTouchEvent(event);
                parentView.onTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            TweetFormatter.TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                mPressedSpan.setPressed(false);
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            } else if (wantClick) {
                grandparentView.onTouchEvent(event);
                return false;
            }
        } else {
            if (mPressedSpan != null) {
                mPressedSpan.setPressed(false);
                super.onTouchEvent(textView, spannable, event);
            } else if (wantClick) {
                grandparentView.onTouchEvent(event);
                parentView.onTouchEvent(event);
            }
            mPressedSpan = null;
            Selection.removeSelection(spannable);
        }
        return true;
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