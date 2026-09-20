package com.falcon.car;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        textView.setText("Hello, falcon.car!");
        textView.setTextSize(24);
        textView.setPadding(16, 16, 16, 16);
        setContentView(textView);
    }
}
