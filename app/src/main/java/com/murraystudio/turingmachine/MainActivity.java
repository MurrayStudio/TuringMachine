/**
 * Author: Shamus Murray
 *
 * Class holds main Turing Machine logic and UI Components for Android app.
 */

package com.murraystudio.turingmachine;

import android.content.res.ObbInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Android related vars
    protected MainAdapter mainAdapter;
    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutManager;

    private Button btnStep;
    private EditText inputEditText;
    private String inputString;


    //Turing machine related vars
    private boolean hasStart = false;

    // keep copy of orignal input
    public static String initialInput = "";

    private int clickIndex;

    String pointerVar; //points to where we are on turing machine

    int pointerPosition; //holds numerical length of where head is


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputString = "";

        mainAdapter = new MainAdapter(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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

                //check for blank entry
                if(!inputString.equals("")) {
                    if (hasStart) {
                        if (clickIndex < mainAdapter.getItemNameCount() - 1) {
                            //make cards for each step (clickIndex = total cards to make)
                            ++clickIndex;
                            mainAdapter.makeVisible(clickIndex);
                            layoutManager.scrollToPosition(clickIndex);
                        } else {
                            finish();
                            startActivity(getIntent());
                        }

                    }

                    if (!hasStart) {
                        inputEditText.setInputType(InputType.TYPE_NULL);
                        turingMachine(inputString); //execute all logic for the turing machine
                        printMessage("End Execution" + "\n" + "Step again to restart"); //we are done, prints message stating so
                        clickIndex = 0;

                        //each time step button is pressed, make visible the cards for each step.
                        mainAdapter.makeVisible(clickIndex);
                        layoutManager.scrollToPosition(clickIndex);
                        hasStart = true;
                    }
                }
            }
        };

        //set on click listener for button so we can step through states
        btnStep = (Button)findViewById(R.id.stepBtn);
        btnStep.setOnClickListener(startListener);
    }

    /**
     * turingMachine
     *
     * Starts our TM execution
     *
     * @param inputStr String passed to TM
     */
    private void turingMachine(String inputStr){
        inputStr = inputEditText.getText().toString();
        initialInput = inputStr;
        pointerVar = "^";
        pointerPosition = 0;

        // Iterate through the input string and add spaces to the pointer to match length
        for (int i = 0; i < inputString.length(); i++)
        {
            pointerVar += ' ';
        }

        int counterUnderscores = 0;
        for (int i = 0; i < inputString.length(); i++)
        {
            if (inputString.charAt(i) == '_')
            {
                counterUnderscores++;
            }
            //no more underscores to count
            else
            {
                break;
            }
        }

        //trim any underscores
        inputString = inputString.substring(counterUnderscores);
        //trim any spaces
        inputString = inputString.trim();

        //intro
        printMessage("Begin Execution");
        printMessage(inputString + "\n" + pointerVar);

        // Begin simulation
        q0(inputString, pointerVar, pointerPosition);

    }

    /**
     * q0
     *
     * Our start state.
     * State which can transition to qAccept.
     *
     * Transitions: (# -> #, Right):    q0
     *              (a -> #, Right):    q1
     *              (_ -> _, Right):    qAccept
     *              (other):            qReject
     *
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param pointerPosition Numerical location of the TM execution head.
     */
    public void q0(String input, String pointer, int pointerPosition)
    {
        // Check for loop back conditions
        if(input.charAt(pointerPosition) == '#')
        {
            // Move pointer right and retrieve those outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0]; //position 0 in array holds input
            pointer = output[1]; //position 1 in array holds pointer
            pointerPosition = output[2].length(); //position 0 in array holds index

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            // Loop back to state q0
            q0(input, pointer, pointerPosition);
        }
        // Next state condition
        else if (input.charAt(pointerPosition) == 'a')
        {
            // Mark character with #
            input = input.substring(0, pointerPosition) + '#' + input.substring(pointerPosition + 1);

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            // Move pointer right + retrieve outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q1(input, pointer, pointerPosition);
        }
        else if (input.charAt(pointerPosition) == '_')
        {
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            // Move to qAccept
            qAccept();
        }
        else
        {
            qReject(input, pointerPosition);
        }
    }

    /**
     * q1
     *
     * Second state of TM execution.
     *
     * Transitions: ([a, #] -> [a, #], Right):  q1
     *              (b -> #, Right):            q2
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param pointerPosition Numerical location of the TM execution head.
     */
    public void q1(String input, String pointer, int pointerPosition)
    {

        // Check for loop back conditions
        if (input.charAt(pointerPosition) == 'a' || input.charAt(pointerPosition) == '#')
        {
            // Move pointer right + retrieve outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q1(input, pointer, pointerPosition);
        }
        // Next state condition
        else if (input.charAt(pointerPosition) == 'b')
        {
            // Mark character with #
            input = input.substring(0, pointerPosition) + '#' + input.substring(pointerPosition + 1);

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            // Move pointer right + retrieve outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q2(input, pointer, pointerPosition);
        }
        else
        {
            qReject(input, pointerPosition);
        }
    }

    /**
     * q2
     *
     * Third state of TM execution.
     *
     * Transitions: ([b, #] -> [b, #], Right):  q2
     *              (c -> #, Right):            q3
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param pointerPosition Numerical location of the TM execution head.
     */
    public void q2(String input, String pointer, int pointerPosition)
    {
        // Check for loop back conditions
        if (input.charAt(pointerPosition) == 'b' || input.charAt(pointerPosition) == '#')
        {
            // Move pointer right + retrieve outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q2(input, pointer, pointerPosition);
        }
        // Next state condition
        else if (input.charAt(pointerPosition) == 'c')
        {
            // Mark character with #
            input = input.substring(0, pointerPosition) + '#' + input.substring(pointerPosition + 1);

            //printState(input, pointer);
            printMessage(input + "\n" + pointer);

            // Move pointer right + retrieve outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q3(input, pointer, pointerPosition);
        }
        else
        {
            qReject(input, pointerPosition);
        }
    }

    /**
     * q3
     *
     * Fourth state of TM execution.
     *
     * Transitions: ([c, _] -> [c, _], Left):   q4
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param pointerPosition Numerical location of the TM execution head.
     */
    public void q3(String input, String pointer, int pointerPosition)
    {
        // Reset condition
        if (input.charAt(pointerPosition) == 'c' || input.charAt(pointerPosition) == '_')
        {
            // Move pointer left + retrieve outputs
            String[] output = moveLeft(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q4(input, pointer, pointerPosition);
        }
        else
        {
            qReject(input, pointerPosition);
        }
    }

    /**
     * q4
     *
     * Fifth state of TM execution.
     *
     * Transitions: ([b, #] -> [b, #], Left):   q4
     *              (a -> a, Left):             q5
     *              (_ -> _, Right):            q0
     *              (other):                    qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param pointerPosition Numerical location of the TM execution head.
     */
    public void q4(String input, String pointer, int pointerPosition)
    {
        // Check for loop back conditions
        if (input.charAt(pointerPosition) == 'b' || input.charAt(pointerPosition) == '#')
        {
            // Move pointer left + retrieve outputs
            String[] output = moveLeft(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q4(input, pointer, pointerPosition);
        }
        // Return to first state condition
        else if (input.charAt(pointerPosition) == 'a')
        {
            // Move pointer left + retrieve outputs
            String[] output = moveLeft(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q5(input, pointer, pointerPosition);
        }
        else if (input.charAt(pointerPosition) == '_')
        {
            // Move pointer left + retrieve outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q0(input, pointer, pointerPosition);
        }
        else
        {
            qReject(input, pointerPosition);
        }
    }

    /**
     * q5
     *
     * Sixth state of TM execution.
     *
     * Transitions: (a -> a, Left):     q5
     *              (# -> #, Right):    q0
     *              (other):            qReject
     *
     * @param input String currently on the TM tape.
     * @param pointer String showing where the TM execution head is.
     * @param pointerPosition Numerical location of the TM execution head.
     */
    public void q5(String input, String pointer, int pointerPosition)
    {
        // Check for loop back conditions
        if (input.charAt(pointerPosition) == 'a')
        {
            // Move pointer right + retrieve outputs
            String[] output = moveLeft(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q5(input, pointer, pointerPosition);
        }
        // Next state condition
        else if (input.charAt(pointerPosition) == '#')
        {
            // Move pointer right + retrieve outputs
            String[] output = moveRight(input, pointer, pointerPosition);
            input = output[0];
            pointer = output[1];
            pointerPosition = output[2].length();

            // Print current state of TM
            printMessage(input + "\n" + pointer);

            q0(input, pointer, pointerPosition);
        }
        else
        {
            qReject(input, pointerPosition);
        }
    }

    /**
     * qAccept
     *
     * Accept state of TM execution.
     */
    public void qAccept()
    {
        printMessage("Result of execution: Accept" + "\n" + "Your original input: " + initialInput);
    }

    /**
     * qReject
     *
     * Reject state of TM execution.
     *
     * @param input String that caused problem.
     * @param index Location of character that caused halt.
     */
    public void qReject(String input, int index)
    {
        printMessage("Result of execution: Reject" + "\n" + "Your original input: " + initialInput + "\n" + "Halt on character: " + input.charAt(index));
    }

    /**
     * printMessage
     *
     * Makes a card in adapter that display text (such as a TM state)
     *
     * @param text a string to print on card
     */
    private void printMessage(String text){

        if(!text.equals("")){
            //add new card to layout for us to view later
            mainAdapter.add(0, text);
            //scroll to the top of our list
            layoutManager.scrollToPosition(0);
        }
    }

    /**
     * moveLeft - Method to move pointer carrot one space to the left. If necessary,
     *          adds an underscore character to the left of the input string, and a space
     *          to the left of the pointer string.
     *
     * @param input String currently on the TM tape.
     * @param pointer Pointer string with TM execution head.
     * @param pointerPosition Numerical location of TM execution head.
     * @return Array of three strings: input string, pointer string, pointerPosition (in spaces).
     */
    public String[] moveLeft(String input, String pointer, int pointerPosition)
    {
        // Move index one left
        pointerPosition -= 1;

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
                    pointerPosition = 0;

                    break;
                }

                // Construct the new pointer string from the old one, modified
                String tempLeft = pointer.substring(0, i-1);
                String tempLeft1 = pointer.substring(i + 1);
                pointer = tempLeft + "^ " + tempLeft1;

                break;
            }
        }

        String[] output = new String[3];
        output[0] = input;
        output[1] = pointer;
        String outTemp = "";
        for (int i = 0; i < pointerPosition; i++)
        {
            outTemp += ' ';
        }
        output[2] = outTemp;

        return output;
    }

    /**
     * moveRight - Method to move pointer carrot one space to the right. If necessary,
     *          adds an underscore character to the right of the input string, and a space
     *          to the left of the pointer string.
     *
     * @param input String currently on the TM tape.
     * @param pointer Pointer string with TM execution head.
     * @param pointerPosition Numerical location of TM execution head.
     * @return Array of three strings: input string, pointer string, pointerPosition (in spaces).
     */
    public String[] moveRight(String input, String pointer, int pointerPosition)
    {

        // Move index one right
        pointerPosition += 1;

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

        String[] output = new String[3];
        output[0] = input;
        output[1] = pointer;
        String outTemp = "";
        for (int i = 0; i < pointerPosition; i++)
        {
            outTemp += ' ';
        }
        output[2] = outTemp;

        return output;
    }
}
