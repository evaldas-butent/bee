package com.butent.bee.client.event;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.CanPlayThroughEvent;
import com.google.gwt.event.dom.client.CanPlayThroughHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.EndedEvent;
import com.google.gwt.event.dom.client.EndedHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.GestureChangeEvent;
import com.google.gwt.event.dom.client.GestureChangeHandler;
import com.google.gwt.event.dom.client.GestureEndEvent;
import com.google.gwt.event.dom.client.GestureEndHandler;
import com.google.gwt.event.dom.client.GestureStartEvent;
import com.google.gwt.event.dom.client.GestureStartHandler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasCanPlayThroughHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasContextMenuHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.HasDragEndHandlers;
import com.google.gwt.event.dom.client.HasDragEnterHandlers;
import com.google.gwt.event.dom.client.HasDragHandlers;
import com.google.gwt.event.dom.client.HasDragLeaveHandlers;
import com.google.gwt.event.dom.client.HasDragOverHandlers;
import com.google.gwt.event.dom.client.HasDragStartHandlers;
import com.google.gwt.event.dom.client.HasDropHandlers;
import com.google.gwt.event.dom.client.HasEndedHandlers;
import com.google.gwt.event.dom.client.HasErrorHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.HasGestureChangeHandlers;
import com.google.gwt.event.dom.client.HasGestureEndHandlers;
import com.google.gwt.event.dom.client.HasGestureStartHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.HasLoadHandlers;
import com.google.gwt.event.dom.client.HasLoseCaptureHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
import com.google.gwt.event.dom.client.HasNativeEvent;
import com.google.gwt.event.dom.client.HasProgressHandlers;
import com.google.gwt.event.dom.client.HasScrollHandlers;
import com.google.gwt.event.dom.client.HasTouchCancelHandlers;
import com.google.gwt.event.dom.client.HasTouchEndHandlers;
import com.google.gwt.event.dom.client.HasTouchMoveHandlers;
import com.google.gwt.event.dom.client.HasTouchStartHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.LoseCaptureEvent;
import com.google.gwt.event.dom.client.LoseCaptureHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.ProgressEvent;
import com.google.gwt.event.dom.client.ProgressHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.media.dom.client.MediaError;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

/**
 * Contains core event processing methods.
 */

public class EventUtils {

  public static final String EVENT_TYPE_BLUR = "blur";

  public static final String EVENT_TYPE_CAN_PLAY_THROUGH = "canplaythrough";

  public static final String EVENT_TYPE_CHANGE = "change";

  public static final String EVENT_TYPE_CLICK = "click";

  public static final String EVENT_TYPE_CONTEXT_MENU = "contextmenu";

  public static final String EVENT_TYPE_DBL_CLICK = "dblclick";

  public static final String EVENT_TYPE_DRAG = "drag";
  public static final String EVENT_TYPE_DRAG_END = "dragend";
  public static final String EVENT_TYPE_DRAG_ENTER = "dragenter";
  public static final String EVENT_TYPE_DRAG_LEAVE = "dragleave";
  public static final String EVENT_TYPE_DRAG_OVER = "dragover";
  public static final String EVENT_TYPE_DRAG_START = "dragstart";
  public static final String EVENT_TYPE_DROP = "drop";

  public static final String EVENT_TYPE_ENDED = "ended";

  public static final String EVENT_TYPE_ERROR = "error";

  public static final String EVENT_TYPE_FOCUS = "focus";
  public static final String EVENT_TYPE_FOCUS_IN = "focusin";
  public static final String EVENT_TYPE_FOCUS_OUT = "focusout";

  public static final String EVENT_TYPE_GESTURE_CHANGE = "gesturechange";
  public static final String EVENT_TYPE_GESTURE_END = "gestureend";
  public static final String EVENT_TYPE_GESTURE_START = "gesturestart";

  public static final String EVENT_TYPE_KEY_DOWN = "keydown";
  public static final String EVENT_TYPE_KEY_PRESS = "keypress";
  public static final String EVENT_TYPE_KEY_UP = "keyup";

  public static final String EVENT_TYPE_LOAD = "load";

  public static final String EVENT_TYPE_LOSE_CAPTURE = "losecapture";

