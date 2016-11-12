package com.butent.bee.client.modules.service;

import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.eventsboard.EventsBoard;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
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
  private static final Set<Action> enabledActions = Sets.newHashSet(Action.ADD, Action.REFRESH);

  private IsRow maintenanceRow;

  @Override
  protected void afterCreateEventNoteCell(BeeRowSet rs, BeeRow row, Flow widget, Flow cell) {
    Flow rowCellTerm = createEventRowCell(widget, COL_TERM, null, false);
    int idxColTerm = rs.getColumnIndex(COL_TERM);
    if (!BeeUtils.isNegative(idxColTerm)) {
      DateTime publishTime = row.getDateTime(idxColTerm);
      if (publishTime != null) {
        rowCellTerm.add(createCellHtmlItem(COL_TERM, BeeUtils.joinWords(LC.svcTerm(),
            Format.getDefaultDateFormat().format(publishTime))));
      }
    }

    int idxColShow = rs.getColumnIndex(COL_SHOW_CUSTOMER);
    if (!BeeUtils.isNegative(idxColShow)) {
      Flow rowCellShow = createEventRowCell(widget, COL_SHOW_CUSTOMER, null, false);
      String showCustomer = row.getString(idxColShow);
      CheckBox checkBox = new CheckBox();
      checkBox.setChecked(BeeUtils.toBoolean(showCustomer));
      checkBox.setText(LC.svcShowCustomer());
      checkBox.addValueChangeHandler(event -> Queries.update(getEventsDataViewName(), row.getId(),
          COL_SHOW_CUSTOMER, Value.getValue(event.getValue()), new Queries.IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              checkBox.setChecked(Boolean.valueOf(event.getValue()));
            }
          }));
      rowCellShow.add(createCellWidgetItem(COL_SHOW_CUSTOMER, checkBox));
    }


    int idxColSend = rs.getColumnIndex(COL_CUSTOMER_SENT);
    if (!BeeUtils.isNegative(idxColSend)) {
      Flow rowCellSend = createEventRowCell(widget, COL_CUSTOMER_SENT, null, false);
      String customerSent = row.getString(idxColSend);
      CheckBox checkBox = new CheckBox();
      checkBox.setChecked(BeeUtils.toBoolean(customerSent));
      checkBox.setText(LC.svcInform());
      checkBox.addValueChangeHandler(event -> Queries.update(getEventsDataViewName(), row.getId(),
          COL_CUSTOMER_SENT, Value.getValue(event.getValue()), new Queries.IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              checkBox.setChecked(Boolean.valueOf(event.getValue()));
            }
          }));
      rowCellSend.add(createCellWidgetItem(COL_CUSTOMER_SENT, checkBox));
    }

    Flow rowCellEdit = createEventRowCell(widget, "Edit", null, false);
    FaLabel editLabel = new FaLabel(FontAwesome.EDIT, STYLE_LABEL);
    rowCellEdit.add(editLabel);
    editLabel.addClickHandler(
        event -> RowEditor.open(getEventsDataViewName(), row, Opener.MODAL, null));

    Flow rowCellDelete = createEventRowCell(widget, "Delete", null, false);
    FaLabel clearLabel = new FaLabel(FontAwesome.TRASH, STYLE_LABEL);
    rowCellDelete.add(clearLabel);
    clearLabel.addClickHandler(event -> Queries.deleteRow(getEventsDataViewName(), row.getId(),
        new Queries.IntCallback() {
          @Override
          public void onSuccess(Integer result) {
            refresh(true);
          }
        }));
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
  protected Set<Action> getEnabledActions() {
    return enabledActions;
  }

  @Override
  protected Order getEventsDataOrder() {
    Order order = Order.ascending(getPublishTimeColumnName());
    return order;
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

  public void setMaintenanceRow(IsRow row) {
    this.maintenanceRow = row;
  }
}