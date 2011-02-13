package com.butent.bee.client.canvas;

import com.google.gwt.canvas.dom.client.Context2d;

import com.butent.bee.shared.utils.BeeUtils;

public class BallGroup {
  final double width;
  final double height;
  Ball[] balls;

  public BallGroup(double width, double height) {
    this.width = width;
    this.height = height;
    balls = new Ball[64];
    
    double x = (width - 8 * 20) / 2 + 10;
    double y = (height - 8 * 20) / 2 + 10;
    double radius;
    int r, g, b;

    for (int i = 0; i < 8; i++) {
      r = BeeUtils.randomInt(0, 255);
      g = BeeUtils.randomInt(0, 255);
      b = BeeUtils.randomInt(0, 255);
      radius = BeeUtils.randomInt(5, 10);

      for (int j = 0; j < 8; j++) {
        balls[i * 8 + j] = new Ball(x + j * 20, y + i * 20, 0, radius, r, g, b);
      }
    }
  }

  public void draw(Context2d context) {
    for (int i = balls.length - 1; i >= 0; i--) {
      balls[i].draw(context);
    }
  }

  public void update(double mouseX, double mouseY) {
    Vector d = new Vector(0, 0);
    for (int i = balls.length - 1; i >= 0; i--) {
      Ball ball = balls[i];
      d.x = mouseX - ball.pos.x;
      d.y = mouseY - ball.pos.y;
      if (d.magSquared() < 100 * 100) {
        ball.goal = Vector.sub(ball.pos, d);
      } else {
        ball.goal.set(ball.startPos);
      }

      ball.update();
    }
  }
}