  public static final String EVENT_TYPE_MOUSE_DOWN = "mousedown";
  public static final String EVENT_TYPE_MOUSE_MOVE = "mousemove";
  public static final String EVENT_TYPE_MOUSE_OUT = "mouseout";
  public static final String EVENT_TYPE_MOUSE_OVER = "mouseover";
  public static final String EVENT_TYPE_MOUSE_UP = "mouseup";
  public static final String EVENT_TYPE_MOUSE_WHEEL = "mousewheel";

  public static final String EVENT_TYPE_PASTE = "paste";

  public static final String EVENT_TYPE_PROGRESS = "progress";

  public static final String EVENT_TYPE_SCROLL = "scroll";

  public static final String EVENT_TYPE_TOUCH_CANCEL = "touchcancel";
  public static final String EVENT_TYPE_TOUCH_END = "touchend";
  public static final String EVENT_TYPE_TOUCH_MOVE = "touchmove";
  public static final String EVENT_TYPE_TOUCH_START = "touchstart";

  public static final int KEY_INSERT = 45;

  public static final String PROPERTY_DROP_EFFECT = "dropEffect";
  public static final String PROPERTY_EFFECT_ALLOWED = "effectAllowed";

  public static final String EFFECT_COPY = "copy";
  public static final String EFFECT_MOVE = "move";

  public static final String DEFAULT_DND_DATA_FORMAT = "text/plain";
  
  private static final Map<String, JavaScriptObject> domHandlers = Maps.newHashMap();
  
  public static void addClassName(NativeEvent ev, String className) {
    Assert.notEmpty(className);
    Element element = getEventTargetElement(ev);
    if (element != null) {
      element.addClassName(className);
    }
  }

  public static void addClassName(HasNativeEvent ev, String className) {
    Assert.notNull(ev);
    addClassName(ev.getNativeEvent(), className);
  }
  
