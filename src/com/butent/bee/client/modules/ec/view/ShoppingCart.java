package com.butent.bee.client.modules.ec.view;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.Cart;
import com.butent.bee.shared.modules.ec.CartItem;
import com.butent.bee.shared.modules.ec.DeliveryMethod;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.CartType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ShoppingCart extends Split {

  private static final String STYLE_PRIMARY = EcStyles.name("shoppingCart");
  private static final String STYLE_ITEMS = STYLE_PRIMARY + "-items";
  private static final String STYLE_ITEM = STYLE_PRIMARY + "-item";

  private static final String STYLE_HEADER_ROW = STYLE_ITEMS + "-headerRow";
  private static final String STYLE_ITEM_ROW = STYLE_PRIMARY + "-itemRow";

  private static final String STYLE_DELIVERY_ADDRESS = STYLE_PRIMARY + "-address";
  private static final String STYLE_DELIVERY_METHOD = STYLE_PRIMARY + "-method";
  private static final String STYLE_COMMENT = STYLE_PRIMARY + "-comment";

  private static final String STYLE_PICTURE = STYLE_ITEM + "-picture";
  private static final String STYLE_INFO = STYLE_ITEM + "-info";
  private static final String STYLE_NAME = STYLE_ITEM + "-name";
  private static final String STYLE_QUANTITY = STYLE_ITEM + "-quantity";
  private static final String STYLE_PRICE = STYLE_ITEM + "-price";
  private static final String STYLE_REMOVE = STYLE_ITEM + "-remove";

  private static final String STYLE_PANEL = "-panel";
  private static final String STYLE_LABEL = "-label";
  private static final String STYLE_INPUT = "-input";

  private static final int COL_PICTURE = 0;
  private static final int COL_INFO = 1;
  private static final int COL_NAME = 2;
  private static final int COL_QUANTITY = 3;
  private static final int COL_PRICE = 4;
  private static final int COL_REMOVE = 5;

  private static final int SIZE_NORTH = 32;
  private static final int SIZE_SOUTH = 100;
  private static final int MARGIN_SOUTH = 25;

  private final CartType cartType;
  private final List<DeliveryMethod> deliveryMethods;

  private final HtmlTable itemTable = new HtmlTable(STYLE_ITEMS + "-table");
  private final CustomDiv totalWidget = new CustomDiv(STYLE_PRIMARY + "-total");

  public ShoppingCart(CartType cartType, Cart cart, List<DeliveryMethod> deliveryMethods) {
    super(0);
    addStyleName(STYLE_PRIMARY);

    this.cartType = cartType;
    this.deliveryMethods = deliveryMethods;

    initNorth();
    initSouth(cart);
    initCenter();

    renderItems(cart.getItems());
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        int containerHeight = BeeUtils.positive(getOffsetHeight(),
            BeeKeeper.getScreen().getActivePanelHeight());
        int itemsHeight = itemTable.getOffsetHeight();

        if (itemsHeight > 0 && containerHeight > itemsHeight) {
          int southHeight = containerHeight - SIZE_NORTH - itemsHeight - MARGIN_SOUTH;
          if (southHeight > SIZE_SOUTH) {
            setDirectionSize(Direction.SOUTH, southHeight, true);
          }
        }
      }
    });
  }

  private void doSubmit() {
  }

  private int getInt(HasText widget) {
    return BeeUtils.toInt(widget.getText());
  }

  private void initCenter() {
    Simple wrapper = new Simple(itemTable);
    wrapper.addStyleName(STYLE_ITEMS + "-wrapper");

    add(wrapper);
  }

  private void initNorth() {
    Label caption = new Label(cartType.getCaption());
    caption.addStyleName(STYLE_PRIMARY + "-caption");

    addNorth(caption, SIZE_NORTH);
  }

  private void initSouth(Cart cart) {
    Flow panel = new Flow(STYLE_PRIMARY + "-south");

    totalWidget.setHTML(renderTotal(cart));
    panel.add(totalWidget);

    if (!BeeUtils.isEmpty(deliveryMethods)) {
      Widget addressWidget = renderDeliveryAddress(cart);
      if (addressWidget != null) {
        panel.add(addressWidget);
      }

      Widget methodWidget = renderDeliveryMethod(cart);
      if (methodWidget != null) {
        panel.add(methodWidget);
      }

      Widget commentWidget = renderComment(cart);
      if (commentWidget != null) {
        panel.add(commentWidget);
      }

      Button submitWidget = new Button(Localized.constants.ecShoppingCartSubmit());
      submitWidget.addStyleName(STYLE_PRIMARY + "-submit");
      submitWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          doSubmit();
        }
      });
      panel.add(submitWidget);
    }

    addSouth(panel, SIZE_SOUTH);
  }

  private Widget renderComment(final Cart cart) {
    Flow panel = new Flow(STYLE_COMMENT + STYLE_PANEL);

    Label label = new Label(Localized.constants.comment());
    label.addStyleName(STYLE_COMMENT + STYLE_LABEL);
    panel.add(label);

    final InputArea input = new InputArea();
    input.addStyleName(STYLE_COMMENT + STYLE_INPUT);

    if (!BeeUtils.isEmpty(cart.getComment())) {
      input.setValue(BeeUtils.trim(cart.getComment()));
    }
    input.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        cart.setComment(Strings.emptyToNull(BeeUtils.trim(input.getValue())));
      }
    });

    panel.add(input);

    return panel;
  }

  private Widget renderDeliveryAddress(final Cart cart) {
    Flow panel = new Flow(STYLE_DELIVERY_ADDRESS + STYLE_PANEL);

    Label label = new Label(Localized.constants.ecDeliveryAddress());
    label.addStyleName(STYLE_DELIVERY_ADDRESS + STYLE_LABEL);
    panel.add(label);

    final InputArea input = new InputArea();
    input.addStyleName(STYLE_DELIVERY_ADDRESS + STYLE_INPUT);

    if (!BeeUtils.isEmpty(cart.getDeliveryAddress())) {
      input.setValue(BeeUtils.trim(cart.getDeliveryAddress()));
    }
    input.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        cart.setDeliveryAddress(Strings.emptyToNull(BeeUtils.trim(input.getValue())));
      }
    });

    panel.add(input);

    return panel;
  }

  private Widget renderDeliveryMethod(final Cart cart) {
    Flow panel = new Flow(STYLE_DELIVERY_METHOD + STYLE_PANEL);

    Label label = new Label(Localized.constants.ecDeliveryMethod());
    label.addStyleName(STYLE_DELIVERY_METHOD + STYLE_LABEL);
    panel.add(label);

    final BeeListBox input = new BeeListBox();
    input.addStyleName(STYLE_DELIVERY_METHOD + STYLE_INPUT);

    for (DeliveryMethod deliveryMethod : deliveryMethods) {
      input.addItem(deliveryMethod.getName());
    }
    
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        input.deselect();

        if (cart.getDeliveryMethod() != null) {
          for (int i = 0; i < deliveryMethods.size(); i++) {
            if (cart.getDeliveryMethod().equals(deliveryMethods.get(i).getId())) {
              input.setSelectedIndex(i);
              break;
            }
          }
        }
      }
    });

    input.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        int index = input.getSelectedIndex();
        cart.setDeliveryMethod(BeeUtils.isIndex(deliveryMethods, index)
            ? deliveryMethods.get(index).getId() : null);
      }
    });

    panel.add(input);

    return panel;
  }

  private Widget renderInfo(CartItem item) {
    return new Label(item.getEcItem().getCode());
  }

  private void renderItem(int row, CartItem item) {
    Widget pictureWidget = renderPicture();
    if (pictureWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_PICTURE, pictureWidget, STYLE_PICTURE);
    }

    Widget infoWidget = renderInfo(item);
    if (infoWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_INFO, infoWidget, STYLE_INFO);
    }

    Widget nameWidget = renderName(item);
    if (nameWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_NAME, nameWidget, STYLE_NAME);
    }

    Widget qtyWidget = renderQuantity(item);
    if (qtyWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_QUANTITY, qtyWidget, STYLE_QUANTITY);
    }

    Widget priceWidget = renderPrice(item);
    if (priceWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_PRICE, priceWidget, STYLE_PRICE);
    }

    Widget removeWidget = renderRemove(item);
    if (removeWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_REMOVE, removeWidget, STYLE_REMOVE);
    }

    itemTable.getRowFormatter().addStyleName(row, STYLE_ITEM_ROW);
  }

  private void renderItems(List<CartItem> items) {
    if (!itemTable.isEmpty()) {
      itemTable.clear();
    }

    if (!BeeUtils.isEmpty(items)) {
      int row = 0;

      Label infoLabel = new Label(Localized.constants.ecItemCode());
      infoLabel.addStyleName(STYLE_INFO + STYLE_LABEL);
      itemTable.setWidget(row, COL_INFO, infoLabel);

      Label nameLabel = new Label(Localized.constants.ecItemName());
      nameLabel.addStyleName(STYLE_NAME + STYLE_LABEL);
      itemTable.setWidget(row, COL_NAME, nameLabel);

      Label qtyLabel = new Label(Localized.constants.quantity());
      qtyLabel.addStyleName(STYLE_QUANTITY + STYLE_LABEL);
      itemTable.setWidget(row, COL_QUANTITY, qtyLabel);

      Label priceLabel = new Label(Localized.constants.price());
      priceLabel.addStyleName(STYLE_PRICE + STYLE_LABEL);
      itemTable.setWidget(row, COL_PRICE, priceLabel);

      Label removeLabel = new Label(Localized.constants.ecShoppingCartRemove());
      removeLabel.addStyleName(STYLE_REMOVE + STYLE_LABEL);
      itemTable.setWidget(row, COL_REMOVE, removeLabel);

      itemTable.getRowFormatter().addStyleName(row, STYLE_HEADER_ROW);

      row++;
      for (CartItem item : items) {
        renderItem(row++, item);
      }
    }
  }

  private Widget renderName(CartItem item) {
    return new Label(item.getEcItem().getName());
  }

  private Widget renderPicture() {
    return EcUtils.randomPicture(20, 50);
  }

  private Widget renderPrice(CartItem item) {
    return new Label(EcUtils.renderCents(item.getEcItem().getPrice()));
  }

  private Widget renderQuantity(final CartItem item) {
    String stylePrefix = STYLE_QUANTITY + "-";

    Horizontal panel = new Horizontal();

    final CustomDiv valueWidget = new CustomDiv(stylePrefix + "value");
    setInt(valueWidget, item.getQuantity());

    panel.add(valueWidget);

    Flow spin = new Flow(stylePrefix + "spin");

    Image plus = new Image(Global.getImages().silverPlus());
    plus.addStyleName(stylePrefix + "plus");

    plus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = getInt(valueWidget) + 1;
        setInt(valueWidget, value);

        item.setQuantity(value);
        Cart cart = EcKeeper.refreshCart(cartType);
        updateTotal(cart);
      }
    });
    spin.add(plus);

    Image minus = new Image(Global.getImages().silverMinus());
    minus.addStyleName(stylePrefix + "minus");

    minus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = getInt(valueWidget) - 1;
        if (value > 0) {
          setInt(valueWidget, value);

          item.setQuantity(value);
          Cart cart = EcKeeper.refreshCart(cartType);
          updateTotal(cart);
        }
      }
    });
    spin.add(minus);

    panel.add(spin);

    return panel;
  }

  private Widget renderRemove(final CartItem item) {
    Image remove = new Image("images/shoppingcart_remove.png");
    remove.setAlt("remove");

    remove.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Cart cart = EcKeeper.removeFromCart(cartType, item.getEcItem());
        if (cart != null) {
          if (cart.isEmpty()) {
            BeeKeeper.getScreen().closeWidget(ShoppingCart.this);
          } else {
            renderItems(cart.getItems());
            updateTotal(cart);
          }
        }
      }
    });

    return remove;
  }

  private String renderTotal(Cart cart) {
    return BeeUtils.joinWords(Localized.constants.ecShoppingCartTotal(),
        EcUtils.renderCents(cart.totalCents()), EcConstants.CURRENCY);
  }

  private void setInt(HasText widget, int value) {
    widget.setText(BeeUtils.toString(value));
  }

  private void updateTotal(Cart cart) {
    if (cart != null) {
      totalWidget.setHTML(renderTotal(cart));
    }
  }
}
