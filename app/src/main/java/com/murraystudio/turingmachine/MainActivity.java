package com.murraystudio.turingmachine;

import android.content.res.ObbInfo;
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

    //Android related vars
    protected MainAdapter mainAdapter;
    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutManager;
    protected String[] mDataset;

    private Button btnStep;
    private EditText inputEditText;
    private String inputString;

    private View.OnClickListener messageListener;


    //turing machine related vars

    // Var to hold execution until user prompts for next state
    boolean nextStep = false;

    boolean hasStart = false;

    private String randomText;

    // Static variable to hold the original input without having to pass it through a bunch
    public static String initialIn = "";

    String pointer; //points to where we are on turing machine

    int index;

    //String message; //text for each card

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        randomText = "memes";

        inputString = "";

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

        View.OnClickListener startListener = new View.OnClickListener() {
            public void onClick(View v) {
                inputString = inputEditText.getText().toString();
                String newName = inputString;

                if(!hasStart) {
                    turingMachine();
                    hasStart = true;
                }

                if(!newName.equals("")){
                    if(mainAdapter.getItemCount()>1){
                        //mainAdapter.add(0, newName);
                        //layoutManager.scrollToPosition(0);
                    }else{
                        //mainAdapter.add(0, newName);
                    }
                }
            }
        };

        messageListener = new View.OnClickListener() {
            public void onClick(View v) {
                inputString = inputEditText.getText().toString();
                String newName = inputString;

                if(hasStart) {
                    printMessage(inputString + "\n" + pointer + randomText);
                }

                if(!newName.equals("")){
                    if(mainAdapter.getItemCount()>1){
                        //mainAdapter.add(0, newName);
                        //layoutManager.scrollToPosition(0);
                    }else{
                        //mainAdapter.add(0, newName);
                    }
                }
            }
        };

        btnStep = (Button)findViewById(R.id.stepBtn);
        btnStep.setOnClickListener(startListener);


