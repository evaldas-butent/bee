package com.butent.bee.client.modules.ec;

import com.google.common.collect.Range;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.CustomSpan;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public final class EcUtils {

  private static final int currentYear = TimeUtils.today().getYear();

  private static final String STYLE_FIELD_CONTAINER = "-container";
  private static final String STYLE_FIELD_LABEL = "-label";

  private static final String IMAGE_DIR = "images/ec/";
  
  public static String imageUrl(String name) {
    return IMAGE_DIR + name;
  }

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

  public static String renderCents(int cents) {
    if (cents >= 0) {
      String s = BeeUtils.toLeadingZeroes(cents, 3);
      int len = s.length();
      return s.substring(0, len - 2) + BeeConst.STRING_POINT + s.substring(len - 2);
    } else {
      return BeeConst.STRING_MINUS + renderCents(-cents);
    }
  }

  public static Widget renderField(String label, String value, String styleName) {
    if (BeeUtils.isEmpty(value)) {
      return null;
    }
    Assert.notEmpty(styleName);

    Flow container = new Flow(styleName + STYLE_FIELD_CONTAINER);

    if (!BeeUtils.isEmpty(label)) {
      CustomSpan labelWidget = new CustomSpan(styleName + STYLE_FIELD_LABEL);
      labelWidget.setHtml(label);
      container.add(labelWidget);
    }

    CustomSpan valueWidget = new CustomSpan(styleName);
    valueWidget.setHtml(value);
    container.add(valueWidget);
    
    return container;
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

  public static String renderStock(int stock) {
    if (stock <= 0) {
      return Localized.getConstants().ecStockAsk();
    } else if (EcConstants.MAX_VISIBLE_STOCK > 0 && stock > EcConstants.MAX_VISIBLE_STOCK) {
      return BeeConst.STRING_GT + EcConstants.MAX_VISIBLE_STOCK;
    } else {
      return BeeUtils.toString(stock);
    }
  }
  
  public static String string(Double value) {
    return (value == null) ? null : BeeUtils.toString(value);
  }

  public static String string(Integer value) {
    return (value == null) ? null : value.toString();
  }

  public static int toCents(Double d) {
    return BeeUtils.isDouble(d) ? BeeUtils.round(d * 100) : 0;
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
