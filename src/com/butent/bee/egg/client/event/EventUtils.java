package com.butent.bee.egg.client.event;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.user.client.Event;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.dom.Features;
import com.butent.bee.egg.client.event.DndEvent.TYPE;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventUtils {
  private static boolean dnd;

  private static JavaScriptObject onDnd;

  private static Map<String, HasDragStartHandler> dndSources =
      new HashMap<String, HasDragStartHandler>();
  private static Map<String, HasDropHandler> dndTargets =
      new HashMap<String, HasDropHandler>();

  static {
    dnd = Features.supportsDnd() && Features.supportsDndEvents();
    if (dnd) {
      initDnd();
    }
  }
  
  public static void eatEvent(NativeEvent ev) {
    Assert.notNull(ev);
    ev.preventDefault();
    ev.stopPropagation();
  }

  public static List<StringProp> getEventInfo(NativeEvent ev) {
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
        ev.getString(), "Type", ev.getType());

    if (ev instanceof Event) {
      PropUtils.addString(lst, "Type Int", ((Event) ev).getTypeInt());
    }

    return lst;
  }

  public static String getEventTargetId(NativeEvent ev) {
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

  public static boolean hasModifierKey(NativeEvent ev) {
    if (ev == null) {
      return false;
    }
    return ev.getShiftKey() || ev.getCtrlKey() || ev.getAltKey() || ev.getMetaKey();
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

  public static void logEvent(NativeEvent ev) {
    logEvent(ev, false);
  }

  public static void logEvent(NativeEvent ev, boolean detailed) {
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

  public static boolean makeDndSource(Element elem, HasDragStartHandler handler) {
    if (!dnd) {
      return false;
    }
    Assert.notNull(elem);
    Assert.notNull(handler);

    String id = DomUtils.ensureId(elem, "dnd");
    if (isDndSource(id)) {
      return true;
    }

    DomUtils.setDraggable(elem);
    sinkDragStart(elem);

    if (handler instanceof HasDragHandler) {
      sinkDrag(elem);
    }
    if (handler instanceof HasDragEndHandler) {
      sinkDragEnd(elem);
    }
    
    dndSources.put(id, handler);

    return true;
  }

  public static boolean makeDndTarget(Element elem, HasDropHandler handler) {
    if (!dnd) {
      return false;
    }

    String id = DomUtils.ensureId(elem, "dnd");
    if (isDndTarget(id)) {
      return true;
    }

    sinkDrop(elem);
    sinkDragEnter(elem);
    sinkDragOver(elem);

    if (handler instanceof HasDragLeaveHandler) {
      sinkDragLeave(elem);
    }

    dndTargets.put(id, handler);
    
    return true;
  }

  public static void removeDndHandler(Object handler) {
    Assert.notNull(handler);

    if (handler instanceof HasDragStartHandler) {
      removeDndSource((HasDragStartHandler) handler);
    }
    if (handler instanceof HasDropHandler) {
      removeDndTarget((HasDropHandler) handler);
    }
  }
  
  public static void removeDndSource(HasDragStartHandler handler) {
    BeeUtils.removeValue(dndSources, handler);
  }

  public static void removeDndTarget(HasDropHandler handler) {
    BeeUtils.removeValue(dndTargets, handler);
  }
  
  public static List<StringProp> showDnd() {
    List<StringProp> lst = new ArrayList<StringProp>();
    
    for (String id : dndSources.keySet()) {
      lst.add(new StringProp(id, dndTargets.containsKey(id) ? "S+T" : "S"));
    }
    for (String id : dndTargets.keySet()) {
      if (!dndSources.containsKey(id)) {
        lst.add(new StringProp(id, "T"));
      }
    }
    
    return lst;
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
  
  public static boolean supportsDnd() {
    return dnd;
  }

  public static String transformCloseEvent(CloseEvent<?> ev) {
    if (ev == null) {
      return BeeConst.STRING_EMPTY;
    }

    return BeeUtils.transformOptions("source", DomUtils.transform(ev.getSource()),
        "target", DomUtils.transform(ev.getTarget()), "auto", ev.isAutoClosed());
  }

  public static String transformEvent(NativeEvent ev) {
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

  private static boolean dispatchDnd(DndEvent evt) {
    boolean ret = false;
    if (evt == null) {
      BeeKeeper.getLog().severe("dnd: event is null");
      return ret;
    }
    
    EventTarget eventTarget = evt.getEventTarget();
    if (eventTarget == null) {
      BeeKeeper.getLog().severe("dnd: event target is null");
      return ret;
    }
    
    String id = Element.as(eventTarget).getId();
    if (BeeUtils.isEmpty(id)) {
      BeeKeeper.getLog().severe("dnd: element has no id");
      return ret;
    }
    
    TYPE type = evt.getDndType();
    if (type == null) {
      BeeKeeper.getLog().severe("dnd: event type", evt.getType(), "not recognized");
      return ret;
    }
    
    if (type.isSourceEvent()) {
      HasDragStartHandler sourceHandler = dndSources.get(id);
      if (sourceHandler == null) {
        BeeKeeper.getLog().severe("dnd: source handler not found, id", id);
        return ret;
      }
      
      switch (type) {
        case DRAGSTART:
          ret = sourceHandler.onDragStart(evt);
          break;
        case DRAG:
          ret = ((HasDragHandler) sourceHandler).onDrag(evt);
          break;
        case DRAGEND:
          ret = ((HasDragEndHandler) sourceHandler).onDragEnd(evt);
          break;
        default:
          Assert.untouchable(transformEvent(evt));
      }

    } else {
      HasDropHandler targetHandler = dndTargets.get(id);
      if (targetHandler == null) {
        BeeKeeper.getLog().severe("dnd: target handler not found, id", id);
        return ret;
      }
      
      switch (type) {
        case DROP:
          ret = targetHandler.onDrop(evt);
          break;
        case DRAGENTER:
          if (targetHandler instanceof HasDragEnterHandler) {
            ret = ((HasDragEnterHandler) targetHandler).onDragEnter(evt);
          } else {
            ret = targetHandler.onDrop(evt);
          }
          break;
        case DRAGOVER:
          if (targetHandler instanceof HasDragOverHandler) {
            ret = ((HasDragOverHandler) targetHandler).onDragOver(evt);
          } else {
            ret = targetHandler.onDrop(evt);
          }
          break;
        case DRAGLEAVE:
          ret = ((HasDragLeaveHandler) targetHandler).onDragLeave(evt);
          break;
        default:
          Assert.untouchable(transformEvent(evt));
      }
    }
    
    return ret;
  }

  private static native void initDnd() /*-{
    @com.butent.bee.egg.client.event.EventUtils::onDnd = $entry(function(evt) {
      return @com.butent.bee.egg.client.event.EventUtils::dispatchDnd(Lcom/butent/bee/egg/client/event/DndEvent;)(evt);
    });
  }-*/;
  
  private static boolean isDndSource(String id) {
    return dndSources.containsKey(id);
  }

  private static boolean isDndTarget(String id) {
    return dndTargets.containsKey(id);
  }

  private static native String sinkDrag(Element elem) /*-{
    elem.ondrag = @com.butent.bee.egg.client.event.EventUtils::onDnd;
  }-*/;

  private static native String sinkDragEnd(Element elem) /*-{
    elem.ondragend = @com.butent.bee.egg.client.event.EventUtils::onDnd;
  }-*/;

  private static native String sinkDragEnter(Element elem) /*-{
    elem.ondragenter = @com.butent.bee.egg.client.event.EventUtils::onDnd;
  }-*/;

  private static native String sinkDragLeave(Element elem) /*-{
    elem.ondragleave = @com.butent.bee.egg.client.event.EventUtils::onDnd;
  }-*/;

  private static native String sinkDragOver(Element elem) /*-{
    elem.ondragover = @com.butent.bee.egg.client.event.EventUtils::onDnd;
  }-*/;

  private static native String sinkDragStart(Element elem) /*-{
    elem.ondragstart = @com.butent.bee.egg.client.event.EventUtils::onDnd;
  }-*/;

  private static native String sinkDrop(Element elem) /*-{
    elem.ondrop = @com.butent.bee.egg.client.event.EventUtils::onDnd;
  }-*/;

  private static String transformEventTarget(EventTarget et) {
    if (et == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return DomUtils.transformElement(Element.as(et));
    }
  }

}
