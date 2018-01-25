package com.butent.bee.client.modules.tasks;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.*;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.PredefinedFormat;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

public class RequestReminder extends DialogBox {

  private static final String STYLE_DIALOG = BeeConst.CSS_CLASS_PREFIX + "reminder-dialog";
  private static final String STYLE_DIALOG_INFO_PANEL = STYLE_DIALOG + "-infoPanel";
  private static final String STYLE_DIALOG_INFO_TEXT = STYLE_DIALOG + "-infoText";
  private static final String STYLE_DIALOG_DATE_TEXT = STYLE_DIALOG + "-dateText";
  private static final String STYLE_DIALOG_PANEL = STYLE_DIALOG + "-panel";
  private static final String STYLE_DIALOG_REMIND_BUTTON = STYLE_DIALOG + "-remindButton";
  private static final String STYLE_DIALOG_SUSPEND_BUTTON = STYLE_DIALOG + "-suspendButton";
  private static final String STYLE_DIALOG_COMPONENT = STYLE_DIALOG + "-component";
  private static final String STYLE_ELEMENT_NOT_VISIBLE = STYLE_DIALOG + "-not-visible";
  private static final String STYLE_DIALOG_TEXT = STYLE_DIALOG + "-text";
  private static final String REMINDER_ACTIVE = "bee-reminder-dialog-active";

  private BeeRow reminderDataRow;
  private Long requestID;

  private FaLabel reminderLabel;

  private InputDate dateInput;
  private InputTime timeInput;

  private TextLabel dateTextLabel;

  Flow selectionPanel = new Flow(STYLE_DIALOG_PANEL);

  protected RequestReminder(Long requestID) {
    super(Localized.dictionary().userReminder(), null);

    this.requestID = requestID;
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

    Queries.getRowSet(VIEW_REQUEST_REMINDERS, null, Filter.equals(COL_REQUEST, requestID),
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
    if (DataUtils.isId(requestID)) {
      Queries.getRowSet(VIEW_REQUEST_REMINDERS, null, Filter.equals(COL_REQUEST, requestID),
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

  private Widget generateReminderWidget() {
    Flow mainPanel = new Flow();

    Flow datePanel = new Flow(STYLE_DIALOG_PANEL);
    datePanel.add(createTextLabelWidget(Localized.dictionary().userReminderSendRemind(), STYLE_DIALOG_TEXT));
    createDateTimeWidget();
    datePanel.add(dateInput);
    datePanel.add(timeInput);

    mainPanel.add(datePanel);

    Flow infoPanel = new Flow(STYLE_DIALOG_INFO_PANEL);
    infoPanel.add(createTextLabelWidget(Localized.dictionary().userReminderDataLabel(), STYLE_DIALOG_INFO_TEXT));

    dateTextLabel = new TextLabel(true);
    dateTextLabel.addStyleName(STYLE_DIALOG_DATE_TEXT);
    infoPanel.add(dateTextLabel);

    mainPanel.add(infoPanel);

    calculateReminderTime(reminderDataRow != null
      ? Data.getDateTime(VIEW_REQUEST_REMINDERS, reminderDataRow, COL_REQUEST_REMINDER_DATE)
      : null);

    mainPanel.add(selectionPanel);

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

  private Button createUpdateButton() {
    final Button updateReminderButton = new Button(Localized.dictionary().userReminderUpdate(),
      event -> {
        Long time = dateInput.getDate() == null ? null : calculateDateTimeValue().getTime();

        if (time != null && System.currentTimeMillis() < time) {

          final List<String> columns = Lists.newArrayList(COL_REQUEST_REMINDER_USER, COL_REQUEST_REMINDER_DATE,
            COL_REQUEST_REMINDER_ACTIVE, COL_REQUEST_REMINDER_USER_DATE);

          List<String> values = Lists.newArrayList(BeeUtils.toString(BeeKeeper.getUser().getUserId()),
            BeeUtils.toString(time), BeeConst.STRING_TRUE, BeeConst.STRING_TRUE);

          Queries.update(VIEW_REQUEST_REMINDERS, Filter.equals(COL_REQUEST, requestID),
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
      ? null : reminderDataRow.getDateTime(Data.getColumnIndex(VIEW_REQUEST_REMINDERS, COL_REQUEST_REMINDER_DATE));

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

        final List<BeeColumn> columns = Data.getColumns(VIEW_REQUEST_REMINDERS,
          Lists.newArrayList(COL_REQUEST_REMINDER_USER, COL_REQUEST, COL_REQUEST_REMINDER_DATE,
            COL_REQUEST_REMINDER_ACTIVE, COL_REQUEST_REMINDER_USER_DATE));

        List<String> values = Lists.newArrayList(BeeUtils.toString(BeeKeeper.getUser().getUserId()),
          BeeUtils.toString(requestID), BeeUtils.toString(time), BeeConst.STRING_TRUE, BeeConst.STRING_TRUE);

        Queries.insert(VIEW_REQUEST_REMINDERS, columns, values, null, row -> {
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

    private Button createSuspendReminderButton() {
      final Button suspendReminderButton = new Button(Localized.dictionary().userReminderSuspend(),
        event -> Queries.update(VIEW_REQUEST_REMINDERS, Filter.equals(COL_REQUEST, requestID),
          COL_REQUEST_REMINDER_ACTIVE, BeeConst.STRING_FALSE, result -> {
            close();
            BeeKeeper.getScreen().notifyInfo(Localized.dictionary().userReminderDisabled());
          }));

      suspendReminderButton.addStyleName(STYLE_DIALOG_SUSPEND_BUTTON);
      suspendReminderButton.addStyleName(STYLE_DIALOG_COMPONENT);
      return suspendReminderButton;
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

    if (reminderDataRow != null) {
      email = Data.getString(VIEW_REQUEST_REMINDERS, reminderDataRow, ClassifierConstants.COL_EMAIL);
    }

    return BeeUtils.joinItems(" ",
      Format.render(PredefinedFormat.DATE_FULL, dateTime),
      Format.render(PredefinedFormat.TIME_SHORT, dateTime),
      email);
  }

  private static boolean isActive(BeeRow dataRow) {
    return dataRow != null && BeeUtils.toBoolean(dataRow
      .getString(Data.getColumnIndex(VIEW_REQUEST_REMINDERS, COL_REQUEST_REMINDER_ACTIVE)));
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
}
