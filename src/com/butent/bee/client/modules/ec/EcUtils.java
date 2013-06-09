package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class EcUtils {
  
  private static List<String> colors = Lists.newArrayList(Color.getNames().values()); 
  
  public static Widget randomPicture(int min, int max) {
    CustomDiv widget = new CustomDiv();
    
    int width = BeeUtils.randomInt(min, max);
    int height = BeeUtils.randomInt(min, max);
    StyleUtils.setSize(widget, width, height);
    
    int index = BeeUtils.randomInt(0, colors.size());
    StyleUtils.setBackgroundColor(widget, colors.get(index));
    
    int radius = BeeUtils.randomInt(0, 6);
    if (radius > 0) {
      widget.getElement().getStyle().setProperty("borderRadius", BeeUtils.toString(radius * 10) + "%");
    }
    
    return widget;
  }

  private EcUtils() {
  }
}
