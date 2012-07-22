package com.butent.bee.client.utils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.widget.BeeFrame;
import com.butent.bee.shared.Assert;

public class Printer {

  private static BeeFrame frame = null;

  public static void print(Element element) {
    Assert.notNull(element);

    int width = Math.max(element.getScrollWidth(), DomUtils.getClientWidth() / 2);
    int height = Math.max(element.getScrollHeight(), DomUtils.getClientHeight());

    print("<div style=\"position: absolute; left: 0px; top: 0px; " +
        "width: " + width + "px; height: " + height + "px;\">" + element.getString() + "</div>");
  }

  public static void print(String html) {
    Assert.notEmpty(html);
    output(html);
  }

  public static void print(UIObject obj) {
    Assert.notNull(obj);
    print(obj.getElement());
  }

  private static void createFrame(String html) {
    frame = new BeeFrame();

    StyleUtils.setSize(frame, 0, 0);
    frame.getElement().getStyle().setBorderStyle(BorderStyle.NONE);

    Document.get().getBody().appendChild(frame.getElement());

    frame.setHtml("<!doctype html><html><head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" +
        "<style type=\"text/css\">" + StyleUtils.getRules() + "</style>" +
        "</head><body>" + html + "</body></html>");
  }

  private static void output(String html) {
    try {
      if (frame == null) {
        createFrame(html);
      } else {
        frame.clear();
        frame.setBody(html);
      }
      
      Timer timer = new Timer() {
        @Override
        public void run() {
          if (!frame.isEmpty()) {
            cancel();

            long now = System.currentTimeMillis();
            
            frame.print();
            
            long millis = System.currentTimeMillis() - now;
            BeeKeeper.getLog().debugWithSeparator("end print", millis);
            
            frame.clear();
          }
        }
      };
      timer.scheduleRepeating(100);

    } catch (Throwable ex) {
      BeeKeeper.getLog().severe(ex.getMessage());
    }
  }

  private Printer() {
    super();
  }
}
