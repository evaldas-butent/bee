package com.butent.bee.client.dialog;

import com.google.common.collect.Lists;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.PreviewHandler;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class Popup extends SimplePanel implements HasAnimation, HasCloseHandlers<Popup>,
    IdentifiableWidget {

  public enum AnimationType {
    CENTER, ONE_WAY_CORNER, ROLL_DOWN
  }

  public interface PositionCallback {
    void setPosition(int offsetWidth, int offsetHeight);
  }

  private class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {
    private int dragStartX, dragStartY;

    private final int clientLeft;
    private final int clientTop;

    private MouseHandler() {
      this.clientLeft = Document.get().getBodyOffsetLeft();
      this.clientTop = Document.get().getBodyOffsetTop();
    }

    public void onMouseDown(MouseDownEvent event) {
      if (DOM.getCaptureElement() == null && isCaptionEvent(event.getNativeEvent())) {
        event.preventDefault();
        DOM.setCapture(getElement());

        setDragging(true);
        dragStartX = event.getX();
        dragStartY = event.getY();
      }
    }

    public void onMouseMove(MouseMoveEvent event) {
      if (isDragging()) {
        int absX = event.getX() + getAbsoluteLeft();
        int absY = event.getY() + getAbsoluteTop();
        if (absX < clientLeft || absX >= getWindowWidth() || absY < clientTop) {
          return;
        }
        setPopupPosition(absX - dragStartX, absY - dragStartY);
      }
    }

    public void onMouseUp(MouseUpEvent event) {
      if (isDragging()) {
        setDragging(false);
        DOM.releaseCapture(getElement());
      }
    }
  }

  private class ResizeAnimation extends Animation {

    private class GlassResizer implements ResizeHandler {
      private final Element glassElement;

      private GlassResizer(Element glassElement) {
        this.glassElement = Assert.notNull(glassElement);
      }

      public void onResize(ResizeEvent event) {
        Style style = glassElement.getStyle();

        int winWidth = Window.getClientWidth();
        int winHeight = Window.getClientHeight();

        style.setDisplay(Display.NONE);
        style.setWidth(0, Unit.PX);
        style.setHeight(0, Unit.PX);

        int width = Document.get().getScrollWidth();
        int height = Document.get().getScrollHeight();

        style.setWidth(Math.max(width, winWidth), Unit.PX);
        style.setHeight(Math.max(height, winHeight), Unit.PX);

        style.setDisplay(Display.BLOCK);
      }
    }

    private final Popup curPanel;

    private boolean show = false;
    private boolean isUnloading = false;
    private int level = BeeConst.UNDEF;

    private int offsetHeight = BeeConst.UNDEF;
    private int offsetWidth = BeeConst.UNDEF;

    private Timer showTimer = null;

    private boolean glassShowing = false;
    private HandlerRegistration resizeRegistration = null;

    private ResizeAnimation(Popup panel) {
      this.curPanel = panel;
    }

    @Override
    protected void onComplete() {
      if (!show) {
        maybeShowGlass();
        if (!isUnloading) {
          RootPanel.get().remove(curPanel);
        }
      }
      StyleUtils.clearClip(curPanel);
      curPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
    }

    @Override
    protected void onStart() {
      offsetHeight = curPanel.getOffsetHeight();
      offsetWidth = curPanel.getOffsetWidth();
      curPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
      super.onStart();
    }

    @Override
    protected void onUpdate(double progress) {
      double p = show ? progress : (1.0 - progress);

      int top = 0;
      int left = 0;
      int right = 0;
      int bottom = 0;

      int height = (int) (p * offsetHeight);
      int width = (int) (p * offsetWidth);

      switch (curPanel.getAnimationType()) {
        case ROLL_DOWN:
          right = offsetWidth;
          bottom = height;
          break;

        case CENTER:
          top = (offsetHeight - height) >> 1;
          left = (offsetWidth - width) >> 1;
          right = left + width;
          bottom = top + height;
          break;

        case ONE_WAY_CORNER:
          right = left + width;
          bottom = top + height;
          break;
      }
      StyleUtils.setClip(curPanel, top, right, bottom, left);
    }

    private void maybeShowGlass() {
      if (show) {
        if (curPanel.isGlassEnabled()) {
          if (level > 0) {
            curPanel.getGlass().getStyle().setZIndex(level);
          }
          Document.get().getBody().appendChild(curPanel.getGlass());

          GlassResizer glassResizer = new GlassResizer(curPanel.getGlass());
          resizeRegistration = Window.addResizeHandler(glassResizer);
          glassResizer.onResize(null);

          glassShowing = true;
        }
      } else if (glassShowing) {
        Document.get().getBody().removeChild(curPanel.getGlass());

        resizeRegistration.removeHandler();
        resizeRegistration = null;

        glassShowing = false;
      }
    }

    private void onInstantaneousRun() {
      maybeShowGlass();
      if (show) {
        curPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        if (!BeeConst.isUndef(curPanel.getTopPosition())) {
          curPanel.setPopupPosition(curPanel.getLeftPosition(), curPanel.getTopPosition());
        }
        RootPanel.get().add(curPanel);
      } else {
        if (!isUnloading) {
          RootPanel.get().remove(curPanel);
        }
      }
      curPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
    }

    private void setState(boolean show, boolean isUnloading, int level) {
      this.isUnloading = isUnloading;
      this.level = level;

      cancel();

      if (showTimer != null) {
        showTimer.cancel();
        showTimer = null;
        onComplete();
      }

      curPanel.setShowing(show);
      curPanel.updateHandlers();

      boolean animate = !isUnloading && curPanel.isAnimationEnabled();
      if (curPanel.getAnimationType() != AnimationType.CENTER && !show) {
        animate = false;
      }

      this.show = show;
      if (animate) {
        if (show) {
          maybeShowGlass();
          curPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
          if (!BeeConst.isUndef(curPanel.getTopPosition())) {
            curPanel.setPopupPosition(curPanel.getLeftPosition(), curPanel.getTopPosition());
          }
          StyleUtils.setClip(curPanel, 0, 0, 0, 0);
          RootPanel.get().add(curPanel);

          showTimer = new Timer() {
            @Override
            public void run() {
              showTimer = null;
              ResizeAnimation.this.run(ANIMATION_DURATION);
            }
          };
          showTimer.schedule(1);
        } else {
          run(ANIMATION_DURATION);
        }
      } else {
        onInstantaneousRun();
      }
    }
  }

  private static final String STYLE_POPUP = "bee-Popup";

  private static final int ANIMATION_DURATION = 250;

  public static Popup getActivePopup() {
    int widgetCount = RootPanel.get().getWidgetCount();
    for (int i = widgetCount - 1; i >= 0; i--) {
      Widget child = RootPanel.get().getWidget(i);
      if (child instanceof Popup && ((Popup) child).isShowing()) {
        return (Popup) child;
      }
    }
    return null;
  }

  private AnimationType animationType = AnimationType.CENTER;

  private final boolean autoHide;

  private final boolean modal;

  private boolean showing = false;

  private List<Element> autoHidePartners = null;

  private String desiredHeight = null;
  private String desiredWidth = null;

  private Element glass = null;
  private boolean isGlassEnabled = false;

  private int leftPosition = BeeConst.UNDEF;
  private int topPosition = BeeConst.UNDEF;

  private boolean previewAllNativeEvents = false;
  private HandlerRegistration nativePreviewHandlerRegistration = null;
  private HandlerRegistration resizeHandlerRegistration = null;

  private List<PreviewHandler> previewHandlers = null;
  private boolean hideOnEscape = false;
  private boolean hideOnSave = false;
  private Scheduler.ScheduledCommand onSave = null;
  private Scheduler.ScheduledCommand onEscape = null;

  private boolean isAnimationEnabled = false;
  private final ResizeAnimation resizeAnimation = new ResizeAnimation(this);

  private final String popupStyleName;

  private int windowWidth;
  private boolean dragging = false;
  private MouseHandler mouseHandler = null;

  public Popup(boolean autoHide, boolean modal) {
    this(autoHide, modal, STYLE_POPUP);
  }

  public Popup(boolean autoHide, boolean modal, String styleName) {
    super();

    this.autoHide = autoHide;
    this.modal = modal;

    setPopupPosition(0, 0);
    DomUtils.createId(this, getIdPrefix());

    this.popupStyleName = styleName;
    setStyleName(styleName);

    this.windowWidth = Window.getClientWidth();
  }

  public void addAutoHidePartner(Element partner) {
    Assert.notNull(partner, "auto hide partner cannot be null");
    if (getAutoHidePartners() == null) {
      setAutoHidePartners(Lists.newArrayList(partner));
    } else {
      getAutoHidePartners().add(partner);
    }
  }

  public HandlerRegistration addCloseHandler(CloseHandler<Popup> handler) {
    return addHandler(handler, CloseEvent.getType());
  }

  public void addPreviewHandler(PreviewHandler handler) {
    if (handler != null) {
      if (previewHandlers == null) {
        previewHandlers = Lists.newArrayList();
      }
      previewHandlers.add(handler);
    }
  }

  public void center() {
    boolean initiallyShowing = isShowing();
    boolean initiallyAnimated = isAnimationEnabled;

    if (!initiallyShowing) {
      setVisible(false);
      setAnimationEnabled(false);
      show();
    }

    Style style = getElement().getStyle();
    style.setLeft(0, Unit.PX);
    style.setTop(0, Unit.PX);

    int left = (Window.getClientWidth() - getOffsetWidth()) >> 1;
    int top = (Window.getClientHeight() - getOffsetHeight()) >> 1;
    setPopupPosition(Math.max(Window.getScrollLeft() + left, 0),
        Math.max(Window.getScrollTop() + top, 0));

    if (!initiallyShowing) {
      setAnimationEnabled(initiallyAnimated);
      if (initiallyAnimated) {
        StyleUtils.setClip(this, 0, 0, 0, 0);
        setVisible(true);
        resizeAnimation.run(ANIMATION_DURATION);
      } else {
        setVisible(true);
      }
    }
  }

  public void enableGlass() {
    setGlassEnabled(true);
  }

  public AnimationType getAnimationType() {
    return animationType;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "popup";
  }

  public Scheduler.ScheduledCommand getOnEscape() {
    return onEscape;
  }

  public Scheduler.ScheduledCommand getOnSave() {
    return onSave;
  }

  public int getPopupLeft() {
    return DOM.getAbsoluteLeft(getElement());
  }

  public int getPopupTop() {
    return DOM.getAbsoluteTop(getElement());
  }

  public void hide() {
    hide(false);
  }

  public void hide(boolean autoClosed) {
    if (!isShowing()) {
      return;
    }
    Stacking.removeContext(this);

    resizeAnimation.setState(false, false, BeeConst.UNDEF);
    CloseEvent.fire(this, this, autoClosed);
  }

  public boolean hideOnEscape() {
    return hideOnEscape;
  }

  public boolean hideOnSave() {
    return hideOnSave;
  }

  public boolean isAnimationEnabled() {
    return isAnimationEnabled;
  }

  public boolean isAutoHideEnabled() {
    return autoHide;
  }

  public boolean isGlassEnabled() {
    return isGlassEnabled;
  }

  public boolean isModal() {
    return modal;
  }

  public boolean isPreviewingAllNativeEvents() {
    return previewAllNativeEvents;
  }

  public boolean isShowing() {
    return showing;
  }

  @Override
  public boolean isVisible() {
    return !BeeUtils.same(getElement().getStyle().getVisibility(), Visibility.HIDDEN.getCssName());
  }

  public void removeAutoHidePartner(Element partner) {
    if (getAutoHidePartners() != null) {
      getAutoHidePartners().remove(partner);
    }
  }

  public void setAnimationEnabled(boolean enable) {
    isAnimationEnabled = enable;
  }

  public void setAnimationType(AnimationType animationType) {
    this.animationType = animationType;
  }

  public void setGlassEnabled(boolean enabled) {
    this.isGlassEnabled = enabled;

    if (enabled && getGlass() == null) {
      Element elem = Document.get().createDivElement();
      elem.addClassName(popupStyleName + "-glass");

      elem.getStyle().setPosition(Position.ABSOLUTE);
      elem.getStyle().setLeft(0, Unit.PX);
      elem.getStyle().setTop(0, Unit.PX);

      setGlass(elem);
    }
  }

  @Override
  public void setHeight(String height) {
    setDesiredHeight(height);
    maybeUpdateSize();
    if (BeeUtils.isEmpty(height)) {
      setDesiredHeight(null);
    }
  }

  public void setHideOnEscape(boolean hideOnEscape) {
    this.hideOnEscape = hideOnEscape;
  }

  public void setHideOnSave(boolean hideOnSave) {
    this.hideOnSave = hideOnSave;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setOnEscape(Scheduler.ScheduledCommand onEscape) {
    this.onEscape = onEscape;
  }

  public void setOnSave(Scheduler.ScheduledCommand onSave) {
    this.onSave = onSave;
  }

  public void setPopupPosition(int left, int top) {
    setLeftPosition(left);
    setTopPosition(top);

    Style style = getElement().getStyle();
    style.setLeft(left - Document.get().getBodyOffsetLeft(), Unit.PX);
    style.setTop(top - Document.get().getBodyOffsetTop(), Unit.PX);
  }

  public void setPopupPositionAndShow(PositionCallback callback) {
    setVisible(false);
    show();
    callback.setPosition(getOffsetWidth(), getOffsetHeight());
    setVisible(true);
  }

  public void setPreviewingAllNativeEvents(boolean previewAllNativeEvents) {
    this.previewAllNativeEvents = previewAllNativeEvents;
  }

  @Override
  public void setVisible(boolean visible) {
    getElement().getStyle().setVisibility(visible ? Visibility.VISIBLE : Visibility.HIDDEN);
    if (getGlass() != null) {
      getGlass().getStyle().setVisibility(visible ? Visibility.VISIBLE : Visibility.HIDDEN);
    }
  }

  @Override
  public void setWidget(Widget w) {
    super.setWidget(w);
    maybeUpdateSize();
  }

  @Override
  public void setWidth(String width) {
    setDesiredWidth(width);
    maybeUpdateSize();
    if (BeeUtils.isEmpty(width)) {
      setDesiredWidth(null);
    }
  }

  public void show() {
    if (isShowing()) {
      return;
    }
    if (isAttached()) {
      this.removeFromParent();
    }

    int level = Stacking.addContext(this);
    resizeAnimation.setState(true, false, level);
  }

  public void showAt(final int x, final int y, final int margin) {
    setPopupPositionAndShow(new PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = fitLeft(x, offsetWidth, margin);
        int top = fitTop(y, offsetHeight, margin);

        setPopupPosition(left, top);
      }
    });
  }

  public void showRelativeTo(final UIObject target) {
    setPopupPositionAndShow(new PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        position(target, offsetWidth, offsetHeight);
      }
    });
  }

  protected void enableDragging() {
    if (getMouseHandler() == null) {
      setMouseHandler(new MouseHandler());

      addDomHandler(getMouseHandler(), MouseDownEvent.getType());
      addDomHandler(getMouseHandler(), MouseUpEvent.getType());
      addDomHandler(getMouseHandler(), MouseMoveEvent.getType());
    }
  }

  /**
   * @param event
   */
  protected boolean isCaptionEvent(NativeEvent event) {
    return false;
  }

  protected void onPreviewNativeEvent(NativePreviewEvent event) {
    if (previewHandlers != null) {
      for (PreviewHandler handler : previewHandlers) {
        if (!event.isCanceled() || !event.isConsumed()) {
          handler.onEventPreview(event);
        }
      }
    }
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    if (isShowing()) {
      resizeAnimation.setState(false, true, BeeConst.UNDEF);
    }
  }

  private native void blur(Element elt) /*-{
    if (elt.blur && elt != $doc.body) {
      elt.blur();
    }
  }-*/;

  private boolean eventTargetsPartner(NativeEvent event) {
    if (getAutoHidePartners() == null) {
      return false;
    }

    EventTarget target = event.getEventTarget();
    if (Element.is(target)) {
      for (Element elem : getAutoHidePartners()) {
        if (elem.isOrHasChild(Element.as(target))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean eventTargetsPopup(NativeEvent event) {
    EventTarget target = event.getEventTarget();
    if (Node.is(target)) {
      return getElement().isOrHasChild(Node.as(target));
    }
    return false;
  }

  private int fitLeft(int left, int width, int margin) {
    int windowLeft = Window.getScrollLeft() + margin;
    int windowRight = Window.getClientWidth() + Window.getScrollLeft() - margin;

    return Math.max(Math.min(left, windowRight - width), windowLeft);
  }

  private int fitTop(int top, int height, int margin) {
    int windowTop = Window.getScrollTop() + margin;
    int windowBottom = Window.getScrollTop() + Window.getClientHeight() - margin;

    return Math.max(Math.min(top, windowBottom - height), windowTop);
  }

  private List<Element> getAutoHidePartners() {
    return autoHidePartners;
  }

  private String getDesiredHeight() {
    return desiredHeight;
  }

  private String getDesiredWidth() {
    return desiredWidth;
  }

  private Element getGlass() {
    return glass;
  }

  private int getLeftPosition() {
    return leftPosition;
  }

  private MouseHandler getMouseHandler() {
    return mouseHandler;
  }

  private HandlerRegistration getNativePreviewHandlerRegistration() {
    return nativePreviewHandlerRegistration;
  }

  private HandlerRegistration getResizeHandlerRegistration() {
    return resizeHandlerRegistration;
  }

  private int getTopPosition() {
    return topPosition;
  }

  private int getWindowWidth() {
    return windowWidth;
  }

  private boolean isDragging() {
    return dragging;
  }

  private void maybeUpdateSize() {
    Widget w = super.getWidget();
    if (w != null) {
      if (!BeeUtils.isEmpty(getDesiredHeight())) {
        w.setHeight(getDesiredHeight());
      }
      if (!BeeUtils.isEmpty(getDesiredWidth())) {
        w.setWidth(getDesiredWidth());
      }
    }
  }

  private void position(UIObject relativeObject, int offsetWidth, int offsetHeight) {
    int objectWidth = relativeObject.getOffsetWidth();
    int offsetWidthDiff = offsetWidth - objectWidth;

    int left = relativeObject.getAbsoluteLeft();

    if (offsetWidthDiff > 0) {
      int windowRight = Window.getClientWidth() + Window.getScrollLeft();
      int windowLeft = Window.getScrollLeft();

      int distanceToWindowRight = windowRight - left;
      int distanceFromWindowLeft = left - windowLeft;

      if (distanceToWindowRight < offsetWidth && distanceFromWindowLeft >= offsetWidthDiff) {
        left -= offsetWidthDiff;
      }
    } else if (offsetWidthDiff < 0) {
      left -= offsetWidthDiff;
    }

    int top = relativeObject.getAbsoluteTop();

    int windowTop = Window.getScrollTop();
    int windowBottom = Window.getScrollTop() + Window.getClientHeight();

    int distanceFromWindowTop = top - windowTop;
    int distanceToWindowBottom = windowBottom - (top + relativeObject.getOffsetHeight());

    if (distanceToWindowBottom < offsetHeight && distanceFromWindowTop >= offsetHeight) {
      top -= offsetHeight;
    } else {
      top += relativeObject.getOffsetHeight();
    }

    left = fitLeft(left, offsetWidth, 2);
    top = fitTop(top, offsetHeight, 2);

    setPopupPosition(left, top);
  }

  private void previewNativeEvent(NativePreviewEvent event) {
    if (event.isCanceled() || (!isPreviewingAllNativeEvents() && event.isConsumed())) {
      if (isModal()) {
        event.cancel();
      }
      return;
    }

    onPreviewNativeEvent(event);
    if (event.isCanceled()) {
      return;
    }

    NativeEvent nativeEvent = event.getNativeEvent();
    boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent)
        || eventTargetsPartner(nativeEvent);
    if (eventTargetsPopupOrPartner) {
      event.consume();
    }

    if (isModal()) {
      event.cancel();
    }

    int type = event.getTypeInt();
    switch (type) {
      case Event.ONMOUSEDOWN:
        if (DOM.getCaptureElement() != null) {
          event.consume();
        } else if (!eventTargetsPopupOrPartner && isAutoHideEnabled()) {
          hide(true);
        }
        break;

      case Event.ONMOUSEUP:
      case Event.ONMOUSEMOVE:
      case Event.ONCLICK:
      case Event.ONDBLCLICK:
        if (DOM.getCaptureElement() != null) {
          event.consume();
        }
        break;

      case Event.ONFOCUS:
        Element target = EventUtils.getEventTargetElement(nativeEvent);
        if (isModal() && !eventTargetsPopupOrPartner && (target != null)) {
          blur(target);
          event.cancel();
          return;
        }
        break;

      case Event.ONKEYDOWN:
        if (nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE) {
          if (hideOnEscape()) {
            hide(true);
          }
          if (getOnEscape() != null) {
            getOnEscape().execute();
          }

        } else if (UiHelper.isSave(nativeEvent)) {
          if (hideOnSave()) {
            hide(true);
          }
          if (getOnSave() != null) {
            getOnSave().execute();
          }
        }
        break;
    }
  }

  private void setAutoHidePartners(List<Element> autoHidePartners) {
    this.autoHidePartners = autoHidePartners;
  }

  private void setDesiredHeight(String desiredHeight) {
    this.desiredHeight = desiredHeight;
  }

  private void setDesiredWidth(String desiredWidth) {
    this.desiredWidth = desiredWidth;
  }

  private void setDragging(boolean dragging) {
    this.dragging = dragging;
  }

  private void setGlass(Element glass) {
    this.glass = glass;
  }

  private void setLeftPosition(int leftPosition) {
    this.leftPosition = leftPosition;
  }

  private void setMouseHandler(MouseHandler mouseHandler) {
    this.mouseHandler = mouseHandler;
  }

  private void setNativePreviewHandlerRegistration(HandlerRegistration nphr) {
    this.nativePreviewHandlerRegistration = nphr;
  }

  private void setResizeHandlerRegistration(HandlerRegistration resizeHandlerRegistration) {
    this.resizeHandlerRegistration = resizeHandlerRegistration;
  }

  private void setShowing(boolean showing) {
    this.showing = showing;
  }

  private void setTopPosition(int topPosition) {
    this.topPosition = topPosition;
  }

  private void setWindowWidth(int windowWidth) {
    this.windowWidth = windowWidth;
  }

  private void updateHandlers() {
    if (getNativePreviewHandlerRegistration() != null) {
      getNativePreviewHandlerRegistration().removeHandler();
      setNativePreviewHandlerRegistration(null);
    }

    if (getResizeHandlerRegistration() != null) {
      getResizeHandlerRegistration().removeHandler();
      setResizeHandlerRegistration(null);
    }

    if (isShowing()) {
      setNativePreviewHandlerRegistration(Event.addNativePreviewHandler(new NativePreviewHandler() {
        public void onPreviewNativeEvent(NativePreviewEvent event) {
          previewNativeEvent(event);
        }
      }));

      setResizeHandlerRegistration(Window.addResizeHandler(new ResizeHandler() {
        public void onResize(ResizeEvent event) {
          setWindowWidth(event.getWidth());
        }
      }));
    }
  }
}
