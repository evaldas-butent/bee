package com.butent.bee.client.output;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReportDateItem extends ReportItem {

  private static final String FORMAT = "FORMAT";

  private DateTimeFunction format = DateTimeFunction.DATE;
  private ListBox formatWidget;
  private InputDate filterFrom;
  private InputDate filterTo;
  private Editor filter;

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
    HOUR() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.hour();
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
    if (getFilter() != null) {
      getFilter().clearValue();
    }
  }

  @Override
  public String getFormatedCaption() {
    switch (getFormat()) {
      case DATE:
        return getCaption();
      default:
        return BeeUtils.joinWords(getCaption(), BeeUtils.parenthesize(getFormat().getCaption()));
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
      Map<String, String> map = Codec.deserializeMap(data);
      filterFrom.setDate(TimeUtils.toDateOrNull(map.get(Service.VAR_FROM)));
      filterTo.setDate(TimeUtils.toDateOrNull(map.get(Service.VAR_TO)));

      if (getFilter() != null) {
        getFilter().setValue(map.get(Service.VAR_DATA));
      }
    }
    return this;
  }

  @Override
  public String evaluate(SimpleRow row) {
    return evaluate(row.getDate(getName()));
  }

  @Override
  public Widget getFilterWidget() {
    Flow container = new Flow(getStyle() + "-filter");
    render(container);
    return container;
  }

  public DateTimeFunction getFormat() {
    return format;
  }

  @Override
  public String getOptionsCaption() {
    return Localized.getConstants().dateFormat();
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
    Map<String, Object> map = new HashMap<>();
    map.put(Service.VAR_FROM, filterFrom.getDate());
    map.put(Service.VAR_TO, filterTo.getDate());

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
        case DATE:
          JustDate date = TimeUtils.parseDate(value);
          filterFrom.setDate(date);
          filterTo.setDate(TimeUtils.nextDay(date, 1));
          break;
        case DAY:
          getFilter().setValue(value);
          break;
        case DAY_OF_WEEK:
          for (int i = 1; i <= 7; i++) {
            if (Objects.equals(value, Format.renderDayOfWeek(i))) {
              getFilter().setValue(BeeUtils.toString(i));
              break;
            }
          }
          break;
        case MONTH:
          for (int i = 1; i <= 31; i++) {
            if (Objects.equals(value, Format.renderMonthFullStandalone(i))) {
              getFilter().setValue(BeeUtils.toString(i));
              break;
            }
          }
          break;
        case YEAR:
          int year = BeeUtils.toInt(value);
          date = new JustDate(year, 1, 1);
          filterFrom.setDate(date);
          filterTo.setDate(new JustDate(year + 1, 1, 1));
          break;
        case YEAR_MONTH:
          date = TimeUtils.parseDate(value);
          filterFrom.setDate(date);
          filterTo.setDate(TimeUtils.goMonth(date, 1));
          break;
        default:
          Assert.untouchable();
          break;
      }
    }
    return this;
  }

  public ReportDateItem setFormat(DateTimeFunction dateFormat) {
    this.format = Assert.notNull(dateFormat);
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    if (getFilterFrom() == null || !row.getRowSet().hasColumn(getName())) {
      return true;
    }
    JustDate from = getFilterFrom().getDate();
    JustDate to = getFilterTo().getDate();

    if (from != null && to != null && TimeUtils.isMeq(from, to)) {
      return false;
    }
    JustDate date = row.getDate(getName());

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

  protected InputDate createDateFilter() {
    return new InputDate();
  }

  protected Editor createFilter() {
    ListBox editor = new ListBox();
    editor.addItem("");

    switch (getFormat()) {
      case DAY:
        for (int i = 1; i <= 31; i++) {
          editor.addItem((i < 10 ? "0" : "") + i);
        }
        break;
      case DAY_OF_WEEK:
        for (int i = 1; i <= 7; i++) {
          editor.addItem(Format.renderDayOfWeek(i), BeeUtils.toString(i));
        }
        break;
      case MONTH:
        for (int i = 1; i <= 12; i++) {
          editor.addItem(Format.renderMonthFullStandalone(i), BeeUtils.toString(i));
        }
        break;

      default:
        editor = null;
        break;
    }
    return editor;
  }

  protected String evaluate(JustDate date) {
    String value = null;

    if (date != null) {
      switch (getFormat()) {
        case DATE:
          value = date.toString();
          break;
        case DAY:
          value = TimeUtils.padTwo(date.getDom());
          break;
        case DAY_OF_WEEK:
          value = Format.renderDayOfWeek(date);
          break;
        case MONTH:
          value = Format.renderMonthFullStandalone(date);
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

  protected Editor getFilter() {
    return filter;
  }

  protected InputDate getFilterFrom() {
    return filterFrom;
  }

  protected InputDate getFilterTo() {
    return filterTo;
  }

  protected EnumSet<DateTimeFunction> getSupportedFunctions() {
    return EnumSet.of(DateTimeFunction.YEAR, DateTimeFunction.YEAR_MONTH,
        DateTimeFunction.MONTH, DateTimeFunction.DAY, DateTimeFunction.DAY_OF_WEEK,
        DateTimeFunction.DATE);
  }

  private void render(final Flow container) {
    container.clear();

    if (getFilterFrom() == null) {
      filterFrom = createDateFilter();
      filterTo = createDateFilter();
    }
    if (getFilter() == null) {
      filter = createFilter();
    }
    container.add(new Button(Localized.getConstants().period(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Global.inputWidget(getOptionsCaption(), getOptionsWidget(), new InputCallback() {
          @Override
          public void onSuccess() {
            filter = null;
            saveOptions();
            render(container);
          }
        });
      }
    }));
    container.add(getFilterFrom());
    container.add(new InlineLabel("-"));
    container.add(getFilterTo());

    if (getFilter() != null) {
      container.add(new InlineLabel(getFormat().getCaption()));
      container.add(getFilter());
    }
  }
}
