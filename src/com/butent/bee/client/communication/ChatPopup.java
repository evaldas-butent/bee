package com.butent.bee.client.communication;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.ElementSize;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public final class ChatPopup extends Popup {

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "Chat";
  private static final String STYLE_MODAL = STYLE_PREFIX + "Modal";
  private static final String STYLE_NORMAL = STYLE_PREFIX + "Normal";
  private static final String STYLE_MINIMIZED = STYLE_PREFIX + "Minimized";

  public static void openMinimized(IdentifiableWidget chatView) {
    open(chatView, true);
  }

  public static void openNormal(IdentifiableWidget chatView) {
    open(chatView, false);
  }

  private static String getSizeKey(ChatView chatView) {
    if (chatView == null) {
      return null;
    } else {
      return ChatUtils.getSizeStorageKey(chatView.getChatId());
    }
  }

  private static Size getStoredSize(ChatView chatView) {
    String key = getSizeKey(chatView);
    if (BeeUtils.isEmpty(key)) {
      return null;
    }

    List<Integer> values = BeeUtils.toInts(BeeKeeper.getStorage().get(key));
    if (values.size() == 2
        && BeeUtils.isPositive(values.get(0)) && BeeUtils.isPositive(values.get(1))) {

      return new Size(values.get(0), values.get(1));

    } else {
      return null;
    }
  }

  private static void open(IdentifiableWidget widget, boolean minimized) {
    if (!(widget instanceof ChatView)) {
      return;
    }

    ChatView chatView = (ChatView) widget;

    ChatPopup chatPopup = new ChatPopup(minimized);
    chatPopup.setWidget(chatView);

    if (!minimized) {
      Size size = getStoredSize(chatView);
      if (size != null) {
        StyleUtils.setSize(chatPopup, size);
      }

      chatPopup.focusOnOpen(chatView.getInputArea());
    }

    int x = Window.getClientWidth();
    int y = Window.getClientHeight();

    for (Popup popup : getVisiblePopups()) {
      if (popup.getWidget() instanceof ChatView) {
        x = Math.min(x, popup.getAbsoluteLeft() - 20);
        y = Math.min(y, popup.getAbsoluteTop() - 20);
      }
    }

    final int right = x;
    final int bottom = y;

    chatPopup.setPopupPositionAndShow((width, height) -> {
      int left;
      int top;

      if (right >= width) {
        left = right - width;
        top = Window.getClientHeight() - height;

      } else if (bottom >= height) {
        top = bottom - height;
        left = Window.getClientWidth() - width;

      } else {
        if (Window.getClientWidth() > width) {
          left = BeeUtils.randomInt(0, Window.getClientWidth() - width);
        } else {
          left = 0;
        }

        if (Window.getClientHeight() > height) {
          top = BeeUtils.randomInt(0, Window.getClientHeight() - height);
        } else {
          top = 0;
        }
      }

      chatPopup.setPopupPosition(BeeUtils.nonNegative(left), BeeUtils.nonNegative(top));
    });
  }

  private boolean minimized;

  private ElementSize clientSize;
  private ElementSize offsetSize;

  private ChatPopup(boolean minimized) {
    super(OutsideClick.IGNORE, STYLE_MODAL);

    this.minimized = minimized;
    addStyleName(minimized ? STYLE_MINIMIZED : STYLE_NORMAL);

    if (!hasEventPreview()) {
      setPreviewEnabled(false);
    }
    setResizable(true);
  }

  @Override
  public boolean isResizable() {
    return !isMinimized() && super.isResizable();
  }

  public void setMinimized(boolean minimized) {
    if (minimized != isMinimized()) {
      int ww = Window.getClientWidth();
      int wh = Window.getClientHeight();

      int left = getAbsoluteLeft();

      int hh = getHeaderHeight();
      int top = getAbsoluteTop();

      if (isMinimized()) {
        int nw;
        int nh;

        Size size = null;

        if (getOffsetSize() == null) {
          size = getStoredSize(getChatView());

          if (size == null) {
            nw = getOffsetWidth();
            nh = wh / 2;
          } else {
            nw = size.getWidth();
            nh = size.getHeight();
          }

        } else {
          nw = getOffsetSize().getWidth();
          nh = getOffsetSize().getHeight();
        }

        left = Math.min(left, ww - nw);
        StyleUtils.setLeft(this, BeeUtils.nonNegative(left));

        top += hh - nh;
        top = Math.min(top, wh - nh);
        StyleUtils.setTop(this, BeeUtils.nonNegative(top));

        if (getClientSize() != null) {
          getClientSize().applyTo(getElement());
        }

        if (getOffsetSize() == null && size == null) {
          Scheduler.get().scheduleDeferred(() -> {
            StyleUtils.setLeft(getElement(), clampLeft(getAbsoluteLeft(), getOffsetWidth()));
            StyleUtils.setTop(getElement(), clampLeft(getAbsoluteTop(), getOffsetHeight()));
          });
        }

      } else {
        setOffsetSize(ElementSize.forOffset(this));
        setClientSize(ElementSize.forClient(this));

        StyleUtils.setMaxWidth(this, getClientSize().getWidth());

        StyleUtils.clearWidth(this);
        StyleUtils.clearHeight(this);

        top += getOffsetHeight() - hh;
        top = Math.min(top, wh - hh);
        StyleUtils.setTop(this, BeeUtils.nonNegative(top));
      }

      this.minimized = minimized;

      setStyleName(STYLE_NORMAL, !minimized);
      setStyleName(STYLE_MINIMIZED, minimized);
    }
  }

  @Override
  protected void afterResize() {
    super.afterResize();

    String key = getSizeKey(getChatView());
    if (!BeeUtils.isEmpty(key)) {
      int width = getElement().getClientWidth();
      int height = getElement().getClientHeight();

      BeeKeeper.getStorage().set(key, BeeUtils.join(BeeConst.STRING_COMMA, width, height));
    }
  }

  @Override
  protected int getHeaderHeight() {
    return ((ChatView) getWidget()).getHeader().getHeight();
  }

  @Override
  protected boolean isCaptionEvent(NativeEvent event) {
    EventTarget target = event.getEventTarget();

    if (Element.is(target) && getChatView() != null) {
      Element el = Element.as(target);
      HeaderView header = getChatView().getHeader();

      return header.getElement().isOrHasChild(el) && !header.isActionOrCommand(el);

    } else {
      return false;
    }
  }

  boolean isMinimized() {
    return minimized;
  }

  private ChatView getChatView() {
    if (getWidget() instanceof ChatView) {
      return (ChatView) getWidget();
    } else {
      return null;
    }
  }

  private ElementSize getClientSize() {
    return clientSize;
  }

  private ElementSize getOffsetSize() {
    return offsetSize;
  }

  private void setClientSize(ElementSize clientSize) {
    this.clientSize = clientSize;
  }

  private void setOffsetSize(ElementSize offsetSize) {
    this.offsetSize = offsetSize;
  }
}
