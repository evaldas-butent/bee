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
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.IntRangeSet;

import java.util.List;

public final class ChatPopup extends Popup {

  private enum MockStyle {
    PHONE_0("phone-0", false), PHONE_1("phone-1", true), PHONE_2("phone-2", true);

    private final String styleName;
    private final boolean needsModifier;

    MockStyle(String styleSuffix, boolean needsModifier) {
      this.styleName = STYLE_PREFIX + BeeConst.STRING_MINUS + styleSuffix;
      this.needsModifier = needsModifier;
    }
  }

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "Chat";
  private static final String STYLE_MODAL = STYLE_PREFIX + "Modal";
  private static final String STYLE_NORMAL = STYLE_PREFIX + "Normal";
  private static final String STYLE_MINIMIZED = STYLE_PREFIX + "Minimized";

  private static final int MARGIN_X = 20;

  private static int getLeft(String ignoreId, int width) {
    return getLeft(ignoreId, width, true);
  }

  private static int getLeft(String ignoreId, int width, boolean openFromLeftCorner) {
    IntRangeSet ranges = new IntRangeSet();

    for (Popup popup : getVisiblePopups()) {
      if (!DomUtils.idEquals(popup, ignoreId)) {
        int left = popup.getAbsoluteLeft();
        int right = popup.getElement().getAbsoluteRight();

        ranges.addClosedOpen(left, right);
      }
    }

    int ww = Window.getClientWidth();
    if (ranges.isEmpty()) {
      if (openFromLeftCorner) {
        return ww - width;
      } else {
        return 0;
      }
    }

    List<Range<Integer>> free = ranges.complement(0, ww - 1).asList();

    if (!free.isEmpty()) {
      for (int i = free.size() - 1; i >= 0; i--) {
        Range<Integer> range = free.get(i);

        int lower = range.lowerEndpoint();
        int upper = range.upperEndpoint() + 1;

        if (upper - lower >= width) {
          if (openFromLeftCorner) {
            int margin = (upper >= ww) ? 0 : MARGIN_X;
            return Math.max(upper - width - margin, lower);
          } else {
            return Math.min(upper - width, lower + MARGIN_X);
          }
        }
      }
    }

    return BeeConst.UNDEF;
  }

  public static void openMinimized(IdentifiableWidget chatView) {
    open(chatView, true, true);
  }

  public static void openNormal(IdentifiableWidget chatView) {
    open(chatView, false, true);
  }

  public static void openNormal(IdentifiableWidget chatView, boolean openFromLeftCorner) {
    open(chatView, false, openFromLeftCorner);
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

  private static MockStyle getStoredStyle(ChatView chatView) {
    String key = getStyleKey(chatView);
    if (BeeUtils.isEmpty(key)) {
      return null;
    }

    return EnumUtils.getEnumByName(MockStyle.class, BeeKeeper.getStorage().get(key));
  }

  private static String getStyleKey(ChatView chatView) {
    if (chatView == null) {
      return null;
    } else {
      return ChatUtils.getStyleStorageKey(chatView.getChatId());
    }
  }

  private static void open(IdentifiableWidget widget, boolean minimized,
      boolean openFromLeftCorner) {
    if (!(widget instanceof ChatView)) {
      return;
    }

    ChatView chatView = (ChatView) widget;

    ChatPopup chatPopup = new ChatPopup(minimized, getStoredStyle(chatView));
    chatPopup.setWidget(chatView);

    if (!minimized) {
      Size size = getStoredSize(chatView);
      if (size != null) {
        StyleUtils.setSize(chatPopup, size);
      }

      chatPopup.focusOnOpen(chatView.getInputArea());
    }

    chatPopup.setPopupPositionAndShow((width, height) -> {
      int left = getLeft(chatPopup.getId(), width, openFromLeftCorner);
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
  private MockStyle mockStyle;

  private ElementSize clientSize;
  private ElementSize offsetSize;

  private ChatPopup(boolean minimized, MockStyle mockStyle) {
    super(OutsideClick.IGNORE, STYLE_MODAL);

    this.minimized = minimized;
    this.mockStyle = mockStyle;

    addStyleName(minimized ? STYLE_MINIMIZED : STYLE_NORMAL);
    if (mockStyle != null) {
      addStyleName(mockStyle.styleName);
    }

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

      if (header.getElement().isOrHasChild(el)) {
        return !header.isActionOrCommand(el);
      } else {
        return getMockStyle() != null && DomUtils.sameId(getChatView().getElement(), el);
      }

    } else {
      return false;
    }
  }

  void handleStyle(boolean hasModifiers) {
    MockStyle newStyle = null;
    boolean found = false;

    MockStyle[] values = MockStyle.values();

    if (getMockStyle() == null) {
      for (MockStyle ms : values) {
        if (hasModifiers || !ms.needsModifier) {
          newStyle = ms;
          found = true;
          break;
        }
      }

      if (!found) {
        newStyle = values[0];
        found = true;
      }

    } else {
      int index = getMockStyle().ordinal();

      if (index < values.length - 1) {
        if (hasModifiers) {
          newStyle = values[index + 1];
          found = true;

        } else {
          for (int i = index + 1; i < values.length; i++) {
            if (!values[i].needsModifier) {
              newStyle = values[i];
              found = true;
              break;
            }
          }
        }
      }

      if (!found && hasModifiers && index > 0) {
        newStyle = values[0];
        found = true;
      }

      if (!found) {
        newStyle = null;
        found = true;
      }
    }

    setMockStyle(newStyle);
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

  private MockStyle getMockStyle() {
    return mockStyle;
  }

  private ElementSize getOffsetSize() {
    return offsetSize;
  }

  private void setClientSize(ElementSize clientSize) {
    this.clientSize = clientSize;
  }

  private void setMockStyle(MockStyle mockStyle) {
    if (this.mockStyle != mockStyle) {
      if (this.mockStyle != null) {
        removeStyleName(this.mockStyle.styleName);
      }

      this.mockStyle = mockStyle;

      if (mockStyle != null) {
        addStyleName(mockStyle.styleName);
      }

      String key = getStyleKey(getChatView());

      if (!BeeUtils.isEmpty(key)) {
        if (mockStyle == null) {
          BeeKeeper.getStorage().remove(key);
        } else {
          BeeKeeper.getStorage().set(key, mockStyle.name());
        }
      }
    }
  }

  private void setOffsetSize(ElementSize offsetSize) {
    this.offsetSize = offsetSize;
  }
}
