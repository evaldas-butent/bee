package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Queries;
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
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

class RecurringTaskHandler extends AbstractFormInterceptor implements CellValidateEvent.Handler {

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

    private DayOfMonth(JustDate date, boolean scheduled) {
      this.dom = date.getDom();
      this.dow = date.getDow();

      this.scheduled = scheduled;
    }

    private void addStyleName(String styleName) {
      styleNames.add(styleName);
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
  private static final String STYLE_TODAY = STYLE_PREFIX + "today";
  private static final String STYLE_HOLIDAY = STYLE_PREFIX + "holiday";
  private static final String STYLE_WEEKEND = STYLE_PREFIX + "weekend";

  private static final String STYLE_SCHEDULED = STYLE_PREFIX + "scheduled";

  private static final String STYLE_INFO_PANEL = STYLE_PREFIX + "info-panel";
  private static final String STYLE_INFO_LABEL = STYLE_PREFIX + "info-label";

  private static final String STYLE_VALUE_TOGGLE = STYLE_PREFIX + "value-toggle";

  private static final String STYLE_CRON_EXAMPLES = STYLE_PREFIX + "cron-examples";

  private static Field getCronField(String source) {
    switch (source) {
      case COL_RT_DAY_OF_MONTH:
        return Field.DAY_OF_MONTH;
      case COL_RT_MONTH:
        return Field.MONTH;
      case COL_RT_DAY_OF_WEEK:
        return Field.DAY_OF_WEEK;
      default:
        return null;
    }
  }

  private static String getCronSource(Field field) {
    switch (field) {
      case DAY_OF_MONTH:
        return COL_RT_DAY_OF_MONTH;
      case MONTH:
        return COL_RT_MONTH;
      case DAY_OF_WEEK:
        return COL_RT_DAY_OF_WEEK;
      default:
        return null;
    }
  }

  private static int getCronValueShift(Field field) {
    switch (field) {
      case DAY_OF_MONTH:
      case MONTH:
      case DAY_OF_WEEK:
        return 1;
      default:
        return 0;
    }
  }

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
  private static Widget renderMonth(YearMonth ym, List<DayOfMonth> days) {
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

    int shift = ym.getDate().getDow() - 1;

    for (DayOfMonth day : days) {
      int row = (day.dom + shift - 1) / TimeUtils.DAYS_PER_WEEK + 1;
      int col = day.dow - 1;
      String text = BeeUtils.toString(day.dom);

      if (day.scheduled) {
        CustomDiv widget = new CustomDiv(STYLE_SCHEDULED);
        widget.setText(text);

        table.setWidget(row, col, widget, STYLE_DAY_SCHEDULED);

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

  private Flow dayOfMonthContainer;
  private Flow monthContainer;
  private Flow dayOfWeekContainer;

  private HasHtml dayOfMonthErrorLabel;

  private HasHtml monthErrorLabel;

  private HasHtml dayOfWeekErrorLabel;

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
            dayOfMonthContainer = (Flow) widget;
            initDayOfMonthContainer();
          }
          break;
        case "MonthValues":
          if (widget instanceof Flow) {
            monthContainer = (Flow) widget;
            initMonthContainer();
          }
          break;
        case "DowValues":
          if (widget instanceof Flow) {
            dayOfWeekContainer = (Flow) widget;
            initDayOfWeekContainer();
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
            dayOfMonthErrorLabel = (HasHtml) widget;
          }
          break;
        case "MonthError":
          if (widget instanceof HasHtml) {
            monthErrorLabel = (HasHtml) widget;
          }
          break;
        case "DowError":
          if (widget instanceof HasHtml) {
            dayOfWeekErrorLabel = (HasHtml) widget;
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
              public void accept(List<DateMode> input) {
                if (getActiveRowId() == id) {
                  timeCube(input);
                }
              }
            });
          }
        }
      });

      header.addCommandItem(schedule);
    }

    refreshCronValues(Field.DAY_OF_MONTH);
    refreshCronValues(Field.MONTH);
    refreshCronValues(Field.DAY_OF_WEEK);
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
    LogUtils.getRootLogger().debug(event.getValidationOrigin(), event.getValidationPhase(),
        event.getNewValue(), event.sameValue());

    if (event.isCellValidation() && event.isPreValidation()) {
      String source = event.getColumnId();
      if (BeeUtils.isEmpty(source)) {
        return true;
      }

      Field field = getCronField(source);
      if (field == null) {
        return true;
      }

      if (BeeUtils.isEmpty(event.getNewValue())) {
        clearCronValues(field);
        
        HasHtml errorLabel = getCronErrorLabel(field);
        if (errorLabel != null) {
          errorLabel.setText(BeeConst.STRING_EMPTY);
        }
        
      } else {
        refreshCronValues(field, event.getNewValue());
      }
    }

    return true;
  }

  private void clearCronValues(Field field) {
    refreshCronValues(field, Collections.<Integer> emptySet());
  }

  private HasHtml getCronErrorLabel(Field field) {
    switch (field) {
      case DAY_OF_MONTH:
        return dayOfMonthErrorLabel;
      case MONTH:
        return monthErrorLabel;
      case DAY_OF_WEEK:
        return dayOfWeekErrorLabel;
      default:
        return null;
    }
  }

  private Consumer<String> getCronFailureHandler(Field field) {
    final HasHtml errorLabel = getCronErrorLabel(field);
    if (errorLabel == null) {
      return null;
    }
    
    errorLabel.setText(BeeConst.STRING_EMPTY);
    
    return new Consumer<String>() {
      @Override
      public void accept(String error) {
        errorLabel.setText(BeeUtils.join(CronExpression.getItemSeparator(),
            errorLabel.getText(), error));
      }
    };
  }

  private Flow getCronValueContainer(Field field) {
    switch (field) {
      case DAY_OF_MONTH:
        return dayOfMonthContainer;
      case MONTH:
        return monthContainer;
      case DAY_OF_WEEK:
        return dayOfWeekContainer;
      default:
        return null;
    }
  }

  private FileCollector getFileCollector() {
    return fileCollector;
  }

  private void initDayOfMonthContainer() {
    for (int i = 1; i <= 31; i++) {
      String text = BeeUtils.toString(i);
      Toggle toggle = new Toggle(text, text, STYLE_VALUE_TOGGLE);

      initToggle(toggle, i, COL_RT_DAY_OF_MONTH);
      dayOfMonthContainer.add(toggle);
    }
  }

  private void initDayOfWeekContainer() {
    for (int i = 1; i <= TimeUtils.DAYS_PER_WEEK; i++) {
      String text = BeeUtils.proper(Format.renderDayOfWeek(i));
      Toggle toggle = new Toggle(text, text, STYLE_VALUE_TOGGLE);

      initToggle(toggle, i, COL_RT_DAY_OF_WEEK);
      dayOfWeekContainer.add(toggle);
    }
  }

  private void initMonthContainer() {
    for (int i = 1; i <= 12; i++) {
      String text = BeeUtils.proper(Format.renderMonthFullStandalone(i));
      Toggle toggle = new Toggle(text, text, STYLE_VALUE_TOGGLE);

      initToggle(toggle, i, COL_RT_MONTH);
      monthContainer.add(toggle);
    }
  }

  private void initToggle(final Toggle toggle, final int value, final String source) {
    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        updateCronValue(source, value, toggle.isDown());
      }
    });
  }
  
  private void refreshCronValues(Field field) {
    refreshCronValues(field, getStringValue(getCronSource(field)));
  }

  private void refreshCronValues(Field field, Set<Integer> values) {
    Flow container = getCronValueContainer(field);
    if (container != null && !container.isEmpty()) {
      int shift = getCronValueShift(field);

      for (int i = 0; i < container.getWidgetCount(); i++) {
        Widget widget = container.getWidget(i);
        if (widget instanceof Toggle) {
          ((Toggle) widget).setDown(values.contains(i + shift));
        }
      }
    }
  }

  private void refreshCronValues(Field field, String input) {
    Set<Integer> values = CronExpression.parseSimpleValues(field, input,
        getCronFailureHandler(field));
    refreshCronValues(field, values);
  }

  private void timeCube(List<DateMode> dateModes) {
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

        days.add(dayOfMonth);
      }

      Widget monthWidget = renderMonth(ym, days);
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

  private void updateCronValue(String source, int value, boolean selected) {
    String oldValue = getStringValue(source);
    List<String> list = CronExpression.split(getStringValue(source));

    String s = BeeUtils.toString(value);
    boolean changed = false;

    String newValue = null;

    if (selected) {
      if (!list.contains(s)) {
        newValue = BeeUtils.join(CronExpression.getItemSeparator(), oldValue, s);
        changed = true;
      }

    } else {
      if (list.contains(s)) {
        list.remove(s);
        newValue = list.isEmpty() ? null : BeeUtils.join(CronExpression.getItemSeparator(), list);
        changed = true;
      }
    }

    if (changed) {
      getActiveRow().setValue(getDataIndex(source), newValue);
      getFormView().refreshBySource(source);
    }
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
