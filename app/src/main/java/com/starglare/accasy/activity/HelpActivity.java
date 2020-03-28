package com.starglare.accasy.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import com.starglare.accasy.R;

public class HelpActivity extends AppCompatActivity {
    private TextView howTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String howToText = "<p>1. Tap map to select a location or</p>" +
                "<p>2. Find your location using the GPS icon or </p>" +
                "<p>3. Search using search bar. </p>" +
                "<p>4. Fill the report form from the bottom sheet (slide up to reveal form).</p>" +
                "<p>5. Submit the report</p>" +
                "<br>" +
                "<p><strong>Note: </strong><br><i>In order to select a location and send a report, internet connection is required.<i></p>";

        howTo = findViewById(R.id.howTo);
        howTo.setText(Html.fromHtml(howToText));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
