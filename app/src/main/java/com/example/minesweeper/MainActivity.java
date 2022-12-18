package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 8;
    private int clock = 0;
    private int numFlags = 10;
    private boolean running = true;
    private boolean flagMode = false;
    private boolean gameEnd = false;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private HashSet<Integer> mines;
    private HashSet<Integer> flags;
    private HashMap<Integer, Integer> cellValues;
    private HashSet<Integer> visited;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            clock = savedInstanceState.getInt("clock");
            running = savedInstanceState.getBoolean("running");
        }

        runTimer();

        cell_tvs = new ArrayList<TextView>();
        mines = new HashSet<Integer>();
        flags = new HashSet<Integer>();
        cellValues = new HashMap<Integer, Integer>();
        visited = new HashSet<Integer>();

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout);
        for (int i=0; i<10; i++) {
            for (int j=0; j<8; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize(20);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.parseColor("lime"));
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cellValues.put(cell_tvs.size(), 0);
                cell_tvs.add(tv);
            }
        }

        // create 10 mines
        while (mines.size() < 10) {
            int random_int = (int)(Math.random() * cell_tvs.size());

            // ignore duplicate mine locations
            if (mines.contains(random_int)) {
                continue;
            }
            mines.add(random_int);
//            System.out.println("Current Node: " + random_int);
            labelMineNeighbors(random_int);
//            System.out.println("-----------------");
        }
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private void addFlag(int index) {
        flags.add(index);
        TextView minetv = cell_tvs.get(index);
        minetv.setText(getResources().getString(R.string.flag));
        numFlags -= 1;
        TextView flagsView = (TextView) findViewById(R.id.numFlags);
        flagsView.setText(Integer.toString(numFlags));
    }

    private void removeFlag(int index) {
        flags.remove(index);
        TextView minetv = cell_tvs.get(index);
        minetv.setText("");
        numFlags += 1;
        TextView flagsView = (TextView) findViewById(R.id.numFlags);
        flagsView.setText(Integer.toString(numFlags));
    }

    // cell was clicked
    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int index = findIndexOfCellTextView(tv);

        // if user presses on cell when game ends, redirect to ending screen
        if (gameEnd) {
            Intent intent = new Intent(this, EndScreen.class);
            intent.putExtra("CLOCK", Integer.toString(clock));
//            System.out.println("Nodes Explored (==70?): " + visited.size());
            if (visited.size() == 70) {
                intent.putExtra("RESULT", "WIN");
            } else {
                intent.putExtra("RESULT", "LOSE");
            }
            startActivity(intent);
        }

        // flag mode -> create/remove flags
        if (flagMode) {
            // if current cell is not flag, add flag there
            if (!flags.contains(index)) {
                // don't mark already displayed (visited) cells
                if ((!visited.contains(index)) && (numFlags > 0))  {
                    addFlag(index);
                }
            }

            // if current cell is flag, remove flag there
            else {
                removeFlag(index);
            }
        }

        // pick mode -> expand grid
        else {
            // pick a flag -> does nothing
            if (flags.contains(index)) {
                return;
            }

            // pick a mine -> lose
            else if (mines.contains(index)) {
                showMines();
                running = false;
                gameEnd = true;

            }

            // pick a space -> expand grid
            else {
                displayNode(index);
                if ((cellValues.get(index) == 0)) {
                    bfs(index);
                } else {
                    visited.add(index);
                }
            }
        }

        // if explored 70 mines (w/o exploding bombs), we won the game
        if (visited.size() == 70) {
            showMines();
            running = false;
            gameEnd = true;
        }
    }

    // change mode between digging and flagging
    public void onClickMode(View view) {
        TextView tv = (TextView) view;
        if (flagMode) {
            flagMode = false;
            tv.setText(getResources().getString(R.string.pick));
        } else {
            flagMode = true;
            tv.setText(getResources().getString(R.string.flag));
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("clock", clock);
        savedInstanceState.putBoolean("running", running);
    }

    // run the clock
    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.time);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                String time = String.format("%d", clock);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    // calculate the numbers at the cells neighboring the mines
    private void labelMineNeighbors(int node) {
        cellValues.put(node, -1);

        // top neighbor
        if (node >= COLUMN_COUNT) {
            int top = node-COLUMN_COUNT;
            cellValues.put(top, cellValues.get(top)+1);
//            System.out.println("Node " + top + ": " + cellValues.get(top));
        }

        // bottom neighbor
        if (node <= (cell_tvs.size() - COLUMN_COUNT - 1)) {
            int bottom = node+COLUMN_COUNT;
            cellValues.put(bottom, cellValues.get(bottom)+1);
//            System.out.println("Node " + bottom + ": " + cellValues.get(bottom));
        }

        // right neighbor
        if (node%COLUMN_COUNT < (COLUMN_COUNT-1)) {
            int right = node+1;
            cellValues.put(right, cellValues.get(right)+1);
//            System.out.println("Node " + right + ": " + cellValues.get(right));
        }

        // left neighbor
        if (node%COLUMN_COUNT > 0) {
            int left = node-1;
            cellValues.put(left, cellValues.get(left)+1);
//            System.out.println("Node " + left + ": " + cellValues.get(left));
        }

        // top-right neighbor
        if ((node >= COLUMN_COUNT) && (node%COLUMN_COUNT < (COLUMN_COUNT-1))) {
            int topRight = node-COLUMN_COUNT+1;
            cellValues.put(topRight, cellValues.get(topRight)+1);
//            System.out.println("Node " + topRight + ": " + cellValues.get(topRight));
        }

        // top-left neighbor
        if ((node >= COLUMN_COUNT) && (node%COLUMN_COUNT > 0)) {
            int topLeft = node-COLUMN_COUNT-1;
            cellValues.put(topLeft, cellValues.get(topLeft)+1);
//            System.out.println("Node " + topLeft + ": " + cellValues.get(topLeft));
        }

        // bottom-right neighbor
        if ((node <= (cell_tvs.size() - COLUMN_COUNT - 1)) && (node%COLUMN_COUNT < (COLUMN_COUNT-1))) {
            int bottomRight = node+COLUMN_COUNT+1;
            cellValues.put(bottomRight, cellValues.get(bottomRight)+1);
//            System.out.println("Node " + bottomRight + ": " + cellValues.get(bottomRight));
        }

        // bottom-left neighbor
        if ((node <= (cell_tvs.size() - COLUMN_COUNT - 1)) && (node%COLUMN_COUNT > 0)) {
            int bottomLeft = node+COLUMN_COUNT-1;
            cellValues.put(bottomLeft, cellValues.get(bottomLeft)+1);
//            System.out.println("Node " + bottomLeft + ": " + cellValues.get(bottomLeft));
        }
    }

    // change node display in grid
    private void displayNode(int node) {
        TextView tv = cell_tvs.get(node);
        tv.setTextColor(Color.GRAY);
        tv.setBackgroundColor(Color.LTGRAY);

        // only show number if it is > 0
        if (cellValues.get(node) > 0) {
            tv.setText(String.valueOf(cellValues.get(node)));
        }
    }

    // show mines (after game ends)
    private void showMines() {
        for (Integer mineIdx: mines) {
            TextView minetv = cell_tvs.get(mineIdx);
            minetv.setText(getResources().getString(R.string.mine));
        }
    }

    // expand the grid each time user clicks on a cell that's not a bomb
    private void bfs(int node) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(node);
        visited.add(node);

        while (queue.size() > 0) {
            int curr = queue.remove();

            // top neighbor
            if (curr >= COLUMN_COUNT) {
                int top = curr-COLUMN_COUNT;

                if ((cellValues.get(top) == 0) && (!visited.contains(top))) {
                    queue.add(top);
                    visited.add(top);
                    // remove flag if it is in expansion
                    if (flags.contains(top)) {
                        removeFlag(top);
                    }
                    displayNode(top);
                }
                else if (cellValues.get(top) > 0) {
                    visited.add(top);
                    // remove flag if it is in expansion
                    if (flags.contains(top)) {
                        removeFlag(top);
                    }
                    displayNode(top);
                }
            }

            // bottom neighbor
            if (curr <= (cell_tvs.size() - COLUMN_COUNT - 1)) {
                int bottom = curr+COLUMN_COUNT;
                if ((cellValues.get(bottom) == 0) && (!visited.contains(bottom))){
                    queue.add(bottom);
                    visited.add(bottom);
                    // remove flag if it is in expansion
                    if (flags.contains(bottom)) {
                        removeFlag(bottom);
                    }
                    displayNode(bottom);
                }
                else if (cellValues.get(bottom) > 0) {
                    visited.add(bottom);
                    // remove flag if it is in expansion
                    if (flags.contains(bottom)) {
                        removeFlag(bottom);
                    }
                    displayNode(bottom);
                }
            }

            // right neighbor
            if (curr%COLUMN_COUNT < (COLUMN_COUNT-1)) {
                int right = curr+1;
                if ((cellValues.get(right) == 0) && (!visited.contains(right))) {
                    queue.add(right);
                    visited.add(right);
                    // remove flag if it is in expansion
                    if (flags.contains(right)) {
                        removeFlag(right);
                    }
                    displayNode(right);
                }
                else if (cellValues.get(right) > 0) {
                    visited.add(right);
                    // remove flag if it is in expansion
                    if (flags.contains(right)) {
                        removeFlag(right);
                    }
                    displayNode(right);
                }
            }

            // left neighbor
            if (curr%COLUMN_COUNT > 0) {
                int left = curr-1;
                if ((cellValues.get(left) == 0) && (!visited.contains(left))) {
                    queue.add(left);
                    visited.add(left);
                    // remove flag if it is in expansion
                    if (flags.contains(left)) {
                        removeFlag(left);
                    }
                    displayNode(left);
                }
                else if (cellValues.get(left) > 0) {
                    visited.add(left);
                    // remove flag if it is in expansion
                    if (flags.contains(left)) {
                        removeFlag(left);
                    }
                    displayNode(left);
                }
            }

            // top-right neighbor
            if ((curr >= COLUMN_COUNT) && (curr%COLUMN_COUNT < (COLUMN_COUNT-1))) {
                int topRight = curr-COLUMN_COUNT+1;
                if ((cellValues.get(topRight) == 0) && (!visited.contains(topRight))){
                    queue.add(topRight);
                    visited.add(topRight);
                    // remove flag if it is in expansion
                    if (flags.contains(topRight)) {
                        removeFlag(topRight);
                    }
                    displayNode(topRight);
                }
                else if (cellValues.get(topRight) > 0) {
                    visited.add(topRight);
                    // remove flag if it is in expansion
                    if (flags.contains(topRight)) {
                        removeFlag(topRight);
                    }
                    displayNode(topRight);
                }
            }

            // top-left neighbor
            if ((curr >= COLUMN_COUNT) && (curr%COLUMN_COUNT > 0)) {
                int topLeft = curr-COLUMN_COUNT-1;
                if ((cellValues.get(topLeft) == 0) && (!visited.contains(topLeft))) {
                    queue.add(topLeft);
                    visited.add(topLeft);
                    // remove flag if it is in expansion
                    if (flags.contains(topLeft)) {
                        removeFlag(topLeft);
                    }
                    displayNode(topLeft);
                }
                else if (cellValues.get(topLeft) > 0) {
                    visited.add(topLeft);
                    // remove flag if it is in expansion
                    if (flags.contains(topLeft)) {
                        removeFlag(topLeft);
                    }
                    displayNode(topLeft);
                }
            }

            // bottom-right neighbor
            if ((curr <= (cell_tvs.size() - COLUMN_COUNT - 1)) && (curr%COLUMN_COUNT < (COLUMN_COUNT-1))) {
                int bottomRight = curr+COLUMN_COUNT+1;
                if ((cellValues.get(bottomRight) == 0) && (!visited.contains(bottomRight))) {
                    queue.add(bottomRight);
                    visited.add(bottomRight);
                    // remove flag if it is in expansion
                    if (flags.contains(bottomRight)) {
                        removeFlag(bottomRight);
                    }
                    displayNode(bottomRight);
                }
                else if (cellValues.get(bottomRight) > 0) {
                    visited.add(bottomRight);
                    // remove flag if it is in expansion
                    if (flags.contains(bottomRight)) {
                        removeFlag(bottomRight);
                    }
                    displayNode(bottomRight);
                }
            }

            // bottom-left neighbor
            if ((curr <= (cell_tvs.size() - COLUMN_COUNT - 1)) && (curr%COLUMN_COUNT > 0)) {
                int bottomLeft = curr+COLUMN_COUNT-1;
                if ((cellValues.get(bottomLeft) == 0) && (!visited.contains(bottomLeft))) {
                    queue.add(bottomLeft);
                    visited.add(bottomLeft);
                    // remove flag if it is in expansion
                    if (flags.contains(bottomLeft)) {
                        removeFlag(bottomLeft);
                    }
                    displayNode(bottomLeft);
                }
                else if (cellValues.get(bottomLeft) > 0) {
                    visited.add(bottomLeft);
                    // remove flag if it is in expansion
                    if (flags.contains(bottomLeft)) {
                        removeFlag(bottomLeft);
                    }
                    displayNode(bottomLeft);
                }
            }
        }
    }

}