package com.butent.bee.client;

import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.*;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.PredefinedFormat;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collections;

/**
 * Reminder dialog class
 */
public abstract class ReminderDialog extends DialogBox {
    private static final String STYLE_DIALOG = BeeConst.CSS_CLASS_PREFIX + "reminder-dialog";
    private static final String STYLE_DIALOG_BUTTONS_PANEL = STYLE_DIALOG + "-panel-buttons";
    private static final String STYLE_DIALOG_DATE_LABEL = STYLE_DIALOG + "-label-dt";
    private static final String STYLE_DIALOG_ELEMENT_NOT_VISIBLE = STYLE_DIALOG + "-not-visible";

    private static final String STYLE_DIALOG_INFO_PANEL = STYLE_DIALOG + "-panel-info";
    private static final String STYLE_DIALOG_REMIND_BUTTON = STYLE_DIALOG + "-remindButton";
    private static final String STYLE_DIALOG_SUSPEND_BUTTON = STYLE_DIALOG + "-suspendButton";

    private static final String STYLE_DIALOG_WIDGET = STYLE_DIALOG + "-widget";
    private static final String STYLE_REMINDER_ACTIVE = "bee-reminder-dialog-active";

    private final FaLabel dialogAction;
    private final Flow reminderUIContent = new Flow();

    private final Flow datePanel = new Flow(STYLE_DIALOG_DT_PANEL);
    private final Flow buttonsPanel = new Flow(STYLE_DIALOG_BUTTONS_PANEL);
    private final Flow infoPanel = new Flow(STYLE_DIALOG_INFO_PANEL);

    private final TextLabel labelNotes = new TextLabel(true);
    private final TextLabel labelReminderSend = new TextLabel(true);
    private final TextLabel labelReminderWillSend = new TextLabel(true);
    private final TextLabel labelReminderWillSendDt = new TextLabel(true);

    private final InputDate reminderDate = new InputDate();
    private final InputTime reminderTime = new InputTime();

    protected static final String STYLE_DIALOG_INFO_LABEL = STYLE_DIALOG + "-label-info";
    protected static final String STYLE_DIALOG_DT_PANEL = STYLE_DIALOG + "-panel-dt";
    protected static final String STYLE_DIALOG_TEXT = STYLE_DIALOG + "-text";

    protected final Button cancelButton = new Button(Localized.dictionary().userReminderCancel(), e->close());
    protected final Button createButton = new Button(Localized.dictionary().userRemind());
    protected final Button suspendButton = new Button(Localized.dictionary().userReminderSuspend());
    protected final Button updateButton = new Button(Localized.dictionary().userReminderUpdate());

    public ReminderDialog() {
        super(Localized.dictionary().userReminder(), null);
        dialogAction = new FaLabel(FontAwesome.BELL_O);
        dialogAction.setTitle(this.getCaption());
        dialogAction.addClickHandler(e-> onDialogActionClicked());
        initDialog();
        addDefaultCloseBox();
    }

    public final FaLabel getDialogAction() {
        return this.dialogAction;
    }

    public void showDialog() {
        setWidget(getReminderUIContent());
        focusOnOpen(getContent());
        center();
    }

    public void onDialogActionClicked() {
        showDialog();
    }

    public DateTime getReminderDT() {
        if (reminderDate.getDate() == null) {
            return null;
        }

        long time = reminderDate.getDate().getTime() + BeeUtils.unbox(reminderTime.getMillis());

        return new DateTime(time);
    }

    public Widget getReminderUIContent() {
        return reminderUIContent;
    }

    public void markAsActive(boolean active) {
        getDialogAction().setStyleName(STYLE_REMINDER_ACTIVE, active);
    }

    public void setReminderDT(DateTime dt) {
        if (dt == null) {
            reminderDate.setDate(null);
            reminderTime.setTime(null);
        } else {
            reminderDate.setDate(dt.getDate());
            reminderTime.setMinutes(TimeUtils.minutesSinceDayStarted(dt));
        }

        updateInfoPanel();
    }

    public void setReminderNotes(String notes) {
        labelNotes.setText(notes);
    }

    protected abstract void afterButtonPanelRender(Button create, Button update, Button suspend, Button cancel);

