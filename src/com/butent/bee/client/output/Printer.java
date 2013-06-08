package com.butent.bee.client.output;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.TextAreaElement;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Frame;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class Printer {

  private static final BeeLogger logger = LogUtils.getLogger(Printer.class);

  private static final String ID_PREFIX = "p-";

  private static final int DELAY_MS = 100;
  private static final int NUMBER_OF_ATTEMPTS = 10;

  private static Frame frame = null;

  public static void onInjectStyleSheet(String css) {
    if (frame != null && !BeeUtils.isEmpty(css)) {
      frame.injectStyleSheet(css);
    }
  }

  public static void print(Element element, Printable widget) {
    Assert.notNull(element);

    String html = element.getString();
    if (BeeUtils.isEmpty(html)) {
      html = DomUtils.getOuterHtml(element);
    }

    print(html, widget);
  }

  public static void print(Printable widget) {
    Assert.notNull(widget);
    Element element = widget.getPrintElement();
    if (element != null) {
      print(element, widget);
    }
  }

  public static void print(String html, Printable widget) {
    Assert.notEmpty(html, "print: html is empty");
    output(html, widget);
  }

  private static void createFrame(String html) {
    frame = new Frame();

    StyleUtils.setSize(frame, 0, 0);
    frame.getElement().getStyle().setBorderStyle(BorderStyle.NONE);

    Document.get().getBody().appendChild(frame.getElement());

    frame.setHtml("<!doctype html><html><head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" +
        "<style type=\"text/css\">" + StyleUtils.getRules() + "</style>" +
        "</head><body>" + html + "</body></html>");
  }

  private static void output(String html, final Printable widget) {
    if (frame == null) {
      createFrame(html);
    } else {
      frame.setBodyHtml(html);
    }

    Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
      int counter = 0;

      @Override
      public boolean execute() {
        NodeList<Element> children = DomUtils.getChildren(frame.getBody());
        if (children == null || children.getLength() <= 0) {
          counter++;
          logger.warning("print attempt", counter, "failed");
          return counter < NUMBER_OF_ATTEMPTS;
        }

        List<Element> hide = Lists.newArrayList();

        for (int i = 0; i < children.getLength(); i++) {
          Element element = children.getItem(i);
          if (!prepare(element, widget)) {
            hide.add(element);
          }
        }

        for (Element element : hide) {
          StyleUtils.hideDisplay(element);
        }

        printFrame();
        return false;
      }
    }, DELAY_MS);
  }

  private static boolean prepare(Element target, Printable widget) {
    if (target == null) {
      return true;
    }

    String id = target.getId();
    if (BeeUtils.isEmpty(id)) {
      return true;
    }

    target.setId(ID_PREFIX + id);

    Element source = DomUtils.getElementQuietly(id);
    if (source == null) {
      logger.warning(id, "source not found");

    } else if (DomUtils.isTextAreaElement(source)) {
      String value = TextAreaElement.as(source).getValue();
      if (!BeeUtils.isEmpty(value)) {
        TextAreaElement.as(target).setValue(value);
      }

    } else if (DomUtils.isInputElement(source)) {
      String value = InputElement.as(source).getValue();
      if (!BeeUtils.equalsTrim(value, InputElement.as(target).getValue())) {
        InputElement.as(target).setValue(value);
      }

    } else if (DomUtils.isSelectElement(source)) {
      if (!SelectElement.as(source).isMultiple()) {
        SelectElement.as(target).setSelectedIndex(SelectElement.as(source).getSelectedIndex());
      }
    }

    if (widget != null && source != null) {
      return widget.onPrint(source, target);
    }
    return true;
  }

  private static void printFrame() {
    frame.focus();
    frame.print();
    frame.clear();
  }

  private Printer() {
    super();
  }
}
