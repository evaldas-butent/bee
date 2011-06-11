package com.butent.bee.client.ui;

public interface HasTextDimensions {
  
  int getCharacterWidth();
  
  int getVisibleLines();
  
  void setCharacterWidth(int width);
  
  void setVisibleLines(int lines);
}
