# Minesweeper Android App

## About:
This project is an Android app implementation of the popular computer game Minesweeper. The app presents users with a 10x8 board with 10 bombs placed randomly. The app's rules operate identically to those of the original game. As the user clicks on any cell, it is explored, along with any neighboring cells that don't contain numbers, using the breadth-first search algorithm. A separate thread is used to run a timer to maintain the duration of the game until the user loses or wins. This app was developed in Android Studio and used the Android SDK to design the GUI elements and touch screen interactivity.

## Getting Started:
1. Clone this repository:  
   `git clone git@github.com:ishuagrawal/Minesweeper.git`
2. Open the project in Android Studio.
3. Create an Android Virtual Device (AVD) by selecting menu "Tools" > "Device Manager".
4. For the device definition, choose "Pixel 2" under the "Phone" tab.
5. For the system image, choose “Nougat API 24” under the "System Image" tab.
6. Run the app on the newly created AVD by selecting menu "Run" > "Run 'app'".

## Screenshots:
- Game Board:  
<img src=screenshots/board.png alt="Game Board" width="200">

- Flags:  
<img src=screenshots/game.png alt="Flags" width="200">

- Game Result:  
<img src=screenshots/loss.png alt="Result" width="200">



