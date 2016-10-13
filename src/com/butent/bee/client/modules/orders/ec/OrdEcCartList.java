package com.butent.bee.client.modules.orders.ec;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.orders.ec.OrdEcCart;
import com.butent.bee.shared.modules.orders.ec.OrdEcCartItem;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.utils.BeeUtils;

public class OrdEcCartList extends HtmlTable {

  private static final String STYLE_PRIMARY = EcStyles.name("cartList");
  private static final String STYLE_LABEL = STYLE_PRIMARY + "-label";
  private static final String STYLE_HAS_ITEMS = STYLE_PRIMARY + "-hasItems";
  private static final String STYLE_UPDATED = STYLE_PRIMARY + "-updated";
  private static final String STYLE_INFO = STYLE_PRIMARY + "-info";
  private static final String STYLE_ACTIVE = STYLE_PRIMARY + "-active";

  private static final int COL_LABEL = 0;
  private static final int COL_INFO = 1;

  private final OrdEcCart cart = new OrdEcCart();

  private String renderInfo() {
    return BeeUtils.parenthesize(cart.totalQuantity());
  }

  private String renderTitle() {
    if (cart == null || cart.isEmpty()) {
      return BeeConst.STRING_EMPTY;

    } else {
      StringBuilder sb = new StringBuilder();

      for (OrdEcCartItem item : cart.getItems()) {
        sb.append(item.getEcItem().getName()).append(BeeUtils.space(3));

        int quantity = item.getQuantity();
        String price = EcUtils.formatCents(item.getEcItem().getPrice());

        sb.append(quantity).append(" x ").append(price);
        sb.append(BeeConst.CHAR_EOL);
      }

      sb.append(Localized.dictionary().ecShoppingCartTotal()).append(BeeConst.CHAR_SPACE);
      sb.append(EcUtils.formatCents(cart.totalCents())).append(BeeConst.CHAR_SPACE);
      sb.append(EcConstants.CURRENCY);

      return sb.toString();
    }
  }

  public OrdEcCartList() {
    super(STYLE_PRIMARY);

    Label label = new Label(cart.getCaption());
    label.addClickHandler(event -> onLabelClick());

    setWidgetAndStyle(0, COL_LABEL, label, STYLE_LABEL);

    Label info = new Label(renderInfo());
    setWidgetAndStyle(0, COL_INFO, info, STYLE_INFO);
    getRowFormatter().addStyleName(0, STYLE_ACTIVE);
  }

  public void addToCart(OrdEcItem ecItem, int quantity) {

    OrdEcKeeper.maybeRecalculatePrices(ecItem, quantity, input -> {
      OrdEcCartItem cartItem = cart.add(ecItem, quantity);
      if (cartItem != null) {
        BeeKeeper.getScreen().notifyInfo(
            Localized.dictionary()
                .ecUpdateCartItem(Localized.dictionary().ecShoppingCartMainShort(),
                    ecItem.getName(), cartItem.getQuantity()));
        refresh();
        OrdEcKeeper.persistCartItem(cartItem);
      }
    });
  }

  public OrdEcCart getCart() {
    return cart;
  }

  public int getQuantity(long itemId) {
    return cart.getQuantity(itemId);
  }

  public void refresh() {
    boolean updated = updateInfo();
    updateTitle();

    final Element rowElement = getRowFormatter().getElement(0);
    if (getCart().isEmpty()) {
      rowElement.removeClassName(STYLE_HAS_ITEMS);
    } else {
      rowElement.addClassName(STYLE_HAS_ITEMS);
    }

    if (updated) {
      rowElement.removeClassName(STYLE_UPDATED);
      Scheduler.get().scheduleDeferred(() -> {
        if (!StyleUtils.hasClassName(rowElement, STYLE_UPDATED)) {
          rowElement.addClassName(STYLE_UPDATED);
        }
      });
    }
  }

  public boolean removeFromCart(OrdEcItem ecItem) {
    if (getCart().remove(ecItem)) {
      BeeKeeper.getScreen().notifyInfo(Localized.dictionary()
          .ecRemoveCartItem(getCart().getCaption(), ecItem.getName()));

      refresh();
      return true;
    } else {
      return false;
    }
  }

  private void onLabelClick() {
    if (!getCart().isEmpty()) {
      OrdEcKeeper.openCart();
    }
  }

  private boolean updateInfo() {
    String info = renderInfo();

    Widget widget = getWidget(0, COL_INFO);
    if (widget != null && !BeeUtils.equalsTrim(info, widget.getElement().getInnerHTML())) {
      widget.getElement().setInnerHTML(info);
      return true;
    } else {
      return false;
    }
  }

  private void updateTitle() {
    String title = renderTitle();
    int row = 0;

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
