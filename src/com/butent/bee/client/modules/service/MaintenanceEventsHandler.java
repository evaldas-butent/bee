package com.butent.bee.client.modules.service;

import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.eventsboard.EventsBoard;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

public class MaintenanceEventsHandler extends EventsBoard {

  private static final Dictionary LC = Localized.dictionary();
  private static final String STYLE_PREFIX = ServiceKeeper.STYLE_PREFIX + "Events-";
  private static final String STYLE_LABEL = STYLE_PREFIX + "label";
  private static final String STYLE_INFO_PANEL = STYLE_PREFIX + "info-panel";
  private static final Set<Action> enabledActions = Sets.newHashSet(Action.ADD, Action.REFRESH);

  private IsRow maintenanceRow;
  private boolean readOnly;
  private boolean canCreate;

  public MaintenanceEventsHandler() {
    super();
    readOnly = !Data.isViewEditable(getEventsDataViewName());
    canCreate = !readOnly && BeeKeeper.getUser().canCreateData(getEventsDataViewName());
  }

  @Override
  protected void afterCreateCellContent(BeeRowSet rs, BeeRow row, Flow widget) {
    Flow infoPanel = new Flow(STYLE_INFO_PANEL);
    infoPanel.addStyleName(StyleUtils.NAME_FLEX_BOX_HORIZONTAL);

    CheckBox customerSentCheckBox = generateCheckBox(infoPanel, rs, row, COL_CUSTOMER_SENT,
        !BeeUtils.unbox(row.getBoolean(rs.getColumnIndex(COL_CUSTOMER_SENT))),
        LC.svcNotifyCustomer());

    if (customerSentCheckBox != null) {
      customerSentCheckBox.addValueChangeHandler(event -> {
            Long contact = Data.getLong(TBL_SERVICE_MAINTENANCE, maintenanceRow, COL_CONTACT);

            if (DataUtils.isId(contact)) {
              row.setValue(Data.getColumnIndex(getEventsDataViewName(), COL_CUSTOMER_SENT),
                  event.getValue());
              ServiceUtils.informClient(row, contact, null);
            } else {
              Global.showError("Nenurodytas kontaktinis asmuo");
              customerSentCheckBox.setChecked(false);
            }
          }
      );
    }
    generateCheckBox(infoPanel, rs, row, COL_SEND_EMAIL, false, LC.svcSendEmail());
    generateCheckBox(infoPanel, rs, row, COL_SEND_SMS, false, LC.svcSendSms());

    CheckBox customerShowCheckBox = generateCheckBox(infoPanel, rs, row, COL_SHOW_CUSTOMER, true,
        LC.svcShowCustomer());

    if (customerShowCheckBox != null) {
      customerShowCheckBox.addValueChangeHandler(event ->
          Queries.update(getEventsDataViewName(), row.getId(),
              COL_SHOW_CUSTOMER, Value.getValue(event.getValue()),
              result -> customerShowCheckBox.setChecked(event.getValue())));
    }
    Flow rowCellTerm = createEventRowCell(infoPanel, COL_TERM, null, false);
    int idxColTerm = rs.getColumnIndex(COL_TERM);
    if (!BeeUtils.isNegative(idxColTerm)) {
      DateTime publishTime = row.getDateTime(idxColTerm);
      if (publishTime != null) {
        rowCellTerm.add(createCellHtmlItem(COL_TERM, BeeUtils.joinWords(LC.svcTerm(),
            Format.renderDate(publishTime))));
      }
    }

    if (!readOnly) {
      Flow rowCellEdit = createEventRowCell(infoPanel, "Edit", null, false);
      FaLabel editLabel = new FaLabel(FontAwesome.EDIT, STYLE_LABEL);
      editLabel.setTitle(LC.rightStateEdit());
      rowCellEdit.add(editLabel);
      editLabel.addClickHandler(
          event -> RowEditor.openForm(FORM_MAINTENANCE_COMMENT,
              Data.getDataInfo(getEventsDataViewName()),
              row, null, new MaintenanceCommentForm(maintenanceRow)));

      if (BeeKeeper.getUser().canDeleteData(getEventsDataViewName())) {
        Flow rowCellDelete = createEventRowCell(infoPanel, "Delete", null, false);
        FaLabel clearLabel = new FaLabel(FontAwesome.TRASH, STYLE_LABEL);
        clearLabel.setTitle(LC.rightStateDelete());
        rowCellDelete.add(clearLabel);
        clearLabel.addClickHandler(event -> Queries.deleteRow(getEventsDataViewName(), row.getId(),
            result -> refresh(true)));

      }
    }
    widget.add(infoPanel);

    infoPanel = new Flow(STYLE_INFO_PANEL);
    infoPanel.addStyleName(StyleUtils.NAME_FLEX_BOX_HORIZONTAL);

    CheckBox repairerSentCheckBox = generateCheckBox(infoPanel, rs, row, COL_REPAIRER_SENT,
        !BeeUtils.unbox(row.getBoolean(rs.getColumnIndex(COL_REPAIRER_SENT))),
        "Informuoti meistrą");

    if (repairerSentCheckBox != null) {
      repairerSentCheckBox.addValueChangeHandler(event -> {
            Long repairer = Data.getLong(TBL_SERVICE_MAINTENANCE, maintenanceRow,
                COL_REPAIRER + COL_COMPANY_PERSON);

            if (DataUtils.isId(repairer)) {
              row.setValue(Data.getColumnIndex(getEventsDataViewName(), COL_REPAIRER_SENT),
                  event.getValue());
              ServiceUtils.informClient(row, null, repairer);
            } else {
              Global.showError("Nenurodytas meistras");
              repairerSentCheckBox.setChecked(false);
            }
          }
      );
    }
    generateCheckBox(infoPanel, rs, row, COL_REPAIRER_SEND_EMAIL, false, LC.svcSendEmail());
    generateCheckBox(infoPanel, rs, row, COL_REPAIRER_SEND_SMS, false, LC.svcSendSms());

    widget.add(infoPanel);
  }

