package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.utils.Evaluator.Evaluation;
import com.butent.bee.client.utils.HasEvaluation;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.HasPrecision;
import com.butent.bee.shared.HasScale;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

/**
 * Enables using columns which contain results of calculations with other columns.
 */

public class CalculatedColumn extends AbstractColumn<String> implements HasDateTimeFormat,
    HasNumberFormat, HasPrecision, HasScale, HasEvaluation {

  private final ValueType valueType;
  private final Evaluator evaluator;

  private DateTimeFormat dateTimeformat = null;
  private NumberFormat numberFormat = null;

  private int precision = BeeConst.UNDEF;
  private int scale = BeeConst.UNDEF;
  
  public CalculatedColumn(Cell<String> cell, ValueType valueType, Evaluator evaluator) {
    super(cell);
    this.valueType = valueType;
    this.evaluator = evaluator;

    UiHelper.setDefaultHorizontalAlignment(this, valueType);
  }

  public CalculatedColumn(ValueType valueType, Evaluator evaluator) {
    this(new CalculatedCell(), valueType, evaluator);
  }

  @Override
  public ColType getColType() {
    return ColType.CALCULATED;
  }

  public DateTimeFormat getDateTimeFormat() {
    return dateTimeformat;
  }

  public NumberFormat getNumberFormat() {
    return numberFormat;
  }

  public int getPrecision() {
    return precision;
  }

  public int getScale() {
    return scale;
  }

  @Override
  public String getString(Context context, IsRow row) {
    if (row == null) {
      return null;
    }
    if (context == null) {
      getEvaluator().update(row);
    } else {
      getEvaluator().update(row, context.getIndex(), context.getColumn());
    }
    return getEvaluator().evaluate();
  }

  @Override
  public String getValue(IsRow object) {
    return null;
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  @Override
  public void render(Context context, IsRow rowValue, SafeHtmlBuilder sb) {
    String value = getString(context, rowValue);
    if (BeeUtils.isEmpty(value)) {
      return;
    }
    getCell().render(context, format(value), sb);
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    this.dateTimeformat = format;
  }

  public void setEvaluation(Evaluation evaluation) {
    if (getEvaluator() != null) {
      getEvaluator().setEvaluation(evaluation);
    }
  }

  public void setNumberFormat(NumberFormat format) {
    this.numberFormat = format;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  private String format(String value) {
    if (getValueType() == null) {
      return value;
    }

    switch (getValueType()) {
      case BOOLEAN:
        return BooleanCell.format(BeeUtils.toBooleanOrNull(value));

      case DATE:
      case DATETIME:
        DateTime dt = TimeUtils.toDateTimeOrNull(value);
        if (dt == null) {
          return null;
        }

        boolean isDate = ValueType.DATE.equals(getValueType());
        DateTimeFormat df = getDateTimeFormat();
        if (df == null) {
          df = isDate ? Format.getDefaultDateFormat() : Format.getDefaultDateTimeFormat();
        }
        if (df == null) {
          if (isDate) {
            return new JustDate(dt).toString();
          } else {
            return dt.toString();
          }
        }
        return df.format(dt);

      case DECIMAL:
        return formatNumber(BeeUtils.toDecimalOrNull(value));
      case INTEGER:
        return formatNumber(BeeUtils.toIntOrNull(value));
      case LONG:
        return formatNumber(BeeUtils.toLongOrNull(value));
      case NUMBER:
        return formatNumber(BeeUtils.toDoubleOrNull(value));

      case TEXT:
      case TIMEOFDAY:
        return BeeUtils.trimRight(value);
    }
    return null;
  }

  private String formatNumber(Number value) {
    if (value == null) {
      return null;
    }
    NumberFormat format = getNumberFormat();
    if (format == null) {
      format = Format.getDefaultNumberFormat(getValueType(), getScale());
    }

    if (format == null) {
      return value.toString();
    } else {
      return format.format(value);
    }
  }

  private Evaluator getEvaluator() {
    return evaluator;
  }
}
