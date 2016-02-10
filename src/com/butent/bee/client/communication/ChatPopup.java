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

    int x = Window.getClientWidth();

    for (Popup popup : getVisiblePopups()) {
      if (popup.getWidget() instanceof ChatView) {
        x = Math.min(x, popup.getAbsoluteLeft());
      }
    }

    final int right = x - 20;

    chatPopup.setPopupPositionAndShow((width, height) -> {
      int left = Math.max(right - width, 0);
      int top = Math.max(Window.getClientHeight() - height - 2, 0);

      chatPopup.setPopupPosition(left, top);
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

  @Override
  protected int getHeaderHeight() {
    return ((ChatView) getWidget()).getHeader().getHeight();
  }

  @Override
  protected boolean isCaptionEvent(NativeEvent event) {
    EventTarget target = event.getEventTarget();

    if (Element.is(target) && getWidget() instanceof ChatView) {
      Element el = Element.as(target);
      HeaderView header = ((ChatView) getWidget()).getHeader();

      return header.getElement().isOrHasChild(el) && !header.isActionOrCommand(el);

    } else {
      return false;
    }
  }

  boolean isMinimized() {
    return minimized;
  }

  void setMinimized(boolean minimized) {
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

  private int getNormalHeight() {
    return normalHeight;
  }

  private void setNormalHeight(int normalHeight) {
    this.normalHeight = normalHeight;
  }
}
