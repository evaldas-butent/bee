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
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReportDateItem extends ReportItem {

  private DateTimeFunction format = DateTimeFunction.DATE;
  private ListBox formatWidget;

  private Editor filterFrom;
  private Editor filterTo;
  private Editor filter;

  public enum DateTimeFunction implements HasLocalizedCaption {
    YEAR() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.year();
      }
    },
    QUATER() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.quater();
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
    if (getFilterFrom() != null) {
      getFilterFrom().clearValue();
      getFilterTo().clearValue();
    }
    if (getFilter() != null) {
      getFilter().clearValue();
    }
  }

  @Override
  public String getFormatedCaption() {
    String cap = getCaption();

    if (getFormat() != DateTimeFunction.DATE) {
      cap = BeeUtils.joinWords(cap, BeeUtils.parenthesize(getFormat().getCaption()));
    }
    return cap;
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeMap(data);

    if (!BeeUtils.isEmpty(map)) {
      setFormat(EnumUtils.getEnumByName(DateTimeFunction.class, map.get(Service.VAR_OPTIONS)));

      getFilterWidget();

      if (map.containsKey(Service.VAR_FROM)) {
        filterFrom.setValue(map.get(Service.VAR_FROM));
      }
      if (map.containsKey(Service.VAR_TO)) {
        filterTo.setValue(map.get(Service.VAR_TO));
      }
      if (map.containsKey(Service.VAR_DATA)) {
        getFilter().setValue(map.get(Service.VAR_DATA));
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ReportDateItem)) {
      return false;
    }
    return super.equals(obj) && Objects.equals(getFormat(), ((ReportDateItem) obj).getFormat());
  }

  @Override
  public ReportValue evaluate(SimpleRow row) {
    JustDate date = row.getDate(getName());

    if (date != null) {
      ReportValue value = evaluate(date);
      return ReportValue.of(value.getValue(), value.toString());
    }
    return ReportValue.empty();
  }

  @Override
  public Widget getFilterWidget() {
    Flow container = new Flow(getStyle() + "-filter");
    renderFilter(container);
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
  public int hashCode() {
    return Objects.hash(getName(), getFormat());
  }

  @Override
  public String saveOptions() {
    if (formatWidget != null) {
      setFormat(EnumUtils.getEnumByName(DateTimeFunction.class, formatWidget.getValue()));
      filterFrom = null;
      filterTo = null;
      filter = null;
    }
    return super.saveOptions();
  }

  @Override
  public String serialize() {
    return serialize(serializeFilter());
  }

  @Override
  public String serializeFilter() {
    Map<String, Object> map = new HashMap<>();

    map.put(Service.VAR_OPTIONS, getFormat().name());

    if (getFilterFrom() != null) {
      map.put(Service.VAR_FROM, getFilterFrom().getNormalizedValue());
      map.put(Service.VAR_TO, getFilterTo().getNormalizedValue());
    }
    if (getFilter() != null) {
      map.put(Service.VAR_DATA, getFilter().getValue());
    }
    return Codec.beeSerialize(map);
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget();

      JustDate from = null;
      JustDate to = null;

      switch (getFormat()) {
        case DATE:
          from = TimeUtils.parseDate(value);
          to = TimeUtils.nextDay(from, 1);
          break;
        case DAY:
        case QUATER:
        case DAY_OF_WEEK:
        case MONTH:
          getFilter().setValue(value);
          break;
        case YEAR:
          from = new JustDate(BeeUtils.toInt(value), 1, 1);
          to = new JustDate(from.getYear() + 1, 1, 1);
          break;
        default:
          Assert.untouchable();
          break;
      }
      if (from != null) {
        getFilterFrom().setValue(from.serialize());
      }
      if (to != null) {
        getFilterTo().setValue(to.serialize());
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
    if (!row.getRowSet().hasColumn(getName())) {
      return true;
    }
    return validate(row.getDate(getName()));
  }

  protected Editor createFilterEditor() {
    ListBox editor = new ListBox();
    editor.addItem("");

    switch (getFormat()) {
      case DATE:
      case YEAR:
        return new InputDate();
      case DAY:
        for (int i = 1; i <= 31; i++) {
          editor.addItem(BeeUtils.toString(i), TimeUtils.padTwo(i));
        }
        break;
      case DAY_OF_WEEK:
        for (int i = 1; i <= 7; i++) {
          editor.addItem(Format.renderDayOfWeek(i), BeeUtils.toString(i));
        }
        break;
      case MONTH:
        for (int i = 1; i <= 12; i++) {
          editor.addItem(Format.renderMonthFullStandalone(i), TimeUtils.padTwo(i));
        }
        break;
      case QUATER:
        for (int i = 1; i <= 4; i++) {
          String display = null;

          switch (i) {
            case 1:
              display = "I";
              break;
            case 2:
              display = "II";
              break;
            case 3:
              display = "III";
              break;
            case 4:
              display = "IV";
              break;
          }
          editor.addItem(display, BeeUtils.toString(i));
        }
        break;

      default:
        editor = null;
        break;
    }
    return editor;
  }

  protected ReportValue evaluate(HasDateValue date) {
    if (getFormat() == DateTimeFunction.DATE) {
      return ReportValue.of(date.toString());
    }
    int val = getValue(date);
    String value = null;
    String display = null;

    switch (getFormat()) {
      case DAY:
        value = TimeUtils.padTwo(val);
        display = BeeUtils.toString(val);
        break;
      case DAY_OF_WEEK:
        value = BeeUtils.toString(val);
        display = Format.renderDayOfWeek(val);
        break;
      case MONTH:
        value = TimeUtils.padTwo(val);
        display = Format.renderMonthFullStandalone(val);
        break;
      case QUATER:
        switch (val) {
          case 1:
            display = "I";
            break;
          case 2:
            display = "II";
            break;
          case 3:
            display = "III";
            break;
          case 4:
            display = "IV";
            break;
        }
        value = BeeUtils.toString(val);
        break;
      case YEAR:
        value = BeeUtils.toString(val);
        display = value;
        break;
      default:
        Assert.unsupported();
        break;
    }
    return ReportValue.of(value, display);
  }

  protected Editor getFilter() {
    return filter;
  }

  protected Editor getFilterFrom() {
    return filterFrom;
  }

  protected Editor getFilterTo() {
    return filterTo;
  }

  protected EnumSet<DateTimeFunction> getSupportedFunctions() {
    return EnumSet.of(DateTimeFunction.YEAR, DateTimeFunction.QUATER, DateTimeFunction.MONTH,
        DateTimeFunction.DAY, DateTimeFunction.DAY_OF_WEEK, DateTimeFunction.DATE);
  }

  protected int getValue(HasDateValue date) {
    switch (getFormat()) {
      case DAY:
        return date.getDom();
      case DAY_OF_WEEK:
        return date.getDow();
      case MONTH:
        return date.getMonth();
      case QUATER:
        return (date.getMonth() - 1) / 3 + 1;
      case YEAR:
        return date.getYear();
      default:
        Assert.untouchable();
    }
    return BeeConst.UNDEF;
  }

  protected boolean validate(HasDateValue date) {
    if (getFilter() != null && !BeeUtils.isEmpty(getFilter().getValue())) {
      if (date == null || BeeUtils.toInt(getFilter().getValue()) != getValue(date)) {
        return false;
      }
    }
    boolean ok = true;

    if (getFilterFrom() != null) {
      Long dt = date != null ? BeeUtils.toLongOrNull(date.serialize()) : null;

      ok = BeeUtils.isMeq(dt, BeeUtils.toLongOrNull(getFilterFrom().getNormalizedValue()));

      if (ok) {
        Long to = BeeUtils.toLongOrNull(getFilterTo().getNormalizedValue());
        ok = to == null || BeeUtils.isLess(dt, to);
      }
    }
    return ok;
  }

  private void renderFilter(final Flow container) {
    container.clear();
    String cap;

    switch (getFormat()) {
      case DAY:
      case DAY_OF_WEEK:
      case HOUR:
      case MINUTE:
      case MONTH:
      case QUATER:
        cap = getFormat().getCaption();

        if (getFilter() == null) {
          filter = createFilterEditor();
        }
        container.add(getFilter());
        break;

      default:
        cap = Localized.getConstants().period();

        if (getFilterFrom() == null) {
          filterFrom = createFilterEditor();
          filterTo = createFilterEditor();
        }
        container.add(new Label(Localized.getConstants().dateFromShort()));
        container.add(getFilterFrom());
        container.add(new Label(Localized.getConstants().dateToShort()));
        container.add(getFilterTo());
        break;
    }
    container.insert(new Button(cap, new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Global.inputWidget(getOptionsCaption(), getOptionsWidget(), new InputCallback() {
          @Override
          public void onSuccess() {
            saveOptions();
            renderFilter(container);
          }
        });
      }
    }), 0);
  }
}
