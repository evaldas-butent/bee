package com.butent.bee.client.communication;

import com.google.common.collect.Range;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.ElementSize;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.IntRangeSet;

import java.util.List;

public final class ChatPopup extends Popup {

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "Chat";
  private static final String STYLE_MODAL = STYLE_PREFIX + "Modal";
  private static final String STYLE_NORMAL = STYLE_PREFIX + "Normal";
  private static final String STYLE_MINIMIZED = STYLE_PREFIX + "Minimized";

  private static final int MARGIN_X = 20;

  private static int getLeft(String ignoreId, int width) {
    IntRangeSet ranges = new IntRangeSet();

    for (Popup popup : getVisiblePopups()) {
      if (popup.getWidget() instanceof ChatView && !DomUtils.idEquals(popup, ignoreId)) {
        int left = popup.getAbsoluteLeft();
        int right = popup.getElement().getAbsoluteRight();

        ranges.addClosedOpen(left, right);
      }
    }

    int ww = Window.getClientWidth();
    if (ranges.isEmpty()) {
      return ww - width;
    }

    List<Range<Integer>> free = ranges.complement(0, ww - 1).asList();

    if (!free.isEmpty()) {
      for (int i = free.size() - 1; i >= 0; i--) {
        Range<Integer> range = free.get(i);

        int lower = range.lowerEndpoint();
        int upper = range.upperEndpoint() + 1;

        if (upper - lower >= width) {
          int margin = (upper >= ww) ? 0 : MARGIN_X;
          return Math.max(upper - width - margin, lower);
        }
      }
    }

    return BeeConst.UNDEF;
  }

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

    chatPopup.setPopupPositionAndShow((width, height) -> {
      int left = getLeft(chatPopup.getId(), width);
      int top;

      if (BeeConst.isUndef(left)) {
        left = BeeUtils.randomInt(0, Window.getClientWidth() - width);
        top = BeeUtils.randomInt(0, Window.getClientHeight() - height);

      } else {
        top = Window.getClientHeight() - height;
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

      int hh = getHeaderHeight();

      int right = Math.min(getElement().getAbsoluteRight(), ww);
      int bottom = Math.min(getElement().getAbsoluteBottom(), wh);

      if (isMinimized()) {
        int nw;
        int nh;

        if (getOffsetSize() == null) {
          Size size = getStoredSize(getChatView());

          if (size == null) {
            nw = BeeConst.UNDEF;
            nh = BeeConst.UNDEF;
          } else {
            nw = size.getWidth();
            nh = size.getHeight();
          }

        } else {
          nw = getOffsetSize().getWidth();
          nh = getOffsetSize().getHeight();
        }

        int left = BeeConst.UNDEF;

        if (nw > 0) {
          left = getLeft(getId(), nw);

          if (!BeeConst.isUndef(left)) {
            StyleUtils.setLeft(this, left);
            StyleUtils.setTop(this, BeeUtils.nonNegative(wh - nh));
          }
        }

        if (getClientSize() != null) {
          getClientSize().applyTo(getElement());
        }

        if (BeeConst.isUndef(left)) {
          Scheduler.get().scheduleDeferred(() -> {
            int width = getOffsetWidth();
            int height = getOffsetHeight();

            StyleUtils.setLeft(getElement(), clampLeft(right - width, width));
            StyleUtils.setTop(getElement(), clampTop(bottom - height, height));
          });
        }

      } else {
        setOffsetSize(ElementSize.forOffset(this));
        setClientSize(ElementSize.forClient(this));

        StyleUtils.clearWidth(this);
        StyleUtils.clearHeight(this);

        Scheduler.get().scheduleDeferred(() -> {
          int width = getOffsetWidth();
          int left = getLeft(getId(), width);

          if (BeeConst.isUndef(left)) {
            left = Math.max(right - width, getAbsoluteLeft());
            StyleUtils.setLeft(getElement(), clampLeft(left, width));
            StyleUtils.setTop(this, BeeUtils.nonNegative(bottom - hh));

          } else {
            StyleUtils.setLeft(this, left);
            StyleUtils.setTop(this, wh - hh);
          }
        });
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
