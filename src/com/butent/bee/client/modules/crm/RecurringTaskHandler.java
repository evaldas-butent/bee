package com.butent.bee.client.modules.crm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
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
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
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
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.html.builder.elements.Span;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskStatus;
import com.butent.bee.shared.time.CronExpression;
import com.butent.bee.shared.time.CronExpression.Field;
import com.butent.bee.shared.time.DateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.ScheduleDateMode;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.WorkdayTransition;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.Property;

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
  private static final String STYLE_DAY_HAS_TASKS = STYLE_PREFIX + "day-has-tasks";
  private static final String STYLE_DAY_SCHEDULED = STYLE_PREFIX + "day-scheduled";
  private static final String STYLE_INFERTILE = STYLE_PREFIX + "infertile";

  private static final String STYLE_HAS_TASKS = STYLE_PREFIX + "has-tasks";
  private static final String STYLE_SCHEDULED = STYLE_PREFIX + "scheduled";

  private static final String STYLE_TODAY = STYLE_PREFIX + "today";
  private static final String STYLE_HOLIDAY = STYLE_PREFIX + "holiday";
  private static final String STYLE_WEEKEND = STYLE_PREFIX + "weekend";

  private static final String STYLE_INFO_PANEL = STYLE_PREFIX + "info-panel";
  private static final String STYLE_INFO_LABEL = STYLE_PREFIX + "info-label";

  private static final String STYLE_VALUE_TOGGLE = STYLE_PREFIX + "value-toggle";

  private static final String STYLE_CRON_EXAMPLES = STYLE_PREFIX + "cron-examples";

  private static final String STYLE_FAILURE = STYLE_PREFIX + "failure";

  private static final String STYLE_OFFSPRING_PREFIX = STYLE_PREFIX + "offspring-";
  private static final String STYLE_OFFSPRING_FIRST_NAME = STYLE_OFFSPRING_PREFIX + "firstName";
  private static final String STYLE_OFFSPRING_LAST_NAME = STYLE_OFFSPRING_PREFIX + "lastName";
  private static final String STYLE_OFFSPRING_STATUS = STYLE_OFFSPRING_PREFIX + "status";
  private static final String STYLE_OFFSPRING_DELETE = STYLE_OFFSPRING_PREFIX + "delete";

  private static List<DateMode> getDateModes(BeeRowSet rowSet) {
    List<DateMode> dateModes = Lists.newArrayList();

    int fromIndex = rowSet.getColumnIndex(COL_RTD_FROM);
    int untilIndex = rowSet.getColumnIndex(COL_RTD_UNTIL);
    int modeIndex = rowSet.getColumnIndex(COL_RTD_MODE);

    for (BeeRow row : rowSet.getRows()) {
      JustDate from = row.getDate(fromIndex);
      JustDate until = row.getDate(untilIndex);

      ScheduleDateMode mode =
          EnumUtils.getEnumByIndex(ScheduleDateMode.class, row.getInteger(modeIndex));

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

    return dateModes;
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

  private final Multimap<Integer, BeeRow> offspring = ArrayListMultimap.create();

  private BeeRowSet executors;

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

    if (!BeeUtils.isEmpty(name)) {
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
          if (!validateBounds(activeRow)) {
            return;
          }

          clearOffspring();
          setExecutors(null);

          if (DataUtils.hasId(activeRow)) {
            int updateSize = Queries.update(getViewName(), getFormView().getDataColumns(),
                getFormView().getOldRow(), activeRow, getFormView().getChildrenForUpdate(),
                new RowCallback() {
                  @Override
                  public void onSuccess(BeeRow result) {
                    RowUpdateEvent.fire(BeeKeeper.getBus(), getViewName(), result);
                    showSchedule(result.getId());
                  }
                });

            if (updateSize == 0) {
              showSchedule(activeRow.getId());
            }

          } else {
            List<DateMode> dateModes = Lists.newArrayList();
            timeCube(dateModes, false);
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

  private void addOffspring(BeeRowSet rowSet) {
    int dateIndex = rowSet.getColumnIndex(COL_START_TIME);

    for (BeeRow row : rowSet.getRows()) {
      JustDate start = JustDate.get(row.getDateTime(dateIndex));
      if (start != null) {
        offspring.put(start.getDays(), row);
      }
    }
  }

  private void clearOffspring() {
    if (!offspring.isEmpty()) {
      offspring.clear();
    }
  }

  private BeeRowSet getExecutors() {
    return executors;
  }

  private BeeRow getOffspringById(int dayNumber, Long taskId) {
    if (DataUtils.isId(taskId)) {
      if (offspring.containsKey(dayNumber)) {
        for (BeeRow row : offspring.get(dayNumber)) {
          if (DataUtils.idEquals(row, taskId)) {
            return row;
          }
        }
      }
    }
    return null;
  }

  private String getOffspringLabel(int dayNumber, Long taskId) {
    BeeRow row = getOffspringById(dayNumber, taskId);

    if (row == null) {
      return null;
    } else {
      return DataUtils.join(Data.getDataInfo(VIEW_TASKS), row,
          Lists.newArrayList(ALS_EXECUTOR_FIRST_NAME, ALS_EXECUTOR_LAST_NAME, COL_STATUS),
          BeeConst.STRING_SPACE);
    }
  }

  private void onDayClick(final Element target, final int dayNumber) {
    if (offspring.containsKey(dayNumber)) {
      showOffspring(target, dayNumber);

    } else if (!DataUtils.isEmpty(getExecutors())) {

      String caption = Format.renderDateFull(new JustDate(dayNumber));

      List<String> messages = Lists.newArrayList();

      List<Integer> indexes = Lists.newArrayList(
          getExecutors().getColumnIndex(CommonsConstants.COL_FIRST_NAME),
          getExecutors().getColumnIndex(CommonsConstants.COL_LAST_NAME));

      int size = getExecutors().getNumberOfRows();
      int maxCount = (size > 15) ? 10 : (size + 1);

      int count = 0;
      for (BeeRow row : getExecutors().getRows()) {
        String userName = DataUtils.join(getExecutors().getColumns(), row, indexes,
            BeeConst.STRING_SPACE);
        messages.add(userName);

        count++;
        if (count >= maxCount) {
          messages.add(BeeConst.ELLIPSIS + BeeUtils.bracket(size));
          break;
        }
      }

      if (size > 1) {
        messages.add(Localized.getConstants().crmRTSpawnTasksQuestion());
      } else {
        messages.add(Localized.getConstants().crmRTSpawnTaskQuestion());
      }

      Global.confirm(caption, Icon.QUESTION, messages,
          Localized.getConstants().actionCreate(), Localized.getConstants().actionCancel(),
          new ConfirmationCallback() {
            @Override
            public void onConfirm() {
              long rtId = getActiveRowId();

              if (DataUtils.isId(rtId)) {
                ParameterList params = CrmKeeper.createArgs(SVC_RT_SPAWN);
                params.addQueryItem(VAR_RT_ID, rtId);
                params.addQueryItem(VAR_RT_DAY, dayNumber);

                BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    if (response.hasResponse(BeeRowSet.class)) {
                      BeeRowSet taskData = BeeRowSet.restore(response.getResponseAsString());

                      if (!DataUtils.isEmpty(taskData)) {
                        if (offspring.containsKey(dayNumber)) {
                          offspring.removeAll(dayNumber);
                        }
                        for (BeeRow row : taskData.getRows()) {
                          offspring.put(dayNumber, row);
                        }

                        target.removeClassName(STYLE_SCHEDULED);
                        target.addClassName(STYLE_HAS_TASKS);

                        TableCellElement cellElement = DomUtils.getParentCell(target, false);
                        if (cellElement != null) {
                          cellElement.removeClassName(STYLE_DAY_SCHEDULED);
                          cellElement.addClassName(STYLE_DAY_HAS_TASKS);
                        }

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

      int dayNumber = startOfMonth.getDays() + day.dom - 1;
      boolean hasTasks = offspring.containsKey(dayNumber);

      if (day.scheduled || hasTasks) {
        CustomDiv widget = new CustomDiv(hasTasks ? STYLE_HAS_TASKS : STYLE_SCHEDULED);
        widget.setText(text);
        DomUtils.setDataIndex(widget.getElement(), dayNumber);

        if (fertile || hasTasks) {
          widget.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              Element target = EventUtils.getEventTargetElement(event);
              int number = DomUtils.getDataIndexInt(target);

              if (number > 0) {
                onDayClick(target, number);
              }
            }
          });
        }

        String styleName = hasTasks ? STYLE_DAY_HAS_TASKS
            : (fertile ? STYLE_DAY_SCHEDULED : STYLE_INFERTILE);
        table.setWidget(row, col, widget, styleName);

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

  private void setExecutors(BeeRowSet executors) {
    this.executors = executors;
  }

  private void showOffspring(Element target, final int dayNumber) {
    Collection<BeeRow> data = offspring.get(dayNumber);
    if (BeeUtils.isEmpty(data)) {
      return;
    }

    List<BeeColumn> columns = Data.getColumns(VIEW_TASKS);
    if (BeeUtils.isEmpty(columns)) {
      return;
    }

    int firstNameIndex = DataUtils.getColumnIndex(ALS_EXECUTOR_FIRST_NAME, columns);
    int lastNameIndex = DataUtils.getColumnIndex(ALS_EXECUTOR_LAST_NAME, columns);
    int statusIndex = DataUtils.getColumnIndex(COL_STATUS, columns);

    Flow panel = new Flow(STYLE_OFFSPRING_PREFIX + "panel");

    final HtmlTable table = new HtmlTable(STYLE_OFFSPRING_PREFIX + "table");
    int r = 0;
    int c = 0;

    for (BeeRow task : data) {
      c = 0;
      table.setText(r, c++, task.getString(firstNameIndex), STYLE_OFFSPRING_FIRST_NAME);
      table.setText(r, c++, task.getString(lastNameIndex), STYLE_OFFSPRING_LAST_NAME);

      table.setText(r, c++, EnumUtils.getCaption(TaskStatus.class, task.getInteger(statusIndex)),
          STYLE_OFFSPRING_STATUS);

      FaLabel delete = new FaLabel(FontAwesome.TRASH_O);

      delete.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();

          final Integer eventRow = table.getEventRow(event, false);
          if (eventRow == null) {
            return;
          }

          final long taskId = DomUtils.getDataIndexLong(table.getRow(eventRow));
          if (!DataUtils.isId(taskId)) {
            return;
          }

          Global.confirmDelete(getOffspringLabel(dayNumber, taskId), Icon.WARNING,
              Lists.newArrayList(Localized.getConstants().crmTaskDeleteQuestion()),
              new ConfirmationCallback() {
                @Override
                public void onConfirm() {
                  offspring.remove(dayNumber, getOffspringById(dayNumber, taskId));
                  table.removeRow(eventRow);
                }
              });
        }
      });

      table.setWidgetAndStyle(r, c, delete, STYLE_OFFSPRING_DELETE);

      DomUtils.setDataIndex(table.getRow(r), task.getId());
      r++;
    }

    table.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        TableRowElement rowElement =
            DomUtils.getParentRow(EventUtils.getEventTargetElement(event), true);
        Long taskId = (rowElement == null) ? null : DomUtils.getDataIndexLong(rowElement);

        BeeRow taskRow = getOffspringById(dayNumber, taskId);
        if (taskRow != null) {
          RowEditor.openRow(VIEW_TASKS, taskRow, true);
        }
      }
    });

    panel.add(table);

    String caption = BeeUtils.joinWords(Localized.getConstants().tasks(),
        new JustDate(dayNumber).toString());

    DialogBox dialog = DialogBox.create(caption, STYLE_OFFSPRING_PREFIX + "dialog");
    dialog.setWidget(panel);

    dialog.setAnimationEnabled(true);
    dialog.setHideOnEscape(true);

    dialog.showRelativeTo(target);
  }

  private void showSchedule(final long rtId) {
    ParameterList params = CrmKeeper.createArgs(SVC_RT_GET_SCHEDULING_DATA);
    params.addQueryItem(VAR_RT_ID, rtId);

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (getFormView() == null || !getFormView().asWidget().isAttached()
            || !DomUtils.isVisible(getFormView().getElement())) {
          return;
        }
        if (!DataUtils.idEquals(getActiveRow(), rtId)) {
          return;
        }

        if (response.hasErrors()) {
          response.notify(getFormView());
          return;
        }

        clearOffspring();
        setExecutors(null);

        List<DateMode> dateModes = Lists.newArrayList();

        if (response.hasResponse()) {
          Map<String, String> data = Codec.beeDeserializeMap(response.getResponseAsString());

          if (data.containsKey(CommonsConstants.VIEW_USERS)) {
            setExecutors(BeeRowSet.restore(data.get(CommonsConstants.VIEW_USERS)));
          }

          if (data.containsKey(VIEW_TASKS)) {
            BeeRowSet tasks = BeeRowSet.restore(data.get(VIEW_TASKS));
            if (!DataUtils.isEmpty(tasks)) {
              addOffspring(tasks);
            }
          }

          if (data.containsKey(VIEW_RT_DATES)) {
            BeeRowSet rtDates = BeeRowSet.restore(data.get(VIEW_RT_DATES));
            if (!DataUtils.isEmpty(rtDates)) {
              dateModes.addAll(getDateModes(rtDates));
            }
          }
        }

        timeCube(dateModes, getExecutors() != null);
      }
    });
  }

  private void timeCube(List<DateMode> dateModes, boolean fertile) {
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

      Widget monthWidget = renderMonth(ym, days, fertile);
      calendarPanel.add(monthWidget);
    }

    container.add(calendarPanel);

    if (Global.isDebug()) {
      Flow infoPanel = new Flow(STYLE_INFO_PANEL);

      List<Property> info = cron.getInfo();
      for (Property p : info) {
        Label infoLabel = new Label(p.getValue());
        infoLabel.addStyleName(STYLE_INFO_LABEL);
        infoLabel.setTitle(p.getName());

        infoPanel.add(infoLabel);
      }

      container.add(infoPanel);
    }

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
