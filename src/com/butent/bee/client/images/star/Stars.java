package com.butent.bee.client.images.star;

import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

import java.util.List;

import elemental.client.Browser;
import elemental.html.ImageElement;

public class Stars {
  
  public interface Resources extends ClientBundle {

    @Source("bang_red.png")
    ImageResource bangRed();
    
    @Source("bang_yellow.png")
    ImageResource bangYellow();

    @Source("check_green.png")
    ImageResource checkGreen();

    @Source("info_blue.png")
    ImageResource infoBlue();

    @Source("question_purple.png")
    ImageResource questionPurple();

    @Source("star_blue.png")
    ImageResource starBlue();

    @Source("star_colorless.png")
    ImageResource starColorless();

    @Source("star_green.png")
    ImageResource starGreen();

    @Source("star_orange.png")
    ImageResource starOrange();

    @Source("star_purple.png")
    ImageResource starPurple();

    @Source("star_red.png")
    ImageResource starRed();
    
    @Source("star_yellow.png")
    ImageResource starYellow();
  }
  
  private static final Resources resources = GWT.create(Resources.class);
  
  private static final List<ImageResource> list = Lists.newArrayList();

  private static final ImageElement imageElement = Browser.getDocument().createImageElement(); 
  
  static {
    list.add(resources.starYellow());
    list.add(resources.starOrange());
    list.add(resources.starRed());
    list.add(resources.starPurple());
    list.add(resources.starBlue());
    list.add(resources.starGreen());

    list.add(resources.bangRed());
    list.add(resources.bangYellow());
    list.add(resources.checkGreen());
    list.add(resources.infoBlue());
    list.add(resources.questionPurple());
  }
  
  public static int count() {
    return list.size();
  }
  
  public static ImageResource get(int index) {
    if (index >= 0 && index < list.size()) {
      return list.get(index);
    } else {
      return null;
    }
  }
  
  public static String getDefaultHeader() {
    return asString(resources.starColorless());
  }

  public static String getHtml(int index) {
    return asString(get(index));
  }

  private static String asString(ImageResource imageResource) {
    if (imageResource == null) {
      return null;
    }
    
    imageElement.setSrc(imageResource.getSafeUri().asString());
    return imageElement.getOuterHTML();
  }
  
  private Stars() {
  }
}
