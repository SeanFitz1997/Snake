class Snake{
  final color snakeColor = #665603;
  final int startingLength = 4;
  int xpos, ypos;
  int xvol, yvol;
  ArrayList<int[]> body;
  
  Snake(){
    //Set starting position
    xpos = int(random(boxWidth / 2) + (boxWidth / 4));
    ypos = int(random(boxHeight / 2) + (boxHeight / 4));
    //Generate body
    body = new ArrayList<int[]>();
    for(int i = 0; i < startingLength; i ++){
      body.add(i, new int[]{this.xpos - i, this.ypos});
    }
    //Start move
    xvol = 1;
  }
  //Functions
  void draw(){    
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
  
  void move(){
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
  
  void collide(String mode, boolean playAudio){
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
  
  void eat(Food food, boolean playAudio){
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
  
  String toString(){
    String output = "";
    for(int i = 0; i < body.size(); i ++){
      int[] pos = body.get(i);
      output += "[" + pos[0] + ", " + pos[1] + "] ";
    }
    output += "Length: " + this.body.size(); 
    return output + "\n";
  }
}
