package com.butent.bee.client.modules.ec.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.Global;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.Assert;

public class ItemPicture extends CustomDiv {

  private static final String STYLE_NAME = EcStyles.name("ItemPicture");

  private boolean hasPicture;

  public ItemPicture() {
    super(STYLE_NAME);
  }

  @Override
  public String getIdPrefix() {
    return "picture";
  }

  public void setPicture(final String picture) {
    Assert.notEmpty(picture);
    StyleUtils.setBackgroundImage(this, picture);

    if (!hasPicture()) {
      setHasPicture(true);
      addStyleName(STYLE_NAME + "-enabled");

      addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Image image = new Image(picture);
          image.addStyleName(STYLE_NAME + "-image");

          Global.showModalWidget(image);
        }
      });
    }
  }

  private boolean hasPicture() {
    return hasPicture;
  }

  private void setHasPicture(boolean hasPicture) {
    this.hasPicture = hasPicture;
  }
}
