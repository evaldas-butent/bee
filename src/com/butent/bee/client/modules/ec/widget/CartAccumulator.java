package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.modules.ec.EcItem;

public class CartAccumulator extends Horizontal implements HasKeyDownHandlers {

  private static final String STYLE_PREFIX = EcStyles.name("cartAccumulator-");

  private final InputInteger input;

  public CartAccumulator(final EcItem item, int quantity) {
    super();
    addStyleName(STYLE_PREFIX + "panel");

    this.input = new InputInteger();
    input.setValue(quantity);
    input.addStyleName(STYLE_PREFIX + "input");

    input.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          int value = input.getIntValue();

          if (value > 0 && DomUtils.isInView(input)) {
            EcKeeper.addToCart(item, value);
            input.setValue(0);
          }
        }
      }
    });
    
    EcKeeper.bindKeyPress(input);

    add(input);

    Flow spin = new Flow(STYLE_PREFIX + "spin");

    FaLabel plus = new FaLabel(FontAwesome.PLUS_SQUARE_O);
    plus.addStyleName(STYLE_PREFIX + "plus");

    plus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = Math.max(input.getIntValue() + 1, 1);
        input.setValue(value);
      }
    });
    spin.add(plus);

    FaLabel minus = new FaLabel(FontAwesome.MINUS_SQUARE_O);
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

    Image cart = new Image(EcUtils.imageUrl("shoppingcart_add.png"));
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

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return input.addKeyDownHandler(handler);
  }

  public void focus() {
    input.setFocus(true);
  }

  public InputInteger getInput() {
    return input;
  }
}
