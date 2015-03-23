package com.butent.bee.client.output;

import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ReportDateTimeItem extends ReportDateItem {

  public ReportDateTimeItem(String name, String caption) {
    super(name, caption);
  }

  @Override
  public String evaluate(SimpleRow row) {
    DateTime date = row.getDateTime(getName());

    if (date != null) {
      if (BeeUtils.isEmpty(getFormat())) {
        return date.toCompactString();
      }
      List<String> values = new ArrayList<>();

      for (DateTimeFunction fnc : getFormat().keySet()) {
        switch (fnc) {
          case DATE:
            values.add(evaluate(date.getDate(), null));
            break;
          case HOUR:
          case MINUTE:
            values.add(TimeUtils.padTwo(getValue(date, fnc)));
            break;
          default:
            values.add(evaluate(date.getDate(), fnc));
            break;
        }
      }
      return BeeUtils.joinItems(values);
    }
    return null;
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget();

      if (BeeUtils.isEmpty(getFormat())) {
        DateTime from = TimeUtils.parseDateTime(value);
        DateTime to = TimeUtils.nextMinute(from, 0);

        getFilterFrom().setDateTime(TimeUtils.max(getFilterFrom().getDateTime(), from));
        getFilterTo().setDateTime(getFilterTo().getDateTime() != null
            ? TimeUtils.min(getFilterTo().getDateTime(), to) : to);
      } else {
        String[] parts = BeeUtils.split(value, ',');
        int x = 0;

        for (DateTimeFunction fnc : getFormat().keySet()) {
          switch (fnc) {
            case DATE:
              setFilter(parts[x++], null);
              break;
            case HOUR:
            case MINUTE:
              getFormat().get(fnc).setValue(parts[x++]);
              break;
            default:
              setFilter(parts[x++], fnc);
          }
        }
      }
    }
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    return getFilterFrom() == null || !row.getRowSet().hasColumn(getName())
        || validate(getFilterFrom().getDateTime(), getFilterTo().getDateTime(),
            row.getDateTime(getName()));
  }

  @Override
  protected Editor createFilterEditor(DateTimeFunction fnc) {
    if (fnc == null) {
      return new InputDateTime();
    }
    int limit = 0;

    switch (fnc) {
      case HOUR:
        limit = 24;
        break;
      case MINUTE:
        limit = 60;
        break;
      default:
        return super.createFilterEditor(fnc);
    }
    ListBox editor = new ListBox();
    editor.addItem("");

    for (int i = 0; i < limit; i++) {
      editor.addItem((i < 10 ? "0" : "") + i);
    }
    return editor;
  }

  @Override
  protected InputDateTime getFilterFrom() {
    return (InputDateTime) super.getFilterFrom();
  }

  @Override
  protected InputDateTime getFilterTo() {
    return (InputDateTime) super.getFilterTo();
  }

  @Override
  protected EnumSet<DateTimeFunction> getSupportedFunctions() {
    return EnumSet.allOf(DateTimeFunction.class);
  }

  @Override
  protected int getValue(HasDateValue date, DateTimeFunction fnc) {
    switch (fnc) {
      case HOUR:
        return date.getHour();
      case MINUTE:
        return date.getMinute();
      default:
        return super.getValue(date, fnc);
    }
  }
}