  private CheckBox generateCheckBox(Flow parentFlow, BeeRowSet rs, BeeRow row, String column,
      boolean enabled, String text) {
    Flow rowCell = createEventRowCell(parentFlow, column, null, false);
    int idxCol = rs.getColumnIndex(column);

    if (!BeeUtils.isNegative(idxCol) && BeeKeeper.getUser()
        .isColumnVisible(Data.getDataInfo(getEventsDataViewName()), column)) {
      CheckBox checkBox = new CheckBox();
      checkBox.setChecked(BeeUtils.toBoolean(row.getString(idxCol)));
      checkBox.setEnabled(enabled && BeeKeeper.getUser()
          .canEditColumn(getEventsDataViewName(), column));
      checkBox.setText(text);
      rowCell.add(createCellWidgetItem(column, checkBox));
      return checkBox;
    }
    return null;
  }

  @Override
  protected IdentifiableWidget getAddEventActionWidget() {
    FaLabel label = new FaLabel(FontAwesome.COMMENT_O);
    label.setTitle(Localized.dictionary().crmActionComment());
    return label;
  }

  @Override
  protected String getAddEventFromName() {
    return TBL_MAINTENANCE_COMMENTS;
  }

  @Override
  public Set<Action> getDisabledActions() {
    if (!canCreate) {
      return Sets.newHashSet(Action.ADD);
    }
    return super.getDisabledActions();
  }

  @Override
  protected Set<Action> getEnabledActions() {
    if (canCreate) {
      return enabledActions;
    } else {
      return Sets.newHashSet(Action.REFRESH);
    }
  }

  @Override
  protected Order getEventsDataOrder() {
    return new Order(getPublishTimeColumnName(), false);
  }

  @Override
  protected String getEventsDataViewName() {
    return TBL_MAINTENANCE_COMMENTS;
  }

  @Override
  protected String getEventNoteColumnName() {
    return COL_COMMENT;
  }

  @Override
  protected String getEventTypeColumnName() {
    return COL_EVENT_NOTE;
  }

  @Override
  protected AbstractFormInterceptor getNewEventFormInterceptor() {
    return new MaintenanceCommentForm(maintenanceRow);
  }

  @Override
  protected String getPublisherPhotoColumnName() {
    return ClassifierConstants.COL_PHOTO;
  }

  @Override
  protected String getPublisherFirstNameColumnName() {
    return ALS_PUBLISHER_FIRST_NAME;
  }

  @Override
  protected String getPublisherLastNameColumnName() {
    return ALS_PUBLISHER_LAST_NAME;
  }

  @Override
  protected String getPublishTimeColumnName() {
    return COL_PUBLISH_TIME;
  }

  @Override
  protected String getRelatedColumnName() {
    return COL_SERVICE_MAINTENANCE;
  }

  @Override
  protected String getStylePrefix() {
    return STYLE_PREFIX;
  }

  @Override
  public String getViewKey() {
    return null;
  }

  @Override
  public String getCaption() {
    return LC.svcComments();
  }

  @Override
  protected void setAdditionalStyleToWidget(String name, BeeRowSet rs, BeeRow row, Widget widget) {
    if (BeeUtils.equals(name, CELL_EVENT_TYPE)) {
      int idxColBackground = rs.getColumnIndex(COL_BACKGROUND);

      if (!BeeUtils.isNegative(idxColBackground)) {
        String color = row.getString(idxColBackground);
        if (!BeeUtils.isEmpty(color)) {
          StyleUtils.setBackgroundColor(widget, color);
        }
      }

      int idxColForeground = rs.getColumnIndex(COL_FOREGROUND);

      if (!BeeUtils.isNegative(idxColForeground)) {
        String color = row.getString(idxColForeground);
        if (!BeeUtils.isEmpty(color)) {
          StyleUtils.setColor(widget, color);
        }
      }
    }
  }

  public void setMaintenanceRow(IsRow row) {
    this.maintenanceRow = row;
  }
}