package com.butent.bee.client.ui;

import com.google.gwt.dom.client.Style.Unit;

public interface HasDimensions {

  String ATTR_WIDTH = "width";
  String ATTR_WIDTH_UNIT = "widthUnit";
  String ATTR_HEIGHT = "height";
  String ATTR_HEIGHT_UNIT = "heightUnit";
  
  Unit getHeightUnit();

  Double getHeightValue();

  Unit getWidthUnit();

  Double getWidthValue();
  
  void setHeightUnit(Unit heightUnit);

  void setHeightValue(Double heightValue);
  
  void setWidthUnit(Unit widthUnit);

  void setWidthValue(Double widthValue);
}
