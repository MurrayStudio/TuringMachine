package com.murraystudio.turingmachine;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    protected RecyclerView navRecyclerView;
    protected RecyclerView.LayoutManager navLayoutManager;
    protected String[] mDataset;

    private EditText inputEditText;
    private String inputString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEditText = (EditText) findViewById(R.id.input_edit_text);

        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    inputString = inputEditText.getText().toString();
                    Log.d("input: ", inputString);
                    return true;
                }
                return false;
            }
        });

    }

    private void initDataset() {
        //mDataset = getResources().getStringArray(R.array.nav_card_titles);
    }
}
