package com.butent.bee.client.dialog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Settings;
import com.butent.bee.client.animation.AnimationState;
import com.butent.bee.client.animation.RafCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.PreviewHandler;
import com.butent.bee.client.event.Previewer;
import com.butent.bee.client.event.Previewer.PreviewConsumer;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.event.logical.OpenEvent;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.values.Cursor;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.List;

public class Popup extends Simple implements HasAnimation, CloseEvent.HasCloseHandlers,
    OpenEvent.HasOpenHandlers, PreviewHandler {

  public enum OutsideClick {
    CLOSE, IGNORE
  }

  @FunctionalInterface
  public interface PositionCallback {
    void setPosition(int offsetWidth, int offsetHeight);
  }

  private static final class Bounds {

    private final int minWidth;
    private final int minHeight;

    private final int maxWidth;
    private final int maxHeight;

    private Bounds(int minWidth, int minHeight, int maxWidth, int maxHeight) {
      this.minWidth = minWidth;
      this.minHeight = minHeight;
      this.maxWidth = maxWidth;
      this.maxHeight = maxHeight;
    }

    private int clampDx(int width, int dx) {
      if (width + dx < minWidth) {
        return minWidth - width;
      } else if (width + dx > maxWidth) {
        return maxWidth - width;
      } else {
        return dx;
      }
    }

    private int clampDy(int height, int dy) {
      if (height + dy < minHeight) {
        return minHeight - height;
      } else if (height + dy > maxHeight) {
        return maxHeight - height;
      } else {
        return dy;
      }
    }
  }

  private enum ResizeDirection {
    TOP {
      @Override
      Cursor getCursor() {
        return Cursor.NS_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        int top = rectangle.getIntTop();
        int height = rectangle.getIntHeight();

        int z = bounds.clampDy(height, top - y);

        if (z != 0) {
          StyleUtils.setTop(style, top - z);
          StyleUtils.setHeight(style, height + z);
          return true;

        } else {
          return false;
        }
      }
    },

    TOP_RIGHT {
      @Override
      Cursor getCursor() {
        return Cursor.NESW_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        return TOP.resize(style, rectangle, bounds, x, y)
            | RIGHT.resize(style, rectangle, bounds, x, y);
      }
    },

    RIGHT {
      @Override
      Cursor getCursor() {
        return Cursor.EW_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        int width = rectangle.getIntWidth();
        int z = bounds.clampDx(width, x - rectangle.getIntLeft() - width);

        if (z != 0) {
          StyleUtils.setWidth(style, width + z);
          return true;

        } else {
          return false;
        }
      }
    },

    BOTTOM_RIGHT {
      @Override
      Cursor getCursor() {
        return Cursor.NWSE_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        return BOTTOM.resize(style, rectangle, bounds, x, y)
            | RIGHT.resize(style, rectangle, bounds, x, y);
      }
    },

    BOTTOM {
      @Override
      Cursor getCursor() {
        return Cursor.NS_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        int height = rectangle.getIntHeight();
        int z = bounds.clampDy(height, y - rectangle.getIntTop() - height);

        if (z != 0) {
          StyleUtils.setHeight(style, height + z);
          return true;

        } else {
          return false;
        }
      }
    },

    BOTTOM_LEFT {
      @Override
      Cursor getCursor() {
        return Cursor.NESW_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        return BOTTOM.resize(style, rectangle, bounds, x, y)
            | LEFT.resize(style, rectangle, bounds, x, y);
      }
    },

    LEFT {
      @Override
      Cursor getCursor() {
        return Cursor.EW_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        int left = rectangle.getIntLeft();
        int width = rectangle.getIntWidth();

        int z = bounds.clampDx(width, left - x);

        if (z != 0) {
          StyleUtils.setLeft(style, left - z);
          StyleUtils.setWidth(style, width + z);
          return true;

        } else {
          return false;
        }
      }
    },

    TOP_LEFT {
      @Override
      Cursor getCursor() {
        return Cursor.NWSE_RESIZE;
      }

      @Override
      boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y) {
        return TOP.resize(style, rectangle, bounds, x, y)
            | LEFT.resize(style, rectangle, bounds, x, y);
      }
    };

    abstract Cursor getCursor();

    abstract boolean resize(Style style, Rectangle rectangle, Bounds bounds, int x, int y);
  }

  private enum WindowState {
    MOVING, RESIZING
  }

  private final class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {

    private int startX;
    private int startY;

    private int maxX;
    private int maxY;

    private Bounds bounds;

    private WindowState windowState;
    private ResizeDirection resizeDirection;

    private MouseHandler() {
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
      if (windowState == null && DOM.getCaptureElement() == null) {
        if (resizeDirection != null) {
          this.windowState = WindowState.RESIZING;
        } else if (isCaptionEvent(event.getNativeEvent())) {
          this.windowState = WindowState.MOVING;
        }

        if (windowState != null) {
          event.preventDefault();
          DOM.setCapture(getElement());

          int clientHeight = DomUtils.getClientHeight();
          int headerHeight = getHeaderHeight();

          switch (windowState) {
            case MOVING:
              this.startX = event.getX();
              this.startY = event.getY();

              this.maxX = DomUtils.getClientWidth() - getOffsetWidth();

              if (headerHeight > 0) {
                this.maxY = clientHeight - headerHeight;
              } else {
                this.maxY = clientHeight - getOffsetHeight();
              }
              break;

            case RESIZING:
              this.maxX = DomUtils.getClientWidth();

              if (EnumUtils.in(resizeDirection,
                  ResizeDirection.TOP_LEFT, ResizeDirection.TOP, ResizeDirection.TOP_RIGHT)
                  && headerHeight > 0) {
                this.maxY = clientHeight - headerHeight;
              } else {
                this.maxY = clientHeight;
              }

              int w = getElement().getClientWidth();
              int h = getElement().getClientHeight();

              int minWidth = Math.min(MIN_WIDTH, w);
              int minHeight = Math.min(Math.max(MIN_HEIGHT, headerHeight), h);

              int maxWidth = DomUtils.getClientWidth() + w - getElement().getOffsetWidth();
              int maxHeight = clientHeight + h - getElement().getOffsetHeight();

              this.bounds = new Bounds(minWidth, minHeight, maxWidth, maxHeight);
              break;
          }
        }
      }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
      if (windowState == WindowState.MOVING) {
        int x = getAbsoluteLeft() + event.getX() - startX;
        int y = getAbsoluteTop() + event.getY() - startY;

        x = BeeUtils.clamp(x, 0, maxX);
        y = BeeUtils.clamp(y, 0, maxY);

        setPopupPosition(x, y);

      } else if (windowState == WindowState.RESIZING) {
        int x = BeeUtils.clamp(event.getClientX(), 0, maxX);
        int y = BeeUtils.clamp(event.getClientY(), 0, maxY);

        Rectangle rectangle = new Rectangle(getAbsoluteLeft(), getAbsoluteTop(),
            getElement().getClientWidth(), getElement().getClientHeight());
        resizeDirection.resize(getElement().getStyle(), rectangle, bounds, x, y);

      } else if (isResizable()) {
        ResizeDirection rd = getResizeDirection(event.getX(), event.getY(),
            getOffsetWidth(), getOffsetHeight());

        if (rd != resizeDirection) {
          this.resizeDirection = rd;

          Cursor cursor = (rd == null) ? Cursor.AUTO : rd.getCursor();
          StyleUtils.setProperty(getElement(), StyleUtils.STYLE_CURSOR, cursor);
        }
      }
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
      if (windowState != null) {
        boolean wasResizing = windowState == WindowState.RESIZING;

        this.windowState = null;
        DOM.releaseCapture(getElement());

        if (wasResizing) {
          afterResize();
        }
      }
    }

    private ResizeDirection getResizeDirection(int x, int y, int width, int height) {
      if (x < 0 || x > width || y < 0 || y > height) {
        return null;

      } else if (x <= RESIZER_WIDTH) {
        if (y <= RESIZER_WIDTH) {
          return ResizeDirection.TOP_LEFT;
        } else if (y >= height - RESIZER_WIDTH) {
          return ResizeDirection.BOTTOM_LEFT;
        } else {
          return ResizeDirection.LEFT;
        }

      } else if (x >= width - RESIZER_WIDTH) {
        if (y <= RESIZER_WIDTH) {
          return ResizeDirection.TOP_RIGHT;
        } else if (y >= height - RESIZER_WIDTH) {
          return ResizeDirection.BOTTOM_RIGHT;
        } else {
          return ResizeDirection.RIGHT;
        }

      } else if (y <= RESIZER_WIDTH) {
        return ResizeDirection.TOP;

      } else if (y >= height - RESIZER_WIDTH) {
        return ResizeDirection.BOTTOM;
      } else {
        return null;
      }
    }
  }

  public abstract static class Animation extends RafCallback {

    private Popup popup;
    private AnimationState state;

    private Runnable onClose;

    protected Animation(double duration) {
      super(duration);
    }

    @Override
    public void start() {
      if (popup != null) {
        setState(AnimationState.RUNNING);
        if (popup.isShowing()) {
          popup.getElement().getStyle().clearVisibility();
        }
        super.start();
      }
    }

    protected double getFactor(double elapsed) {
      if (popup.isShowing()) {
        return normalize(elapsed);
      } else {
        return BeeConst.DOUBLE_ONE - normalize(elapsed);
      }
    }

    protected Runnable getOnClose() {
      return onClose;
    }

    protected Popup getPopup() {
      return popup;
    }

    protected AnimationState getState() {
      return state;
    }

    protected boolean isCanceled() {
      return getState() == AnimationState.CANCELED;
    }

    protected boolean isRunning() {
      return getState() == AnimationState.RUNNING;
    }

    @Override
    protected void onComplete() {
      setState(AnimationState.FINISHED);

      if (onClose != null) {
        onClose.run();

      } else if (popup.isShowing()) {
        OpenEvent.fire(popup);

      } else {
        BodyPanel.get().remove(popup);
      }
    }

    protected void setOnClose(Runnable onClose) {
      this.onClose = onClose;
    }

    private void setPopup(Popup popup) {
      this.popup = popup;
    }

    private void setState(AnimationState state) {
      this.state = state;
    }
  }

  private static class DefaultAnimation extends Animation {

    protected DefaultAnimation(double duration) {
      super(duration);
    }

    @Override
    public void start() {
      if (getPopup().isShowing()) {
        StyleUtils.setOpacity(getPopup(), BeeConst.DOUBLE_ZERO);
      }
      super.start();
    }

    @Override
    protected void onComplete() {
      getPopup().getElement().getStyle().clearOpacity();
      super.onComplete();
    }

    @Override
    protected boolean run(double elapsed) {
      if (isCanceled()) {
        return false;
      } else {
        StyleUtils.setOpacity(getPopup(), getFactor(elapsed));
        return true;
      }
    }
  }

  private static final String STYLE_POPUP = BeeConst.CSS_CLASS_PREFIX + "Popup";

  private static final double DEFAULT_ANIMATION_DURATION = 250;

  private static final int RESIZER_WIDTH = BeeUtils.positive(Settings.getDialogResizerWidth(), 10);

  private static final int MIN_WIDTH = RESIZER_WIDTH * 3;
  private static final int MIN_HEIGHT = RESIZER_WIDTH * 3;

  public static Popup getActivePopup() {
    if (!hasEventPreview()) {
      String id = Stacking.peek();

      if (!BeeUtils.isEmpty(id)) {
        for (Widget child : BodyPanel.get()) {
          if (child instanceof Popup) {
            Popup popup = (Popup) child;
            if (popup.isShowing() && BeeUtils.same(popup.getId(), id)) {
              return popup;
            }
          }
        }
      }
    }

    int widgetCount = BodyPanel.get().getWidgetCount();

    for (int i = widgetCount - 1; i >= 0; i--) {
      Widget child = BodyPanel.get().getWidget(i);
      if (child instanceof Popup && ((Popup) child).isShowing()) {
        return (Popup) child;
      }
    }

    return null;
  }

  public static List<Popup> getVisiblePopups() {
    List<Popup> popups = new ArrayList<>();

    for (Widget child : BodyPanel.get()) {
      if (child instanceof Popup && ((Popup) child).isShowing()) {
        popups.add((Popup) child);
      }
    }

    return popups;
  }

  public static boolean hasEventPreview() {
    for (Widget child : BodyPanel.get()) {
      if (child instanceof Popup && ((Popup) child).isPreviewEnabled()) {
        return true;
      }
    }

    return false;
  }

  protected static int clampLeft(int left, int width) {
    int windowLeft = 0;
    int windowRight = Window.getClientWidth();

    return Math.max(Math.min(left, windowRight - width), windowLeft);
  }

  protected static int clampTop(int top, int height) {
    int windowTop = 0;
    int windowBottom = Window.getClientHeight();

    return Math.max(Math.min(top, windowBottom - height), windowTop);
  }

  private final OutsideClick onOutsideClick;
  private boolean showing;

  private boolean hideOnEscape;
  private boolean hideOnSave;

  private PreviewConsumer onSave;
  private PreviewConsumer onEscape;

  private boolean animationEnabled;
  private double animationDuration = DEFAULT_ANIMATION_DURATION;
  private Animation animation;

  private MouseHandler mouseHandler;

  private Element keyboardPartner;
  private boolean previewEnabled = true;

  private boolean resizable;

  public Popup(OutsideClick onOutsideClick) {
    this(onOutsideClick, STYLE_POPUP);
  }

  public Popup(OutsideClick onOutsideClick, String styleName) {
    super();

    this.onOutsideClick = onOutsideClick;

    StyleUtils.makeAbsolute(getElement());
    if (styleName != null) {
      setStyleName(styleName);
    }
  }

  @Override
  public HandlerRegistration addCloseHandler(CloseEvent.Handler handler) {
    return addHandler(handler, CloseEvent.getType());
  }

  @Override
  public HandlerRegistration addOpenHandler(OpenEvent.Handler handler) {
    return addHandler(handler, OpenEvent.getType());
  }

  public void cascade() {
    int x = BeeUtils.clamp(Window.getClientWidth() / 30, 5, 30);
    int y = BeeUtils.clamp(Window.getClientHeight() / 30, 5, 30);
    cascade(x, y);
  }

  public void cascade(int x, int y) {
    Popup activePopup = getActivePopup();
    if (activePopup == null) {
      center();
    } else {
      showAt(activePopup.getAbsoluteLeft() + x, activePopup.getAbsoluteTop() + y);
    }
  }

  public void center() {
    setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = (Window.getClientWidth() - offsetWidth) >> 1;
        int top = (Window.getClientHeight() - offsetHeight) >> 1;

        setPopupPosition(Math.max(left, 0), Math.max(top, 0));
      }
    });
  }

  public void close() {
    hide(true);
  }

  public void focusOnOpen(final Widget target) {
    addOpenHandler(new OpenEvent.Handler() {
      @Override
      public void onOpen(OpenEvent event) {
        UiHelper.focus(target);
      }
    });
  }

  public Animation getAnimation() {
    return animation;
  }

  public double getAnimationDuration() {
    return animationDuration;
  }

  public Widget getContent() {
    return getWidget();
  }

  @Override
  public String getIdPrefix() {
    return "popup";
  }

  public Element getKeyboardPartner() {
    return keyboardPartner;
  }

  public PreviewConsumer getOnEscape() {
    return onEscape;
  }

  public PreviewConsumer getOnSave() {
    return onSave;
  }

  public int getPopupLeft() {
    return getElement().getAbsoluteLeft();
  }

  public int getPopupTop() {
    return getElement().getAbsoluteTop();
  }

  public boolean hideOnEscape() {
    return hideOnEscape;
  }

  public boolean hideOnSave() {
    return hideOnSave;
  }

  @Override
  public boolean isAnimationEnabled() {
    return animationEnabled;
  }

  @Override
  public boolean isModal() {
    return OutsideClick.IGNORE.equals(onOutsideClick);
  }

  public boolean isPreviewEnabled() {
    return previewEnabled;
  }

  public boolean isResizable() {
    return resizable;
  }

  public boolean isShowing() {
    return showing;
  }

  public void maybeBringToFront() {
    if (isShowing() && !isPreviewEnabled() && Stacking.size() > 1 && !hasEventPreview()) {
      Stacking.bringToFront(this);
    }
  }

  @Override
  public void onEventPreview(NativePreviewEvent event, Node targetNode) {
    if (!isShowing() || DOM.getCaptureElement() != null) {
      return;
    }

    NativeEvent nativeEvent = event.getNativeEvent();
    boolean eventTargetsPopup = (targetNode != null) && getElement().isOrHasChild(targetNode);

    String type = nativeEvent.getType();

    if (!isPreviewEnabled()) {
      if (EventUtils.isMouseDown(type) && eventTargetsPopup) {
        maybeBringToFront();
      }

      return;
    }

    if (EventUtils.isMouseDown(type)) {
      if (eventTargetsPopup) {
        event.consume();
      } else if (isModal()) {
        event.cancel();
      } else {
        hide(CloseEvent.Cause.MOUSE_OUTSIDE, targetNode, true);
      }

    } else if (EventUtils.isKeyEvent(type)) {

      if (EventUtils.isKeyDown(type)) {
        if (nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE) {
          if (hideOnEscape()) {
            event.cancel();
            hide(CloseEvent.Cause.KEYBOARD_ESCAPE, targetNode, true);
          }
          if (getOnEscape() != null) {
            getOnEscape().accept(event);
          }

        } else if (UiHelper.isSave(nativeEvent)) {
          if (hideOnSave()) {
            event.cancel();
            hide(CloseEvent.Cause.KEYBOARD_SAVE, targetNode, true);
          }
          if (getOnSave() != null) {
            getOnSave().accept(event);
          }

        } else if (nativeEvent.getKeyCode() == KeyCodes.KEY_TAB) {
          if (!eventTargetsPopup
              || Element.is(targetNode) && handleTabulation(Element.as(targetNode))) {
            event.cancel();
            UiHelper.moveFocus(getWidget(), !EventUtils.hasModifierKey(nativeEvent));
          }
        }
      }

      if (!event.isCanceled() && !UiHelper.isCopy(nativeEvent)) {
        if (eventTargetsPopup || getKeyboardPartner() != null && targetNode != null
            && getKeyboardPartner().isOrHasChild(targetNode)) {
          event.consume();

        } else {
          event.cancel();
        }
      }

    } else if (eventTargetsPopup) {
      event.consume();
    } else {
      event.cancel();
    }
  }

  @Override
  public void onResize() {
    int x = Math.min(getAbsoluteLeft(), DomUtils.getClientWidth() - getOffsetWidth());
    int y = Math.min(getAbsoluteTop(), DomUtils.getClientHeight() - getOffsetHeight());

    setPopupPosition(Math.max(x, 0), Math.max(y, 0));

    super.onResize();
  }

  public void setAnimation(Animation animation) {
    this.animation = animation;
  }

  @Override
  public void setAnimationEnabled(boolean enable) {
    animationEnabled = enable;
  }

  public void setAnimationDuration(double animationDuration) {
    this.animationDuration = animationDuration;
  }

  public void setHideOnEscape(boolean hideOnEscape) {
    this.hideOnEscape = hideOnEscape;
  }

  public void setHideOnSave(boolean hideOnSave) {
    this.hideOnSave = hideOnSave;
  }

  public void setKeyboardPartner(Element keyboardPartner) {
    this.keyboardPartner = keyboardPartner;
  }

  public void setOnEscape(PreviewConsumer onEscape) {
    this.onEscape = onEscape;
  }

  public void setOnSave(PreviewConsumer onSave) {
    this.onSave = onSave;
  }

  public void setPopupPosition(int left, int top) {
    Style style = getElement().getStyle();
    StyleUtils.setLeft(style, left);
    StyleUtils.setTop(style, top);
  }

  public void setPopupPositionAndShow(PositionCallback callback) {
    show(callback);
  }

  public void setPreviewEnabled(boolean previewEnabled) {
    this.previewEnabled = previewEnabled;
  }

  public void setResizable(boolean resizable) {
    this.resizable = resizable;

    if (resizable) {
      enableDragging();
    }
  }

  public void showAt(final int x, final int y) {
    setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = clampLeft(x, offsetWidth);
        int top = clampTop(y, offsetHeight);

        setPopupPosition(left, top);
      }
    });
  }

  public void showOnTop(final Element target) {
    if (target == null) {
      center();
    } else {
      setPopupPositionAndShow(new PositionCallback() {
        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
          int x = target.getAbsoluteLeft();
          int y = target.getAbsoluteTop();

          int left = clampLeft(x, offsetWidth);
          int top = clampTop(y, offsetHeight);

          setPopupPosition(left, top);
        }
      });
    }
  }

  public void showRelativeTo(final Element target) {
    showRelativeTo(target, null);
  }

  public void showRelativeTo(final Element target, final Edges margins) {
    if (target == null) {
      center();
    } else {
      setPopupPositionAndShow(new PositionCallback() {
        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
          position(target, margins, offsetWidth, offsetHeight);
        }
      });
    }
  }

  protected void afterResize() {
    super.onResize();
  }

  protected int getHeaderHeight() {
    return 0;
  }

  protected void hide(CloseEvent.Cause cause, Node target, boolean fireEvent) {
    if (isShowing()) {
      setShowing(false);

      Stacking.removeContext(this);

      Runnable onClose = () -> {
        BodyPanel.get().remove(Popup.this);
        if (fireEvent) {
          CloseEvent.fire(Popup.this, cause, target);
        }
      };

      if (cause == CloseEvent.Cause.SCRIPT || !maybeAnimate(onClose)) {
        onClose.run();
      }
    }
  }

  /**
   * @param event
   */
  protected boolean isCaptionEvent(NativeEvent event) {
    return false;
  }

  @Override
  protected void onLoad() {
    Previewer.ensureRegistered(this);
    super.onLoad();
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    Previewer.ensureUnregistered(this);

    if (isShowing()) {
      setShowing(false);
      Stacking.removeContext(this);
    }
  }

  private void enableDragging() {
    if (getMouseHandler() == null) {
      setMouseHandler(new MouseHandler());

      addDomHandler(getMouseHandler(), MouseDownEvent.getType());
      addDomHandler(getMouseHandler(), MouseUpEvent.getType());
      addDomHandler(getMouseHandler(), MouseMoveEvent.getType());
    }
  }

  private MouseHandler getMouseHandler() {
    return mouseHandler;
  }

  private boolean handleTabulation(Element target) {
    if (target == null) {
      return true;
    }

    Widget content = getContent();
    if (content == null) {
      return true;
    }
    if (content instanceof TabulationHandler && ((TabulationHandler) content).handlesTabulation()) {
      return false;
    }

    Widget w = DomUtils.getChildByElement(content, target);
    for (Widget p = w; p != null && p != content; p = p.getParent()) {
      if (p instanceof TabulationHandler && ((TabulationHandler) p).handlesTabulation()) {
        return false;
      }
    }

    return true;
  }

  private void hide(boolean fireEvent) {
    hide(CloseEvent.Cause.SCRIPT, null, fireEvent);
  }

  private boolean maybeAnimate(Runnable onClose) {
    if (isAnimationEnabled()) {
      if (getAnimation() == null) {
        setAnimation(new DefaultAnimation(getAnimationDuration()));
      } else if (getAnimation().isRunning()) {
        getAnimation().setState(AnimationState.CANCELED);
        return false;
      }

      getAnimation().setPopup(this);
      getAnimation().setOnClose(onClose);

      getAnimation().start();
      return true;

    } else {
      return false;
    }
  }

  private void position(Element relativeElement, Edges margins, int offsetWidth, int offsetHeight) {
    int left = relativeElement.getAbsoluteLeft();
    int top = relativeElement.getAbsoluteTop();

    int objectWidth = relativeElement.getOffsetWidth();
    int objectHeight = relativeElement.getOffsetHeight();

    if (margins != null) {
      left -= margins.getIntLeft();
      top -= margins.getIntTop();

      objectWidth += margins.getIntRight();
      objectHeight += margins.getIntBottom();
    }

    int offsetWidthDiff = offsetWidth - objectWidth;
    if (offsetWidthDiff > 0) {
      int windowLeft = 0;
      int windowRight = Window.getClientWidth();

      int distanceFromWindowLeft = left - windowLeft;
      int distanceToWindowRight = windowRight - left;

      if (distanceToWindowRight < offsetWidth && distanceFromWindowLeft >= offsetWidthDiff) {
        left -= offsetWidthDiff;
      }
    } else if (offsetWidthDiff < 0) {
      left -= offsetWidthDiff;
    }

    int windowTop = 0;
    int windowBottom = Window.getClientHeight();

    int distanceFromWindowTop = top - windowTop;
    int distanceToWindowBottom = windowBottom - (top + objectHeight);

    if (distanceToWindowBottom < offsetHeight && distanceFromWindowTop >= offsetHeight) {
      top -= offsetHeight;
    } else {
      top += objectHeight;
    }

    left = clampLeft(left, offsetWidth);
    top = clampTop(top, offsetHeight);

    setPopupPosition(left, top);
  }

  private void setMouseHandler(MouseHandler mouseHandler) {
    this.mouseHandler = mouseHandler;
  }

  private void setShowing(boolean showing) {
    this.showing = showing;
  }

  private void show(final PositionCallback callback) {
    if (!isShowing()) {
      if (isAttached()) {
        this.removeFromParent();
      }

      setShowing(true);

      Stacking.addContext(this);

      getElement().getStyle().setVisibility(Visibility.HIDDEN);
      BodyPanel.get().add(this);

      Scheduler.get().scheduleDeferred(() -> {
        if (callback != null) {
          callback.setPosition(getOffsetWidth(), getOffsetHeight());
        }

        Runnable onClose = () -> OpenEvent.fire(Popup.this);

        if (!maybeAnimate(onClose)) {
          getElement().getStyle().clearVisibility();
          onClose.run();
        }
      });
    }
  }
}
