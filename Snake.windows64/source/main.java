import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 
import java.io.PrintWriter; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class main extends PApplet {

//Imports================================================ //<>//


//Constants===============================================
public final int topEdgeBuffer = 40;
public final int sideEdgeBuffer = 5;
public final int boxWidth = 64;
public final int boxHeight = 48;
public final int boxLength = 10;
public final int backgroundColor = 0xffBCC900;
public final int white = 255;
public final int black = 0;

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
public void displayGrid(){
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

public void displayBorder(boolean displayMenu){
  stroke(black);
  noFill();
  if(displayMenu)
    rect(sideEdgeBuffer, sideEdgeBuffer, width - (2 * sideEdgeBuffer), height - (2 * sideEdgeBuffer));
  else
    rect(sideEdgeBuffer, topEdgeBuffer, width - (2 * sideEdgeBuffer), height - (topEdgeBuffer + sideEdgeBuffer));    
}

public void setUpGame(boolean[] settings){
  //Set up game
  displayMenu = true;
  gameOver = false;
  player = new Snake();
  food = new Food(player);
  menu = new Menu(settings[0], settings[1], settings[2]);
  score = 0;
}

public void displayGameHeader(){
  fill(black);
  textAlign(LEFT);
  text("[Score: " + score + "]", sideEdgeBuffer + 10, topEdgeBuffer - 10);
  text("[High Score: " + ((topHS != null) ? topHS : "ERROR") +"]", sideEdgeBuffer + 175, topEdgeBuffer - 10);
  textAlign(RIGHT);
  text("(Press [SPACE] to Pause)", width - 10, topEdgeBuffer - 10);
}

public void updateHighScore(int playerScore){
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
public void setup() {
  
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
    topHS = new Integer(0);
    println("Error: Could not loading highscore");
  };
  settings_output = createWriter("Game_State.txt");
  score_output = createWriter("High_Score.txt");
  
}

//Draw=====================================================
public void draw(){
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

public void mouseClicked(){
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
class Food{
  final int foodColor = white;
  int xpos, ypos;
  
  Food(Snake player){
    placeFood(player);
  }
  
  public void placeFood(Snake player){
    xpos = (int) random(0, boxWidth - 1);
    ypos = (int) random(0, boxHeight - 1);
    //Check if placed on snake
    for(int i = 0; i < player.body.size(); i ++){
      int[] playerPos = player.body.get(i);
      if(xpos == playerPos[0] || ypos == playerPos[1])
        placeFood(player);
    }
  }
  
  public void draw(){
    fill(foodColor);
    rect(xpos * boxLength + sideEdgeBuffer, ypos * boxLength + topEdgeBuffer, 
    boxLength, boxLength);
  }
}
class Menu{
  final int textColor = black;
  final int fillColor = 0xff665603;
  final int hoverFill = 0xff8AA207;
  String menuHeader;
  Button actionButton;
  CircleButton audioSwitch;
  CircleButton gridSwitch;
  CircleButton collisionSwitch;
  CircleButton returnSwitch;
  
  public class Button{
    int xpos, ypos, length, width;
    String text;
    boolean clicked = false;
    
    Button(int xpos, int ypos, int width, int length, String text){
      this.xpos = xpos;
      this.ypos = ypos;
      this.length = length;
      this.width = width;
      this.text = text;
    }
    
    public void draw(){
      //button
      fill(fillColor);
      if(mouseX > xpos && mouseX < (xpos + this.width) &&
      mouseY > ypos && mouseY < (ypos + this.length)){
        fill(hoverFill);
        if(mousePressed)
          clicked = true;
      }
      rect(xpos, ypos, this.width, this.length, 10);
      fill(textColor);
      //text
      textSize(20);
      textAlign(CENTER);
      text(this.text, xpos + (this.width / 2), ypos + (this.length / 1.3f));
    }
  }
  
     public class CircleButton extends Button{
       String onText, offText;
       CircleButton(int xpos, int ypos, int width, int length, String text, boolean clicked, String onText, String offText){
         super(xpos, ypos, width, length, text);
         this.clicked = clicked;
         this.onText = onText;
         this.offText = offText;
       }
       
       public @Override
       void draw(){
        fill(fillColor);
        if(clicked)
          fill(hoverFill);
        ellipseMode(CORNER);
        ellipse(xpos, ypos, this.width, this.length);
        fill(textColor);
        //text
        textSize(16);
        textAlign(CENTER);
        text(this.text, xpos + (this.width / 2), ypos + (this.length / 2));
        text(clicked ? onText : offText, xpos + (this.width / 2), ypos + (this.length / 1.3f));
       }
       public void mouseClicked(){
         if(mouseX > xpos && mouseX < (xpos + width) &&
           mouseY > ypos && mouseY < (ypos + length)){
           clicked = !clicked;
         }
       }
     }
      
  
  Menu(boolean audio, boolean grid, boolean crash){
    menuHeader = "Snake";
    
    actionButton = new Button((width / 2) - 150, 300, 300, 30, "Start Game");
    audioSwitch = new CircleButton((width /2) - 150, 375, 75, 75, "Audio", audio, "[On]", "[Off]");
    gridSwitch = new CircleButton((width /2) - 50, 375, 75, 75, "Grid", grid, "[On]", "[Off]");
    collisionSwitch  = new CircleButton((width /2) + 50, 375, 75, 75, "Collision", crash, "[Wrap]", "[Crash]");
}

  public void draw(){
    //Draw Icon
    image(icon, (width / 2) - 75, height / 7, 150, 210);
    //Draw Header
    textAlign(CENTER);
    textSize(26);
    fill(textColor);
    text(menuHeader , width / 2, height / 8);
    
    //Draw buttons
    actionButton.draw();
    if(menuHeader == "Paused") actionButton.text = "Continue";
    else actionButton.text = "Start Game"; 
    audioSwitch.draw();
    gridSwitch.draw();
    collisionSwitch.draw();
  }

}
class Snake{
  final int snakeColor = 0xff665603;
  final int startingLength = 4;
  int xpos, ypos;
  int xvol, yvol;
  ArrayList<int[]> body;
  
  Snake(){
    //Set starting position
    xpos = PApplet.parseInt(random(boxWidth / 2) + (boxWidth / 4));
    ypos = PApplet.parseInt(random(boxHeight / 2) + (boxHeight / 4));
    //Generate body
    body = new ArrayList<int[]>();
    for(int i = 0; i < startingLength; i ++){
      body.add(i, new int[]{this.xpos - i, this.ypos});
    }
    //Start move
    xvol = 1;
  }
  //Functions
  public void draw(){    
    stroke(black);
    fill(snakeColor);
    for(int i = 0; i < body.size(); i ++){
      int pos[] = body.get(i);
      //dont draw if not in grid
      if(pos[0] < 0 || pos[0] > boxWidth -1 || pos[1] < 0 || pos[1] > boxHeight - 1)
        continue;
      rect(pos[0] * boxLength + sideEdgeBuffer, pos[1] * boxLength + topEdgeBuffer, 
      boxLength, boxLength);
    }
  }
  
  public void move(){
    //Set changes to vol
    if(keyPressed){
      switch(keyCode){
      //Left
      case 37:  xvol = xvol != 1 ? -1 : xvol;
                yvol = 0;
        break;
      //Up
      case 38:  yvol = yvol != 1 ? -1 : yvol;
                xvol = 0;
        break;
      //Right
      case 39:  xvol = xvol != -1 ? 1 : xvol;
                yvol = 0;
        break;
      //Down
      case 40:  yvol = yvol != -1 ? 1 : yvol;
                xvol = 0;
        break;
      }
    }
    
    //move rest of body
    for(int i = body.size() - 1; i > 0; i --){
      int[] nextPos = body.get(i - 1); 
      body.set(i, new int[]{nextPos[0], nextPos[1]});
    }
    //move head
    int[] headPos = body.get(0);
    headPos[0] += xvol;
    headPos[1] += yvol;
    body.set(0, headPos);
    xpos = headPos[0];
    ypos = headPos[1];
    
    //print(this.toString());
  }
  
  public void collide(String mode, boolean playAudio){
    //Wall Collide
    switch(mode){
      case "Crash":
        if(xpos >= boxWidth || xpos <= 0 || ypos >= boxHeight || ypos <= 0){
          print("CRASH\n");
          if(playAudio)  crashSound.play();
          gameOver = true;
        }
        break;
       case "Wrap":
         if(xpos >= boxWidth || xpos < 0 || ypos >= boxHeight || ypos < 0){
          print("WRAP\n");
          if(xpos < 0 || ypos < 0){
          xpos = xpos <= 0 ? boxWidth -1 : xpos;
          ypos = ypos <= 0 ? boxHeight : ypos;
          }else{
          xpos = xpos >= boxWidth -1 ? 0 : xpos;
          ypos = ypos >= boxHeight ? 0 : ypos;
          }
          this.body.set(0, new int[] {xpos, ypos});
        }
         break;
      default:
        print("Error: Invalid Collide Mode");
    }
    
    //Body Collision
    for(int i = 1; i < body.size(); i ++){
      int[] pos = body.get(i);
      if(xpos == pos[0] && ypos == pos[1]){
        print("CRASH\n");
        if(playAudio)  crashSound.play();
        gameOver = true;
      }
    }
  }
  
  public void eat(Food food, boolean playAudio){
    if(xpos == food.xpos && ypos == food.ypos){
      print("EAT\n");
      score += 10;
      if(playAudio)  eatSound.play();
      //respawn food
      food.placeFood(this);
      //grow snake
      body.add(body.size(), new int[]{ -1, -1});
    }
  }
  
  public String toString(){
    String output = "";
    for(int i = 0; i < body.size(); i ++){
      int[] pos = body.get(i);
      output += "[" + pos[0] + ", " + pos[1] + "] ";
    }
    output += "Length: " + this.body.size(); 
    return output + "\n";
  }
}
  public void settings() {  size(650, 525); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "main" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
