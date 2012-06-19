package com.butent.bee.client.modules.calendar;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.InputTime;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.modules.calendar.event.AppointmentEvent;
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
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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
      AppointmentBuilder.this.save(null);
    }
  }

  private class PropWidgetHandler implements ValueChangeHandler<String> {
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
      updateDuration(event.getValue());
    }
  }
  
  private class ResourceWidgetHandler implements KeyDownHandler, DoubleClickHandler {
    @Override
    public void onDoubleClick(DoubleClickEvent event) {
      event.preventDefault();
      addResources();
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DELETE) {
        event.preventDefault();
        if (event.getSource() instanceof BeeListBox) {
          int index = ((BeeListBox) event.getSource()).getSelectedIndex();
          if (BeeUtils.isIndex(resources, index)) {
            resources.remove(index);
            refreshResourceWidget();
          }
        }
      }
    }
  }

  private static final KeyDownHandler LIST_BOX_CLEANER = new KeyDownHandler() {
    @Override
    public void onKeyDown(KeyDownEvent event) {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DELETE) {
        event.preventDefault();
        ((BeeListBox) event.getSource()).deselect();
      }
    }
  };

  private static final String NAME_SERVICE_TYPE = "ServiceType";
  private static final String NAME_REPAIR_TYPE = "RepairType";

  private static final String NAME_RESOURCES = "Resources";
  private static final String NAME_ADD_RESOURCE = "AddResource";
  private static final String NAME_REMOVE_RESOURCE = "RemoveResource";

  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_START_TIME = "StartTime";
  private static final String NAME_END_DATE = "EndDate";
  private static final String NAME_END_TIME = "EndTime";

  private static final String NAME_HOURS = "Hours";
  private static final String NAME_MINUTES = "Minutes";

  private static final String NAME_COLORS = "Colors";

  private static final String NAME_REMINDER = "Reminder";

  private static final String NAME_BUILD_SEPARATOR = "BuildSeparator";
  private static final String NAME_BUILD = "Build";
  private static final String NAME_BUILD_INFO = "BuildInfo";

  private static final String STYLE_COLOR_BAR_PREFIX = "bee-cal-ColorBar-";
  private static final String STYLE_ADD_RESOURCES = "bee-cal-AddResources";

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

  private final boolean isNew;

  private ModalCallback modalCallback = null;

  private final PropWidgetHandler propWidgetHandler = new PropWidgetHandler();
  private final ResourceWidgetHandler resourceWidgetHandler = new ResourceWidgetHandler();

  private final List<Long> serviceTypes = Lists.newArrayList();
  private Long defaultServiceType = null;

  private final List<Long> repairTypes = Lists.newArrayList();
  private Long defaultRepairType = null;

  private final List<Long> colors = Lists.newArrayList();
  
  private final SetMultimap<Long, Long> serviceResources = HashMultimap.create();
  private final SetMultimap<Long, Long> repairResources = HashMultimap.create();

  private final List<Long> reminderTypes = Lists.newArrayList();

  private final List<Long> resources = Lists.newArrayList();

  private String serviceTypeWidgetId = null;
  private String repairTypeWidgetId = null;

  private String resourceWidgetId = null;

  private String startDateWidgetId = null;
  private String startTimeWidgetId = null;
  private String endDateWidgetId = null;
  private String endTimeWidgetId = null;

  private String hourWidgetId = null;
  private String minuteWidgetId = null;

  private String reminderWidgetId = null;

  private String buildInfoWidgetId = null;

  private final TabBar colorWidget = new TabBar(STYLE_COLOR_BAR_PREFIX, false);

  private boolean saving = false;

  AppointmentBuilder(boolean isNew) {
    super();
    this.isNew = isNew;

    addColorHandlers();
  }

  @Override
  public void afterCreate(FormView form) {
    loadProperties();
    loadReminders();
  }

  @Override
  public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, NAME_SERVICE_TYPE)) {
      setServiceTypeWidgetId(DomUtils.getId(widget));
      if (widget instanceof BeeListBox) {
        ((BeeListBox) widget).addKeyDownHandler(LIST_BOX_CLEANER);
      }

    } else if (BeeUtils.same(name, NAME_REPAIR_TYPE)) {
      setRepairTypeWidgetId(DomUtils.getId(widget));
      if (widget instanceof Editor) {
        ((Editor) widget).addValueChangeHandler(propWidgetHandler);
      }
      if (widget instanceof BeeListBox) {
        ((BeeListBox) widget).addKeyDownHandler(LIST_BOX_CLEANER);
      }

    } else if (BeeUtils.same(name, NAME_RESOURCES)) {
      setResourceWidgetId(DomUtils.getId(widget));
      if (widget instanceof BeeListBox) {
        ((BeeListBox) widget).addDoubleClickHandler(resourceWidgetHandler);
        ((BeeListBox) widget).addKeyDownHandler(resourceWidgetHandler);
      }

    } else if (BeeUtils.same(name, NAME_ADD_RESOURCE)) {
      if (widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            addResources();
          }
        });
      }
    } else if (BeeUtils.same(name, NAME_REMOVE_RESOURCE)) {
      if (widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            removeResource();
          }
        });
      }

    } else if (BeeUtils.same(name, NAME_START_DATE)) {
      setStartDateWidgetId(DomUtils.getId(widget));
    } else if (BeeUtils.same(name, NAME_START_TIME)) {
      setStartTimeWidgetId(DomUtils.getId(widget));

    } else if (BeeUtils.same(name, NAME_END_DATE)) {
      setEndDateWidgetId(DomUtils.getId(widget));
    } else if (BeeUtils.same(name, NAME_END_TIME)) {
      setEndTimeWidgetId(DomUtils.getId(widget));
      
    } else if (BeeUtils.same(name, NAME_HOURS)) {
      setHourWidgetId(DomUtils.getId(widget));
    } else if (BeeUtils.same(name, NAME_MINUTES)) {
      setMinuteWidgetId(DomUtils.getId(widget));

    } else if (BeeUtils.same(name, NAME_COLORS) && widget instanceof HasWidgets) {
      ((HasWidgets) widget).add(colorWidget);
      initColorWidget();

    } else if (BeeUtils.same(name, NAME_REMINDER)) {
      setReminderWidgetId(DomUtils.getId(widget));
      if (widget instanceof BeeListBox) {
        ((BeeListBox) widget).addKeyDownHandler(LIST_BOX_CLEANER);
      }

    } else if (BeeUtils.same(name, NAME_BUILD)) {
      if (widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            buildIncrementally();
          }
        });
      }
    } else if (BeeUtils.same(name, NAME_BUILD_INFO)) {
      setBuildInfoWidgetId(DomUtils.getId(widget));
    }
  }
  
  @Override
  public void afterRefresh(FormView form, IsRow row) {
    if (row == null) {
      return;
    }

    DateTime start = Data.getDateTime(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME);
    DateTime end = Data.getDateTime(VIEW_APPOINTMENTS, row, COL_END_DATE_TIME);
    
    getInputDate(getStartDateWidgetId()).setDate(start);
    getInputTime(getStartTimeWidgetId()).setDateTime(start);
    
    getInputDate(getEndDateWidgetId()).setDate(end);
    getInputTime(getEndTimeWidgetId()).setDateTime(end);
  }

  @Override
  public boolean beforeAction(Action action, FormPresenter presenter) {
    switch (action) {
      case SAVE:
        if (!isSaving() && validate() && save(null)) {
          presenter.handleAction(Action.CLOSE);
        }
        return false;

      case REFRESH:
        loadProperties();
        loadReminders();

        getFormView().refresh(false);
        return false;

      default:
        return true;
    }
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    return isNew || BeeUtils.isEmpty(name)
        || !BeeUtils.inListSame(name, NAME_BUILD_SEPARATOR, NAME_BUILD, NAME_BUILD_INFO);
  }

  @Override
  public FormCallback getInstance() {
    Assert.untouchable();
    return null;
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

  void setAttenddes(List<Long> attendees) {
    BeeUtils.overwrite(resources, attendees);
    refreshResourceWidget();
  }
  
  void setColor(Long color) {
    if (color != null && colors.contains(color)) {
      colorWidget.selectTab(colors.indexOf(color));
    }
  }

  void setProperties(List<Long> properties) {
    int serviceTypeIndex = BeeConst.UNDEF;
    int repairTypeIndex = BeeConst.UNDEF;

    for (Long id : properties) {
      if (serviceTypes.contains(id)) {
        serviceTypeIndex = serviceTypes.indexOf(id);
      } else if (repairTypes.contains(id)) {
        repairTypeIndex = repairTypes.indexOf(id);
      }
    }

    setSelectedIndex(getListBox(getServiceTypeWidgetId()), serviceTypeIndex);
    setSelectedIndex(getListBox(getRepairTypeWidgetId()), repairTypeIndex);
  }

  void setReminders(List<Long> reminders) {
    int reminderIndex = BeeConst.UNDEF;

    for (Long id : reminders) {
      if (reminderTypes.contains(id)) {
        reminderIndex = reminderTypes.indexOf(id);
      }
    }

    setSelectedIndex(getListBox(getReminderWidgetId()), reminderIndex);
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
          widget.getElement().setInnerHTML("X");
        }
      }
    });
  }

  private void addResources() {
    BeeRowSet attendees = CalendarKeeper.getAttendees();
    if (attendees == null || attendees.isEmpty()) {
      getFormView().notifyWarning("Attendees not available");
      return;
    }

    Long serviceType = getSelectedId(getServiceTypeWidgetId(), serviceTypes);
    Long repairType = getSelectedId(getRepairTypeWidgetId(), repairTypes);
    
    final List<Long> attIds = Lists.newArrayList();
    final BeeListBox widget = new BeeListBox(true);

    String viewName = attendees.getViewName();
    for (BeeRow row : attendees.getRows()) {
      long id = row.getId();
      if (resources.contains(id)) {
        continue;
      }
      
      if (serviceType != null && serviceResources.containsKey(id)
          && !serviceResources.containsEntry(id, serviceType)) {
        continue;
      }
      if (repairType != null && repairResources.containsKey(id)
          && !repairResources.containsEntry(id, repairType)) {
        continue;
      }

      String item = BeeUtils.concat(BeeConst.DEFAULT_LIST_SEPARATOR,
          Data.getString(viewName, row, COL_NAME), Data.getString(viewName, row, COL_TYPE_NAME));
      widget.addItem(item);
      attIds.add(id);
    }

    if (attIds.isEmpty()) {
      getFormView().notifyWarning("Nerasta resursų, kuriuos galima pridėti");
      return;
    }

    if (BeeUtils.betweenInclusive(attIds.size(), 2, 20)) {
      widget.setAllVisible();
    } else if (attIds.size() > 20) {
      widget.setVisibleItemCount(20);
    }

    widget.addStyleName(STYLE_ADD_RESOURCES);
    
    final InputWidgetCallback callback = new InputWidgetCallback() {
      @Override
      public void onSuccess() {
        int cnt = 0;
        for (int index = 0; index < widget.getItemCount(); index++) {
          if (widget.isItemSelected(index)) {
            resources.add(attIds.get(index));
            cnt++;
          }
        }
        if (cnt > 0) {
          refreshResourceWidget();
        }
      }
    };
    
    widget.addDoubleClickHandler(new DoubleClickHandler() {
      @Override
      public void onDoubleClick(DoubleClickEvent event) {
        Popup popup = DomUtils.getParentPopup(widget);
        if (popup != null) {
          popup.hide();
          callback.onSuccess();
        }  
      }
    });

    Global.inputWidget("Pasirinkite resursus", widget, callback, false, RowFactory.DIALOG_STYLE);
  }

  private void buildIncrementally() {
    if (isSaving() || !validate()) {
      return;
    }

    save(new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        reset(result);
      }
    });
  }

  private String getBuildInfoWidgetId() {
    return buildInfoWidgetId;
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

  private DateTime getEnd() {
    HasDateValue datePart = getInputDate(getEndDateWidgetId()).getDate();
    DateTime timePart = getInputTime(getEndTimeWidgetId()).getDateTime();

    if (datePart == null) {
      if (timePart != null && TimeUtils.minutesSinceDayStarted(timePart) > 0) {
        datePart = getInputDate(getStartDateWidgetId()).getDate();
      }
      if (datePart == null) {
        return null;
      }
    }
    
    return TimeUtils.combine(datePart, timePart);
  }
  
  private String getEndDateWidgetId() {
    return endDateWidgetId;
  }

  private String getEndTimeWidgetId() {
    return endTimeWidgetId;
  }

  private String getHourWidgetId() {
    return hourWidgetId;
  }
  
  private InputDate getInputDate(String id) {
    Widget widget = getWidget(id);
    return (widget instanceof InputDate) ? (InputDate) widget : null;
  }

  private InputTime getInputTime(String id) {
    Widget widget = getWidget(id);
    return (widget instanceof InputTime) ? (InputTime) widget : null;
  }

  private BeeListBox getListBox(String id) {
    Widget widget = getWidget(id);
    return (widget instanceof BeeListBox) ? (BeeListBox) widget : null;
  }

  private String getMinuteWidgetId() {
    return minuteWidgetId;
  }

  private String getReminderWidgetId() {
    return reminderWidgetId;
  }

  private String getRepairTypeWidgetId() {
    return repairTypeWidgetId;
  }

  private String getResourceWidgetId() {
    return resourceWidgetId;
  }

  private Long getSelectedId(String widgetId, List<Long> rowIds) {
    BeeListBox listBox = getListBox(widgetId);

    if (listBox != null) {
      int index = listBox.getSelectedIndex();
      if (BeeUtils.isIndex(rowIds, index)) {
        return rowIds.get(index);
      }
    }
    return null;
  }

  private String getServiceTypeWidgetId() {
    return serviceTypeWidgetId;
  }
  
  private DateTime getStart() {
    HasDateValue datePart = getInputDate(getStartDateWidgetId()).getDate();
    if (datePart == null) {
      return null;
    }
    DateTime timePart = getInputTime(getStartTimeWidgetId()).getDateTime();
    
    return TimeUtils.combine(datePart, timePart);
  }

  private String getStartDateWidgetId() {
    return startDateWidgetId;
  }

  private String getStartTimeWidgetId() {
    return startTimeWidgetId;
  }

  private Widget getWidget(String id) {
    return DomUtils.getChildQuietly(getFormView().asWidget(), id);
  }

  private boolean hasValue(String widgetId) {
    Widget widget = getWidget(widgetId);

    if (widget instanceof BeeListBox) {
      return ((BeeListBox) widget).getSelectedIndex() >= 0;
    } else {
      return false;
    }
  }

  private void initColorWidget() {
    if (colorWidget.getItemCount() > 0) {
      colorWidget.clear();
    }
    if (!colors.isEmpty()) {
      colors.clear();
    }

    BeeRowSet themeColors = CalendarKeeper.getThemeColors();
    if (themeColors == null || themeColors.isEmpty()) {
      BeeKeeper.getLog().warning("theme colors not found");
      return;
    }

    Long theme = null;

    Long defAppType = CalendarKeeper.getDefaultAppointmentType();
    if (defAppType != null) {
      theme = CalendarKeeper.CACHE.getLong(VIEW_APPOINTMENT_TYPES, defAppType, COL_THEME);
    }

    String viewName = themeColors.getViewName();
    if (!DataUtils.isId(theme)) {
      theme = Data.getLong(viewName, themeColors.getRow(0), COL_THEME);
    }

    for (BeeRow row : themeColors.getRows()) {
      if (DataUtils.isId(theme) && !theme.equals(Data.getLong(viewName, row, COL_THEME))) {
        continue;
      }

      Long color = Data.getLong(viewName, row, COL_COLOR);

      String bc = Data.getString(viewName, row, COL_BACKGROUND);
      String fc = Data.getString(viewName, row, COL_FOREGROUND);

      Html item = new Html();
      item.getElement().getStyle().setBackgroundColor(bc);
      if (!BeeUtils.isEmpty(fc)) {
        item.getElement().getStyle().setColor(fc);
      }

      colorWidget.addItem(item);
      colors.add(color);
    }

    if (colorWidget.getItemCount() > 0) {
      if (isNew && DataUtils.isId(theme)) {
        Long defColor = CalendarKeeper.CACHE.getLong(VIEW_THEMES, theme, COL_DEFAULT_COLOR);
        if (defColor != null && colors.contains(defColor)) {
          colorWidget.selectTab(colors.indexOf(defColor));
        }
      }
    } else {
      BeeKeeper.getLog().warning("theme", theme, "colors not found");
    }
  }

  private void initPropWidget(Widget widget, BeeRowSet rowSet, List<Long> rowIds, Long def) {
    if (widget instanceof BeeListBox && !rowIds.isEmpty()) {
      final BeeListBox listBox = (BeeListBox) widget;
      if (listBox.getItemCount() > 0) {
        listBox.clear();
      }

      for (long id : rowIds) {
        BeeRow row = rowSet.getRowById(id);
        String item = Data.getString(rowSet.getViewName(), row, COL_NAME);
        listBox.addItem(item);
      }

      if (isNew) {
        int index = (def == null) ? BeeConst.UNDEF : rowIds.indexOf(def);
        setSelectedIndex(listBox, index);
      }
    }
  }

  private void initReminderWidget(Widget widget, BeeRowSet rowSet) {
    if (widget instanceof BeeListBox && !reminderTypes.isEmpty()) {
      final BeeListBox listBox = (BeeListBox) widget;
      if (listBox.getItemCount() > 0) {
        listBox.clear();
      }

      String viewName = rowSet.getViewName();
      for (long id : reminderTypes) {
        BeeRow row = rowSet.getRowById(id);
        String item = Data.getString(viewName, row, COL_NAME);
        listBox.addItem(BeeUtils.trimRight(item));
      }

      if (isNew) {
        setSelectedIndex(listBox, BeeConst.UNDEF);
      }
    }
  }
  
  private boolean isEmpty(IsRow row, String columnId) {
    return BeeUtils.isEmpty(Data.getString(VIEW_APPOINTMENTS, row, columnId));
  }

  private boolean isSaving() {
    return saving;
  }

  private void loadProperties() {
    BeeRowSet properties = CalendarKeeper.getExtendedProperties();

    serviceTypes.clear();
    repairTypes.clear();

    defaultServiceType = null;
    defaultRepairType = null;

    if (properties == null || properties.isEmpty()) {
      BeeKeeper.getLog().warning("extended properties not available");
      return;
    }

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
    
    loadResourceProperties();

    if (!BeeUtils.isEmpty(getServiceTypeWidgetId())) {
      initPropWidget(getWidget(getServiceTypeWidgetId()), properties, serviceTypes,
          defaultServiceType);
    }
    if (!BeeUtils.isEmpty(getRepairTypeWidgetId())) {
      initPropWidget(getWidget(getRepairTypeWidgetId()), properties, repairTypes,
          defaultRepairType);
    }
  }

  private void loadReminders() {
    BeeRowSet rowSet = CalendarKeeper.getReminderTypes();
    if (rowSet == null || rowSet.isEmpty()) {
      BeeKeeper.getLog().warning("reminder types not available");
      return;
    }

    BeeUtils.overwrite(reminderTypes, DataUtils.getRowIds(rowSet));
    if (!BeeUtils.isEmpty(getReminderWidgetId())) {
      initReminderWidget(getWidget(getReminderWidgetId()), rowSet);
    }
  }

  private void loadResourceProperties() {
    BeeRowSet rowSet = CalendarKeeper.getAttendeeProps();
    
    if (!serviceResources.isEmpty()) {
      serviceResources.clear();
    }
    if (!repairResources.isEmpty()) {
      repairResources.clear();
    }
    if (rowSet == null || rowSet.isEmpty()) {
      return;
    }
    
    String viewName = rowSet.getViewName();
    for (BeeRow row : rowSet.getRows()) {
      Long resource = Data.getLong(viewName, row, COL_ATTENDEE);
      Long property = Data.getLong(viewName, row, COL_PROPERTY);
      
      if (serviceTypes.contains(property)) {
        serviceResources.put(resource, property);
      } else if (repairTypes.contains(property)) {
        repairResources.put(resource, property);
      }
    }
  }

  private void refreshResourceWidget() {
    BeeListBox listBox = getListBox(getResourceWidgetId());
    BeeRowSet attendees = CalendarKeeper.getAttendees();

    if (listBox != null && attendees != null) {
      if (listBox.getItemCount() > 0) {
        listBox.clear();
      }

      String viewName = attendees.getViewName();
      for (long id : resources) {
        BeeRow row = attendees.getRowById(id);
        String item = BeeUtils.concat(BeeConst.DEFAULT_LIST_SEPARATOR,
            Data.getString(viewName, row, COL_NAME), Data.getString(viewName, row, COL_TYPE_NAME));
        listBox.addItem(item);
      }
    }
  }

  private void removeResource() {
    if (resources.isEmpty()) {
      getFormView().notifyWarning("Nothing to remove");
      return;
    }

    BeeListBox listBox = getListBox(getResourceWidgetId());
    if (listBox != null) {
      int index = listBox.getSelectedIndex();
      if (!BeeUtils.isIndex(resources, index)) {
        index = resources.size() - 1;
      }

      resources.remove(index);
      refreshResourceWidget();

    } else {
      getFormView().notifySevere("Resource widget not found");
    }
  }

  private void reset(BeeRow createdRow) {
    StringBuilder info = new StringBuilder();
    String separator = BeeConst.DEFAULT_LIST_SEPARATOR;

    BeeListBox listBox = getListBox(getRepairTypeWidgetId());
    if (listBox != null) {
      int index = listBox.getSelectedIndex();
      if (index >= 0) {
        info.append(listBox.getItemText(index)).append(separator);
      }
      listBox.deselect();
    }

    if (!resources.isEmpty()) {
      BeeRowSet attendees = CalendarKeeper.getAttendees();
      for (long attId : resources) {
        info.append(attendees.getStringByRowId(attId, COL_NAME)).append(separator);
      }
      resources.clear();
      refreshResourceWidget();
    }

    DateTime start = Data.getDateTime(VIEW_APPOINTMENTS, createdRow, COL_START_DATE_TIME);
    DateTime end = Data.getDateTime(VIEW_APPOINTMENTS, createdRow, COL_END_DATE_TIME);
    
    DateTimeFormat format = DateTimeFormat.getFormat("MMM d HH:mm");
    if (start != null) {
      info.append(format.format(start));
    }
    info.append(" - ");
    if (end != null) {
      info.append(format.format(end));
    }

    listBox = getListBox(getReminderWidgetId());
    if (listBox != null) {
      int index = listBox.getSelectedIndex();
      if (index >= 0) {
        info.append(separator).append(listBox.getItemText(index));
      }
      listBox.deselect();
    }

    Widget widget = getWidget(getHourWidgetId());
    if (widget instanceof Editor) {
      ((Editor) widget).setValue(BeeConst.STRING_ZERO);
    }
    widget = getWidget(getMinuteWidgetId());
    if (widget instanceof Editor) {
      ((Editor) widget).setValue(BeeConst.STRING_ZERO);
    }

    Data.clearCell(VIEW_APPOINTMENTS, getFormView().getActiveRow(), COL_DESCRIPTION);

    widget = getWidget(getBuildInfoWidgetId());
    if (widget instanceof HasItems) {
      ((HasItems) widget).addItem(info.toString());
    }

    getFormView().refresh(false);
  }

  private boolean save(final RowCallback callback) {
    if (isSaving()) {
      return false;
    }
    setSaving(true);

    BeeRow row = DataUtils.cloneRow(getFormView().getActiveRow());
    final String viewName = VIEW_APPOINTMENTS;
    
    DateTime start = getStart();
    Data.setValue(viewName, row, COL_START_DATE_TIME, start);

    DateTime end = getEnd();
    if (end == null) {
      long millis = start.getTime() + getDuration() * TimeUtils.MILLIS_PER_MINUTE;
      Data.setValue(viewName, row, COL_END_DATE_TIME, millis);
    } else {
      Data.setValue(viewName, row, COL_END_DATE_TIME, end);
    }

    if (!colors.isEmpty()) {
      int index = colorWidget.getSelectedTab();
      if (!BeeUtils.isIndex(colors, index) && isEmpty(row, COL_COLOR)) {
        index = 0;
      }
      if (BeeUtils.isIndex(colors, index)) {
        Data.setValue(viewName, row, COL_COLOR, colors.get(index));
      }
    }
    
    BeeRowSet rowSet;
    List<BeeColumn> columns = CalendarKeeper.getAppointmentViewColumns();
    if (isNew) {
      rowSet = Queries.createRowSetForInsert(viewName, columns, row);
    } else {
      rowSet = new BeeRowSet(viewName, columns);
      rowSet.addRow(row);
    }

    final String propList = DataUtils.buildList(getSelectedId(getServiceTypeWidgetId(),
        serviceTypes), getSelectedId(getRepairTypeWidgetId(), repairTypes));
    if (!BeeUtils.isEmpty(propList)) {
      rowSet.setTableProperty(COL_PROPERTY, propList);
    }

    final String attList = DataUtils.buildList(resources);
    if (!BeeUtils.isEmpty(attList)) {
      rowSet.setTableProperty(COL_ATTENDEE, attList);
    }

    Long reminderType = getSelectedId(getReminderWidgetId(), reminderTypes);
    final String remindList = DataUtils.isId(reminderType) ? reminderType.toString() : null; 
    if (!BeeUtils.isEmpty(remindList)) {
      rowSet.setTableProperty(COL_REMINDER_TYPE, remindList);
    }
    
    final String svc = isNew ? SVC_CREATE_APPOINTMENT : SVC_UPDATE_APPOINTMENT;
    ParameterList params = CalendarKeeper.createRequestParameters(svc);

    BeeKeeper.getRpc().sendText(params, Codec.beeSerialize(rowSet), new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          getFormView().notifySevere(response.getErrors());
        } else if (!response.hasResponse(BeeRow.class)) {
          getFormView().notifySevere(svc, ": response not a BeeRow");
        } else {
          BeeRow result = BeeRow.restore((String) response.getResponse());
          if (result == null) {
            getFormView().notifySevere(svc, ": cannot restore row");
          } else {
            
            if (isNew) {
              BeeKeeper.getBus().fireEvent(new RowInsertEvent(viewName, result));
            } else {
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(viewName, result));
            }
            
            Appointment appointment = new Appointment(result, attList, propList, remindList);
            State state = isNew ? State.CREATED : State.CHANGED;
            AppointmentEvent.fire(appointment, state);

            if (callback != null) {
              callback.onSuccess(result);
            }
          }
        }
        setSaving(false);
      }
    });

    return true;
  }

  private void setBuildInfoWidgetId(String buildInfoWidgetId) {
    this.buildInfoWidgetId = buildInfoWidgetId;
  }

  private void setEndDateWidgetId(String endDateWidgetId) {
    this.endDateWidgetId = endDateWidgetId;
  }

  private void setEndTimeWidgetId(String endTimeWidgetId) {
    this.endTimeWidgetId = endTimeWidgetId;
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

  private void setReminderWidgetId(String reminderWidgetId) {
    this.reminderWidgetId = reminderWidgetId;
  }

  private void setRepairTypeWidgetId(String repairTypeWidgetId) {
    this.repairTypeWidgetId = repairTypeWidgetId;
  }

  private void setResourceWidgetId(String resourceWidgetId) {
    this.resourceWidgetId = resourceWidgetId;
  }

  private void setSaving(boolean saving) {
    this.saving = saving;
  }

  private void setSelectedIndex(final BeeListBox listBox, int index) {
    if (listBox == null || listBox.isEmpty()) {
      return;
    }

    if (index >= 0 && index < listBox.getItemCount()) {
      listBox.setSelectedIndex(index);
    } else {
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          listBox.deselect();
        }
      });
    }
  }

  private void setServiceTypeWidgetId(String serviceTypeWidgetId) {
    this.serviceTypeWidgetId = serviceTypeWidgetId;
  }

  private void setStartDateWidgetId(String startDateWidgetId) {
    this.startDateWidgetId = startDateWidgetId;
  }

  private void setStartTimeWidgetId(String startTimeWidgetId) {
    this.startTimeWidgetId = startTimeWidgetId;
  }

  private void updateDuration(String propName) {
    BeeRowSet properties = CalendarKeeper.getExtendedProperties();
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

    if (!hasValue(getServiceTypeWidgetId())) {
      getFormView().notifySevere("Pasirinkite serviso tipą");
      return false;
    }
    if (!hasValue(getRepairTypeWidgetId())) {
      getFormView().notifySevere("Pasirinkite remonto tipą");
      return false;
    }
    
    DateTime start = getStart();
    DateTime end = getEnd();

    if (start == null) {
      getFormView().notifySevere("Įveskite planuojamą pradžios laiką");
      return false;
    }

    if (end == null) {
      if (getDuration() <= 0) {
        getFormView().notifySevere("Įveskite trukmę arba planuojamą pabaigos laiką");
        return false;
      }
    } else if (TimeUtils.isLeq(end, start)) {
      getFormView().notifySevere("Pabaigos laikas turi būti didesnis už pradžios laiką");
      return false;
    }

    return true;
  }
}
