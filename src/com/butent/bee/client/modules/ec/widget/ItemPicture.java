package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.ImmutableList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.SlideDeck;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.utils.BeeUtils;

public class ItemPicture extends Flow {

  private static final String STYLE_PREFIX = EcStyles.name("ItemPicture-");
  
  private final String itemCaption;

  public ItemPicture(String itemCaption) {
    super(STYLE_PREFIX + "container");
    this.itemCaption = itemCaption;
  }

  @Override
  public String getIdPrefix() {
    return "picture";
  }

  public String getItemCaption() {
    return itemCaption;
  }

  public void setPictures(final ImmutableList<String> pictures) {
    if (!BeeUtils.isEmpty(pictures)) {
      if (!isEmpty()) {
        clear();
      }
      addStyleName(STYLE_PREFIX + "notEmpty");

      CustomDiv wrapper = new CustomDiv(STYLE_PREFIX + "wrapper");
      StyleUtils.setBackgroundImage(wrapper, pictures.get(0));
      add(wrapper);

      if (pictures.size() > 1) {
        Flow more = new Flow(STYLE_PREFIX + "more");
        
        FaLabel play = new FaLabel(FontAwesome.PLAY, true);
        play.addStyleName(STYLE_PREFIX + "more-play");
        more.add(play);
        
        InlineLabel count = new InlineLabel(BeeUtils.toString(pictures.size() - 1));
        count.addStyleName(STYLE_PREFIX + "more-count");
        more.add(count);

        more.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            event.stopPropagation();

            SlideDeck.create(pictures, new Callback<SlideDeck>() {
              @Override
              public void onSuccess(SlideDeck result) {
                DialogBox dialog = DialogBox.create(getItemCaption(), STYLE_PREFIX + "slides");
                StyleUtils.setWidth(dialog.getHeader(), result.getWidth());

                dialog.setWidget(result);
                
                dialog.setAnimationEnabled(true);
                dialog.setHideOnEscape(true);
                
                dialog.center();
                result.handleKeyboard();
              }
            });
          }
        });

        add(more);
      }

      addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Image image = new Image(pictures.get(0));
          image.addStyleName(STYLE_PREFIX + "-open");

          Global.showModalWidget(image, getElement());
        }
      });
    }
  }
}
