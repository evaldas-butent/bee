package com.butent.bee.client.ui;

/**
 * Requires implementing classes to have methods to get and set parameters for character width and
 * visible lines.
 */

public interface HasTextDimensions {

  int getCharacterWidth();

  int getVisibleLines();

  void setCharacterWidth(int width);

  void setVisibleLines(int lines);
}
