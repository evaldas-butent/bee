package com.butent.bee.client.canvas;

import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;

/**
 * Implements lens visual component for zooming in.
 */

public class Lens {
  final int radius;
  final int mag;
  final int width;
  final int height;

  Vector pos;
  Vector vel;
  int[][] lensArray;

  final FillStrokeStyle strokeStyle = CssColor.make("#333300");

  public Lens(int radius, int mag, int w, int h, Vector initPos, Vector vel) {
    this.radius = radius;
    this.mag = mag;
    this.width = w;
    this.height = h;
    this.pos = initPos;
    this.vel = vel;

    ArrayList<int[]> calcLensArray = new ArrayList<int[]>(0);
    int a;
    int b;
    double s = Math.sqrt(radius * radius - mag * mag);

    for (int y = -radius; y < radius; y++) {
      for (int x = -radius; x < radius; x++) {
        if (x * x + y * y < s * s) {
          double z = Math.sqrt(radius * radius - x * x - y * y);
          a = (int) (x * mag / z + 0.5);
          b = (int) (y * mag / z + 0.5);
          int dstIdx = (y + radius) * 2 * radius + (x + radius);
          int srcIdx = (b + radius) * 2 * radius + (a + radius);
          calcLensArray.add(new int[] {dstIdx, srcIdx});
        }
      }
    }

    lensArray = new int[calcLensArray.size()][2];
    for (int i = 0; i < calcLensArray.size(); i++) {
      int[] fromTo = calcLensArray.get(i);
      lensArray[i][0] = fromTo[0];
      lensArray[i][1] = fromTo[1];
    }
  }

  public void draw(Context2d back, Context2d front) {
    front.drawImage(back.getCanvas(), 0, 0);

    if (GWT.isScript()) {
      ImageData frontData = front.getImageData((int) (pos.getX() - radius),
          (int) (pos.getY() - radius), 2 * radius, 2 * radius);
      CanvasPixelArray frontPixels = frontData.getData();
      ImageData backData = back.getImageData((int) (pos.getX() - radius),
          (int) (pos.getY() - radius), 2 * radius, 2 * radius);
      CanvasPixelArray backPixels = backData.getData();

      int srcIdx;
      int dstIdx;
      for (int i = lensArray.length - 1; i >= 0; i--) {
        dstIdx = 4 * lensArray[i][0];
        srcIdx = 4 * lensArray[i][1];
        frontPixels.set(dstIdx + 0, backPixels.get(srcIdx + 0));
        frontPixels.set(dstIdx + 1, backPixels.get(srcIdx + 1));
        frontPixels.set(dstIdx + 2, backPixels.get(srcIdx + 2));
      }
      front.putImageData(frontData, (int) (pos.getX() - radius), (int) (pos.getY() - radius));
    }

    front.setStrokeStyle(strokeStyle);
    front.beginPath();
    front.arc(pos.getX(), pos.getY(), radius, 0, Math.PI * 2, true);
    front.closePath();
    front.stroke();
  }

  public void update() {
    if (pos.getX() + radius + vel.getX() > width || pos.getX() - radius + vel.getX() < 0) {
      vel.setX(vel.getX() * -1);
    }
    if (pos.getY() + radius + vel.getY() > height || pos.getY() - radius + vel.getY() < 0) {
      vel.setY(vel.getY() * -1);
    }

    pos.setX(pos.getX() + vel.getX());
    pos.setY(pos.getY() + vel.getY());
  }
}
