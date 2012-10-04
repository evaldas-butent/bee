package com.butent.bee.client.canvas;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.GestureStartEvent;
import com.google.gwt.event.dom.client.GestureStartHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.user.client.Timer;

import com.butent.bee.client.BeeKeeper;

/**
 * Implements canvas demo, add elements and handlers.
 */

public class CanvasDemo {
  Canvas canvas;
  Canvas backBuffer;
  BallGroup ballGroup;
  Lens lens;

  int mouseX, mouseY;

  int refreshRate = 25;
  int height = 400;
  int width = 400;

  CssColor redrawColor = CssColor.make("rgba(255,255,255,0.6)");
  Context2d context;
  Context2d backBufferContext;

  public CanvasDemo() {
  }

  public void start() {
    canvas = Canvas.createIfSupported();
    backBuffer = Canvas.createIfSupported();
    if (canvas == null) {
      BeeKeeper.getLog().severe("canvas not supported");
      return;
    }

    canvas.setWidth(width + "px");
    canvas.setHeight(height + "px");
    canvas.setCoordinateSpaceWidth(width);
    canvas.setCoordinateSpaceHeight(height);
    backBuffer.setCoordinateSpaceWidth(width);
    backBuffer.setCoordinateSpaceHeight(height);

    BeeKeeper.getScreen().updateActivePanel(canvas);

    context = canvas.getContext2d();
    backBufferContext = backBuffer.getContext2d();

    ballGroup = new BallGroup(width, height);
    lens = new Lens(35, 15, width, height, new Vector(320, 150), new Vector(1, 1));

    initHandlers();

    final Timer timer = new Timer() {
      @Override
      public void run() {
        doUpdate();
      }
    };
    timer.scheduleRepeating(refreshRate);
  }

  void doUpdate() {
    backBufferContext.setFillStyle(redrawColor);
    backBufferContext.fillRect(0, 0, width, height);
    ballGroup.update(mouseX, mouseY);
    ballGroup.draw(backBufferContext);

    lens.update();
    lens.draw(backBufferContext, context);
  }

  void initHandlers() {
    canvas.addMouseMoveHandler(new MouseMoveHandler() {
      public void onMouseMove(MouseMoveEvent event) {
        mouseX = event.getRelativeX(canvas.getElement());
        mouseY = event.getRelativeY(canvas.getElement());
      }
    });

    canvas.addMouseOutHandler(new MouseOutHandler() {
      public void onMouseOut(MouseOutEvent event) {
        mouseX = -200;
        mouseY = -200;
      }
    });

    canvas.addTouchMoveHandler(new TouchMoveHandler() {
      public void onTouchMove(TouchMoveEvent event) {
        event.preventDefault();
        if (event.getTouches().length() > 0) {
          Touch touch = event.getTouches().get(0);
          mouseX = touch.getRelativeX(canvas.getElement());
          mouseY = touch.getRelativeY(canvas.getElement());
        }
        event.preventDefault();
      }
    });

    canvas.addTouchEndHandler(new TouchEndHandler() {
      public void onTouchEnd(TouchEndEvent event) {
        event.preventDefault();
        mouseX = -200;
        mouseY = -200;
      }
    });

    canvas.addGestureStartHandler(new GestureStartHandler() {
      public void onGestureStart(GestureStartEvent event) {
        event.preventDefault();
      }
    });
  }
}
