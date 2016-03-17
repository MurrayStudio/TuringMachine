package com.murraystudio.turingmachine;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnItemClickListener {

    protected MainAdapter mainAdapter;
    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutManager;
    protected String[] mDataset;

    private Button btnStep;
    private EditText inputEditText;
    private String inputString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainAdapter = new MainAdapter(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mainAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mainAdapter);

        inputEditText = (EditText) findViewById(R.id.input_edit_text);
        btnStep = (Button) findViewById(R.id.stepBtn);

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


        btnStep = (Button)findViewById(R.id.stepBtn);
        btnStep.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String newName = "poop";

                if(!newName.equals("")){
                    if(mainAdapter.getItemCount()>1){
                        mainAdapter.add(0, newName);
                        layoutManager.scrollToPosition(0);
                    }else{
                        mainAdapter.add(0, newName);
                    }
                }
            }
        });

    }

    @Override
    public void onItemClick(MainAdapter.ItemHolder item, int position) {
        Toast.makeText(this,
                "Remove " + position + " : " + item.getItemName(),
                Toast.LENGTH_SHORT).show();
        mainAdapter.remove(position);
    }


    private void initDataset() {
        //mDataset = getResources().getStringArray(R.array.nav_card_titles);
    }
}
