package com.butent.bee.client.output;

import com.google.gwt.dom.client.OptionElement;
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ReportDateItem extends ReportItem {

  private Map<DateTimeFunction, Editor> format = new LinkedHashMap<>();
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
    },
    DATE() {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.date();
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
    for (Editor editor : getFormat().values()) {
      if (editor != null) {
        editor.clearValue();
      }
    }
  }

  @Override
  public String getFormatedCaption() {
    if (BeeUtils.isEmpty(getFormat())) {
      return getCaption();
    }
    List<String> captions = new ArrayList<>();

    for (DateTimeFunction fnc : getFormat().keySet()) {
      captions.add(fnc.getCaption());
    }
    return BeeUtils.joinWords(getCaption(), BeeUtils.parenthesize(BeeUtils.joinItems(captions)));
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeMap(data);

    if (!BeeUtils.isEmpty(map)) {
      format.clear();

      for (Entry<String, String> entry : Codec.deserializeMap(map.get(Service.VAR_DATA))
          .entrySet()) {
        DateTimeFunction fnc = EnumUtils.getEnumByName(DateTimeFunction.class, entry.getKey());
        Editor editor = null;

        if (!BeeUtils.isEmpty(entry.getValue())) {
          editor = createFilterEditor(fnc);
          editor.setValue(entry.getValue());
        }
        format.put(fnc, editor);
      }
      if (map.containsKey(Service.VAR_FROM)) {
        filterFrom = (InputDate) createFilterEditor(null);
        filterFrom.setValue(map.get(Service.VAR_FROM));
      }
      if (map.containsKey(Service.VAR_TO)) {
        filterTo = (InputDate) createFilterEditor(null);
        filterTo.setValue(map.get(Service.VAR_TO));
      }
    }
  }

  @Override
  public String evaluate(SimpleRow row) {
    JustDate date = row.getDate(getName());

    if (date != null) {
      if (BeeUtils.isEmpty(getFormat())) {
        return evaluate(date, null);
      }
      List<String> values = new ArrayList<>();

      for (DateTimeFunction fnc : getFormat().keySet()) {
        values.add(evaluate(date, fnc));
      }
      return BeeUtils.joinItems(values);
    }
    return null;
  }

  @Override
  public Widget getFilterWidget() {
    Flow container = new Flow(getStyle() + "-filter");
    render(container);
    return container;
  }

  public Map<DateTimeFunction, Editor> getFormat() {
    return format;
  }

  @Override
  public String getOptionsCaption() {
    return Localized.getConstants().dateFormat();
  }

  @Override
  public ListBox getOptionsWidget() {
    if (formatWidget == null) {
      formatWidget = new ListBox(true);

      for (DateTimeFunction fnc : getSupportedFunctions()) {
        formatWidget.addItem(fnc.getCaption(), fnc.name());
      }
      formatWidget.setVisibleItemCount(getSupportedFunctions().size());
    }
    for (int i = 0; i < formatWidget.getItemCount(); i++) {
      OptionElement option = formatWidget.getOptionElement(i);
      option.setSelected(getFormat().keySet()
          .contains(EnumUtils.getEnumByName(DateTimeFunction.class, option.getValue())));
    }
    return formatWidget;
  }

  @Override
  public String getStyle() {
    return STYLE_DATE;
  }

  @Override
  public ReportDateItem saveOptions() {
    if (formatWidget != null) {
      List<DateTimeFunction> formats = new ArrayList<>();

      for (int i = 0; i < formatWidget.getItemCount(); i++) {
        OptionElement optionElement = formatWidget.getOptionElement(i);

        if (optionElement.isSelected()) {
          formats.add(EnumUtils.getEnumByName(DateTimeFunction.class, optionElement.getValue()));
        }
      }
      setFormat(formats);
    }
    return this;
  }

  @Override
  public String serialize() {
    return super.serialize(serializeFilter());
  }

  @Override
  public String serializeFilter() {
    Map<String, Object> map = new HashMap<>();
    Map<DateTimeFunction, String> values = new LinkedHashMap<>();

    for (Entry<DateTimeFunction, Editor> entry : getFormat().entrySet()) {
      values.put(entry.getKey(), entry.getValue() != null ? entry.getValue().getValue() : null);
    }
    map.put(Service.VAR_DATA, values);

    if (getFilterFrom() != null) {
      map.put(Service.VAR_FROM, getFilterFrom().getNormalizedValue());
      map.put(Service.VAR_TO, getFilterTo().getNormalizedValue());
    }
    return Codec.beeSerialize(map);
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget();

      if (BeeUtils.isEmpty(getFormat())) {
        setFilter(value, null);
      } else {
        String[] parts = BeeUtils.split(value, ',');
        int x = 0;

        for (DateTimeFunction fnc : getFormat().keySet()) {
          setFilter(parts[x++], fnc);
        }
      }
    }
    return this;
  }

  public ReportDateItem setFormat(List<DateTimeFunction> dateFormat) {
    format.clear();

    if (!BeeUtils.isEmpty(dateFormat)) {
      for (DateTimeFunction fnc : dateFormat) {
        format.put(fnc, null);
      }
    }
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    return getFilterFrom() == null || !row.getRowSet().hasColumn(getName())
        || validate(getFilterFrom().getDate(), getFilterTo().getDate(), row.getDate(getName()));
  }

  protected Editor createFilterEditor(DateTimeFunction fnc) {
    if (fnc == null) {
      return new InputDate();
    }
    ListBox editor = new ListBox();
    editor.addItem("");

    switch (fnc) {
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
      case QUATER:
        for (int i = 1; i <= 4; i++) {
          editor.addItem(BeeUtils.toString(i));
        }
        break;

      default:
        editor = null;
        break;
    }
    return editor;
  }

  protected String evaluate(HasDateValue date, DateTimeFunction fnc) {
    String value = null;

    if (date != null) {
      if (fnc == null) {
        return date.toString();
      }
      switch (fnc) {
        case DAY:
          value = TimeUtils.padTwo(getValue(date, fnc));
          break;
        case DAY_OF_WEEK:
          value = Format.renderDayOfWeek(getValue(date, fnc));
          break;
        case MONTH:
          value = Format.renderMonthFullStandalone(getValue(date, fnc));
          break;
        case QUATER:
        case YEAR:
          value = BeeUtils.toString(getValue(date, fnc));
          break;
        default:
          Assert.unsupported();
          break;
      }
    }
    return value;
  }

  protected InputDate getFilterFrom() {
    return filterFrom;
  }

  protected InputDate getFilterTo() {
    return filterTo;
  }

  protected EnumSet<DateTimeFunction> getSupportedFunctions() {
    return EnumSet.of(DateTimeFunction.YEAR, DateTimeFunction.QUATER,
        DateTimeFunction.MONTH, DateTimeFunction.DAY, DateTimeFunction.DAY_OF_WEEK);
  }

  protected int getValue(HasDateValue date, DateTimeFunction fnc) {
    switch (fnc) {
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

  protected void setFilter(String part, DateTimeFunction fnc) {
    JustDate from = null;
    JustDate to = null;

    if (fnc == null) {
      from = TimeUtils.parseDate(part);
      to = TimeUtils.nextDay(from, 1);
    } else {
      switch (fnc) {
        case DAY:
        case QUATER:
          getFormat().get(fnc).setValue(part);
          break;
        case DAY_OF_WEEK:
          for (int i = 1; i <= 7; i++) {
            if (Objects.equals(part, Format.renderDayOfWeek(i))) {
              getFormat().get(fnc).setValue(BeeUtils.toString(i));
              break;
            }
          }
          break;
        case MONTH:
          for (int i = 1; i <= 31; i++) {
            if (Objects.equals(part, Format.renderMonthFullStandalone(i))) {
              getFormat().get(fnc).setValue(BeeUtils.toString(i));
              break;
            }
          }
          break;
        case YEAR:
          from = new JustDate(BeeUtils.toInt(part), 1, 1);
          to = new JustDate(from.getYear() + 1, 1, 1);
          break;
        default:
          Assert.untouchable();
          break;
      }
    }
    if (from != null) {
      getFilterFrom().setDate(TimeUtils.max(getFilterFrom().getDate(), from));
      getFilterTo().setDate(getFilterTo().getDate() != null
          ? TimeUtils.min(getFilterTo().getDate(), to) : to);
    }
  }

  protected boolean validate(HasDateValue from, HasDateValue to, HasDateValue date) {
    if (from != null && to != null && TimeUtils.isMeq(from, to)) {
      return false;
    }
    for (Entry<DateTimeFunction, Editor> entry : getFormat().entrySet()) {
      Editor editor = entry.getValue();

      if (editor != null && !BeeUtils.isEmpty(editor.getValue())) {
        if (date == null || BeeUtils.toInt(editor.getValue()) != getValue(date, entry.getKey())) {
          return false;
        }
      }
    }
    return TimeUtils.isBetweenExclusiveNotRequired(date, from, to);
  }

  private void render(final Flow container) {
    container.clear();

    container.add(new Button(Localized.getConstants().period(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Global.inputWidget(getOptionsCaption(), getOptionsWidget(), new InputCallback() {
          @Override
          public void onSuccess() {
            saveOptions();
            render(container);
          }
        });
      }
    }));
    if (getFilterFrom() == null) {
      filterFrom = (InputDate) createFilterEditor(null);
      filterTo = (InputDate) createFilterEditor(null);
    }
    container.add(getFilterFrom());
    container.add(new InlineLabel("-"));
    container.add(getFilterTo());

    for (Entry<DateTimeFunction, Editor> entry : getFormat().entrySet()) {
      DateTimeFunction fnc = entry.getKey();

      if (entry.getValue() == null) {
        entry.setValue(createFilterEditor(fnc));
      }
      if (entry.getValue() != null) {
        Flow flow = new Flow();
        flow.add(new InlineLabel(fnc.getCaption()));
        flow.add(entry.getValue());
        container.add(flow);
      }
    }
  }
}
