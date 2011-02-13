package com.butent.bee.client.canvas;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class LogoGroup {
  final double width;
  final double height;
  final int numLogos;
  final double radius;
  Image logoImg;
  Logo[] logos;
  boolean imageLoaded;

  double k;

  public LogoGroup(double width, double height, int numLogos, double radius) {
    this.width = width;
    this.height = height;
    this.numLogos = numLogos;
    this.radius = radius;

    logos = new Logo[numLogos];

    logoImg = new Image("test.png");
    logoImg.addLoadHandler(new LoadHandler() {
      public void onLoad(LoadEvent event) {
        imageLoaded = true;
        ImageElement imageElement = (ImageElement) logoImg.getElement().cast();
        for (int i = logos.length - 1; i >= 0; i--) {
          Logo logo = new Logo(imageElement);
          logo.pos.x = LogoGroup.this.width / 2;
          logo.pos.y = LogoGroup.this.height / 2;
          logos[i] = logo;
        }
      }
    });
    logoImg.setVisible(false);
    RootPanel.get().add(logoImg);
  }

  public void draw(Context2d context) {
    if (!imageLoaded) {
      return;
    }

    for (int i = numLogos - 1; i >= 0; i--) {
      logos[i].draw(context);
    }
  }

  public void update(double mouseX, double mouseY) {
    if (!imageLoaded) {
      return;
    }

    k = (k + Math.PI / 2.0 * 0.009);

    for (int i = numLogos - 1; i >= 0; i--) {
      Logo logo = logos[i];
      double logoPerTPi = 2 * Math.PI * i / numLogos;
      Vector goal = new Vector(width / 2 + radius * Math.cos(k + logoPerTPi),
          height / 2 + radius * Math.sin(k + logoPerTPi));
      logo.goal.set(goal);
      logo.rot = k + logoPerTPi + Math.PI / 2.0;

      Vector d = new Vector(mouseX, mouseY);
      d.sub(logo.pos);
      if (d.magSquared() < 50 * 50) {
        logo.goal = Vector.sub(logo.pos, d);
      }

      logo.update();
    }
  }
}