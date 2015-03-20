package com.butent.bee.client.output;

import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ReportDateTimeItem extends ReportDateItem {

  public ReportDateTimeItem(String name, String caption) {
    super(name, caption);
  }

  @Override
  public ReportItem deserializeFilter(String data) {
    if (!BeeUtils.isEmpty(data)) {
      getFilterWidget();
      Map<String, String> map = Codec.deserializeMap(data);
      getFilterFrom().setDateTime(TimeUtils.toDateTimeOrNull(map.get(Service.VAR_FROM)));
      getFilterTo().setDateTime(TimeUtils.toDateTimeOrNull(map.get(Service.VAR_TO)));

      if (getFilter() != null) {
        getFilter().setValue(map.get(Service.VAR_DATA));
      }
    }
    return this;
  }

  @Override
  public String evaluate(SimpleRow row) {
    String value = null;
    DateTime dateTime = row.getDateTime(getName());

    if (dateTime != null) {
      switch (getFormat()) {
        case DATETIME:
          value = dateTime.toCompactString();
          break;
        case DATE:
          value = dateTime.toDateString();
          break;
        case HOUR:
          value = TimeUtils.padTwo(dateTime.getHour());
          break;
        default:
          value = evaluate(dateTime.getDate());
          break;
      }
    }
    return value;
  }

  @Override
  public String getFormatedCaption() {
    switch (getFormat()) {
      case DATETIME:
        return getCaption();
      default:
        return BeeUtils.joinWords(getCaption(), BeeUtils.parenthesize(getFormat().getCaption()));
    }
  }

  @Override
  public String serializeFilter() {
    if (getFilterFrom() == null) {
      return null;
    }
    Map<String, Object> map = new HashMap<>();
    map.put(Service.VAR_FROM, getFilterFrom().getDateTime());
    map.put(Service.VAR_TO, getFilterTo().getDateTime());

    if (getFilter() != null) {
      map.put(Service.VAR_DATA, getFilter().getValue());
    }
    return Codec.beeSerialize(map);
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget();

      switch (getFormat()) {
        case DATETIME:
          DateTime date = TimeUtils.parseDateTime(value);
          getFilterFrom().setDateTime(date);
          getFilterTo().setDateTime(TimeUtils.nextHour(date, 0));
          break;
        case HOUR:
          getFilter().setValue(value);
          break;
        default:
          return super.setFilter(value);
      }
    }
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    if (getFilterFrom() == null || !row.getRowSet().hasColumn(getName())) {
      return true;
    }
    DateTime from = getFilterFrom().getDateTime();
    DateTime to = getFilterTo().getDateTime();

    if (from != null && to != null && TimeUtils.isMeq(from, to)) {
      return false;
    }
    DateTime date = row.getDateTime(getName());

    if (getFilter() != null && !BeeUtils.isEmpty(getFilter().getValue())) {
      boolean ok = date != null;

      if (ok) {
        int value = 0;

        switch (getFormat()) {
          case DAY:
            value = date.getDom();
            break;
          case DAY_OF_WEEK:
            value = date.getDow();
            break;
          case HOUR:
            value = date.getHour();
            break;
          case MONTH:
            value = date.getMonth();
            break;
          default:
            Assert.untouchable();
            break;
        }
        ok = BeeUtils.toInt(getFilter().getValue()) == value;
      }
      if (!ok) {
        return false;
      }
    }
    return TimeUtils.isBetweenExclusiveNotRequired(date, from, to);
  }

  @Override
  protected InputDate createDateFilter() {
    return new InputDateTime();
  }

  @Override
  protected Editor createFilter() {
    switch (getFormat()) {
      case HOUR:
        ListBox editor = new ListBox();
        editor.addItem("");

        for (int i = 0; i < 24; i++) {
          editor.addItem((i < 10 ? "0" : "") + i);
        }
        return editor;

      default:
        return super.createFilter();
    }
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
}
