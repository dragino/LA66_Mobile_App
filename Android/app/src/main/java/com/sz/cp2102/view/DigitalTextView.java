package com.sz.cp2102.view;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class DigitalTextView extends androidx.appcompat.widget.AppCompatTextView {
    public DigitalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        String file = "lcdfont.ttf";
        AssetManager assets = context.getAssets();
        Typeface font = Typeface.createFromAsset(assets, file);
        setTypeface(font);
    }
}
