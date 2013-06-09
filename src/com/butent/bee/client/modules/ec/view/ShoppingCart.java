package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.modules.ec.EcConstants.CartType;

class ShoppingCart extends EcView {
  
  private final CartType type;

  ShoppingCart(CartType type) {
    super();
    this.type = type;
  }

  @Override
  protected void createUi() {
    add(new Label(type.getCaption()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "shoppingCart";
  }
}
