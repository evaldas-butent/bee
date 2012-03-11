package com.butent.bee.client.ui;

/**
 * Requires implementing classes to have methods to get and set parameters for character width and
 * visible lines.
 */

public interface HasTextDimensions {

  String ATTR_CHARACTER_WIDTH = "characterWidth";
  String ATTR_VISIBLE_LINES = "visibleLines";
  
  int getCharacterWidth();

  int getVisibleLines();

  void setCharacterWidth(int width);

  void setVisibleLines(int lines);
}
