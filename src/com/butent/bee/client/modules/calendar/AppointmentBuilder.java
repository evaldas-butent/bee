package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class AppointmentBuilder extends AbstractFormCallback {

  private class ModalCallback extends InputWidgetCallback {
    @Override
    public String getErrorMessage() {
      if (AppointmentBuilder.this.validate()) {
        return null;
      } else {
        return InputBoxes.SILENT_ERROR;
      }
    }

    @Override
    public void onSuccess() {
      AppointmentBuilder.this.save();
    }
  }

  private class PropWidgetHandler implements ValueChangeHandler<String> {
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
      updateDuration(event.getValue());
    }
  }

  private static final String NAME_SERVICE_TYPE = "ServiceType";
  private static final String NAME_REPAIR_TYPE = "RepairType";
  private static final String NAME_REPAIR_PLACE = "RepairPlace";

  private static final String NAME_HOURS = "Hours";
  private static final String NAME_MINUTES = "Minutes";

  private static final String NAME_COLORS = "Colors";

  private static BeeRowSet attendees = null;
  private static State attState = State.UNKNOWN;

  private static BeeRowSet properties = null;
  private static State propState = State.UNKNOWN;

  private static final List<Long> serviceTypes = Lists.newArrayList();
  private static Long defaultServiceType = null;

  private static final List<Long> repairTypes = Lists.newArrayList();
  private static Long defaultRepairType = null;

  private static final List<Long> repairPlaces = Lists.newArrayList();

  private static BeeRowSet colors = null;
  private static State colorState = State.UNKNOWN;

  static BeeRow createEmptyRow(DateTime start) {
    BeeRow row = RowFactory.createEmptyRow(CalendarKeeper.getAppointmentViewInfo(), true);

    long type = CalendarKeeper.getDefaultAppointmentType();
    if (DataUtils.isId(type)) {
      Data.setValue(VIEW_APPOINTMENTS, row, COL_APPOINTMENT_TYPE, type);
    }
    if (start != null) {
      Data.setValue(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME, start);
    }
    return row;
  }

  private final DateTime originalStart;

  private ModalCallback modalCallback = null;
  private final PropWidgetHandler propWidgetHandler = new PropWidgetHandler();

  private String serviceTypeWidgetId = null;
  private String repairTypeWidgetId = null;
  private String repairPlaceWidgetId = null;

  private String hourWidgetId = null;
  private String minuteWidgetId = null;

  private final TabBar colorWidget = new TabBar("bee-ColorBar-");

  AppointmentBuilder(DateTime originalStart) {
    super();
    this.originalStart = originalStart;

    addColorHandlers();
  }

  @Override
  public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, NAME_SERVICE_TYPE)) {
      setServiceTypeWidgetId(DomUtils.getId(widget));
      if (State.UNKNOWN.equals(propState)) {
        loadProperties();
      } else {
        initPropWidget(widget, serviceTypes, defaultServiceType);
      }
      if (widget instanceof Editor) {
        ((Editor) widget).addValueChangeHandler(propWidgetHandler);
      }

    } else if (BeeUtils.same(name, NAME_REPAIR_TYPE)) {
      setRepairTypeWidgetId(DomUtils.getId(widget));
      if (State.UNKNOWN.equals(propState)) {
        loadProperties();
      } else {
        initPropWidget(widget, repairTypes, defaultRepairType);
      }
      if (widget instanceof Editor) {
        ((Editor) widget).addValueChangeHandler(propWidgetHandler);
      }

    } else if (BeeUtils.same(name, NAME_REPAIR_PLACE)) {
      setRepairPlaceWidgetId(DomUtils.getId(widget));
      if (State.UNKNOWN.equals(attState)) {
        loadAttendees();
      } else {
        initRepairPlaceWidget(widget);
      }

    } else if (BeeUtils.same(name, NAME_HOURS)) {
      setHourWidgetId(DomUtils.getId(widget));
    } else if (BeeUtils.same(name, NAME_MINUTES)) {
      setMinuteWidgetId(DomUtils.getId(widget));

    } else if (BeeUtils.same(name, NAME_COLORS) && widget instanceof HasWidgets) {
      ((HasWidgets) widget).add(colorWidget);
      if (State.UNKNOWN.equals(colorState)) {
        loadColors();
      } else {
        initColorWidget();
      }
    }
  }

  @Override
  public boolean beforeAction(Action action, FormPresenter presenter) {
    switch (action) {
      case SAVE:
        if (validate()) {
          save();
          presenter.handleAction(Action.CLOSE);
        }
        return false;

      case REFRESH:
        loadAttendees();
        loadProperties();
        loadColors();

        getFormView().refresh(false);
        return false;

      default:
        return true;
    }
  }

  @Override
  public FormCallback getInstance() {
    Assert.untouchable();
    return null;
  }

  @Override
  public BeeRowSet getRowSet() {
    BeeRowSet rowSet = new BeeRowSet(VIEW_APPOINTMENTS,
        CalendarKeeper.getAppointmentViewInfo().getColumns());
    rowSet.addRow(createEmptyRow(originalStart));
    return rowSet;
  }

  @Override
  public boolean hasFooter(int rowCount) {
    return false;
  }

  @Override
  public void onStart(FormView form) {
    form.setEditing(true);
  }

  ModalCallback getModalCallback() {
    if (this.modalCallback == null) {
      setModalCallback(new ModalCallback());
    }
    return modalCallback;
  }

  private void addColorHandlers() {
    colorWidget.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        Widget widget = colorWidget.getSelectedWidget();
        if (widget != null) {
          widget.getElement().setInnerHTML(BeeConst.STRING_EMPTY);
        }
      }
    });

    colorWidget.addSelectionHandler(new SelectionHandler<Integer>() {
      public void onSelection(SelectionEvent<Integer> event) {
        Widget widget = colorWidget.getSelectedWidget();
        if (widget != null) {
          widget.getElement().setInnerHTML("x");
        }
      }
    });
  }

  private int getDuration() {
    int result = 0;

    if (!BeeUtils.isEmpty(getHourWidgetId())) {
      Widget widget = getWidget(getHourWidgetId());
      if (widget instanceof Editor) {
        String hours = ((Editor) widget).getNormalizedValue();
        if (BeeUtils.isPositiveInt(hours)) {
          result += BeeUtils.toInt(hours) * 60;
        }
      }
    }

    if (!BeeUtils.isEmpty(getMinuteWidgetId())) {
      Widget widget = getWidget(getMinuteWidgetId());
      if (widget instanceof Editor) {
        String minutes = ((Editor) widget).getNormalizedValue();
        if (BeeUtils.isPositiveInt(minutes)) {
          result += BeeUtils.toInt(minutes);
        }
      }
    }
    return result;
  }

  private String getHourWidgetId() {
    return hourWidgetId;
  }

  private String getMinuteWidgetId() {
    return minuteWidgetId;
  }

  private String getRepairPlaceWidgetId() {
    return repairPlaceWidgetId;
  }

  private String getRepairTypeWidgetId() {
    return repairTypeWidgetId;
  }

  private Long getSelectedId(String widgetId, List<Long> rowIds) {
    Widget widget = getWidget(widgetId);

    if (widget instanceof BeeListBox) {
      int index = ((BeeListBox) widget).getSelectedIndex();
      if (BeeUtils.isIndex(rowIds, index)) {
        return rowIds.get(index);
      }
    }
    return null;
  }

  private String getServiceTypeWidgetId() {
    return serviceTypeWidgetId;
  }

  private Widget getWidget(String id) {
    return DomUtils.getChildQuietly(getFormView().asWidget(), id);
  }

  private void initColorWidget() {
    if (colorWidget.getItemCount() > 0) {
      colorWidget.clear();
    }

    if (colors == null) {
      return;
    }
    String viewName = colors.getViewName();

    for (BeeRow row : colors.getRows()) {
      String bc = Data.getString(viewName, row, COL_BACKGROUND);
      String fc = Data.getString(viewName, row, COL_FOREGROUND);

      Html color = new Html();
      color.getElement().getStyle().setBackgroundColor(bc);
      if (!BeeUtils.isEmpty(fc)) {
        color.getElement().getStyle().setColor(fc);
      }
      colorWidget.addItem(color);
    }

    if (colorWidget.getItemCount() > 0) {
      colorWidget.selectTab(0);
    }
  }

  private void initPropWidget(Widget widget, List<Long> rowIds, Long def) {
    if (widget instanceof BeeListBox && !rowIds.isEmpty()) {
      BeeListBox listBox = (BeeListBox) widget;
      if (listBox.getItemCount() > 0) {
        listBox.clear();
      }

      for (long id : rowIds) {
        BeeRow row = properties.getRowById(id);
        String item = Data.getString(properties.getViewName(), row, COL_NAME);
        listBox.addItem(item);
      }

      if (def != null && rowIds.contains(def)) {
        listBox.setSelectedIndex(rowIds.indexOf(def));
      }
    }
  }

  private void initRepairPlaceWidget(Widget widget) {
    if (widget instanceof BeeListBox && !repairPlaces.isEmpty()) {
      BeeListBox listBox = (BeeListBox) widget;
      if (listBox.getItemCount() > 0) {
        listBox.clear();
      }
      
      String viewName = attendees.getViewName();
      for (long id : repairPlaces) {
        BeeRow row = attendees.getRowById(id);
        String item = BeeUtils.concat(BeeConst.DEFAULT_LIST_SEPARATOR,
            Data.getString(viewName, row, COL_NAME), Data.getString(viewName, row, COL_TYPE_NAME));
        listBox.addItem(item);
      }
    }
  }

  private void insertAttendee(long appId, long attId) {
    List<BeeColumn> columns = Lists.newArrayList(new BeeColumn(ValueType.LONG, COL_APPOINTMENT),
        new BeeColumn(ValueType.LONG, COL_ATTENDEE));
    List<String> values = Lists.newArrayList(BeeUtils.toString(appId), BeeUtils.toString(attId));

    Queries.insert(VIEW_APPOINTMENT_ATTENDEES, columns, values, null);
  }

  private void insertProperty(long appId, long propId) {
    List<BeeColumn> columns = Lists.newArrayList(new BeeColumn(ValueType.LONG, COL_APPOINTMENT),
        new BeeColumn(ValueType.LONG, COL_PROPERTY));
    List<String> values = Lists.newArrayList(BeeUtils.toString(appId), BeeUtils.toString(propId));

    Queries.insert(VIEW_APPOINTMENT_PROPS, columns, values, null);
  }

  private boolean isEmpty(IsRow row, String columnId) {
    return BeeUtils.isEmpty(Data.getString(VIEW_APPOINTMENTS, row, columnId));
  }

  private void loadAttendees() {
    if (State.PENDING.equals(attState)) {
      return;
    }
    attState = State.PENDING;

    Queries.getRowSet(VIEW_ATTENDEES, null, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        attendees = result.clone();

        repairPlaces.clear();
        for (BeeRow row : attendees.getRows()) {
          repairPlaces.add(row.getId());
        }

        if (!BeeUtils.isEmpty(getRepairPlaceWidgetId())) {
          initRepairPlaceWidget(getWidget(getRepairPlaceWidgetId()));
        }
        attState = State.INITIALIZED;
      }
    });
  }

  private void loadColors() {
    if (State.PENDING.equals(colorState)) {
      return;
    }
    colorState = State.PENDING;

    Queries.getRowSet(VIEW_THEME_COLORS, null, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        colors = result.clone();
        initColorWidget();

        colorState = State.INITIALIZED;
      }
    });
  }

  private void loadProperties() {
    if (State.PENDING.equals(propState)) {
      return;
    }
    propState = State.PENDING;

    Queries.getRowSet(VIEW_EXTENDED_PROPERTIES, null, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        properties = result.clone();

        serviceTypes.clear();
        repairTypes.clear();

        defaultServiceType = null;
        defaultRepairType = null;
        
        String viewName = properties.getViewName();
        for (BeeRow row : properties.getRows()) {
          long id = row.getId();

          String groupName = Data.getString(viewName, row, COL_GROUP_NAME);
          boolean isDef = BeeUtils.equals(Data.getLong(viewName, row, COL_DEFAULT_PROPERTY), id);

          if (BeeUtils.context("serv", groupName)) {
            serviceTypes.add(id);
            if (isDef) {
              defaultServiceType = id;
            }

          } else if (BeeUtils.context("rem", groupName)) {
            repairTypes.add(id);
            if (isDef) {
              defaultRepairType = id;
            }
          }
        }

        if (!BeeUtils.isEmpty(getServiceTypeWidgetId())) {
          initPropWidget(getWidget(getServiceTypeWidgetId()), serviceTypes, defaultServiceType);
        }
        if (!BeeUtils.isEmpty(getRepairTypeWidgetId())) {
          initPropWidget(getWidget(getRepairTypeWidgetId()), repairTypes, defaultRepairType);
        }
        propState = State.INITIALIZED;
      }
    });
  }

  private void save() {
    BeeRow row = DataUtils.cloneRow(getFormView().getActiveRow());

    if (isEmpty(row, COL_END_DATE_TIME)) {
      long millis = Data.getDateTime(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME).getTime()
          + getDuration() * TimeUtils.MILLIS_PER_MINUTE;
      Data.setValue(VIEW_APPOINTMENTS, row, COL_END_DATE_TIME, millis);
    }

    int index = colorWidget.getSelectedTab();
    if (BeeConst.isUndef(index)) {
      index = 0;
    }
    if (colors != null && index < colors.getNumberOfRows()) {
      Data.setValue(VIEW_APPOINTMENTS, row, COL_COLOR,
          Data.getLong(colors.getViewName(), colors.getRow(index), COL_COLOR));
    }

    final Long serviceType = getSelectedId(getServiceTypeWidgetId(), serviceTypes);
    final Long repairType = getSelectedId(getRepairTypeWidgetId(), repairTypes);

    final Long repairPlace = getSelectedId(getRepairPlaceWidgetId(), repairPlaces);

    Queries.insert(VIEW_APPOINTMENTS, CalendarKeeper.getAppointmentViewInfo().getColumns(), row,
        new Queries.RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            long appId = result.getId();

            if (DataUtils.isId(serviceType)) {
              insertProperty(appId, serviceType);
            }
            if (DataUtils.isId(repairType)) {
              insertProperty(appId, repairType);
            }

            if (DataUtils.isId(repairPlace)) {
              insertAttendee(appId, repairPlace);
            }

            BeeKeeper.getBus().fireEvent(new RowInsertEvent(VIEW_APPOINTMENTS, result));
            NewAppointmentEvent.fire(result);
          }
        });
  }

  private void setHourWidgetId(String hourWidgetId) {
    this.hourWidgetId = hourWidgetId;
  }

  private void setMinuteWidgetId(String minuteWidgetId) {
    this.minuteWidgetId = minuteWidgetId;
  }

  private void setModalCallback(ModalCallback modalCallback) {
    this.modalCallback = modalCallback;
  }

  private void setRepairPlaceWidgetId(String repairPlaceWidgetId) {
    this.repairPlaceWidgetId = repairPlaceWidgetId;
  }

  private void setRepairTypeWidgetId(String repairTypeWidgetId) {
    this.repairTypeWidgetId = repairTypeWidgetId;
  }

  private void setServiceTypeWidgetId(String serviceTypeWidgetId) {
    this.serviceTypeWidgetId = serviceTypeWidgetId;
  }

  private void updateDuration(String propName) {
    if (BeeUtils.isEmpty(propName) || properties == null) {
      return;
    }

    Integer hours = null;
    Integer minutes = null;

    String viewName = properties.getViewName();
    for (BeeRow row : properties.getRows()) {
      if (BeeUtils.equalsTrim(propName, Data.getString(viewName, row, COL_NAME))) {
        hours = Data.getInteger(viewName, row, COL_HOURS);
        minutes = Data.getInteger(viewName, row, COL_MINUTES);
        break;
      }
    }

    if (BeeUtils.isPositive(hours) || BeeUtils.isPositive(minutes)) {
      if (!BeeUtils.isEmpty(getHourWidgetId())) {
        Widget widget = getWidget(getHourWidgetId());
        if (widget instanceof Editor) {
          String hv = BeeUtils.isPositive(hours) ? hours.toString() : BeeConst.STRING_ZERO;
          ((Editor) widget).setValue(hv);
        }
      }

      if (!BeeUtils.isEmpty(getMinuteWidgetId())) {
        Widget widget = getWidget(getMinuteWidgetId());
        if (widget instanceof Editor) {
          String mv = BeeUtils.isPositive(minutes) ? minutes.toString() : BeeConst.STRING_ZERO;
          ((Editor) widget).setValue(mv);
        }
      }
    }
  }

  private boolean validate() {
    if (!getFormView().validate()) {
      return false;
    }

    IsRow row = getFormView().getActiveRow();
    if (row == null) {
      return false;
    }

    if (isEmpty(row, COL_COMPANY)) {
      getFormView().notifySevere("Įveskite klientą");
      return false;
    }
    if (isEmpty(row, COL_VEHICLE)) {
      getFormView().notifySevere("Įveskite automobilį");
      return false;
    }

    if (isEmpty(row, COL_START_DATE_TIME)) {
      getFormView().notifySevere("Įveskite planuojamą pradžios laiką");
      return false;
    }

    DateTime end = Data.getDateTime(VIEW_APPOINTMENTS, row, COL_END_DATE_TIME);
    if (end == null) {
      if (getDuration() <= 0) {
        getFormView().notifySevere("Įveskite trukmę arba planuojamą pabaigos laiką");
        return false;
      }
    } else if (TimeUtils.isLeq(end,
        Data.getDateTime(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME))) {
      getFormView().notifySevere("Pabaigos laikas turi būti didesnis už pradžios laiką");
      return false;
    }

    return true;
  }
}
