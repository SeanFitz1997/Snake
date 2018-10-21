//Imports================================================ //<>//
import processing.sound.*;
import java.io.PrintWriter;
//Constants===============================================
public final int topEdgeBuffer = 40;
public final int sideEdgeBuffer = 5;
public final int boxWidth = 64;
public final int boxHeight = 48;
public final int boxLength = 10;
public final color backgroundColor = #BCC900;
public final color white = 255;
public final color black = 0;

//Variables
Snake player;
Food food;
Menu   menu;
boolean displayMenu;
boolean gameOver;
public SoundFile eatSound;
public SoundFile crashSound;
public PImage icon;
PrintWriter settings_output;
PrintWriter score_output;
public int score;
public Integer topHS;

//Functions===============================================
void displayGrid(){
  stroke(white);
  //Horizontal lines
  for(int i = 0; i <= boxHeight; i ++)
      line(sideEdgeBuffer, topEdgeBuffer + (i * boxLength), 
      width - sideEdgeBuffer, topEdgeBuffer + (i * boxLength));
  //Vertical lines
  for(int i = 0; i <= boxWidth; i++)
      line(sideEdgeBuffer + (i * boxLength), topEdgeBuffer, 
      sideEdgeBuffer + (i * boxLength), height - sideEdgeBuffer);
  displayBorder(displayMenu); 
}

void displayBorder(boolean displayMenu){
  stroke(black);
  noFill();
  if(displayMenu)
    rect(sideEdgeBuffer, sideEdgeBuffer, width - (2 * sideEdgeBuffer), height - (2 * sideEdgeBuffer));
  else
    rect(sideEdgeBuffer, topEdgeBuffer, width - (2 * sideEdgeBuffer), height - (topEdgeBuffer + sideEdgeBuffer));    
}

void setUpGame(boolean[] settings){
  //Set up game
  displayMenu = true;
  gameOver = false;
  player = new Snake();
  food = new Food(player);
  menu = new Menu(settings[0], settings[1], settings[2]);
  score = 0;
}

void displayGameHeader(){
  fill(black);
  textAlign(LEFT);
  text("[Score: " + score + "]", sideEdgeBuffer + 10, topEdgeBuffer - 10);
  text("[High Score: " + ((topHS != null) ? topHS : "ERROR") +"]", sideEdgeBuffer + 175, topEdgeBuffer - 10);
  textAlign(RIGHT);
  text("(Press [SPACE] to Pause)", width - 10, topEdgeBuffer - 10);
}

void updateHighScore(int playerScore){
  //Check New High score
  if(topHS == null || playerScore <= topHS)  
    return;
      //<>//
    topHS = new Integer(playerScore);
    //Write score to file
    score_output = createWriter("High_Score.txt");
    println("Saving High Score");
    score_output.println(topHS);
    score_output.flush();
    score_output.close();
}

//SetUp===================================================
void setup() {
  size(650, 525);
  background(backgroundColor);
  //Load assets
  eatSound = new SoundFile(this, "beep.mp3");
  crashSound = new SoundFile(this, "crash.mp3");
  icon = loadImage("snake_icon.PNG");
  //Load saved settings
  try{
    String[] settings_state = loadStrings("Game_State.txt");
    setUpGame(new boolean[] { Boolean.parseBoolean(settings_state[0]), 
                              Boolean.parseBoolean(settings_state[1]), 
                              Boolean.parseBoolean(settings_state[2])});
  }catch(Exception e){
    setUpGame(new boolean[] { true, false, false});
    println("Error: Could not loading game settings");
  };
  //Load highScore
   try{
    String[] score = loadStrings("High_Score.txt");
    topHS = new Integer(score[0]);
  }catch(Exception e){
    topHS = null;
    println("Error: Could not loading highscore");
  };
  settings_output = createWriter("Game_State.txt");
}

//Draw=====================================================
void draw(){
  background(backgroundColor);
  displayBorder(displayMenu);
  
  //display Menu
  if(displayMenu){
    frameRate(30);
    menu.draw();
    if(menu.actionButton.clicked){
      menu.actionButton.clicked = false;  // reset button
      displayMenu = false;  //play game
    }
  }
  else{
  //Play game
    frameRate(10);
    if(menu.gridSwitch.clicked)  displayGrid();
    player.draw();
    food.draw();
    
    if(!gameOver){
      displayGameHeader();
      player.move();
      player.collide(menu.collisionSwitch.clicked ? "Wrap" : "Crash", menu.audioSwitch.clicked);
      player.eat(food, menu.audioSwitch.clicked);
      //Pause Game
      if(keyPressed && key == ' '){
        menu.menuHeader = "Paused";
        displayMenu = true;
      }
    }else{
      updateHighScore(score);
      menu.menuHeader = "Game Over";
      gameOver = false;
      setUpGame(new boolean[] {menu.audioSwitch.clicked, menu.gridSwitch.clicked, menu.collisionSwitch.clicked});
      displayMenu = true;
    }
  }
  
}

void mouseClicked(){
  menu.audioSwitch.mouseClicked();
  menu.gridSwitch.mouseClicked();
  menu.collisionSwitch.mouseClicked();
}

public void dispose(){      
  println("Saving settings");
  settings_output.println(menu.audioSwitch.clicked);
  settings_output.println(menu.gridSwitch.clicked);
  settings_output.println(menu.collisionSwitch.clicked);
  settings_output.flush();
  settings_output.close();
}
