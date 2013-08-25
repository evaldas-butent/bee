package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.RadioButton;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.Cart;
import com.butent.bee.shared.modules.ec.CartItem;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.CartType;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.EnumMap;

public class CartList extends HtmlTable implements ValueChangeHandler<Boolean> {

  private static final String STYLE_PRIMARY = EcStyles.name("cartList");

  private static final String STYLE_ACTIVATE = STYLE_PRIMARY + "-activate";
  private static final String STYLE_LABEL = STYLE_PRIMARY + "-label";
  private static final String STYLE_INFO = STYLE_PRIMARY + "-info";

  private static final String STYLE_ACTIVE = STYLE_PRIMARY + "-active";
  private static final String STYLE_INACTIVE = STYLE_PRIMARY + "-inactive";

  private static final String STYLE_HAS_ITEMS = STYLE_PRIMARY + "-hasItems";
  private static final String STYLE_UPDATED = STYLE_PRIMARY + "-updated";

  private static final int COL_ACTIVATE = 0;
  private static final int COL_LABEL = 1;
  private static final int COL_INFO = 2;

  private final EnumMap<CartType, Cart> carts = Maps.newEnumMap(CartType.class);
  private final EnumHashBiMap<CartType, Integer> typesToRows = EnumHashBiMap.create(CartType.class);

  private CartType activeCartType = CartType.MAIN;

  public CartList() {
    super(STYLE_PRIMARY);

    String radioName = NameUtils.createUniqueName("ca-");
    int row = 0;

    for (CartType cartType : CartType.values()) {
      Cart cart = new Cart();
      carts.put(cartType, new Cart());
      typesToRows.put(cartType, row);

      RadioButton activity = new RadioButton(radioName);
      activity.addValueChangeHandler(this);
      setWidgetAndStyle(row, COL_ACTIVATE, activity, STYLE_ACTIVATE);

      Label label = new Label(cartType.getLabel());
      label.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          onLabelClick(event);
        }
      });
      setWidgetAndStyle(row, COL_LABEL, label, STYLE_LABEL);

      Label info = new Label(renderInfo(cart));
      setWidgetAndStyle(row, COL_INFO, info, STYLE_INFO);

      if (cartType == activeCartType) {
        activity.setValue(true);
        getRowFormatter().addStyleName(row, STYLE_ACTIVE);
      } else {
        getRowFormatter().addStyleName(row, STYLE_INACTIVE);
      }

      row++;
    }
  }

  public void addToCart(EcItem ecItem, int quantity) {
    CartType cartType = getActiveCartType();
    CartItem cartItem = carts.get(cartType).add(ecItem, quantity);

    if (cartItem != null) {
      BeeKeeper.getScreen().notifyInfo(Localized.getMessages()
          .ecUpdateCartItem(cartType.getCaption(), ecItem.getName(),
              BeeUtils.toString(cartItem.getQuantity())));

      refresh(cartType);

      EcKeeper.persistCartItem(cartType, cartItem);
    }
  }

  public Cart getCart(CartType cartType) {
    return carts.get(cartType);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    if (BeeUtils.isTrue(event.getValue())) {
      Integer newRow = getEventRow(event);

      if (newRow != null) {
        CartType newType = typesToRows.inverse().get(newRow);

        if (newType != null && newType != getActiveCartType()) {
          int oldRow = typesToRows.get(getActiveCartType());
          getRowFormatter().removeStyleName(oldRow, STYLE_ACTIVE);
          getRowFormatter().addStyleName(oldRow, STYLE_INACTIVE);

          getRowFormatter().removeStyleName(newRow, STYLE_INACTIVE);
          getRowFormatter().addStyleName(newRow, STYLE_ACTIVE);

          setActiveCartType(newType);
        }
      }
    }
  }

  public void refresh(CartType cartType) {
    boolean updated = updateInfo(cartType);
    updateTitle(cartType);

    final Element rowElement = getRowFormatter().getElement(typesToRows.get(cartType));
    if (carts.get(cartType).isEmpty()) {
      rowElement.removeClassName(STYLE_HAS_ITEMS);
    } else {
      rowElement.addClassName(STYLE_HAS_ITEMS);
    }

    if (updated) {
      rowElement.removeClassName(STYLE_UPDATED);
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          if (!StyleUtils.hasClassName(rowElement, STYLE_UPDATED)) {
            rowElement.addClassName(STYLE_UPDATED);
          }
        }
      });
    }
  }

  public boolean removeFromCart(CartType cartType, EcItem ecItem) {
    if (cartType != null && carts.get(cartType).remove(ecItem)) {
      BeeKeeper.getScreen().notifyInfo(Localized.getMessages()
          .ecRemoveCartItem(cartType.getCaption(), ecItem.getName()));

      refresh(cartType);
      return true;
    } else {
      return false;
    }
  }

  private CartType getActiveCartType() {
    return activeCartType;
  }

  private static Integer getEventRow(GwtEvent<?> event) {
    if (event.getSource() instanceof Widget) {
      TableRowElement rowElement =
          DomUtils.getParentRow(((Widget) event.getSource()).getElement(), false);

      if (rowElement != null) {
        return rowElement.getRowIndex();
      }
    }
    return null;
  }

  private void onLabelClick(ClickEvent event) {
    CartType type = typesToRows.inverse().get(getEventRow(event));
    if (type != null && !getCart(type).isEmpty()) {
      EcKeeper.openCart(type);
    }
  }

  private static String renderInfo(Cart cart) {
    return BeeUtils.parenthesize(cart.totalQuantity());
  }

  private static String renderTitle(Cart cart) {
    if (cart == null || cart.isEmpty()) {
      return BeeConst.STRING_EMPTY;

    } else {
      StringBuilder sb = new StringBuilder();

      for (CartItem item : cart.getItems()) {
        sb.append(item.getEcItem().getName()).append(BeeUtils.space(3));

        int quantity = item.getQuantity();
        int price = item.getEcItem().getPrice();

        sb.append(quantity).append(" x ").append(EcUtils.renderCents(price));
        sb.append(BeeConst.CHAR_EOL);
      }

      sb.append(Localized.getConstants().ecShoppingCartTotal()).append(BeeConst.CHAR_SPACE);
      sb.append(EcUtils.renderCents(cart.totalCents())).append(BeeConst.CHAR_SPACE);
      sb.append(EcConstants.CURRENCY);

      return sb.toString();
    }
  }

  private void setActiveCartType(CartType activeCartType) {
    this.activeCartType = activeCartType;
  }

  private boolean updateInfo(CartType cartType) {
    String info = renderInfo(carts.get(cartType));

    Widget widget = getWidget(typesToRows.get(cartType), COL_INFO);
    if (widget != null && !BeeUtils.equalsTrim(info, widget.getElement().getInnerHTML())) {
      widget.getElement().setInnerHTML(info);
      return true;
    } else {
      return false;
    }
  }

  private void updateTitle(CartType cartType) {
    String title = renderTitle(carts.get(cartType));
    int row = typesToRows.get(cartType);

    Widget widget = getWidget(row, COL_LABEL);
    if (widget != null) {
      widget.setTitle(title);
    }

    widget = getWidget(row, COL_INFO);
    if (widget != null) {
      widget.setTitle(title);
    }
  }
}
