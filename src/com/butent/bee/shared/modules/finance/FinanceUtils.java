package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FinanceUtils {

  public static Filter getPeriodFilter(DateTime dt, String colFrom, String colTo) {
    Assert.notNull(dt);
    DateTimeValue value = new DateTimeValue(dt);

    return Filter.and(
        Filter.or(Filter.isNull(colFrom), Filter.isLessEqual(colFrom, value)),
        Filter.or(Filter.isNull(colTo), Filter.isMore(colTo, value)));
  }

  public static Filter getStartFilter(String code, String column) {
    if (BeeUtils.isEmpty(code)) {
      return Filter.isFalse();
    }

    CompoundFilter filter = Filter.or();
    int length = BeeUtils.trimRight(code).length();

    for (int i = 1; i <= length; i++) {
      String value = code.substring(0, i);

      if (!BeeUtils.isEmpty(value)) {
        filter.add(Filter.equals(column, value));
      }
    }

    return filter;
  }

  public static List<String> groupingFunction(BeeRow row, int amountIndex, int quantityIndex) {
    List<String> values = new ArrayList<>(row.getValues());

    values.set(amountIndex, null);
    values.set(quantityIndex, null);

    return values;
  }

  public static boolean isValidEntry(Long debit, Long credit) {
    return DataUtils.isId(debit) && DataUtils.isId(credit) && !Objects.equals(debit, credit);
  }

  public static boolean isValidEntry(DateTime dt, Long debit, Long credit,
      Double amount, Long currency) {

    return dt != null && isValidEntry(debit, credit)
        && BeeUtils.nonZero(amount) && DataUtils.isId(currency);
  }

  public static void setColors(Element element, String bg, String fg) {
    Assert.notNull(element);

    if (!BeeUtils.same(bg, fg)) {
      if (!BeeUtils.isEmpty(bg)) {
        element.setBackgroundColor(bg);
      }

      if (!BeeUtils.isEmpty(fg)) {
        element.setColor(fg);
      }
    }
  }

  public static String summarizeDimensions(List<BeeColumn> columns, IsRow row) {
    if (BeeUtils.isEmpty(columns) || row == null) {
      return BeeConst.STRING_EMPTY;
    }

    List<Div> elements = new ArrayList<>();

    for (int i = 1; i <= Dimensions.getObserved(); i++) {
      Long id = DataUtils.getLongQuietly(columns, row, Dimensions.getRelationColumn(i));
      String name = DataUtils.getStringQuietly(columns, row, Dimensions.getNameColumn(i));

      if (DataUtils.isId(id) && !BeeUtils.isEmpty(name)) {
        Div div = new Div().text(name.trim());

        String bg = DataUtils.getStringQuietly(columns, row, Dimensions.getBackgroundColumn(i));
        String fg = DataUtils.getStringQuietly(columns, row, Dimensions.getForegroundColumn(i));

        setColors(div, bg, fg);

        elements.add(div);
      }
    }

    if (elements.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    } else {
      return new Div().addClass(Dimensions.STYLE_SUMMARY).append(elements).build();
    }
  }

  private FinanceUtils() {
  }
}
