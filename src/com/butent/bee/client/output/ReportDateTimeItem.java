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
  public ReportValue evaluate(SimpleRow row) {
    DateTime date = row.getDateTime(getName());

    if (date != null) {
      if (BeeUtils.isEmpty(getFormat())) {
        return ReportValue.of(date.getTime(), date.toCompactString());
      }
      List<String> values = new ArrayList<>();
      List<ReportValue> displays = new ArrayList<>();

      for (DateTimeFunction fnc : getFormat().keySet()) {
        ReportValue value;

        switch (fnc) {
          case DATE:
            value = evaluate(date.getDate(), null);
            break;
          case HOUR:
          case MINUTE:
            int val = getValue(date, fnc);
            value = ReportValue.of(val, TimeUtils.padTwo(val));
            break;
          default:
            value = evaluate(date.getDate(), fnc);
            break;
        }
        values.add(value.getValue());
        displays.add(value);
      }
      return ReportValue.of(BeeUtils.joinItems(values), BeeUtils.joinItems(displays));
    }
    return ReportValue.empty();
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget();

      if (BeeUtils.isEmpty(getFormat())) {
        DateTime from = TimeUtils.toDateTimeOrNull(value);
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
      editor.addItem(TimeUtils.padTwo(i), BeeUtils.toString(i));
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
