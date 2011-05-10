package com.butent.bee.client.canvas;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;

/**
 * Implements image type logo visual component.
 */

public class Logo extends SpringObject {
  ImageElement image;
  double rot;

  Logo(ImageElement image) {
    super(new Vector(0, 0));
    this.image = image;
    this.rot = 0;
  }

  void draw(Context2d context) {
    context.save();
    context.translate(this.pos.x, this.pos.y);
    context.rotate(rot);
    context.drawImage(image, 0, 0);
    context.restore();
  }
}
