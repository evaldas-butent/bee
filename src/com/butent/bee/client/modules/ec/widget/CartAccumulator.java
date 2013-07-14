package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.Global;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.shared.modules.ec.EcItem;

public class CartAccumulator extends Horizontal {
  
  private static final String STYLE_PREFIX = EcStyles.name("cartAccumulator-");

  public CartAccumulator(final EcItem item, int quantity) {
    super();
    addStyleName(STYLE_PREFIX + "panel");
    
    final InputInteger input = new InputInteger(quantity);
    input.addStyleName(STYLE_PREFIX + "input");
    add(input);

    Flow spin = new Flow(STYLE_PREFIX + "spin");

    Image plus = new Image(Global.getImages().silverPlus());
    plus.addStyleName(STYLE_PREFIX + "plus");

    plus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = Math.max(input.getIntValue() + 1, 1);
        input.setValue(value);
      }
    });
    spin.add(plus);

    Image minus = new Image(Global.getImages().silverMinus());
    minus.addStyleName(STYLE_PREFIX + "minus");

    minus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = Math.max(input.getIntValue() - 1, 0);
        input.setValue(value);
      }
    });
    spin.add(minus);

    add(spin);

    Image cart = new Image("images/shoppingcart_add.png");
    cart.setAlt("cart");
    cart.addStyleName(STYLE_PREFIX + "add");

    cart.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = input.getIntValue();
        if (value > 0) {
          EcKeeper.addToCart(item, value);
          input.setValue(0);
        }
      }
    });

    add(cart);
  }
}
