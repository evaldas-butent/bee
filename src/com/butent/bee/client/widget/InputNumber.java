package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasBounds;
import com.butent.bee.shared.HasIntStep;
import com.butent.bee.shared.HasPrecision;
import com.butent.bee.shared.HasScale;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a user interface component for inserting number type values.
 */

public class InputNumber extends InputText implements HasBounds, HasIntStep,
    HasNumberFormat, HasPrecision, HasScale {

  public static final CharMatcher intCharMatcher =
      CharMatcher.inRange(BeeConst.CHAR_ZERO, BeeConst.CHAR_NINE)
          .or(CharMatcher.is(BeeConst.CHAR_SPACE))
          .or(CharMatcher.is(BeeConst.CHAR_MINUS));

  public static final CharMatcher numCharMatcher = intCharMatcher
      .or(CharMatcher.is(BeeConst.CHAR_POINT))
      .or(CharMatcher.is(BeeConst.CHAR_COMMA));
  
  private int precision = BeeConst.UNDEF;
  private int scale = BeeConst.UNDEF;

  private String minValue = null;
  private String maxValue = null;

  private int stepValue = BeeConst.UNDEF;

  private NumberFormat format = null;

  public InputNumber() {
    super();
  }

  public InputNumber(Element element) {
    super(element);
  }

  public InputNumber(HasStringValue source) {
    super(source);
  }

  @Override
  public String getIdPrefix() {
    return "number";
  }

  @Override
  public String getMaxValue() {
    return maxValue;
  }

  @Override
  public String getMinValue() {
    return minValue;
  }

  @Override
  public String getNormalizedValue() {
    String v = BeeUtils.trim(getValue());
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    }

    if (getNumberFormat() != null) {
      Double d = Format.parseQuietly(getNumberFormat(), v);
      if (d == null) {
        return null;
      }
      v = BeeUtils.toString(d);
    }
    return normalize(v);
  }

  public Double getNumber() {
    return BeeUtils.toDoubleOrNull(getNormalizedValue());
  }

  @Override
  public NumberFormat getNumberFormat() {
    return format;
  }

  @Override
  public int getPrecision() {
    return precision;
  }

  @Override
  public int getScale() {
    return scale;
  }

  @Override
  public int getStepValue() {
    return stepValue;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_DECIMAL;
  }

  @Override
  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  @Override
  public void setNumberFormat(NumberFormat format) {
    this.format = format;
  }

  @Override
  public void setPrecision(int precision) {
    this.precision = precision;
  }

  @Override
  public void setScale(int scale) {
    this.scale = scale;
  }

  @Override
  public void setStepValue(int stepValue) {
    this.stepValue = stepValue;
  }

  public void setValue(Double value) {
    if (value == null) {
      setValue(BeeConst.STRING_EMPTY);
    } else if (getNumberFormat() != null) {
      setValue(getNumberFormat().format(value));
    } else {
      setValue(BeeUtils.toString(value));
    }
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
    if (BeeUtils.isEmpty(oldValue) || acceptChar(charCode) || getNumberFormat() == null) {
      super.startEdit(oldValue, charCode, onEntry, sourceElement);
    } else {
      setValue(getNumberFormat().format(BeeUtils.toDouble(oldValue)));
      if (onEntry != null) {
        super.startEdit(getValue(), charCode, onEntry, sourceElement);
      }
    }
  }

  @Override
  public List<String> validate(boolean checkForNull) {
    List<String> messages = Lists.newArrayList();
    messages.addAll(super.validate(checkForNull));
    if (!messages.isEmpty()) {
      return messages;
    }

    String v = BeeUtils.trim(getValue());
    if (BeeUtils.isEmpty(v)) {
      if (checkForNull && !isNullable()) {
        messages.add(Localized.constants.valueRequired());
      }
      return messages;
    }

    if (getNumberFormat() != null) {
      Double d = Format.parseQuietly(getNumberFormat(), v);
      if (d == null) {
        messages.add("Neteisingas skaičiaus formatas:");
        messages.add(BeeUtils.joinWords(v, BeeUtils.bracket(getNumberFormat().getPattern())));
        return messages;
      }

      v = normalize(BeeUtils.toString(d));
    }

    if (!checkType(v)) {
      messages.add(BeeUtils.joinWords("Neteisingas skaičius:", v));
      return messages;
    }

    if (!checkBounds(getNumber())) {
      messages.addAll(ValidationHelper.getBounds(this));
    }
    return messages;
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    List<String> messages = Lists.newArrayList();
    messages.addAll(super.validate(normalizedValue, checkForNull));
    if (!messages.isEmpty()) {
      return messages;
    }

    if (BeeUtils.isEmpty(normalizedValue)) {
      if (checkForNull && !isNullable()) {
        messages.add(Localized.constants.valueRequired());
      }
      return messages;
    }

    if (!checkBounds(BeeUtils.toDoubleOrNull(normalizedValue))) {
      messages.addAll(ValidationHelper.getBounds(this));
    }
    return messages;
  }

  protected boolean checkType(String v) {
    return BeeUtils.isDouble(v);
  }

  @Override
  protected CharMatcher getDefaultCharMatcher() {
    return InputNumber.numCharMatcher;
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InputNumber";
  }

  protected String normalize(String v) {
    return BeeUtils.toString(BeeUtils.toDouble(v));
  }

  private boolean checkBounds(Double v) {
    if (v == null) {
      return isNullable();
    }

    Double min = BeeUtils.toDoubleOrNull(getMinValue());
    if (min != null && v < min) {
      return false;
    }

    Double max = BeeUtils.toDoubleOrNull(getMaxValue());
    if (max != null && v > max) {
      return false;
    }
    return true;
  }
}
