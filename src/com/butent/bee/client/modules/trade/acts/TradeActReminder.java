package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.ReminderDialog;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import java.util.*;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TRADE_ACT;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.VIEW_TRADE_ACT_REMINDERS;

public class TradeActReminder extends ReminderDialog {

    private TextLabel recipientLabel;
    private Flow recipientPanel;
    private Relation recipientRelation;
    private UnboundSelector recipientSelector;

    private IsRow reminderRow;
    private Long tradeActId;

    TradeActReminder() {
        super();
        super.addCloseHandler(e -> loadData(null));
        recipientRelation.disableNewRow();
    }

    @Override
    protected void afterButtonPanelRender(Button create, Button update, Button suspend, Button cancel) {
        renderRecipientPanel(create.getParent());
        create.addClickHandler(e -> performCreateReminder());
        update.addClickHandler(e -> performUpdateReminder());
        suspend.addClickHandler(e -> performSuspendReminder());
    }

    @Override
    public void onDialogActionClicked() {
        loadData(e -> super.onDialogActionClicked());
    }

    void loadData(Queries.RowSetCallback callback) {
        Queries.getRowSet(TradeActConstants.VIEW_TRADE_ACT_REMINDERS, null,
                Filter.equals(COL_TRADE_ACT, tradeActId), rs -> {
                    reminderRow = !DataUtils.isEmpty(rs) ? rs.getRow(0) : null;
                    setReminderDT(getReminderDate());
                    setReminderNotes(getContactEmail());
                    recipientSelector.setValue(getReminderUser() ,true);
                    createButton.setVisible(!isCreated());
                    updateButton.setVisible(isCreated());
                    suspendButton.setVisible(isActive());
                    markAsActive(isActive());

                    if (callback != null) {
                        callback.onSuccess(rs);
                    }
                });
    }

    public String getContactEmail() {
        return reminderRow != null
                ? Data.getString(TradeActConstants.VIEW_TRADE_ACT_REMINDERS, reminderRow, ClassifierConstants.COL_EMAIL)
                : "";
    }

    public DateTime getReminderDate() {
        return reminderRow != null
                ? Data.getDateTime(TradeActConstants.VIEW_TRADE_ACT_REMINDERS, reminderRow,
                AdministrationConstants.COL_REMINDER_DATE)
                : null;
    }


    public Long getReminderUser() {
        Long userId = reminderRow != null
                ? Data.getLong(TradeActConstants.VIEW_TRADE_ACT_REMINDERS, reminderRow, COL_USER_REMINDER_USER)
                : null;

        return DataUtils.isId(userId) ? userId : BeeKeeper.getUser().getUserId();
    }

    public boolean isActive() {
        return reminderRow != null && !Data.isNull(TradeActConstants.VIEW_TRADE_ACT_REMINDERS, reminderRow,
                AdministrationConstants.COL_USER_REMINDER_ACTIVE);
    }

    public boolean isCreated() {
        return reminderRow != null && !DataUtils.isNewRow(reminderRow);
    }

    public void setTradeActId(Long tradeActId) {
        Assert.notNull(tradeActId);
        this.tradeActId = tradeActId;
    }

    private Map<String, String> collectData() {
        Map<String, String> data = new LinkedHashMap<>();

        data.put(COL_TRADE_ACT, BeeUtils.toString(tradeActId));
        data.put(COL_REMINDER_DATE, BeeUtils.toString(getReminderDT().getTime()));
        data.put(COL_USER_REMINDER_USER, BeeUtils.toString(recipientSelector.getRelatedId()));
        data.put(COL_USER_REMINDER_ACTIVE, BeeConst.STRING_TRUE);

        return data;
    }

    private void performCreateReminder() {
        if (getReminderDT() == null || getReminderDT().getTime() < System.currentTimeMillis()) {
            ReminderDialog.showDateError();
            return;
        }

        Map<String, String> data = collectData();

        Queries.insert(VIEW_TRADE_ACT_REMINDERS, Data.getColumns(VIEW_TRADE_ACT_REMINDERS, new ArrayList<>(data.keySet())),
                new ArrayList<>(data.values()), null, row -> {
            close();
            notifyReminderCreated();
        });
    }

    private void performSuspendReminder() {
        Queries.update(VIEW_TRADE_ACT_REMINDERS, Filter.equals(COL_TRADE_ACT, tradeActId),
                COL_USER_REMINDER_ACTIVE, (String) null, r -> {
                    close();
                    notifyReminderSuspend();
                });
    }

    private void performUpdateReminder() {
        if (getReminderDT() == null || getReminderDT().getTime() < System.currentTimeMillis()) {
            ReminderDialog.showDateError();
            return;
        }

        Map<String, String> data = collectData();

        loadData(rs -> {
            IsRow row = !DataUtils.isEmpty(rs) ? rs.getRow(0) : null;

            if (row != null) {
                data.remove(COL_TRADE_ACT);

                Queries.update(VIEW_TRADE_ACT_REMINDERS, Filter.equals(COL_TRADE_ACT, tradeActId),
                        new ArrayList<>(data.keySet()), new ArrayList<>(data.values()), r -> {
                            close();
                            notifyReminderCreated();
                });
            } else {
                Queries.insert(VIEW_TRADE_ACT_REMINDERS, Data.getColumns(VIEW_TRADE_ACT_REMINDERS, new ArrayList<>(data.keySet())),
                        new ArrayList<>(data.values()), null, r -> {
                            close();
                            notifyReminderCreated();
                        });
            }
        });
    }

    private void renderRecipientPanel(Widget beforeWidget) {
        recipientPanel = new Flow(STYLE_DIALOG_DT_PANEL);
        recipientLabel = new TextLabel(true);
        recipientRelation = Relation.create(VIEW_USERS,
                Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME, ALS_COMPANY_NAME));
        recipientRelation.disableNewRow();
        recipientSelector = UnboundSelector.create(recipientRelation);
        StyleUtils.setWidth(recipientSelector, 300);

        if (getReminderUIContent() instanceof Flow) {
            Flow uiContent = (Flow) getReminderUIContent();
            int beforeIndex = uiContent.getWidgetIndex(beforeWidget);
            uiContent.insert(recipientPanel, beforeIndex);
        }

        recipientPanel.add(ReminderDialog.renderLabel(
                recipientLabel, Localized.dictionary().recipient(), STYLE_DIALOG_TEXT));
        recipientPanel.add(recipientSelector);
    }
}
