package com.butent.bee.client.output;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.report.ReportFunction;
import com.butent.bee.shared.report.ResultValue;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

public class ReportNumericItem extends ReportItem {

  private static final String PRECISION = "PRECISION";

  private int precision;
  private InputSpinner precisionWidget;

  public ReportNumericItem(String expression, String caption) {
    super(expression, caption);
  }

  @Override
  public Object calculate(Object total, ResultValue value, ReportFunction function) {
    BigDecimal val = BeeUtils.toDecimalOrNull(value.getValue());

    if (val != null) {
      switch (function) {
        case MAX:
          if (total == null) {
            return val;
          }
          return val.max((BigDecimal) total);
        case MIN:
          if (total == null) {
            return val;
          }
          return val.min((BigDecimal) total);
        case SUM:
          if (total == null) {
            return val;
          }
          return val.add((BigDecimal) total);
        default:
          return super.calculate(total, value, function);
      }
    }
    return total;
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeLinkedHashMap(data);

    if (!BeeUtils.isEmpty(map)) {
      setPrecision(BeeUtils.toInt(map.get(PRECISION)));
    }
  }

  @Override
  public ResultValue evaluate(SimpleRow row) {
    return ResultValue.of(BeeUtils.round(row.getValue(getExpression()), getPrecision()));
  }

  @Override
  public EnumSet<ReportFunction> getAvailableFunctions() {
    EnumSet<ReportFunction> functions = super.getAvailableFunctions();
    functions.add(ReportFunction.SUM);
    return functions;
  }

  @Override
  public String getOptionsCaption() {
    return Localized.dictionary().precision();
  }

  @Override
  public Widget getOptionsWidget() {
    if (precisionWidget == null) {
      precisionWidget = new InputSpinner(0, 5);
    }
    precisionWidget.setValue(getPrecision());
    return precisionWidget;
  }

  public int getPrecision() {
    return precision;
  }

  @Override
  public String getStyle() {
    return STYLE_NUM;
  }

  @Override
  public String saveOptions() {
    if (precisionWidget != null) {
      setPrecision(precisionWidget.getIntValue());
    }
    return super.saveOptions();
  }

  @Override
  public String serialize() {
    return serialize(Codec.beeSerialize(Collections.singletonMap(PRECISION, getPrecision())));
  }

  public ReportNumericItem setPrecision(int prec) {
    this.precision = Assert.nonNegative(prec);
    return this;
  }
}
