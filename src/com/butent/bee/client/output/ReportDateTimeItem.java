package com.butent.bee.client.output;

import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;

public class ReportDateTimeItem extends ReportDateItem {

  public ReportDateTimeItem(String name, String caption) {
    super(name, caption);
    setFormat(DateTimeFunction.DATETIME);
  }

  @Override
  public ReportValue evaluate(SimpleRow row) {
    DateTime date = row.getDateTime(getName());

    if (date != null) {
      ReportValue value;

      switch (getFormat()) {
        case DATETIME:
          return ReportValue.of(date.toCompactString());
        case HOUR:
        case MINUTE:
          value = ReportValue.of(TimeUtils.padTwo(getValue(date)));
          break;
        default:
          value = evaluate(date.getDate());
          break;
      }
      return ReportValue.of(value.getValue(), value.toString());
    }
    return ReportValue.empty();
  }

  @Override
  public String getFormatedCaption() {
    String cap = getCaption();

    if (getFormat() != DateTimeFunction.DATETIME) {
      cap = BeeUtils.joinWords(cap, BeeUtils.parenthesize(getFormat().getCaption()));
    }
    return cap;
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget();

      switch (getFormat()) {
        case DATETIME:
          DateTime date = TimeUtils.parseDateTime(value);
          getFilterFrom().setValue(date.serialize());
          getFilterTo().setValue(TimeUtils.nextMinute(date, 0).serialize());
          break;
        case HOUR:
        case MINUTE:
          getFilter().setValue(value);
          break;
        default:
          super.setFilter(value);
      }
    }
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    if (!row.getRowSet().hasColumn(getName())) {
      return true;
    }
    DateTime date = row.getDateTime(getName());

    if (date != null && getFormat() == DateTimeFunction.DATE) {
      return validate(date.getDate());
    }
    return validate(row.getDateTime(getName()));
  }

  @Override
  protected Editor createFilterEditor() {
    int limit = 0;

    switch (getFormat()) {
      case DATETIME:
        return new InputDateTime();
      case HOUR:
        limit = 24;
        break;
      case MINUTE:
        limit = 60;
        break;
      default:
        return super.createFilterEditor();
    }
    ListBox editor = new ListBox();
    editor.addItem("");

    for (int i = 0; i < limit; i++) {
      editor.addItem(TimeUtils.padTwo(i));
    }
    return editor;
  }

  @Override
  protected EnumSet<DateTimeFunction> getSupportedFunctions() {
    return EnumSet.allOf(DateTimeFunction.class);
  }

  @Override
  protected int getValue(HasDateValue date) {
    switch (getFormat()) {
      case HOUR:
        return date.getHour();
      case MINUTE:
        return date.getMinute();
      default:
        return super.getValue(date);
    }
  }
}
