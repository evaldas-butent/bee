package com.butent.bee.client.modules.crm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.HasHtml;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.html.builder.elements.Span;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.CronExpression;
import com.butent.bee.shared.time.CronExpression.Field;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.ScheduleDateMode;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.WorkdayTransition;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class RecurringTaskHandler extends AbstractFormInterceptor implements CellValidateEvent.Handler {

  private enum Cron {
    DAY_OF_MONTH(Field.DAY_OF_MONTH, COL_RT_DAY_OF_MONTH, 1) {
      @Override
      String getLabel(int value) {
        return BeeUtils.toString(value);
      }
    },
    MONTH(Field.MONTH, COL_RT_MONTH, 1) {
      @Override
      String getLabel(int value) {
        return BeeUtils.proper(Format.renderMonthFullStandalone(value));
      }
    },
    DAY_OF_WEEK(Field.DAY_OF_WEEK, COL_RT_DAY_OF_WEEK, 1) {
      @Override
      String getLabel(int value) {
        return BeeUtils.proper(Format.renderDayOfWeek(value));
      }
    };

    private static Cron getBySource(String source) {
      for (Cron cron : Cron.values()) {
        if (cron.source.equals(source)) {
          return cron;
        }
      }

      return null;
    }

    private final Field field;
    private final String source;
    private final int shift;

    private Flow toggleContainer;
    private HasHtml errorLabel;

    private Cron(Field field, String source, int shift) {
      this.field = field;
      this.source = source;
      this.shift = shift;
    }

    abstract String getLabel(int value);

    private void clearErrors() {
      if (errorLabel != null) {
        errorLabel.setText(BeeConst.STRING_EMPTY);
      }
    }

    private void clearValues() {
      if (toggleContainer != null) {
        for (int i = 0; i < toggleContainer.getWidgetCount(); i++) {
          setValue(i, false);
        }
      }
    }

    private Consumer<Map<Integer, String>> getFailureHandler(final String input) {
      if (errorLabel == null) {
        return null;
      }
      clearErrors();

      return new Consumer<Map<Integer, String>>() {
        @Override
        public void accept(Map<Integer, String> failures) {
          StringBuilder sb = new StringBuilder();

          int index = 0;
          while (index < input.length()) {
            if (failures.containsKey(index)) {
              String failure = failures.get(index);
              Span span = new Span().addClass(STYLE_FAILURE).text(failure);

              sb.append(span.build());
              index += failure.length();

            } else {
              sb.append(input.charAt(index));
              index++;
            }
          }

          errorLabel.setHtml(sb.toString());
        }
      };
    }

    private void initToggle(final Toggle toggle, final int value,
        final RecurringTaskHandler handler) {
      toggle.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String updated = handler.updateCronValue(source, value, toggle.isDown());
          refreshErrors(updated);
        }
      });
    }

    private void initValueContainer(Flow widget, RecurringTaskHandler handler) {
      this.toggleContainer = widget;

      for (int i = field.getMin(); i <= field.getMax(); i++) {
        String text = getLabel(i);
        Toggle toggle = new Toggle(text, text, STYLE_VALUE_TOGGLE);

        initToggle(toggle, i, handler);
        toggleContainer.add(toggle);
      }
    }

    private void refreshErrors(String input) {
      if (BeeUtils.isEmpty(input)) {
        clearErrors();
      } else {
        CronExpression.parseSimpleValues(field, input, getFailureHandler(input));
      }
    }

    private void refreshValues(String input) {
      Set<Integer> values = CronExpression.parseSimpleValues(field, input,
          getFailureHandler(input));
      setValues(values);
    }

    private void setValue(int index, boolean selected) {
      Widget widget = toggleContainer.getWidget(index);
      if (widget instanceof Toggle) {
        ((Toggle) widget).setDown(selected);

      }
    }

    private void setValues(Set<Integer> values) {
      if (toggleContainer != null) {
        for (int i = 0; i < toggleContainer.getWidgetCount(); i++) {
          setValue(i, values.contains(i + shift));
        }
      }
    }
  }

  private static final class DateMode {
    private final DateRange range;
    private final ScheduleDateMode mode;

    private DateMode(DateRange range, ScheduleDateMode mode) {
      this.range = range;
      this.mode = mode;
    }
  }

  private static final class DayOfMonth {
    private final int dom;
    private final int dow;

    private final boolean scheduled;

    private final List<String> styleNames = Lists.newArrayList();

    private final List<BeeRow> tasks = Lists.newArrayList();

    private DayOfMonth(JustDate date, boolean scheduled) {
      this.dom = date.getDom();
      this.dow = date.getDow();

      this.scheduled = scheduled;
    }

    private void addStyleName(String styleName) {
      styleNames.add(styleName);
    }

    private void addTasks(Collection<BeeRow> rows) {
      tasks.addAll(rows);
    }
  }

  private static final String STYLE_PREFIX = CRM_STYLE_PREFIX + "rt-";

  private static final String STYLE_SCHEDULE_PANEL = STYLE_PREFIX + "schedule-panel";
  private static final String STYLE_CALENDAR_PANEL = STYLE_PREFIX + "calendar-panel";
  private static final String STYLE_MONTH_PANEL = STYLE_PREFIX + "month-panel";
  private static final String STYLE_MONTH_LABEL = STYLE_PREFIX + "month-label";
  private static final String STYLE_MONTH_TABLE = STYLE_PREFIX + "month-table";
  private static final String STYLE_WEEKDAY_CELL = STYLE_PREFIX + "weekday-cell";

  private static final String STYLE_DAY_CELL = STYLE_PREFIX + "day-cell";
  private static final String STYLE_DAY_SCHEDULED = STYLE_PREFIX + "day-scheduled";
  private static final String STYLE_INFERTILE = STYLE_PREFIX + "infertile";
  private static final String STYLE_TODAY = STYLE_PREFIX + "today";
  private static final String STYLE_HOLIDAY = STYLE_PREFIX + "holiday";
  private static final String STYLE_WEEKEND = STYLE_PREFIX + "weekend";

  private static final String STYLE_SCHEDULED = STYLE_PREFIX + "scheduled";

  private static final String STYLE_INFO_PANEL = STYLE_PREFIX + "info-panel";
  private static final String STYLE_INFO_LABEL = STYLE_PREFIX + "info-label";

  private static final String STYLE_VALUE_TOGGLE = STYLE_PREFIX + "value-toggle";

  private static final String STYLE_CRON_EXAMPLES = STYLE_PREFIX + "cron-examples";

  private static final String STYLE_FAILURE = STYLE_PREFIX + "failure";

  private static void getDateModes(Long rtId, final Consumer<List<DateMode>> consumer) {
    if (DataUtils.isId(rtId)) {
      Queries.getRowSet(VIEW_RT_DATES,
          Lists.newArrayList(COL_RTD_FROM, COL_RTD_UNTIL, COL_RTD_MODE),
          Filter.isEqual(COL_RTD_RECURRING_TASK, new LongValue(rtId)),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              List<DateMode> dateModes = Lists.newArrayList();

              if (!DataUtils.isEmpty(result)) {
                for (BeeRow row : result.getRows()) {
                  JustDate from = row.getDate(0);
                  JustDate until = row.getDate(1);

                  ScheduleDateMode mode =
                      EnumUtils.getEnumByIndex(ScheduleDateMode.class, row.getInteger(2));

                  if (from != null && mode != null) {
                    DateRange range;
                    if (until == null || from.equals(until)) {
                      range = DateRange.day(from);
                    } else if (TimeUtils.isLess(from, until)) {
                      range = DateRange.closed(from, until);
                    } else {
                      range = null;
                    }

                    if (range != null) {
                      dateModes.add(new DateMode(range, mode));
                    }
                  }
                }
              }

              consumer.accept(dateModes);
            }
          });

    } else {
      consumer.accept(new ArrayList<DateMode>());
    }
  }

  private static void getOffspring(Long rtId, Queries.RowSetCallback callback) {
    if (DataUtils.isId(rtId)) {
      Queries.getRowSet(VIEW_TASKS, null, Filter.isEqual(COL_RECURRING_TASK, new LongValue(rtId)),
          callback);
    } else {
      callback.onSuccess(null);
    }
  }

  private static void initDayOfMonthHelp(HasClickHandlers widget, final Element target) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showExamples(target, CronExpression.getDayOfMonthExamples());
      }
    });
  }

  private static void initDayOfWeekHelp(HasClickHandlers widget, final Element target) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showExamples(target, CronExpression.getDayOfWeekExamples());
      }
    });
  }

  private static void initMonthHelp(HasClickHandlers widget, final Element target) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showExamples(target, CronExpression.getMonthExamples());
      }
    });
  }

  private static void isFertile(Long rtId, final Consumer<Boolean> consumer) {
    if (DataUtils.isId(rtId)) {
      ParameterList params = CrmKeeper.createArgs(SVC_RT_GET_EXECUTORS);
      params.addQueryItem(VAR_RT_ID, rtId);

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasResponse(String.class)) {
            consumer.accept(!BeeUtils.isEmpty(response.getResponseAsString()));
          } else {
            consumer.accept(false);
          }
        }
      });

    } else {
      consumer.accept(false);
    }
  }

  private static void showExamples(Element target, List<Property> examples) {
    HtmlTable table = new HtmlTable(STYLE_CRON_EXAMPLES);

    int row = 0;
    for (Property property : examples) {
      table.setText(row, 0, property.getName());
      table.setText(row, 1, property.getValue());

      row++;
    }

    Global.showModalWidget(null, table, target);
  }

  private FileCollector fileCollector;

  RecurringTaskHandler() {
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (BeeUtils.inList(editableWidget.getColumnId(),
        COL_RT_DAY_OF_MONTH, COL_RT_MONTH, COL_RT_DAY_OF_WEEK)) {
      editableWidget.addCellValidationHandler(this);
    }

    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof FileCollector) {
      this.fileCollector = (FileCollector) widget;
      this.fileCollector.bindDnd(getFormView());

    } else if (!BeeUtils.isEmpty(name)) {
      switch (name) {
        case "DomValues":
          if (widget instanceof Flow) {
            Cron.DAY_OF_MONTH.initValueContainer((Flow) widget, this);
          }
          break;
        case "MonthValues":
          if (widget instanceof Flow) {
            Cron.MONTH.initValueContainer((Flow) widget, this);
          }
          break;
        case "DowValues":
          if (widget instanceof Flow) {
            Cron.DAY_OF_WEEK.initValueContainer((Flow) widget, this);
          }
          break;

        case "DomHelp":
          if (widget instanceof HasClickHandlers) {
            initDayOfMonthHelp((HasClickHandlers) widget, widget.getElement());
          }
          break;
        case "MonthHelp":
          if (widget instanceof HasClickHandlers) {
            initMonthHelp((HasClickHandlers) widget, widget.getElement());
          }
          break;
        case "DowHelp":
          if (widget instanceof HasClickHandlers) {
            initDayOfWeekHelp((HasClickHandlers) widget, widget.getElement());
          }
          break;

        case "DomError":
          if (widget instanceof HasHtml) {
            Cron.DAY_OF_MONTH.errorLabel = (HasHtml) widget;
          }
          break;
        case "MonthError":
          if (widget instanceof HasHtml) {
            Cron.MONTH.errorLabel = (HasHtml) widget;
          }
          break;
        case "DowError":
          if (widget instanceof HasHtml) {
            Cron.DAY_OF_WEEK.errorLabel = (HasHtml) widget;
          }
          break;
      }
    }
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    if (getFileCollector() != null && !getFileCollector().isEmpty()) {
      FileUtils.commitFiles(getFileCollector().getFiles(), VIEW_RT_FILES,
          COL_RTF_RECURRING_TASK, result.getId(), COL_RTF_FILE, COL_RTF_CAPTION);
    }

    super.afterInsertRow(result, forced);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = getHeaderView();

    if (header != null && !header.hasCommands()) {
      FaLabel schedule = new FaLabel(FontAwesome.CALENDAR);
      schedule.addStyleName(STYLE_PREFIX + "actionSchedule");
      schedule.setTitle(Localized.getConstants().crmRTActionSchedule());

      schedule.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          IsRow activeRow = getActiveRow();
          if (validateBounds(activeRow)) {
            final long id = activeRow.getId();

            getDateModes(id, new Consumer<List<DateMode>>() {
              @Override
              public void accept(final List<DateMode> dateModes) {

                getOffspring(id, new Queries.RowSetCallback() {
                  @Override
                  public void onSuccess(final BeeRowSet offspring) {

                    isFertile(id, new Consumer<Boolean>() {
                      @Override
                      public void accept(Boolean fertile) {
                        if (getActiveRowId() == id) {
                          timeCube(dateModes, offspring, BeeUtils.isTrue(fertile));
                        }
                      }
                    });
                  }
                });
              }
            });
          }
        }
      });

      header.addCommandItem(schedule);
    }

    for (Cron cron : Cron.values()) {
      cron.refreshValues(getStringValue(cron.source));
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new RecurringTaskHandler();
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    if (getFileCollector() != null) {
      getFileCollector().clear();
    }

    super.onStartNewRow(form, oldRow, newRow);
  }

  @Override
  public Boolean validateCell(CellValidateEvent event) {
    if (event.isCellValidation() && event.isPreValidation()) {
      String source = event.getColumnId();
      if (BeeUtils.isEmpty(source)) {
        return true;
      }

      Cron cron = Cron.getBySource(source);
      if (cron == null) {
        return true;
      }

      if (BeeUtils.isEmpty(event.getNewValue())) {
        cron.clearValues();
        cron.clearErrors();

      } else {
        cron.refreshValues(event.getNewValue());
      }
    }

    return true;
  }

  private FileCollector getFileCollector() {
    return fileCollector;
  }

  private Widget renderMonth(YearMonth ym, List<DayOfMonth> days, boolean fertile) {
    Flow panel = new Flow(STYLE_MONTH_PANEL);

    Label monthLabel = new Label(BeeUtils.joinWords(ym.getYear(),
        Format.renderMonthFullStandalone(ym).toLowerCase()));
    monthLabel.addStyleName(STYLE_MONTH_LABEL);

    panel.add(monthLabel);

    HtmlTable table = new HtmlTable(STYLE_MONTH_TABLE);

    String[] wn = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysNarrow();
    for (int i = 0; i < TimeUtils.DAYS_PER_WEEK; i++) {
      String text = (i == 6) ? wn[0] : wn[i + 1];
      table.setText(0, i, text, STYLE_WEEKDAY_CELL);
    }

    JustDate startOfMonth = ym.getDate();
    int shift = startOfMonth.getDow() - 1;

    for (DayOfMonth day : days) {
      int row = (day.dom + shift - 1) / TimeUtils.DAYS_PER_WEEK + 1;
      int col = day.dow - 1;
      String text = BeeUtils.toString(day.dom);

      if (day.scheduled) {
        CustomDiv widget = new CustomDiv(STYLE_SCHEDULED);
        widget.setText(text);

        if (fertile) {
          final int spawnDate = startOfMonth.getDays() + day.dom - 1;

          widget.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              Global.confirm("spawn", new ConfirmationCallback() {
                @Override
                public void onConfirm() {
                  long rtId = getActiveRowId();

                  if (DataUtils.isId(rtId)) {
                    ParameterList params = CrmKeeper.createArgs(SVC_RT_SPAWN);
                    params.addQueryItem(VAR_RT_ID, rtId);
                    params.addQueryItem(VAR_RT_DATE, spawnDate);

                    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
                      @Override
                      public void onResponse(ResponseObject response) {
                        if (response.hasResponse(BeeRowSet.class)) {
                          BeeRowSet taskData = BeeRowSet.restore(response.getResponseAsString());

                          if (!DataUtils.isEmpty(taskData)) {
                            String message =
                                Localized.getMessages().crmCreatedNewTasks(
                                    taskData.getNumberOfRows());
                            BeeKeeper.getScreen().notifyInfo(message);

                            DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_TASKS);
                          }
                        }
                      }
                    });
                  }
                }
              });
            }
          });
        }

        table.setWidget(row, col, widget, fertile ? STYLE_DAY_SCHEDULED : STYLE_INFERTILE);

      } else {
        table.setText(row, col, text, STYLE_DAY_CELL);
      }

      if (!day.styleNames.isEmpty()) {
        for (String styleName : day.styleNames) {
          table.getCellFormatter().addStyleName(row, col, styleName);
        }
      }
    }

    panel.add(table);
    return panel;
  }

  private void timeCube(List<DateMode> dateModes, BeeRowSet offspring, boolean fertile) {
    JustDate from = getDateValue(COL_RT_SCHEDULE_FROM);
    JustDate until = getDateValue(COL_RT_SCHEDULE_UNTIL);

    CronExpression.Builder builder = new CronExpression.Builder(from, until)
        .dayOfMonth(getStringValue(COL_RT_DAY_OF_MONTH))
        .month(getStringValue(COL_RT_MONTH))
        .dayOfWeek(getStringValue(COL_RT_DAY_OF_WEEK))
        .year(getStringValue(COL_RT_YEAR))
        .workdayTransition(EnumUtils.getEnumByIndex(WorkdayTransition.class,
            getIntegerValue(COL_RT_WORKDAY_TRANSITION)));

    if (!BeeUtils.isEmpty(dateModes)) {
      for (DateMode dateMode : dateModes) {
        builder.rangeMode(dateMode.range, dateMode.mode);
      }
    }

    CronExpression cron = builder.build();

    JustDate min;
    JustDate max;

    if (from == null && until == null) {
      min = TimeUtils.startOfMonth();
      max = TimeUtils.endOfMonth(min, 12);
    } else if (from == null) {
      min = TimeUtils.startOfMonth(until);
      max = until;
    } else if (until == null) {
      min = TimeUtils.max(from, TimeUtils.startOfPreviousMonth(TimeUtils.today()));
      max = TimeUtils.endOfMonth(min, 12);
    } else {
      min = from;
      max = TimeUtils.max(from, until);
    }

    List<JustDate> cronDates = cron.getDates(min, max);

    Multimap<JustDate, BeeRow> tasks = ArrayListMultimap.create();
    if (!DataUtils.isEmpty(offspring)) {
      int dateIndex = offspring.getColumnIndex(COL_START_TIME);

      for (BeeRow row : offspring.getRows()) {
        JustDate start = JustDate.get(row.getDateTime(dateIndex));

        if (TimeUtils.isBetweenInclusiveRequired(start, min, max)) {
          tasks.put(start, row);
        }
      }
    }

    Flow container = new Flow(STYLE_SCHEDULE_PANEL);

    Flow calendarPanel = new Flow(STYLE_CALENDAR_PANEL);

    JustDate today = TimeUtils.today();

    int monthCount = TimeUtils.monthDiff(min, max);
    for (int i = 0; i <= monthCount; i++) {
      YearMonth ym = new YearMonth(min).shiftMonth(i);

      int domMin = (i == 0) ? min.getDom() : 1;
      int domMax = (i == monthCount) ? max.getDom() : TimeUtils.monthLength(ym);

      List<DayOfMonth> days = Lists.newArrayList();
      for (int dom = domMin; dom <= domMax; dom++) {
        JustDate date = new JustDate(ym.getYear(), ym.getMonth(), dom);
        ScheduleDateMode mode = cron.getDateMode(date);
        boolean scheduled = cronDates.contains(date);

        DayOfMonth dayOfMonth = new DayOfMonth(date, scheduled);

        if (today.equals(date)) {
          dayOfMonth.addStyleName(STYLE_TODAY);
        }
        if (mode == ScheduleDateMode.NON_WORK) {
          dayOfMonth.addStyleName((dayOfMonth.dow <= 5) ? STYLE_HOLIDAY : STYLE_WEEKEND);
        } else if (mode != null) {
          dayOfMonth.addStyleName(STYLE_PREFIX + mode.name().toLowerCase());
        }

        if (tasks.containsKey(date)) {
          dayOfMonth.addTasks(tasks.get(date));
        }

        days.add(dayOfMonth);
      }

      Widget monthWidget = renderMonth(ym, days, fertile);
      calendarPanel.add(monthWidget);
    }

    container.add(calendarPanel);

    Flow infoPanel = new Flow(STYLE_INFO_PANEL);

    List<Property> info = cron.getInfo();
    for (Property p : info) {
      Label infoLabel = new Label(p.getValue());
      infoLabel.addStyleName(STYLE_INFO_LABEL);
      infoLabel.setTitle(p.getName());

      infoPanel.add(infoLabel);
    }

    container.add(infoPanel);

    StyleUtils.setMaxWidth(container, BeeKeeper.getScreen().getWidth() * 8 / 10);
    StyleUtils.setMaxHeight(container, BeeKeeper.getScreen().getHeight() * 8 / 10);

    String caption = BeeUtils.joinWords(getStringValue(COL_SUMMARY),
        DateRange.closed(min, max));
    Global.showModalWidget(caption, container);
  }

  private String updateCronValue(String source, int value, boolean selected) {
    String oldValue = getStringValue(source);
    String newValue;

    String s = BeeUtils.toString(value);

    if (selected) {
      newValue = CronExpression.appendItem(oldValue, s);
    } else {
      newValue = CronExpression.removeItem(oldValue, s);
    }

    if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
      getActiveRow().setValue(getDataIndex(source), newValue);
      getFormView().refreshBySource(source);
    }

    return newValue;
  }

  private boolean validateBounds(IsRow row) {
    if (row == null) {
      return false;
    }

    JustDate from = getDateValue(COL_RT_SCHEDULE_FROM);
    if (from == null) {
      getFormView().notifyWarning(Localized.getConstants().valueRequired());
      focusSource(COL_RT_SCHEDULE_FROM);
      return false;
    }

    JustDate until = getDateValue(COL_RT_SCHEDULE_UNTIL);
    if (until != null && TimeUtils.isMore(from, until)) {
      getFormView().notifyWarning(Localized.getConstants().invalidRange(),
          BeeUtils.joinWords(from, until));
      focusSource(COL_RT_SCHEDULE_UNTIL);
      return false;
    }

    return true;
  }
}
