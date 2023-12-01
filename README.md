[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/6WCkqZbI)
# Tic Tac Toe

This is a starter code for the Tic Tac Toe multiplayer game app assignment.

It uses Android Navigation Component, with a single activity and three fragments:

- The DashboardFragment is the home screen. If a user is not logged in, it should navigate to the
LoginFragment. (See the TODO comment in code.)

- The floating button in the dashboard creates a dialog that asks which type of game to create and
passes that information to the GameFragment (using SafeArgs).

- The GameFragment UI has a 3x3 grid of buttons. They are initialized in the starter code.
Appropriate listeners and game play logic needs to be provided.

- Pressing the back button in the GameFragment opens a dialog that confirms if the user wants to
forfeit the game. (See the FIXME comment in code.)

- A "log out" action bar menu is shown on both the dashboard and the game fragments. Clicking it
should log the user out and show the LoginFragment. This click is handled in the MainActivity.

Team Members: Bharath Kalyan B (2020B4A71354G) (f20201354@goa.bits-pilani.ac.in) and Animesh Jajoo (2020B3A71260G) (f20201260@goa.bits-pilani.ac.in)
 
This app creates a simulation of a Tic Tac Toe game that allows two players or a single player to play on a device. The game sessions and their data are stored in Firebase. Currently, any player can enter a game to play against you.
 
Description of Completed Tasks and Steps:
 
Task 1 - Implementing Sign-in Screen:
I initialized the Firebase database (firebaseio) and pushed user data to add a user. Users can log in with any email and password, enabling them to view active games in the database.
 
Task 2 - Implementing Single-Player Mode:
After the user logs in, they can opt for a single-player game. In this mode, the computer randomly chooses a block after the player's turn.
 
Task 3 - Implementing Two-Player Mode:
Once the user logs in and selects a two-player game, a new game entry is created in the database. Two users can play together, and the Tic Tac Toe grid is tracked through an array.
 
Testing and Time Taken:
 
During testing, the app crashed multiple times due to various gradle issues, login issues with the firebase database and also while starting a two player game. Fixing these app crashes took most of our time.
 
Approximately 30 hours were spent on coding, testing, and addressing accessibility issues.
 
Assignment Difficulty:
On a difficulty scale from 1 to 10, I would rate this assignment as a 9.5
