package com.butent.bee.client.communication;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

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

  private static void open(IdentifiableWidget chatView, boolean minimized) {
    ChatPopup chatPopup = new ChatPopup(minimized);
    chatPopup.setWidget(chatView);

    if (!minimized && chatView instanceof ChatView) {
      chatPopup.focusOnOpen(((ChatView) chatView).getInputArea());
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
  private int normalHeight;

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
      int wh = Window.getClientHeight();
      int hh = getHeaderHeight();
      int top = getAbsoluteTop();

      if (isMinimized()) {
        int nh = BeeUtils.positive(getNormalHeight(), wh / 2);
        top += hh - nh;
        top = Math.min(top, wh - nh);

      } else {
        setNormalHeight(getOffsetHeight());

        top += getOffsetHeight() - hh;
        top = Math.min(top, wh - hh);
      }

      StyleUtils.setTop(this, BeeUtils.max(top, 0));

      this.minimized = minimized;

      setStyleName(STYLE_NORMAL, !minimized);
      setStyleName(STYLE_MINIMIZED, minimized);
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

  private int getNormalHeight() {
    return normalHeight;
  }

  private void setNormalHeight(int normalHeight) {
    this.normalHeight = normalHeight;
  }
}
