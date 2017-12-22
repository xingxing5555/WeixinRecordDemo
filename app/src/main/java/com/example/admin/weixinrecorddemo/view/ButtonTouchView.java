package com.example.admin.weixinrecorddemo.view;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * @author Xinxin Shi
 */

public class ButtonTouchView extends AppCompatButton {

    public ButtonTouchView(Context context) {
        super(context);
    }

    public ButtonTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        return true;
    }
}
