package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class UiHelper {

  private static final String TEXT_ALIGN_START = "start";
  private static final String TEXT_ALIGN_END = "end";

  private static final List<HorizontalAlignmentConstant> HORIZONTAL_ALIGNMENT_CONSTANTS =
    Lists.newArrayList(HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_CENTER,
        HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_JUSTIFY,
        HasHorizontalAlignment.ALIGN_LOCALE_START, HasHorizontalAlignment.ALIGN_LOCALE_END,
        HasHorizontalAlignment.ALIGN_DEFAULT);

  public static HorizontalAlignmentConstant getDefaultHorizontalAlignment(ValueType type) {
    if (type == null) {
      return null;
    }

    HorizontalAlignmentConstant align;
    switch (type) {
      case BOOLEAN:
        align = HasHorizontalAlignment.ALIGN_CENTER;
        break;
      case DECIMAL:
      case INTEGER:
      case LONG:
      case NUMBER:
        align = HasHorizontalAlignment.ALIGN_LOCALE_END;
        break;
      default:
        align = null;
    }
    return align;
  }

  public static void setDefaultHorizontalAlignment(HasHorizontalAlignment obj, ValueType type) {
    Assert.notNull(obj);
    HorizontalAlignmentConstant align = getDefaultHorizontalAlignment(type);
    if (align != null) {
      obj.setHorizontalAlignment(align);
    }
  }

  public static void setHorizontalAlignment(HasHorizontalAlignment obj, String text) {
    Assert.notNull(obj);
    Assert.notEmpty(text);
    
    HorizontalAlignmentConstant align = null;
    
    if (BeeUtils.same(text, TEXT_ALIGN_START)) {
      align = HasHorizontalAlignment.ALIGN_LOCALE_START;
    } else if (BeeUtils.same(text, TEXT_ALIGN_END)) {
      align = HasHorizontalAlignment.ALIGN_LOCALE_END;
    } else {
      for (HorizontalAlignmentConstant hac : HORIZONTAL_ALIGNMENT_CONSTANTS) {
        if (BeeUtils.same(text, hac.getTextAlignString())) {
          align = hac;
          break;
        }
      }
    }
    
    if (align != null) {
      obj.setHorizontalAlignment(align);
    }
  }

  private UiHelper() {
  }
}
