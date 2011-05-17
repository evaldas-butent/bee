package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.client.HasSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

/**
 * Enables using a pop up window with several possible options for a user.
 */

public class DialogBox extends PopupPanel implements HasId, HasHTML, HasSafeHtml {

  /**
   * Extends {@code HasHTML, HasSafeHtml, Is Widget}, sets requirements for captions.
   */

  public interface Caption extends HasHTML, HasSafeHtml, IsWidget {
  }

  /**
   * Implements caption element in user dialog interface components.
   */

  public static class CaptionImpl extends Html implements Caption {
    public CaptionImpl() {
      super();
    }

    public CaptionImpl(String html) {
      super(html);
    }
  }

  private class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {
    public void onMouseDown(MouseDownEvent event) {
      beginDragging(event);
    }

    public void onMouseMove(MouseMoveEvent event) {
      continueDragging(event);
    }

    public void onMouseUp(MouseUpEvent event) {
      endDragging();
    }
  }

  private static final String STYLE_CONTAINER = "bee-DialogBox";
  private static final String STYLE_CAPTION = "Caption";
  private static final String STYLE_CONTENT = "Content";
  private static final String STYLE_CLOSE = "Close";

  private final Complex container = new Complex();
  private final Vertical layout = new Vertical();
  private final Caption caption;

  private boolean dragging;
  private int dragStartX, dragStartY;
  private int windowWidth;
  private int clientLeft;
  private int clientTop;

  private HandlerRegistration resizeHandlerRegistration;

  public DialogBox() {
    this(false);
  }

  public DialogBox(boolean autoHide) {
    this(autoHide, true);
  }

  public DialogBox(boolean autoHide, boolean modal) {
    this(autoHide, modal, new CaptionImpl());
  }

  public DialogBox(boolean autoHide, boolean modal, Caption captionWidget) {
    super(autoHide, modal);

    Assert.notNull(captionWidget);
    captionWidget.asWidget().addStyleName(STYLE_CAPTION);
    this.caption = captionWidget;

    this.layout.add(captionWidget);
    this.container.add(layout);

    BeeImage close = new BeeImage(Global.getImages().close(), new BeeCommand() {
      @Override
      public void execute() {
        hide();
      }
    });
    close.addStyleName(STYLE_CLOSE);
    this.container.add(close);

    createId();
    setStyleName(STYLE_CONTAINER);

    windowWidth = Window.getClientWidth();
    clientLeft = Document.get().getBodyOffsetLeft();
    clientTop = Document.get().getBodyOffsetTop();

    MouseHandler mouseHandler = new MouseHandler();
    addDomHandler(mouseHandler, MouseDownEvent.getType());
    addDomHandler(mouseHandler, MouseUpEvent.getType());
    addDomHandler(mouseHandler, MouseMoveEvent.getType());
  }

  public DialogBox(Caption captionWidget) {
    this(false, true, captionWidget);
  }

  public DialogBox(String html) {
    this(new CaptionImpl(html));
  }

  public void createId() {
    DomUtils.createId(this, "dialog");
  }

  public Caption getCaption() {
    return caption;
  }

  public String getHTML() {
    return caption.getHTML();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getText() {
    return caption.getText();
  }

  @Override
  public void hide() {
    if (resizeHandlerRegistration != null) {
      resizeHandlerRegistration.removeHandler();
      resizeHandlerRegistration = null;
    }
    super.hide();
  }

  @Override
  public void onBrowserEvent(Event event) {
    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
      case Event.ONMOUSEUP:
      case Event.ONMOUSEMOVE:
        if (!dragging && !isCaptionEvent(event)) {
          return;
        }
    }
    super.onBrowserEvent(event);
  }

  public void setHTML(SafeHtml html) {
    caption.setHTML(html);
  }

  public void setHTML(String html) {
    caption.setHTML(SafeHtmlUtils.fromTrustedString(html));
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setText(String text) {
    caption.setText(text);
  }

  @Override
  public void setWidget(Widget w) {
    Assert.notNull(w);
    w.addStyleName(STYLE_CONTENT);
    layout.add(w);

    super.setWidget(container);
  }

  @Override
  public void show() {
    if (resizeHandlerRegistration == null) {
      resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {
        public void onResize(ResizeEvent event) {
          windowWidth = event.getWidth();
        }
      });
    }
    super.show();
  }

  protected void beginDragging(MouseDownEvent event) {
    if (DOM.getCaptureElement() == null) {
      dragging = true;
      DOM.setCapture(getElement());
      dragStartX = event.getX();
      dragStartY = event.getY();
    }
  }

  protected void continueDragging(MouseMoveEvent event) {
    if (dragging) {
      int absX = event.getX() + getAbsoluteLeft();
      int absY = event.getY() + getAbsoluteTop();
      if (absX < clientLeft || absX >= windowWidth || absY < clientTop) {
        return;
      }
      setPopupPosition(absX - dragStartX, absY - dragStartY);
    }
  }

  protected void endDragging() {
    dragging = false;
    DOM.releaseCapture(getElement());
  }

  @Override
  protected void onPreviewNativeEvent(NativePreviewEvent event) {
    NativeEvent nativeEvent = event.getNativeEvent();

    if (!event.isCanceled() && (event.getTypeInt() == Event.ONMOUSEDOWN)
        && isCaptionEvent(nativeEvent)) {
      nativeEvent.preventDefault();
    }
    super.onPreviewNativeEvent(event);
  }

  private boolean isCaptionEvent(NativeEvent event) {
    EventTarget target = event.getEventTarget();
    if (Element.is(target)) {
      return caption.asWidget().getElement().getParentElement().isOrHasChild(Element.as(target));
    }
    return false;
  }
}
