package com.butent.bee.client.modules.transport;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.JsonUtils;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CargoPlaceUnboundForm extends PrintFormInterceptor {

  private Map<String, UnboundSelector> unboundWidgets = new HashMap<>();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    for (String col : new String[] {
        ClassifierConstants.COL_COUNTRY, ClassifierConstants.COL_CITY}) {

      if (widget instanceof UnboundSelector && BeeUtils.isSuffix(name, col)) {
        unboundWidgets.put(name, (UnboundSelector) widget);
      }
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoPlaceUnboundForm();
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    if (!checkDates()) {
      event.consume();
      return;
    }
    event.getColumns().add(Data.getColumn(getViewName(), ALS_CARGO_HANDLING_NOTES));
    event.getValues().add(toJson());
    super.onReadyForInsert(listener, event);
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    if (!checkDates()) {
      event.consume();
      return;
    }
    String oldValue = getStringValue(ALS_CARGO_HANDLING_NOTES);
    String newValue = toJson();

    if (!Objects.equals(oldValue, newValue)) {
      event.getColumns().add(Data.getColumn(getViewName(), ALS_CARGO_HANDLING_NOTES));
      event.getOldValues().add(oldValue);
      event.getNewValues().add(newValue);
    }
    super.onSaveChanges(listener, event);
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand) {
    fromJson(row.getString(form.getDataIndex(ALS_CARGO_HANDLING_NOTES)));
    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    fromJson(newRow.getString(form.getDataIndex(ALS_CARGO_HANDLING_NOTES)));
    super.onStartNewRow(form, oldRow, newRow);
  }

  protected UnboundSelector getUnboundWidget(String col) {
    return unboundWidgets.get(col);
  }

  private boolean checkDates() {
    DateTime start = getDateTimeValue(ALS_LOADING_DATE);
    DateTime end = getDateTimeValue(ALS_UNLOADING_DATE);

    if (TimeUtils.isMeq(start, end)) {
      getFormView().notifyWarning(Localized.dictionary().invalidDateRange() + " "
              + TimeUtils.nowMinutes().toCompactString(), TimeUtils.renderPeriod(start, end));
      return false;
    }
    return true;
  }

  private void fromJson(String jsonString) {
    if (BeeUtils.isEmpty(jsonString)) {
      for (UnboundSelector widget : unboundWidgets.values()) {
        widget.clearValue();
      }
    } else {
      JSONObject json = JsonUtils.parseObject(jsonString);

      for (String key : unboundWidgets.keySet()) {
        unboundWidgets.get(key).setValue(JsonUtils.getString(json, key));
      }
    }
  }

  private String toJson() {
    JSONObject json = new JSONObject();

    for (String key : unboundWidgets.keySet()) {
      String value = unboundWidgets.get(key).getValue();

      if (!BeeUtils.isEmpty(value)) {
        json.put(key, new JSONString(value));
      }
    }
    return json.toString();
  }
}
