package com.butent.bee.client.output;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

public class ReportDateItem extends ReportItem {

  private static final String FORMAT = "FORMAT";

  private DateTimeFunction format = DateTimeFunction.DATE;
  private ListBox formatWidget;
  private InputDate filterFrom;
  private InputDate filterTo;

  public enum DateTimeFunction implements HasLocalizedCaption {
    YEAR() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.year();
      }
    },
    YEAR_MONTH() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.yearMonth();
      }
    },
    MONTH() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.month();
      }
    },
    DAY() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.day();
      }
    },
    DAY_OF_WEEK() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.dayOfWeek();
      }
    },
    DATE() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.date();
      }
    },
    DATETIME() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.dateTime();
      }
    },
    TIME() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.time();
      }
    },
    HOUR() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.hour();
      }
    },
    MINUTE() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.minute();
      }
    };

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }
  }

  public ReportDateItem(String name, String caption) {
    super(name, caption);
  }

  @Override
  public void clearFilter() {
    if (filterFrom != null) {
      filterFrom.clearValue();
      filterTo.clearValue();
    }
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeMap(data);

    if (!BeeUtils.isEmpty(map)) {
      setFormat(EnumUtils.getEnumByName(DateTimeFunction.class, map.get(FORMAT)));
    }
  }

  @Override
  public ReportItem deserializeFilter(String data) {
    if (!BeeUtils.isEmpty(data)) {
      getFilterWidget();
      Pair<String, String> pair = Pair.restore(data);
      filterFrom.setDate(TimeUtils.toDateOrNull(pair.getA()));
      filterTo.setDate(TimeUtils.toDateOrNull(pair.getB()));
    }
    return this;
  }

  @Override
  public String evaluate(SimpleRow row) {
    String value = null;
    JustDate date = row.getDate(getName());

    if (date != null) {
      switch (getFormat()) {
        case DATE:
          value = date.toString();
          break;
        case DAY:
          value = TimeUtils.padTwo(date.getDom());
          break;
        case DAY_OF_WEEK:
          value = BeeUtils.toString(date.getDow());
          break;
        case MONTH:
          value = TimeUtils.padTwo(date.getMonth());
          break;
        case YEAR:
          value = BeeUtils.toString(date.getYear());
          break;
        case YEAR_MONTH:
          value = BeeUtils.join(".", date.getYear(), TimeUtils.padTwo(date.getMonth()));
          break;
        default:
          Assert.unsupported();
          break;
      }
    }
    return value;
  }

  @Override
  public Widget getFilterWidget() {
    Flow container = new Flow(getStyle() + "-filter");

    if (filterFrom == null) {
      filterFrom = new InputDate();
      filterTo = new InputDate();
    }
    container.add(filterFrom);
    container.add(new InlineLabel("-"));
    container.add(filterTo);

    return container;
  }

  public DateTimeFunction getFormat() {
    return format;
  }

  @Override
  public String getOptionsCaption() {
    return "Formatas";
  }

  @Override
  public ListBox getOptionsWidget() {
    if (formatWidget == null) {
      formatWidget = new ListBox();

      for (DateTimeFunction fnc : getSupportedFunctions()) {
        formatWidget.addItem(fnc.getCaption(), fnc.name());
      }
    }
    formatWidget.setValue(getFormat().name());
    return formatWidget;
  }

  @Override
  public String getStyle() {
    return STYLE_DATE;
  }

  @Override
  public ReportDateItem saveOptions() {
    if (formatWidget != null) {
      setFormat(EnumUtils.getEnumByName(DateTimeFunction.class, formatWidget.getValue()));
    }
    return this;
  }

  @Override
  public String serialize() {
    return super.serialize(Codec.beeSerialize(Collections.singletonMap(FORMAT, format)));
  }

  @Override
  public String serializeFilter() {
    if (filterFrom == null) {
      return null;
    }
    return Codec.beeSerialize(Pair.of(filterFrom.getDate(), filterTo.getDate()));
  }

  public ReportDateItem setFormat(DateTimeFunction dateFormat) {
    this.format = Assert.notNull(dateFormat);
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    if (filterFrom == null || !row.getRowSet().hasColumn(getName())) {
      return true;
    }
    JustDate from = filterFrom.getDate();
    JustDate to = filterTo.getDate();

    if (from != null && to != null && TimeUtils.isMeq(from, to)) {
      return false;
    }
    return TimeUtils.isBetweenExclusiveNotRequired(row.getDate(getName()), from, to);
  }

  protected EnumSet<DateTimeFunction> getSupportedFunctions() {
    return EnumSet.of(DateTimeFunction.YEAR, DateTimeFunction.YEAR_MONTH,
        DateTimeFunction.MONTH, DateTimeFunction.DAY, DateTimeFunction.DAY_OF_WEEK,
        DateTimeFunction.DATE);
  }
}
