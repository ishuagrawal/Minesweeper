package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EndScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_screen);

        Intent intent = getIntent();
        String clock = intent.getStringExtra("CLOCK");
        String result = intent.getStringExtra("RESULT");
        System.out.println(result);

        TextView text1 = (TextView) findViewById(R.id.text1);
        TextView text2 = (TextView) findViewById(R.id.text2);
        TextView text3 = (TextView) findViewById(R.id.text3);

        text1.setText("Used " + clock + " seconds.");
        if (result.equals("WIN")) {
            text2.setText("You won!");
            text3.setText("Good job!");
        } else {
            text2.setText("You lost!");
            text3.setText("Nice try!");
        }
    }

    public void playAgain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
