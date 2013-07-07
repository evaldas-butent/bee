package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public final class EcUtils {
  
  private static final List<String> colors = Lists.newArrayList(Color.getNames().values());
  
  private static final int currentYear = TimeUtils.today().getYear(); 
  
  public static boolean isProduced(Integer producedFrom, Integer producedTo, int year) {
    Integer yearFrom = normalizeYear(producedFrom);
    if (yearFrom == null || yearFrom > year) {
      return false;
    }

    Integer yearTo = normalizeYear(producedTo);
    if (yearTo == null) {
      yearTo = currentYear;
    }
    
    if (yearTo > yearFrom) {
      return year <= yearTo;
    } else {
      return year <= yearFrom;
    }
  }

  public static Integer normalizeYear(Integer year) {
    if (year == null) {
      return null;
    } else if (year > 1900 * 100) {
      return year / 100;
    } else if (year > 1900) {
      return year;
    } else {
      return null;
    }
  }
  
  public static Widget randomPicture(int min, int max) {
    CustomDiv widget = new CustomDiv();
    
    int width = BeeUtils.randomInt(min, max);
    int height = BeeUtils.randomInt(min, max);
    StyleUtils.setSize(widget, width, height);
    
    int index = BeeUtils.randomInt(0, colors.size());
    StyleUtils.setBackgroundColor(widget, colors.get(index));
    
    int radius = BeeUtils.randomInt(0, 6);
    if (radius > 0) {
      widget.getElement().getStyle().setProperty("borderRadius", 
          BeeUtils.toString(radius * 10) + "%");
    }
    
    return widget;
  }
  
  public static String renderCents(int cents) {
    if (cents >= 0) {
      String s = BeeUtils.toLeadingZeroes(cents, 3);
      int len = s.length();
      return s.substring(0, len - 2) + BeeConst.STRING_POINT + s.substring(len - 2);
    } else {
      return BeeConst.STRING_MINUS + renderCents(-cents);
    }
  }
  
  public static String renderProduced(Integer producedFrom, Integer producedTo) {
    Integer yearFrom = normalizeYear(producedFrom);
    if (yearFrom == null) {
      return BeeConst.STRING_EMPTY;
    }

    Integer yearTo = normalizeYear(producedTo);
    if (yearTo == null) {
      yearTo = currentYear;
    }
    
    if (yearTo > yearFrom) {
      return yearFrom.toString() + BeeConst.STRING_MINUS + yearTo.toString();
    } else {
      return yearFrom.toString();
    }
  }
  
  public static String string(Double value) {
    return (value == null) ? null : BeeUtils.toString(value);
  }
  
  public static String string(Integer value) {
    return (value == null) ? null : value.toString();
  }

  public static Range<Integer> yearsProduced(Integer producedFrom, Integer producedTo) {
    Integer yearFrom = normalizeYear(producedFrom);
    if (yearFrom == null) {
      return null;
    }

    Integer yearTo = normalizeYear(producedTo);
    if (yearTo == null) {
      yearTo = currentYear;
    }
    
    return Range.closed(yearFrom, Math.max(yearFrom, yearTo));
  }
  
  private EcUtils() {
  }
}