  public static boolean addDomHandler(final Widget widget, String type, String body) {
    Assert.notNull(widget);
    Assert.notEmpty(type);
    Assert.notEmpty(body);

    final JavaScriptObject handler = getDomHandler(body);
    boolean ok = false;

    if (BeeUtils.same(type, EVENT_TYPE_BLUR)) {
      ok = widget instanceof HasBlurHandlers;
      if (ok) {
        ((HasBlurHandlers) widget).addBlurHandler(new BlurHandler() {
          public void onBlur(BlurEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_CAN_PLAY_THROUGH)) {
      ok = widget instanceof HasCanPlayThroughHandlers;
      if (ok) {
        ((HasCanPlayThroughHandlers) widget).addCanPlayThroughHandler(new CanPlayThroughHandler() {
          public void onCanPlayThrough(CanPlayThroughEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_CHANGE)) {
      ok = widget instanceof HasChangeHandlers;
      if (ok) {
        ((HasChangeHandlers) widget).addChangeHandler(new ChangeHandler() {
          public void onChange(ChangeEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }
 
    } else if (BeeUtils.same(type, EVENT_TYPE_CLICK)) {
      ok = widget instanceof HasClickHandlers;
      if (ok) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_CONTEXT_MENU)) {
      ok = widget instanceof HasContextMenuHandlers;
      if (ok) {
        ((HasContextMenuHandlers) widget).addContextMenuHandler(new ContextMenuHandler() {
          public void onContextMenu(ContextMenuEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DBL_CLICK)) {
      ok = widget instanceof HasDoubleClickHandlers;
      if (ok) {
        ((HasDoubleClickHandlers) widget).addDoubleClickHandler(new DoubleClickHandler() {
          public void onDoubleClick(DoubleClickEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DRAG)) {
      ok = widget instanceof HasDragHandlers;
      if (ok) {
        ((HasDragHandlers) widget).addDragHandler(new DragHandler() {
          public void onDrag(DragEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DRAG_END)) {
      ok = widget instanceof HasDragEndHandlers;
      if (ok) {
        ((HasDragEndHandlers) widget).addDragEndHandler(new DragEndHandler() {
          public void onDragEnd(DragEndEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DRAG_ENTER)) {
      ok = widget instanceof HasDragEnterHandlers;
      if (ok) {
        ((HasDragEndHandlers) widget).addDragEndHandler(new DragEndHandler() {
          public void onDragEnd(DragEndEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DRAG_LEAVE)) {
      ok = widget instanceof HasDragLeaveHandlers;
      if (ok) {
        ((HasDragLeaveHandlers) widget).addDragLeaveHandler(new DragLeaveHandler() {
          public void onDragLeave(DragLeaveEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DRAG_OVER)) {
      ok = widget instanceof HasDragOverHandlers;
      if (ok) {
        ((HasDragOverHandlers) widget).addDragOverHandler(new DragOverHandler() {
          public void onDragOver(DragOverEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DRAG_START)) {
      ok = widget instanceof HasDragStartHandlers;
      if (ok) {
        ((HasDragStartHandlers) widget).addDragStartHandler(new DragStartHandler() {
          public void onDragStart(DragStartEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_DROP)) {
      ok = widget instanceof HasDropHandlers;
      if (ok) {
        ((HasDropHandlers) widget).addDropHandler(new DropHandler() {
          public void onDrop(DropEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_ENDED)) {
      ok = widget instanceof HasEndedHandlers;
      if (ok) {
        ((HasEndedHandlers) widget).addEndedHandler(new EndedHandler() {
          public void onEnded(EndedEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_ERROR)) {
      ok = widget instanceof HasErrorHandlers;
      if (ok) {
        ((HasErrorHandlers) widget).addErrorHandler(new ErrorHandler() {
          public void onError(ErrorEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_FOCUS)) {
      ok = widget instanceof HasFocusHandlers;
      if (ok) {
        ((HasFocusHandlers) widget).addFocusHandler(new FocusHandler() {
          public void onFocus(FocusEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_GESTURE_CHANGE)) {
      ok = widget instanceof HasGestureChangeHandlers;
      if (ok) {
        ((HasGestureChangeHandlers) widget).addGestureChangeHandler(new GestureChangeHandler() {
          public void onGestureChange(GestureChangeEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_GESTURE_END)) {
      ok = widget instanceof HasGestureEndHandlers;
      if (ok) {
        ((HasGestureEndHandlers) widget).addGestureEndHandler(new GestureEndHandler() {
          public void onGestureEnd(GestureEndEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_GESTURE_START)) {
      ok = widget instanceof HasGestureStartHandlers;
      if (ok) {
        ((HasGestureStartHandlers) widget).addGestureStartHandler(new GestureStartHandler() {
          public void onGestureStart(GestureStartEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_KEY_DOWN)) {
      ok = widget instanceof HasKeyDownHandlers;
      if (ok) {
        ((HasKeyDownHandlers) widget).addKeyDownHandler(new KeyDownHandler() {
          public void onKeyDown(KeyDownEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_KEY_PRESS)) {
      ok = widget instanceof HasKeyPressHandlers;
      if (ok) {
        ((HasKeyPressHandlers) widget).addKeyPressHandler(new KeyPressHandler() {
          public void onKeyPress(KeyPressEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_KEY_UP)) {
      ok = widget instanceof HasKeyUpHandlers;
      if (ok) {
        ((HasKeyUpHandlers) widget).addKeyUpHandler(new KeyUpHandler() {
          public void onKeyUp(KeyUpEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_LOAD)) {
      ok = widget instanceof HasLoadHandlers;
      if (ok) {
        ((HasLoadHandlers) widget).addLoadHandler(new LoadHandler() {
          public void onLoad(LoadEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_LOSE_CAPTURE)) {
      ok = widget instanceof HasLoseCaptureHandlers;
      if (ok) {
        ((HasLoseCaptureHandlers) widget).addLoseCaptureHandler(new LoseCaptureHandler() {
          public void onLoseCapture(LoseCaptureEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_MOUSE_DOWN)) {
      ok = widget instanceof HasMouseDownHandlers;
      if (ok) {
        ((HasMouseDownHandlers) widget).addMouseDownHandler(new MouseDownHandler() {
          public void onMouseDown(MouseDownEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_MOUSE_MOVE)) {
      ok = widget instanceof HasMouseMoveHandlers;
      if (ok) {
        ((HasMouseMoveHandlers) widget).addMouseMoveHandler(new MouseMoveHandler() {
          public void onMouseMove(MouseMoveEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_MOUSE_OUT)) {
      ok = widget instanceof HasMouseOutHandlers;
      if (ok) {
        ((HasMouseOutHandlers) widget).addMouseOutHandler(new MouseOutHandler() {
          public void onMouseOut(MouseOutEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_MOUSE_OVER)) {
      ok = widget instanceof HasMouseOverHandlers;
      if (ok) {
        ((HasMouseOverHandlers) widget).addMouseOverHandler(new MouseOverHandler() {
          public void onMouseOver(MouseOverEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_MOUSE_UP)) {
      ok = widget instanceof HasMouseUpHandlers;
      if (ok) {
        ((HasMouseUpHandlers) widget).addMouseUpHandler(new MouseUpHandler() {
          public void onMouseUp(MouseUpEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }
    
    } else if (BeeUtils.same(type, EVENT_TYPE_MOUSE_WHEEL)) {
      ok = widget instanceof HasMouseWheelHandlers;
      if (ok) {
        ((HasMouseWheelHandlers) widget).addMouseWheelHandler(new MouseWheelHandler() {
          public void onMouseWheel(MouseWheelEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_PROGRESS)) {
      ok = widget instanceof HasProgressHandlers;
      if (ok) {
        ((HasProgressHandlers) widget).addProgressHandler(new ProgressHandler() {
          public void onProgress(ProgressEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_SCROLL)) {
      ok = widget instanceof HasScrollHandlers;
      if (ok) {
        ((HasScrollHandlers) widget).addScrollHandler(new ScrollHandler() {
          public void onScroll(ScrollEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_TOUCH_CANCEL)) {
      ok = widget instanceof HasTouchCancelHandlers;
      if (ok) {
        ((HasTouchCancelHandlers) widget).addTouchCancelHandler(new TouchCancelHandler() {
          public void onTouchCancel(TouchCancelEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_TOUCH_END)) {
      ok = widget instanceof HasTouchEndHandlers;
      if (ok) {
        ((HasTouchEndHandlers) widget).addTouchEndHandler(new TouchEndHandler() {
          public void onTouchEnd(TouchEndEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_TOUCH_MOVE)) {
      ok = widget instanceof HasTouchMoveHandlers;
      if (ok) {
        ((HasTouchMoveHandlers) widget).addTouchMoveHandler(new TouchMoveHandler() {
          public void onTouchMove(TouchMoveEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }

    } else if (BeeUtils.same(type, EVENT_TYPE_TOUCH_START)) {
      ok = widget instanceof HasTouchStartHandlers;
      if (ok) {
        ((HasTouchStartHandlers) widget).addTouchStartHandler(new TouchStartHandler() {
          public void onTouchStart(TouchStartEvent event) {
            dispatchDomHandler(widget, handler, event);
          }
        });
      }
    }

    return ok;
  }

  public static void blur(Element elem) {
    Assert.notNull(elem);
    elem.blur();
  }

  public static NativeEvent createKeyDown(int keyCode) {
    return Document.get().createKeyDownEvent(false, false, false, false, keyCode);
  }

  public static NativeEvent createKeyPress(int charCode) {
    return Document.get().createKeyPressEvent(false, false, false, false, charCode);
  }

  public static NativeEvent createKeyUp(int keyCode) {
    return Document.get().createKeyUpEvent(false, false, false, false, keyCode);
  }

  public static void eatEvent(HasNativeEvent ev) {
    Assert.notNull(ev);
    eatEvent(ev.getNativeEvent());
  }

  public static void eatEvent(NativeEvent ev) {
    Assert.notNull(ev);
    ev.preventDefault();
    ev.stopPropagation();
  }

  public static boolean equalsOrIsChild(Element parent, EventTarget target) {
    if (parent == null || target == null) {
      return false;
    }
    return parent.isOrHasChild(Node.as(target));
  }

  public static void fireKeyDown(Element target, int keyCode) {
    Assert.notNull(target);
    target.dispatchEvent(createKeyDown(keyCode));
  }

  public static void fireKeyDown(EventTarget target, int keyCode) {
    Assert.notNull(target);
    fireKeyDown(Element.as(target), keyCode);
  }

  public static void fireKeyPress(Element target, int charCode) {
    Assert.notNull(target);
    target.dispatchEvent(createKeyPress(charCode));
  }

  public static void fireKeyPress(EventTarget target, int charCode) {
    Assert.notNull(target);
    fireKeyPress(Element.as(target), charCode);
  }

  public static void fireKeyUp(Element target, int keyCode) {
    Assert.notNull(target);
    target.dispatchEvent(createKeyUp(keyCode));
  }

  public static void fireKeyUp(EventTarget target, int keyCode) {
    Assert.notNull(target);
    fireKeyUp(Element.as(target), keyCode);
  }

  public static void focus(Element elem) {
    Assert.notNull(elem);
    elem.focus();
  }

  public static List<Property> getEventInfo(NativeEvent ev) {
    Assert.notNull(ev);
    return PropertyUtils.createProperties(
        "Client X", ev.getClientX(),
        "Client Y", ev.getClientY(),
        "Screen X", ev.getScreenX(),
        "Screen Y", ev.getScreenY(),
        "Key Code", ev.getKeyCode(),
        "Char Code", ev.getCharCode(),
        "Alt Key", ev.getAltKey(),
        "Shift Key", ev.getShiftKey(),
        "Ctrl Key", ev.getCtrlKey(),
        "Meta Key", ev.getMetaKey(),
        "Button", ev.getButton(),
        "Mouse Wheel Velocity Y", ev.getMouseWheelVelocityY(),
        "Event Target", transformEventTarget(ev.getEventTarget()),
        "Current Event Target", transformEventTarget(ev.getCurrentEventTarget()),
        "Related Event Target", transformEventTarget(ev.getRelatedEventTarget()),
        "String", ev.getString(),
        "Type", ev.getType(),
        "Type Int", getTypeInt(ev));
  }

  public static Element getEventTargetElement(HasNativeEvent ev) {
    Assert.notNull(ev);
    return getEventTargetElement(ev.getNativeEvent());
  }

  public static Element getEventTargetElement(NativeEvent ev) {
    Assert.notNull(ev);
    return Element.as(ev.getEventTarget());
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

  public static String getTargetTagName(EventTarget et) {
    Assert.notNull(et);
    return Element.as(et).getTagName();
  }

  public static int getTypeInt(NativeEvent ev) {
    if (ev == null) {
      return 0;
    }
    if (ev instanceof Event) {
      return ((Event) ev).getTypeInt();
    }
    return Event.getTypeInt(ev.getType());
  }

  public static boolean hasModifierKey(NativeEvent ev) {
    if (ev == null) {
      return false;
    }
    return ev.getShiftKey() || ev.getCtrlKey() || ev.getAltKey() || ev.getMetaKey();
  }

  public static boolean isArrowKey(NativeEvent ev) {
    if (ev == null) {
      return false;
    } else {
      return isArrowKey(ev.getKeyCode());
    }
  }
  
  public static boolean isArrowKey(int keyCode) {
    switch (keyCode) {
      case KeyCodes.KEY_DOWN:
      case KeyCodes.KEY_LEFT:
      case KeyCodes.KEY_RIGHT:
      case KeyCodes.KEY_UP:
        return true;
      default:
        return false;
    }
  }
  
  public static boolean isBlur(String type) {
    return isEventType(type, EVENT_TYPE_BLUR);
  }

  public static boolean isChange(String type) {
    return isEventType(type, EVENT_TYPE_CHANGE);
  }

  public static boolean isClick(NativeEvent ev) {
    if (ev == null) {
      return false;
    }
    return isClick(ev.getType());
  }

  public static boolean isClick(String type) {
    return isEventType(type, EVENT_TYPE_CLICK);
  }

  public static boolean isDblClick(String type) {
    return isEventType(type, EVENT_TYPE_DBL_CLICK);
  }
  
  public static boolean isEventType(String t1, String t2) {
    Assert.notEmpty(t1);
    Assert.notEmpty(t2);
    return BeeUtils.same(t1, t2);
  }

  public static boolean isFocus(String type) {
    return isEventType(type, EVENT_TYPE_FOCUS);
  }

  public static boolean isInputElement(EventTarget et) {
    return isTargetTagName(et, DomUtils.TAG_INPUT);
  }

  public static boolean isKeyDown(String type) {
    return isEventType(type, EVENT_TYPE_KEY_DOWN);
  }

  public static boolean isKeyEvent(Event ev) {
    if (ev == null) {
      return false;
    } else {
      return isKeyEvent(ev.getTypeInt());
    }
  }

  public static boolean isKeyEvent(int type) {
    return (type & Event.KEYEVENTS) != 0;
  }

  public static boolean isKeyEvent(String type) {
    return isKeyDown(type) || isKeyPress(type) || isKeyUp(type);
  }
  
  public static boolean isKeyPress(String type) {
    return isEventType(type, EVENT_TYPE_KEY_PRESS);
  }

  public static boolean isKeyUp(String type) {
    return isEventType(type, EVENT_TYPE_KEY_UP);
  }

  public static boolean isMouseDown(String type) {
    return isEventType(type, EVENT_TYPE_MOUSE_DOWN);
  }

  public static boolean isMouseMove(String type) {
    return isEventType(type, EVENT_TYPE_MOUSE_MOVE);
  }

  public static boolean isMouseOut(String type) {
    return isEventType(type, EVENT_TYPE_MOUSE_OUT);
  }

  public static boolean isMouseOver(String type) {
    return isEventType(type, EVENT_TYPE_MOUSE_OVER);
  }

  public static boolean isMouseUp(String type) {
    return isEventType(type, EVENT_TYPE_MOUSE_UP);
  }

  public static boolean isTargetId(EventTarget et, String id) {
    if (et == null || BeeUtils.isEmpty(id)) {
      return false;
    }
    return BeeUtils.same(getTargetId(et), id);
  }

  public static boolean isTargetTagName(EventTarget et, String tagName) {
    if (et == null || BeeUtils.isEmpty(tagName)) {
      return false;
    }
    return BeeUtils.same(getTargetTagName(et), tagName);
  }

  public static void logEvent(NativeEvent ev) {
    logEvent(ev, false, null);
  }

  public static void logEvent(NativeEvent ev, boolean detailed, String title) {
    Assert.notNull(ev);
    if (!BeeUtils.isEmpty(title)) {
      BeeKeeper.getLog().debug(title);
    }

    if (detailed) {
      List<Property> lst = getEventInfo(ev);
      for (Property el : lst) {
        BeeKeeper.getLog().debug(el.getName(), el.getValue());
      }

      BeeKeeper.getLog().addSeparator();
    } else {
      BeeKeeper.getLog().debug(transformEvent(ev));
    }
  }

  public static void logEvent(NativeEvent ev, String title) {
    logEvent(ev, false, title);
  }

  public static boolean makeDndSource(HasAllDragAndDropHandlers widget, DragStartHandler handler) {
    if (!DragDropEventBase.isSupported()) {
      return false;
    }
    Assert.notNull(widget);
    Assert.notNull(handler);
    
    if (widget instanceof UIObject) {
      DomUtils.setDraggable((UIObject) widget);
    }
    
    widget.addDragStartHandler(handler);
    if (handler instanceof DragHandler) {
      widget.addDragHandler((DragHandler) handler);
    }
    if (handler instanceof DragEndHandler) {
      widget.addDragEndHandler((DragEndHandler) handler);
    }
    return true;
  }
  
  public static boolean makeDndTarget(HasAllDragAndDropHandlers widget, DropHandler handler) {
    if (!DragDropEventBase.isSupported()) {
      return false;
    }
    Assert.notNull(widget);
    Assert.notNull(handler);
    
    widget.addDropHandler(handler);
    if (handler instanceof DragEnterHandler) {
      widget.addDragEnterHandler((DragEnterHandler) handler);
    }
    if (handler instanceof DragOverHandler) {
      widget.addDragOverHandler((DragOverHandler) handler);
    }
    if (handler instanceof DragLeaveHandler) {
      widget.addDragLeaveHandler((DragLeaveHandler) handler);
    }
    return true;
  }
  
  public static void removeClassName(NativeEvent ev, String className) {
    Assert.notEmpty(className);
    Element element = getEventTargetElement(ev);
    if (element != null) {
      element.removeClassName(className);
    }
  }

  public static void removeClassName(HasNativeEvent ev, String className) {
    Assert.notNull(ev);
    removeClassName(ev.getNativeEvent(), className);
  }
  
  public static void setDndData(DragDropEventBase<?> event, String data) {
    setDndData(event, DEFAULT_DND_DATA_FORMAT, data);
  }
  
  public static void setDndData(DragDropEventBase<?> event, String format, String data) {
    Assert.notNull(event);
    Assert.notEmpty(format);
    Assert.notEmpty(data);
    
    event.setData(format, data);
  }
  
  public static void setDropEffect(DragDropEventBase<?> event, String effect) {
    Assert.notNull(event);
    Assert.notEmpty(effect);
    
    JsUtils.setProperty(event.getDataTransfer(), PROPERTY_DROP_EFFECT, effect);
  }

  public static void setEffectAllowed(DragDropEventBase<?> event, String effect) {
    Assert.notNull(event);
    Assert.notEmpty(effect);
    
    JsUtils.setProperty(event.getDataTransfer(), PROPERTY_EFFECT_ALLOWED, effect);
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

  public static String transformEvent(NativeEvent ev) {
    return transformEvent(ev, true);
  }

  public static String transformEvent(NativeEvent ev, boolean targets) {
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
    if (b != 0) {
      sb.append(" b=" + b);
    }
    int v = ev.getMouseWheelVelocityY();
    if (v != 0) {
      sb.append(" mwv=" + v);
    }

    if (targets) {
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
    }
    return sb.toString();
  }

  public static String transformMediaError(MediaError error) {
    if (error == null) {
      return BeeConst.STRING_EMPTY;
    }

    switch (error.getCode()) {
      case MediaError.MEDIA_ERR_ABORTED:
        return "playback aborted";
      case MediaError.MEDIA_ERR_DECODE:
        return "decoding error";
      case MediaError.MEDIA_ERR_NETWORK:
        return "network error";
      case MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED:
        return "source not supported";
    }
    return "unknown error";
  }

  private static JavaScriptObject createDomHandler(String body) {
    return JsUtils.createFunction("event, target, row, rowId, rowVersion", body);
  }

  private static void dispatchDomHandler(Widget widget, JavaScriptObject handler,
      HasNativeEvent event) {
    JavaScriptObject row;
    double rowId;
    double rowVersion;
    
    FormView form = UiHelper.getForm(widget);
    IsRow data = (form == null) ? null : form.getRow();
    
    if (data == null) {
      row = null;
      rowId = BeeConst.UNDEF;
      rowVersion = BeeConst.UNDEF;
    } else {
      row = form.getRowJso();
      rowId = data.getId();
      rowVersion = data.getVersion();
    }

    String errorMessage = evalDomHandler(handler, event.getNativeEvent(),
        event.getNativeEvent().getEventTarget(), row, rowId, rowVersion);
    if (!BeeUtils.isEmpty(errorMessage)) {
      BeeKeeper.getLog().severe(errorMessage);
      BeeKeeper.getScreen().notifySevere(errorMessage);
    }
  }

  private static String domHandlerKey(String body) {
    return body;
  }

  private static native String evalDomHandler(JavaScriptObject handler, NativeEvent event,
      JavaScriptObject target, JavaScriptObject row, double rowId, double rowVersion) /*-{
    try {
      handler(event, target, row, rowId, rowVersion);
      return null;
    } catch (err) {
      return String(err);
    }
  }-*/;

  private static JavaScriptObject getDomHandler(String body) {
    String key = domHandlerKey(body);
    JavaScriptObject handler = domHandlers.get(key);

    if (handler == null) {
      handler = createDomHandler(body);
      domHandlers.put(key, handler);
    }
    return handler;
  }

  private static String transformEventTarget(EventTarget et) {
    if (et == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return DomUtils.transformElement(Element.as(et));
    }
  }

  private EventUtils() {
  }
}