    protected static void notifyReminderCreated() {
        BeeKeeper.getScreen().notifyInfo(Localized.dictionary().userReminderCreated());
    }

    protected static void notifyReminderSuspend() {
        BeeKeeper.getScreen().notifyInfo(Localized.dictionary().userReminderDisabled());
    }

    protected static TextLabel renderLabel(TextLabel textLabel, String label, String styleName) {
        textLabel.setText(label);
        textLabel.setStyleName(styleName);

        return textLabel;
    }

    protected static void showDateError() {
        Global.showError(Localized.dictionary().error(), Collections.singletonList(
                Localized.dictionary().userReminderSendRemindDateError()));
    }

    private void initDialog() {
        reminderUIContent.add(datePanel);
        reminderUIContent.add(infoPanel);
        reminderUIContent.add(buttonsPanel);

        renderDatePanel();
        renderInfoPanel();
        renderButtonsPanel();
    }

    private void renderButtonsPanel() {
        buttonsPanel.add(createButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(suspendButton);
        buttonsPanel.add(cancelButton);

        createButton.addStyleName(STYLE_DIALOG_WIDGET);
        suspendButton.addStyleName(STYLE_DIALOG_WIDGET);
        updateButton.addStyleName(STYLE_DIALOG_WIDGET);


        createButton.addStyleName(STYLE_DIALOG_REMIND_BUTTON);
        suspendButton.addStyleName(STYLE_DIALOG_SUSPEND_BUTTON);
        updateButton.addStyleName(STYLE_DIALOG_REMIND_BUTTON);

        afterButtonPanelRender(createButton, updateButton, suspendButton, cancelButton);
    }

    private void renderDatePanel() {
        datePanel.add(renderLabel(labelReminderSend, Localized.dictionary().userReminderSendRemind(),
                STYLE_DIALOG_TEXT));

        datePanel.add(reminderDate);
        datePanel.add(reminderTime);

        renderReminderDate();
        renderReminderTime();
    }

    private void renderInfoPanel() {
        infoPanel.add(renderLabel(labelReminderWillSend, Localized.dictionary().userReminderDataLabel(),
                STYLE_DIALOG_INFO_LABEL));
        infoPanel.add(renderLabel(labelReminderWillSendDt, "", STYLE_DIALOG_DATE_LABEL));
        infoPanel.add(ReminderDialog.renderLabel(labelNotes, "", STYLE_DIALOG_INFO_LABEL));
    }

    private void renderReminderDate() {
        reminderDate.addStyleName(STYLE_DIALOG_WIDGET);
        reminderDate.setDateTimeFormat(Format.getPredefinedFormat(PredefinedFormat.DATE_SHORT));
        StyleUtils.setWidth(reminderDate, 100);

        reminderDate.addEditStopHandler(e -> updateInfoPanel());
        reminderDate.addInputHandler(e -> updateInfoPanel());
    }

    private void renderReminderTime() {
        reminderTime.setStepValue(15);
        reminderTime.setMillis(Global.getParameterTime(TaskConstants.PRM_END_OF_WORK_DAY));
        reminderTime.addStyleName(STYLE_DIALOG_WIDGET);
        reminderTime.addEditStopHandler(e-> updateInfoPanel());
        reminderTime.addInputHandler(e -> updateInfoPanel());
    }

    private void updateInfoPanel() {
        if (reminderTime.getMillis() == null) {
            reminderTime.setMillis(Global.getParameterTime(TimeUtils.isToday(getReminderDT())
                    ? TaskConstants.PRM_END_OF_WORK_DAY : TaskConstants.PRM_START_OF_WORK_DAY));
        }

        DateTime dt = getReminderDT();

        if (dt == null) {
            infoPanel.addStyleName(STYLE_DIALOG_ELEMENT_NOT_VISIBLE);
            labelReminderWillSendDt.setText("");
        } else {
            infoPanel.setStyleName(STYLE_DIALOG_ELEMENT_NOT_VISIBLE, dt.getTime() < System.currentTimeMillis());
            labelReminderWillSendDt.setText(BeeUtils.joinItems(" ",
                    Format.render(PredefinedFormat.DATE_FULL, dt),
                    Format.render(PredefinedFormat.TIME_SHORT, dt)));
        }
    }
}
