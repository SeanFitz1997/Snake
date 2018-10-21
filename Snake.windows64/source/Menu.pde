class Menu{
  final color textColor = black;
  final color fillColor = #665603;
  final color hoverFill = #8AA207;
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
    
    void draw(){
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
      text(this.text, xpos + (this.width / 2), ypos + (this.length / 1.3));
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
       
       @Override
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
        text(clicked ? onText : offText, xpos + (this.width / 2), ypos + (this.length / 1.3));
       }
       void mouseClicked(){
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

  void draw(){
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