/*        btnStep = (Button)findViewById(R.id.stepBtn);
        btnStep.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                inputString = inputEditText.getText().toString();
                String newName = inputString;

                if(hasStart){
                    nextStep = true;
                }

                if(!hasStart) {
                    turingMachine();
                    hasStart = true;
                }

                if(!newName.equals("")){
                    if(mainAdapter.getItemCount()>1){
                        //mainAdapter.add(0, newName);
                        //layoutManager.scrollToPosition(0);
                    }else{
                        //mainAdapter.add(0, newName);
                    }
                }
            }
        });*/

    }

    private void turingMachine(){
        inputString = inputEditText.getText().toString();
        initialIn = inputString;
        pointer = "^";
        index = 0;

        // Give the user the benefit of the doubt
        boolean cheater = false;

        // Iterate through the input string and add spaces to the pointer to match length
        for (int i = 0; i < inputString.length(); i++)
        {
            // If the input string has # characters (marked by TM), call out the user for cheating.
            if (inputString.charAt(i) == '#' && !cheater)
            {
                //System.out.println("I see you trying to be clever. Throwing in your own pre-marked symbols. No cheating.");
                //System.out.println("Let's see what happens when we run your string anyway. Cheater.");
                printMessage("CHEATER!");
                cheater = true;
            }
            pointer += ' ';
        }

        // Trim leading underscores
        int counter = 0;
        for (int i = 0; i < inputString.length(); i++)
        {
            if (inputString.charAt(i) == '_')
            {
                counter++;
            }
            else
            {
                break;
            }
        }

        //if no input entered
        if (inputString.length() == 0)
        {
            printMessage("Initial state:" + "\n" + inputString + "\n" + pointer + "\n" + "BLANK ENTRY!");

            //qAccept();
        }

        //trim any superfluous characters
        inputString = inputString.substring(counter);
        inputString = inputString.trim();

        //intro
        printMessage("Initial state:" + "\n" + inputString + "\n" + pointer);

        // Begin simulation
        q0(inputString, pointer, index);

    }

    /**
     * q0 - Initial state of TM execution.
     *
     * Transitions: (# -> #, Right):    q0
     *              (a -> #, Right):    q1
     *              (_ -> _, Right):    qAccept
     *              (other):            qReject
     *
     * IMPORTANT: Only state which can transition to qAccept.
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param index Numerical location of the TM execution head.
     */
    public void q0(String input, String pointer, int index)
    {
        // Self loop conditions
        if(input.charAt(index) == '#')
        {
            // Move pointer right + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();

            // Print state of Turing machine with helper
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            // Loop back to state q0
            q0(input, pointer, index);
        }
        // Next state condition
        else if (input.charAt(index) == 'a')
        {
            System.out.println("Marked character " + input.charAt(index) + " with #.");

            // Mark character with #
            input = input.substring(0, index) + '#' + input.substring(index + 1);

            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            // Move pointer right + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q1(input, pointer, index);
        }
        else if (input.charAt(index) == '_')
        {
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();

            // Print state of turing machine with helper
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            // Move to qAccept
            qAccept();
        }
        else
        {
            qReject(input, index);
        }
    }

    /**
     * q1 - Second state of TM execution.
     *
     * Transitions: ([a, #] -> [a, #], Right):  q1
     *              (b -> #, Right):            q2
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param index Numerical location of the TM execution head.
     */
    public void q1(String input, String pointer, int index)
    {

        // Self loop conditions
        if (input.charAt(index) == 'a' || input.charAt(index) == '#')
        {
            // Move pointer right + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q1(input, pointer, index);
        }
        // Next state condition
        else if (input.charAt(index) == 'b')
        {
            System.out.println("Marked character " + input.charAt(index) + " with #.");

            // Mark character with #
            input = input.substring(0, index) + '#' + input.substring(index + 1);

            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            // Move pointer right + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q2(input, pointer, index);
        }
        else
        {
            qReject(input, index);
        }
    }

    /**
     * q2 - Third state of TM execution.
     *
     * Transitions: ([b, #] -> [b, #], Right):  q2
     *              (c -> #, Right):            q3
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param index Numerical location of the TM execution head.
     */
    public void q2(String input, String pointer, int index)
    {
        // Self loop conditions
        if (input.charAt(index) == 'b' || input.charAt(index) == '#')
        {
            // Move pointer right + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q2(input, pointer, index);
        }
        // Next state condition
        else if (input.charAt(index) == 'c')
        {
            System.out.println("Marked character " + input.charAt(index) + " with #.");

            // Mark character with #
            input = input.substring(0, index) + '#' + input.substring(index + 1);

            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            // Move pointer right + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q3(input, pointer, index);
        }
        else
        {
            qReject(input, index);
        }
    }

    /**
     * q3 - Fourth state of TM execution.
     *
     * Transitions: ([c, _] -> [c, _], Left):   q4
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param index Numerical location of the TM execution head.
     */
    public void q3(String input, String pointer, int index)
    {
        // Reset condition
        if (input.charAt(index) == 'c' || input.charAt(index) == '_')
        {
            // Move pointer left + retrieve outputs
            String[] output = left(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q4(input, pointer, index);
        }
        else
        {
            qReject(input, index);
        }
    }

    /**
     * q4 - Fifth state of TM execution.
     *
     * Transitions: ([b, #] -> [b, #], Left):   q4
     *              (a -> a, Left):             q5
     *              (_ -> _, Right):            q0
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param index Numerical location of the TM execution head.
     */
    public void q4(String input, String pointer, int index)
    {
        // Self loop conditions
        if (input.charAt(index) == 'b' || input.charAt(index) == '#')
        {
            // Move pointer left + retrieve outputs
            String[] output = left(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q4(input, pointer, index);
        }
        // Return to first state condition
        else if (input.charAt(index) == 'a')
        {
            // Move pointer left + retrieve outputs
            String[] output = left(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q5(input, pointer, index);
        }
        else if (input.charAt(index) == '_')
        {
            // Move pointer left + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q0(input, pointer, index);
        }
        else
        {
            qReject(input, index);
        }
    }

    /**
     * q5 - Sixth state of TM execution.
     *
     * Transitions: (a -> a, Left):     q5
     *              (# -> #, Right):    q0
     *              (other):            qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param index Numerical location of the TM execution head.
     */
    public void q5(String input, String pointer, int index)
    {
        // Self loop conditions
        if (input.charAt(index) == 'a')
        {
            // Move pointer right + retrieve outputs
            String[] output = left(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q5(input, pointer, index);
        }
        // Next state condition
        else if (input.charAt(index) == '#')
        {
            // Move pointer right + retrieve outputs
            String[] output = right(input, pointer, index);
            input = output[0];
            pointer = output[1];
            index = output[2].length();
            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            q0(input, pointer, index);
        }
        else
        {
            qReject(input, index);
        }
    }

    /**
     * qAccept - Accept state of TM execution. This state halts execution and
     *              prints a message to indicate that execution was successful.
     */
    public void qAccept()
    {
        //System.out.println("Result of execution: Accept");
        //System.out.println("Your original input: " + initialIn);

        printMessage("Result of execution: Accept" + "\n" + "Your original input: " + initialIn);
    }

    /**
     * qReject - Reject state of TM execution. This state halts execution and
     *              prints a message indicating what character caused the halt.
     *
     * @param input String currently on the TM tape.
     * @param index Location of character that caused halt.
     */
    public void qReject(String input, int index)
    {
        //System.out.println("Result of execution: Reject");
        //System.out.println("Your original input: " + initialIn);
        //System.out.println("Halt on character: " + input.charAt(index));
        printMessage("Result of execution: Reject" + "\n" + "Your original input: " + initialIn + "\n" + "Halt on character: " + input.charAt(index));
    }

    private void printMessage(String text){



        randomText = "beams";

        //it is now true so print
        if(!text.equals("")){
            mainAdapter.add(0, text);
            layoutManager.scrollToPosition(0);
        }

        nextStep = false;
    }

    public String[] left(String input, String pointer, int index)
    {
        //System.out.println("Moved left from character " + input.charAt(index) + '.');
        //printMessage("Moved left from character " + input.charAt(index) + '.');

        // Move index one left
        index -= 1;

        for (int i = 0; i < input.length(); i++)
        {
            if (pointer.charAt(i) == '^')
            {
                // If the carrot was at the far left, add an underscore to the
                // left of input and a space to the right of pointer
                if (i == 0)
                {
                    input = '_' + input;
                    pointer += ' ';

                    // Reset index to 0 so we don't get out of bounds exception
                    index = 0;

                    break;
                }

                // Construct the new pointer string from the old one, modified
                String tempLeft = pointer.substring(0, i-1);
                String tempLeft1 = pointer.substring(i + 1);
                pointer = tempLeft + "^ " + tempLeft1;

                break;
            }
        }

        // Output array (I really didn't want to make arraylists so the index is expressed as
        // a number of spaces equal to the index.)
        // DON'T JUDGE ME, I WAS TIRED. YOU WOULD HAVE DONE THE SAME.
        String[] output = new String[3];
        output[0] = input;
        output[1] = pointer;
        String outTemp = "";
        for (int i = 0; i < index; i++)
        {
            outTemp += ' ';
        }
        output[2] = outTemp;

        return output;
    }

    /**
     * right - Method to move pointer carrot one space to the right. If necessary,
     *          adds an underscore character to the right of the input string, and a space
     *          to the left of the pointer string.
     *
     * @param input String currently on the TM tape.
     * @param pointer Pointer string with TM execution head.
     * @param index Numerical location of TM execution head.
     * @return Array of three strings: input string, pointer string, index (in spaces).
     */
    public String[] right(String input, String pointer, int index)
    {
        //System.out.println("Moved right from character " + input.charAt(index) + '.');
        //printMessage("Moved right from character " + input.charAt(index) + '.');

        // Move index one right
        index += 1;

        // Find the pointer carrot in the pointer string and move it right
        for (int i = 0; i < input.length(); i++)
        {
            if (pointer.charAt(i) == '^')
            {
                // If the carrot was at the far right, add an underscore to input
                if (i == input.length() - 1)
                {
                    input += '_';
                }

                // Construct the new pointer string from the old one, modified
                String tempRight = pointer.substring(0, i);
                String tempRight1 = pointer.substring(i+1, input.length());
                pointer = tempRight + " ^" + tempRight1;

                break;
            }
        }

        // Yeah, yeah, poorly coded, I know.
        String[] output = new String[3];
        output[0] = input;
        output[1] = pointer;
        String outTemp = "";
        for (int i = 0; i < index; i++)
        {
            outTemp += ' ';
        }
        output[2] = outTemp;

        return output;
    }

    @Override
    public void onItemClick(MainAdapter.ItemHolder item, int position) {
        Toast.makeText(this,
                "Remove " + position + " : " + item.getItemName(),
                Toast.LENGTH_SHORT).show();
        mainAdapter.remove(position);
    }
}
