package com.butent.bee.egg.client.event;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.user.client.Event;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;

import java.util.ArrayList;
import java.util.List;

public class EventUtils {

  public static List<StringProp> getEventInfo(Event ev) {
    Assert.notNull(ev);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Client X", ev.getClientX(), "Client Y",
        ev.getClientY(), "Screen X", ev.getScreenX(), "Screen Y",
        ev.getScreenY(), "Key Code", ev.getKeyCode(), "Char Code",
        ev.getCharCode(), "Alt Key", ev.getAltKey(), "Shift Key",
        ev.getShiftKey(), "Ctrl Key", ev.getCtrlKey(), "Meta Key",
        ev.getMetaKey(), "Button", ev.getButton(), "Mouse Wheel Velocity Y",
        ev.getMouseWheelVelocityY(), "Event Target",
        transformEventTarget(ev.getEventTarget()), "Current Event Target",
        transformEventTarget(ev.getCurrentEventTarget()),
        "Related Event Target",
        transformEventTarget(ev.getRelatedEventTarget()), "String",
        ev.getString(), "Type", ev.getType(), "Type Int", ev.getTypeInt());

    return lst;
  }

  public static String getEventTargetId(Event ev) {
    Assert.notNull(ev);
    EventTarget target = ev.getEventTarget();

    if (target == null) {
      return null;
    } else {
      return getTargetId(target);
    }
  }

  public static String getTargetId(EventTarget et) {
    Assert.notNull(et);
    return Element.as(et).getId();
  }
  
  public static boolean isKeyEvent(int type) {
    return (type & Event.KEYEVENTS) != 0;
  }

  public static boolean isKeyEvent(Event ev) {
    if (ev == null) {
      return false;
    } else {
      return isKeyEvent(ev.getTypeInt());
    }
  }

  public static void logCloseEvent(CloseEvent<?> ev) {
    Assert.notNull(ev);
    BeeKeeper.getLog().info(transformCloseEvent(ev));
  }
  
  public static void logEvent(Event ev) {
    logEvent(ev, false);
  }
  
  public static void logEvent(Event ev, boolean detailed) {
    Assert.notNull(ev);
    
    if (detailed) {
      List<StringProp> lst = getEventInfo(ev);
      for (StringProp el : lst) {
        BeeKeeper.getLog().info(el.getName(), el.getValue());
      }

      BeeKeeper.getLog().addSeparator();
    } else {
      BeeKeeper.getLog().info(transformEvent(ev));
    }
  }

  public static void sinkChildEvents(Element parent, String childTag, int eventBits) {
    Assert.notNull(parent);
    Assert.notEmpty(childTag);
    Assert.isTrue(eventBits != 0);
    
    NodeList<Element> lst = parent.getElementsByTagName(childTag);
    Assert.isTrue(lst.getLength() > 0, childTag + " children not found");
    
    for (int i = 0; i < lst.getLength(); i++) {
      Event.sinkEvents(lst.getItem(i), eventBits);
    }
  }

  public static String transformCloseEvent(CloseEvent<?> ev) {
    if (ev == null) {
      return BeeConst.STRING_EMPTY;
    }
    
    return BeeUtils.transformOptions("source", DomUtils.transform(ev.getSource()),
      "target", DomUtils.transform(ev.getTarget()), "auto", ev.isAutoClosed());
  }
  
  public static String transformEvent(Event ev) {
    if (ev == null) {
      return BeeConst.STRING_EMPTY;
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append(ev.getType());
    
    int x = ev.getClientX();
    if (x != 0) {
      sb.append(" x=" + x);
    }
    int y = ev.getClientY();
    if (y != 0) {
      sb.append(" y=" + y);
    }
    
    int k = ev.getKeyCode();
    if (k != 0) {
      sb.append(" k=" + k);
    }
    int c = ev.getCharCode();
    if (c != 0 && c != k) {
      sb.append(" c=" + c);
    }
    
    if (ev.getAltKey()) {
      sb.append(" alt");
    }
    if (ev.getShiftKey()) {
      sb.append(" shift");
    }
    if (ev.getCtrlKey()) {
      sb.append(" ctrl");
    }
    if (ev.getMetaKey()) {
      sb.append(" meta");
    }
    
    int b = ev.getButton();
    if (k != 0) {
      sb.append(" b=" + b);
    }
    int v = ev.getMouseWheelVelocityY();
    if (v != 0) {
      sb.append(" mwv=" + v);
    }

    EventTarget et = ev.getEventTarget();
    if (et != null) {
      sb.append(" et=" + transformEventTarget(et));
    }
    EventTarget cet = ev.getCurrentEventTarget();
    if (cet != null && cet != et) {
      sb.append(" cet=" + transformEventTarget(cet));
    }
    EventTarget ret = ev.getRelatedEventTarget();
    if (ret != null) {
      sb.append(" ret=" + transformEventTarget(ret));
    }
    
    return sb.toString();
  }

  private static String transformEventTarget(EventTarget et) {
    if (et == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return DomUtils.transformElement(Element.as(et));
    }
  }
  
}
