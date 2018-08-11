package com.butent.bee.client.modules.documents;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.documents.DocumentConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.CellKind;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputTime;
import com.butent.bee.client.widget.RadioButton;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.PredefinedFormat;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DocumentsReminder extends DialogBox {

  private static final String STYLE_DIALOG = BeeConst.CSS_CLASS_PREFIX + "reminder-dialog";
  private static final String STYLE_DIALOG_INFO_PANEL = STYLE_DIALOG + "-infoPanel";
  private static final String STYLE_DIALOG_INFO_TEXT = STYLE_DIALOG + "-infoText";
  private static final String STYLE_DIALOG_DATE_TEXT = STYLE_DIALOG + "-dateText";
  private static final String STYLE_DIALOG_PANEL = STYLE_DIALOG + "-panel";
  private static final String STYLE_DIALOG_RADIO_TASK = STYLE_DIALOG + "-radioTask";
  private static final String STYLE_DIALOG_REMIND_BUTTON = STYLE_DIALOG + "-remindButton";
  private static final String STYLE_DIALOG_SUSPEND_BUTTON = STYLE_DIALOG + "-suspendButton";
  private static final String STYLE_DIALOG_COMPONENT = STYLE_DIALOG + "-component";
  private static final String STYLE_ELEMENT_NOT_VISIBLE = STYLE_DIALOG + "-not-visible";
  private static final String STYLE_DIALOG_TEXT = STYLE_DIALOG + "-text";
  private static final String REMINDER_ACTIVE = "bee-reminder-dialog-active";

  private BeeRow reminderDataRow;
  private Long documentId;
  private Long company;

  private InputDate dateInput;
  private InputTime timeInput;

  private FaLabel reminderLabel;

  Flow selectionPanel = new Flow(STYLE_DIALOG_PANEL);

  private RadioButton radioMail;
  private RadioButton radioTaskTmpl;

  private TextLabel mailLabel;
  private TextLabel taskTmplLabel;
  private TextLabel executorsLabel;
  private TextLabel dateTextLabel;

  private UnboundSelector mailSelector;
  private UnboundSelector taskTemplateSelector;

  private MultiSelector executors;

  protected DocumentsReminder(Long documentId, Long company) {
    super(Localized.dictionary().userReminder(), null);

    this.documentId = documentId;
    this.company = company;

    reminderLabel = generateReminderLabel();

    addDefaultCloseBox();
    super.addCloseHandler(event -> generateReminderLabel());
  }

  public FaLabel getReminderLabel() {
    return reminderLabel;
  }

  public FaLabel generateReminderLabel() {
    if (reminderLabel == null) {
      reminderLabel = new FaLabel(FontAwesome.BELL_O);
      reminderLabel.setTitle(Localized.dictionary().userReminder());
    }

    Queries.getRowSet(VIEW_DOCUMENT_REMINDERS, null, Filter.equals(COL_DOCUMENT, documentId),
        result -> {
          if (result.isEmpty()) {
            return;
          }

          BeeRow dataRow = result.getRow(0);
          if (dataRow != null && isActive(dataRow)) {
            reminderLabel.addStyleName(REMINDER_ACTIVE);
          } else {
            reminderLabel.removeStyleName(REMINDER_ACTIVE);
          }
        });

    return reminderLabel;
  }

  public void showDialog() {
    if (DataUtils.isId(documentId)) {
      Queries.getRowSet(VIEW_DOCUMENT_REMINDERS, null, Filter.equals(COL_DOCUMENT, documentId),
          result -> {
            if (!result.isEmpty()) {
              reminderDataRow = result.getRow(0);
            }

            setWidget(generateReminderWidget());
            focusOnOpen(getContent());
            center();
      });
    }
  }

  private DateTime calculateDateTimeValue() {
    if (dateInput != null && timeInput != null) {
      if (dateInput.getDate() != null) {
        Long dateInMillis = dateInput.getDate().getTime();

        if (timeInput.getMillis() != null) {
          dateInMillis += timeInput.getMillis();
        }

        return new DateTime(dateInMillis);
      }
    }

    return null;
  }

  private Button createCancelButton() {
    return new Button(Localized.dictionary().userReminderCancel(), event -> close());
  }

  private void createDateTimeWidget() {
    DateTime dataValue = reminderDataRow == null
        ? null : reminderDataRow.getDateTime(Data.getColumnIndex(VIEW_DOCUMENT_REMINDERS,
        COL_DOCUMENT_REMINDER_DATE));

    dateInput = new InputDate();
    dateInput.addStyleName(STYLE_DIALOG_COMPONENT);
    dateInput.setDateTimeFormat(Format.getPredefinedFormat(PredefinedFormat.DATE_SHORT));
    StyleUtils.setWidth(dateInput, 100);

    timeInput = new InputTime();
    timeInput.setStepValue(15);
    timeInput.setMillis(Global.getParameterTime(TaskConstants.PRM_START_OF_WORK_DAY));
    timeInput.addStyleName(STYLE_DIALOG_COMPONENT);

    if (dataValue != null) {
      dateInput.setDate(dataValue.getDate());
      timeInput.setMinutes(TimeUtils.minutesSinceDayStarted(dataValue));
    }

    dateInput.addEditStopHandler(valueChangeEvent -> setTime());
    dateInput.addInputHandler(valueChangeEvent -> calculateReminderTime(calculateDateTimeValue()));

    timeInput.addEditStopHandler(valueChangeEvent -> setTime());
    timeInput.addInputHandler(valueChangeEvent -> calculateReminderTime(calculateDateTimeValue()));
  }

  private Button createReminderButton() {
    final Button reminderButton = new Button(Localized.dictionary().userRemind(), event -> {
      Long time = dateInput.getDate() == null ? null : calculateDateTimeValue().getTime();

      if (time != null && System.currentTimeMillis() < time) {

        if (radioTaskTmpl.isChecked() && executors.getIds().isEmpty()) {
          showExecutorError();
          return;
        }

        final List<BeeColumn> columns = Data.getColumns(VIEW_DOCUMENT_REMINDERS,
            Lists.newArrayList(COL_DOCUMENT_REMINDER_ISTASK, COL_DOCUMENT_REMINDER_USER,
                COL_DOCUMENT_REMINDER_TASK_TEMPLATE, COL_DOCUMENT, COL_DOCUMENT_REMINDER_EXECUTORS,
                COL_DOCUMENT_REMINDER_DATE, COL_DOCUMENT_REMINDER_ACTIVE,
                COL_DOCUMENT_REMINDER_USER_DATE));

        List<String> values = Lists.newArrayList(
            radioTaskTmpl.isChecked() ? BeeConst.STRING_TRUE : null, mailSelector.getValue(),
            taskTemplateSelector.getValue(), BeeUtils.toString(documentId),
            executors.getIds().isEmpty() ? null : Codec.beeSerialize(executors.getIds()),
            BeeUtils.toString(time), BeeConst.STRING_TRUE, BeeConst.STRING_TRUE);

        Queries.insert(VIEW_DOCUMENT_REMINDERS, columns, values, null, row -> {
          close();
          BeeKeeper.getScreen().notifyInfo(Localized.dictionary().userReminderCreated());
        });
      } else {
        showDateError();
      }
    });

    reminderButton.addStyleName(STYLE_DIALOG_REMIND_BUTTON);
    reminderButton.addStyleName(STYLE_DIALOG_COMPONENT);
    return reminderButton;
  }

  private void createChoiceSelectors() {
    Relation mailRel = Relation.create(VIEW_USERS,
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME, ALS_COMPANY_NAME));
    mailRel.disableNewRow();

    mailSelector = UnboundSelector.create(mailRel);
    StyleUtils.setWidth(mailSelector, 300);

    Relation taskTemplateRel = Relation.create(TaskConstants.VIEW_TASK_TEMPLATES,
        Lists.newArrayList(TaskConstants.COL_TASK_TEMPLATE_NAME));
    taskTemplateRel.disableNewRow();

    taskTemplateSelector = UnboundSelector.create(taskTemplateRel);
    taskTemplateSelector.addSelectorHandler(event -> {

      JustDate reminderDate = dateInput.getDate();
      JustDate date = reminderDate == null ? new JustDate() : reminderDate;

      Filter filter = Filter.and(Filter.or(Filter.isNull(COL_START_TIME),
          Filter.compareWithValue(COL_START_TIME, Operator.LE, new DateValue(date))),
          Filter.or(Filter.isNull(COL_FINISH_TIME), Filter.compareWithValue(COL_FINISH_TIME,
              Operator.GE, new DateValue(date))), Filter.or(Filter.equals(COL_COMPANY, company),
              Filter.isNull(COL_COMPANY)));

      event.getSelector().setAdditionalFilter(filter);
    });

    StyleUtils.setWidth(taskTemplateSelector, 300);
    DomUtils.setPlaceholder(taskTemplateSelector, Localized.dictionary().actionSelect());

    executors = MultiSelector.autonomous(VIEW_USERS, Arrays.asList(COL_FIRST_NAME, COL_LAST_NAME));
    StyleUtils.setWidth(executors, 300);
    executors.setIds(BeeUtils.toString(BeeKeeper.getUser().getUserId()));
  }

  private Button createSuspendReminderButton() {
    final Button suspendReminderButton = new Button(Localized.dictionary().userReminderSuspend(),
        event -> Queries.update(VIEW_DOCUMENT_REMINDERS, Filter.equals(COL_DOCUMENT, documentId),
            COL_DOCUMENT_REMINDER_ACTIVE, BeeConst.STRING_FALSE, result -> {
              close();
              BeeKeeper.getScreen().notifyInfo(Localized.dictionary().userReminderDisabled());
            }));

    suspendReminderButton.addStyleName(STYLE_DIALOG_SUSPEND_BUTTON);
    suspendReminderButton.addStyleName(STYLE_DIALOG_COMPONENT);
    return suspendReminderButton;
  }

  private Button createUpdateButton() {
    final Button updateReminderButton = new Button(Localized.dictionary().userReminderUpdate(),
        event -> {
          Long time = dateInput.getDate() == null ? null : calculateDateTimeValue().getTime();

          if (time != null && System.currentTimeMillis() < time) {

            if (radioTaskTmpl.isChecked() && executors.getIds().isEmpty()) {
              showExecutorError();
              return;
            }

            final List<String> columns = Lists.newArrayList(COL_DOCUMENT_REMINDER_ISTASK,
                COL_DOCUMENT_REMINDER_USER, COL_DOCUMENT_REMINDER_TASK_TEMPLATE,
                COL_DOCUMENT_REMINDER_EXECUTORS, COL_DOCUMENT_REMINDER_DATE,
                COL_DOCUMENT_REMINDER_ACTIVE, COL_DOCUMENT_REMINDER_USER_DATE);

            List<String> values = Lists.newArrayList(
                radioTaskTmpl.isChecked() ? BeeConst.STRING_TRUE : null, mailSelector.getValue(),
                taskTemplateSelector.getValue(),
                executors.getIds().isEmpty() ? null : Codec.beeSerialize(executors.getIds()),
                BeeUtils.toString(time), BeeConst.STRING_TRUE, BeeConst.STRING_TRUE);

            Queries.update(VIEW_DOCUMENT_REMINDERS, Filter.equals(COL_DOCUMENT, documentId),
                columns, values, result -> {
                  close();
                  BeeKeeper.getScreen().notifyInfo(Localized.dictionary().userReminderCreated());
                });
          } else {
            showDateError();
          }
        });

    updateReminderButton.addStyleName(STYLE_DIALOG_REMIND_BUTTON);
    updateReminderButton.addStyleName(STYLE_DIALOG_COMPONENT);
    return updateReminderButton;
  }

  private static TextLabel createTextLabelWidget(String text, String style) {
    TextLabel textLabel = new TextLabel(true);
    textLabel.setText(text);
    textLabel.setStyleName(style);
    return textLabel;
  }

  private void calculateReminderTime(DateTime dateTime) {
    if (dateTime != null) {
      dateTextLabel.setText(formatDate(dateTime));
    }

    if (dateTime != null && dateTime.getTime() > System.currentTimeMillis()) {
      dateTextLabel.getParent().removeStyleName(STYLE_ELEMENT_NOT_VISIBLE);
    } else {
      dateTextLabel.getParent().addStyleName(STYLE_ELEMENT_NOT_VISIBLE);
    }
  }

  private String formatDate(DateTime dateTime) {
    String email = null;

    if (reminderDataRow != null && radioMail.isChecked()) {
      email = Data.getString(VIEW_DOCUMENT_REMINDERS, reminderDataRow, COL_EMAIL);
    }

    return BeeUtils.joinItems(" ",
        Format.render(PredefinedFormat.DATE_FULL, dateTime),
        Format.render(PredefinedFormat.TIME_SHORT, dateTime),
        email);
  }

  private Widget generateReminderWidget() {
    Flow mainPanel = new Flow();

    radioMail = new RadioButton(null, Localized.dictionary().userReminderSendRemind());
    radioTaskTmpl = new RadioButton(null, Localized.dictionary().crmTodoCreateTask());
    radioTaskTmpl.addStyleName(STYLE_DIALOG_RADIO_TASK);

    radioMail.addValueChangeHandler(valueChangeEvent -> showSelectorByRadioValue());
    radioTaskTmpl.addValueChangeHandler(valueChangeEvent -> showSelectorByRadioValue());

    if (reminderDataRow != null && !Data.isNull(VIEW_DOCUMENT_REMINDERS, reminderDataRow,
        COL_DOCUMENT_REMINDER_ISTASK)) {
      radioTaskTmpl.setChecked(true);
    } else {
      radioMail.setChecked(true);
    }

    Flow radioPanel = new Flow(STYLE_DIALOG_PANEL);
    radioPanel.add(radioMail);
    radioPanel.add(radioTaskTmpl);

    mainPanel.add(radioPanel);

    Flow datePanel = new Flow(STYLE_DIALOG_PANEL);
    datePanel.add(createTextLabelWidget(Localized.dictionary().userReminderSendRemind(),
        STYLE_DIALOG_TEXT));
    createDateTimeWidget();
    datePanel.add(dateInput);
    datePanel.add(timeInput);

    mainPanel.add(datePanel);

    Flow infoPanel = new Flow(STYLE_DIALOG_INFO_PANEL);
    infoPanel.add(createTextLabelWidget(Localized.dictionary().userReminderDataLabel(),
        STYLE_DIALOG_INFO_TEXT));

    dateTextLabel = new TextLabel(true);
    dateTextLabel.addStyleName(STYLE_DIALOG_DATE_TEXT);
    infoPanel.add(dateTextLabel);

    mainPanel.add(infoPanel);

    calculateReminderTime(reminderDataRow != null
        ? Data.getDateTime(VIEW_DOCUMENT_REMINDERS, reminderDataRow, COL_DOCUMENT_REMINDER_DATE)
        : null);

    mailLabel = createTextLabelWidget(Localized.dictionary().recipient(), STYLE_DIALOG_TEXT);
    taskTmplLabel = createTextLabelWidget(Localized.dictionary().crmTaskTemplate(),
        STYLE_DIALOG_TEXT);
    executorsLabel = createTextLabelWidget(Localized.dictionary().crmTaskExecutors(),
        STYLE_DIALOG_TEXT);
    executorsLabel.addStyleName(StyleUtils.NAME_REQUIRED);

    mainPanel.add(selectionPanel);

    createChoiceSelectors();
    showSelectorByRadioValue();

    Flow buttonsPanel = new Flow(STYLE_DIALOG_PANEL);
    if (reminderDataRow == null) {
      buttonsPanel.add(createReminderButton());
    } else {
      buttonsPanel.add(createUpdateButton());
    }

    boolean active = isActive(reminderDataRow);

    if (active) {
      buttonsPanel.add(createSuspendReminderButton());
    }

    buttonsPanel.add(createCancelButton());

    if (active) {
      reminderLabel.addStyleName(REMINDER_ACTIVE);
    } else {
      reminderLabel.removeStyleName(REMINDER_ACTIVE);
    }

    mainPanel.add(buttonsPanel);

    return mainPanel;
  }

  private static boolean isActive(BeeRow dataRow) {
    return dataRow != null && BeeUtils.toBoolean(dataRow
        .getString(Data.getColumnIndex(VIEW_DOCUMENT_REMINDERS, COL_DOCUMENT_REMINDER_ACTIVE)));
  }

  private void setTime() {
    Long time = timeInput.getMillis() != null ? timeInput.getMillis()
        : Global.getParameterTime(TimeUtils.isToday(calculateDateTimeValue().getDateTime())
        ? TaskConstants.PRM_END_OF_WORK_DAY : TaskConstants.PRM_START_OF_WORK_DAY);

    timeInput.setMillis(time);

    setTimeToDateTimeInput(time);
  }

  private void setTimeToDateTimeInput(Long time) {
    if (Objects.nonNull(time)) {
      DateTime dateTime = TimeUtils.toDateTimeOrNull(time);

      if (dateTime != null) {
        DateTime value = calculateDateTimeValue();
        timeInput.setMinutes(TimeUtils.minutesSinceDayStarted(value));
        dateInput.setDate(value.getDate());
      }
    }

    calculateReminderTime(calculateDateTimeValue());
  }

  private static void showDateError() {
    Global.showError(Localized.dictionary().error(), Collections.singletonList(
        Localized.dictionary().userReminderSendRemindDateError()));
  }

  private static void showExecutorError() {
    Global.showError(Localized.dictionary().error(), Collections.singletonList(
        Localized.dictionary().fieldRequired(Localized.dictionary().crmTaskExecutors())));
  }

  private void showSelectorByRadioValue() {
    selectionPanel.clear();

    calculateReminderTime(calculateDateTimeValue());

    if (radioMail.isChecked()) {
      Flow mailPanel = new Flow(STYLE_DIALOG_PANEL);
      mailPanel.add(mailLabel);
      mailPanel.add(mailSelector);

      if (reminderDataRow != null) {
        Long userId = Data.getLong(VIEW_DOCUMENT_REMINDERS, reminderDataRow,
            COL_DOCUMENT_REMINDER_USER);
        if (DataUtils.isId(userId)) {
          mailSelector.setValue(userId, true);
        }
      }

      selectionPanel.add(mailPanel);
    }

    if (radioTaskTmpl.isChecked()) {
      Flow tasksPanel = new Flow(STYLE_DIALOG_PANEL);

      if (reminderDataRow != null) {
        Long template = Data.getLong(VIEW_DOCUMENT_REMINDERS, reminderDataRow,
            COL_DOCUMENT_REMINDER_TASK_TEMPLATE);
        if (DataUtils.isId(template)) {
          taskTemplateSelector.setValue(template, true);
        }

        String executorIds = Data.getString(VIEW_DOCUMENT_REMINDERS, reminderDataRow,
            COL_DOCUMENT_REMINDER_EXECUTORS);
        if (!BeeUtils.isEmpty(executorIds)) {
          executors.setIds(Codec.deserializeIdList(executorIds));
        }
      }

      HtmlTable table = new HtmlTable();
      table.setColumnCellKind(0, CellKind.LABEL);

      table.setWidget(0, 0, taskTmplLabel);
      table.setWidget(0, 1, taskTemplateSelector);
      table.setWidget(1, 0, executorsLabel);
      table.setWidget(1, 1, executors);

      tasksPanel.add(table);
      selectionPanel.add(tasksPanel);
    }
  }
}
