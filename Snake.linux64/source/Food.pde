class Food{
  final color foodColor = white;
  int xpos, ypos;
  
  Food(Snake player){
    placeFood(player);
  }
  
  void placeFood(Snake player){
    xpos = (int) random(0, boxWidth - 1);
    ypos = (int) random(0, boxHeight - 1);
    //Check if placed on snake
    for(int i = 0; i < player.body.size(); i ++){
      int[] playerPos = player.body.get(i);
      if(xpos == playerPos[0] || ypos == playerPos[1])
        placeFood(player);
    }
  }
  
  void draw(){
    fill(foodColor);
    rect(xpos * boxLength + sideEdgeBuffer, ypos * boxLength + topEdgeBuffer, 
    boxLength, boxLength);
  }
}
