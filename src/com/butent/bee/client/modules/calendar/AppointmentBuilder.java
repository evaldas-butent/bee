package com.butent.bee.client.modules.calendar;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
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
import com.butent.bee.client.data.RelationUtils;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.calendar.event.AppointmentEvent;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.CloseCallback;
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
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Set;

class AppointmentBuilder extends AbstractFormCallback {

  private static final BeeLogger logger = LogUtils.getLogger(AppointmentBuilder.class);
  
  private class DateOrTimeWidgetHandler implements BlurHandler {
    @Override
    public void onBlur(BlurEvent event) {
      checkOverlap(true);
    }
  }

  private class ModalCallback extends InputCallback {
    @Override
    public String getErrorMessage() {
      if (getFormView().checkOnSave() && AppointmentBuilder.this.validate()) {
        return null;
      } else {
        return InputBoxes.SILENT_ERROR;
      }
    }

    @Override
    public void onClose(final CloseCallback closeCallback) {
      Assert.notNull(closeCallback);
      if (!getFormView().checkOnClose()) {
        return;
      }

      IsRow oldRow = getFormView().getOldRow();
      IsRow newRow = getFormView().getActiveRow();
      
      if (oldRow == null || newRow == null) {
        closeCallback.onClose();
        return;
      }
      
      List<String> changes = Lists.newArrayList();
      
      BeeRowSet rowSet = DataUtils.getUpdated(VIEW_APPOINTMENTS,
          CalendarKeeper.getAppointmentViewColumns(), oldRow, newRow);
      if (!DataUtils.isEmpty(rowSet)) {
        changes.addAll(rowSet.getColumnLabels());
      }
      
      Long oldService = null;
      Long oldRepair = null;
      for (Long prop : DataUtils.parseIdSet(oldRow.getProperty(VIEW_APPOINTMENT_PROPS))) {
        if (serviceTypes.contains(prop)) {
          oldService = prop;
        } else if (repairTypes.contains(prop)) {
          oldRepair = prop;
        }
      }
      
      Long newService = getSelectedId(getServiceTypeWidgetId(), serviceTypes);
      if (oldService == null && newService != null && newService.equals(defaultServiceType)) {
        newService = null;
      }
      Long newRepair = getSelectedId(getRepairTypeWidgetId(), repairTypes);
      if (oldRepair == null && newRepair != null && newRepair.equals(defaultRepairType)) {
        newRepair = null;
      }

      if (!Objects.equal(oldService, newService)) {
        changes.add("Serviso tipas");
      }
      if (!Objects.equal(oldRepair, newRepair)) {
        changes.add("Remonto tipas");
      }
      
      if (!DataUtils.sameIdSet(oldRow.getProperty(VIEW_APPOINTMENT_ATTENDEES), resources)) {
        changes.add("Resursai");
      }

      DateTime oldStart = Data.getDateTime(VIEW_APPOINTMENTS, oldRow, COL_START_DATE_TIME);
      DateTime newStart = getStart();
      if (!Objects.equal(oldStart, newStart)) {
        changes.add("Pradžia");
      }
      
      DateTime oldEnd = Data.getDateTime(VIEW_APPOINTMENTS, oldRow, COL_END_DATE_TIME);
      DateTime newEnd = getEnd(newStart);
      if (!Objects.equal(oldEnd, newEnd)) {
        changes.add("Pabaiga");
      }
      
      List<Long> reminders = Lists.newArrayList();
      Long reminderType = getSelectedId(getReminderWidgetId(), reminderTypes);
      if (reminderType != null) {
        reminders.add(reminderType);
      }
      if (!DataUtils.sameIdSet(oldRow.getProperty(VIEW_APPOINTMENT_REMINDERS), reminders)) {
        changes.add("Priminimas");
      }
      
      Long oldColor = Data.getLong(VIEW_APPOINTMENTS, oldRow, COL_COLOR);
      Long newColor = BeeUtils.getQuietly(colors, colorWidget.getSelectedTab());
      if (oldColor != null && newColor != null && !oldColor.equals(newColor)) {
        changes.add("Spalva");
      }
      
      if (changes.isEmpty()) {
        closeCallback.onClose();
        return;
      }
     
      List<String> messages = Lists.newArrayList();

      String msg = isNew ? Global.CONSTANTS.newValues() : Global.CONSTANTS.changedValues();
      messages.add(msg + BeeConst.STRING_SPACE 
          + BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, changes));

      messages.add(isNew ?
          Global.CONSTANTS.createNewAppointment() : Global.CONSTANTS.saveChanges());
      
      DecisionCallback callback = new DecisionCallback() {
        @Override
        public void onCancel() {
          UiHelper.focus(getFormView().asWidget());
        }

        @Override
        public void onConfirm() {
          closeCallback.onSave();
        }

        @Override
        public void onDeny() {
          closeCallback.onClose();
        }
      };
      
      String cap = isNew ?
          CalendarKeeper.getAppointmentViewInfo().getNewRowCaption() : getFormView().getCaption();
      Global.getMsgBoxen().decide(cap, messages, callback, DialogConstants.DECISION_YES);
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
      checkOverlap(true);
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
            if (isOverlapVisible()) {
              checkOverlap(false);
            }
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

  private static final String NAME_ADD_RESOURCE = "AddResource";
  private static final String NAME_REMOVE_RESOURCE = "RemoveResource";

  private static final String NAME_RESOURCES = "Resources";
  private static final String NAME_OVERLAP = "Overlap";

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

  static BeeRow createEmptyRow(BeeRow typeRow, DateTime start) {
    BeeRow row = RowFactory.createEmptyRow(CalendarKeeper.getAppointmentViewInfo(), true);

    if (typeRow != null) {
      RelationUtils.updateRow(VIEW_APPOINTMENTS, COL_APPOINTMENT_TYPE, row,
          VIEW_APPOINTMENT_TYPES, typeRow, true);
    }
    if (start != null) {
      Data.setValue(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME, start);
    }
    return row;
  }

  private final boolean isNew;

  private ModalCallback modalCallback = null;

  private final DateOrTimeWidgetHandler dateOrTimeWidgetHandler = new DateOrTimeWidgetHandler();
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
  private String overlapWidgetId = null;

  private String startDateWidgetId = null;
  private String startTimeWidgetId = null;
  private String endDateWidgetId = null;
  private String endTimeWidgetId = null;

  private String hourWidgetId = null;
  private String minuteWidgetId = null;

  private String reminderWidgetId = null;

  private String buildInfoWidgetId = null;

  private final TabBar colorWidget = new TabBar(STYLE_COLOR_BAR_PREFIX, Orientation.HORIZONTAL);

  private boolean saving = false;

  private boolean overlapVisible = false;
  private final List<Appointment> overlappingAppointments = Lists.newArrayList();
  
  private DateTime lastCheckStart = null;
  private DateTime lastCheckEnd = null;

  private final Set<String> requiredFields = Sets.newHashSet();
  
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

    } else if (BeeUtils.same(name, NAME_RESOURCES)) {
      setResourceWidgetId(DomUtils.getId(widget));
      if (widget instanceof BeeListBox) {
        ((BeeListBox) widget).addDoubleClickHandler(resourceWidgetHandler);
        ((BeeListBox) widget).addKeyDownHandler(resourceWidgetHandler);
      }

    } else if (BeeUtils.same(name, NAME_OVERLAP)) {
      setOverlapWidgetId(DomUtils.getId(widget));
      if (widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            showOverlappingAppointments();
          }
        });
      }
      widget.getElement().getStyle().setVisibility(Visibility.HIDDEN);
      setOverlapVisible(false);

    } else if (BeeUtils.same(name, NAME_START_DATE)) {
      setStartDateWidgetId(DomUtils.getId(widget));
      if (widget instanceof Editor) {
        ((Editor) widget).addBlurHandler(dateOrTimeWidgetHandler);
      }
    } else if (BeeUtils.same(name, NAME_START_TIME)) {
      setStartTimeWidgetId(DomUtils.getId(widget));
      if (widget instanceof Editor) {
        ((Editor) widget).addBlurHandler(dateOrTimeWidgetHandler);
      }

    } else if (BeeUtils.same(name, NAME_END_DATE)) {
      setEndDateWidgetId(DomUtils.getId(widget));
      if (widget instanceof Editor) {
        ((Editor) widget).addBlurHandler(dateOrTimeWidgetHandler);
      }
    } else if (BeeUtils.same(name, NAME_END_TIME)) {
      setEndTimeWidgetId(DomUtils.getId(widget));
      if (widget instanceof Editor) {
        ((Editor) widget).addBlurHandler(dateOrTimeWidgetHandler);
      }

    } else if (BeeUtils.same(name, NAME_HOURS)) {
      setHourWidgetId(DomUtils.getId(widget));
      if (widget instanceof Editor) {
        ((Editor) widget).addBlurHandler(dateOrTimeWidgetHandler);
      }
    } else if (BeeUtils.same(name, NAME_MINUTES)) {
      setMinuteWidgetId(DomUtils.getId(widget));
      if (widget instanceof Editor) {
        ((Editor) widget).addBlurHandler(dateOrTimeWidgetHandler);
      }

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
    
    if (!BeeUtils.isEmpty(getStartDateWidgetId())) {
      getInputDate(getStartDateWidgetId()).setDate(start);
    }
    if (!BeeUtils.isEmpty(getStartTimeWidgetId())) {
      getInputTime(getStartTimeWidgetId()).setDateTime(start);
    }

    if (!BeeUtils.isEmpty(getEndDateWidgetId())) {
      getInputDate(getEndDateWidgetId()).setDate(end);
    }
    if (!BeeUtils.isEmpty(getEndTimeWidgetId())) {
      getInputTime(getEndTimeWidgetId()).setDateTime(end);
    }
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

  boolean isRequired(String name) {
    return requiredFields.contains(name);
  }

  void setAttenddes(List<Long> attendees) {
    BeeUtils.overwrite(resources, attendees);
    refreshResourceWidget();
    checkOverlap(false);
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
  
  void setRequiredFields(String fieldNames) {
    BeeUtils.overwrite(requiredFields, NameUtils.toSet(fieldNames));
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

      String item = BeeUtils.joinItems(Data.getString(viewName, row, COL_NAME),
          Data.getString(viewName, row, COL_TYPE_NAME));
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

    widget.addStyleName(CalendarStyleManager.ADD_RESOURCES);

    final InputCallback callback = new InputCallback() {
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
          if (!isOverlapVisible()) {
            checkOverlap(false);
          }
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

    Global.inputWidget("Pasirinkite resursus", widget, callback, false,
        RowFactory.DIALOG_STYLE, false);
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

  private void checkOverlap(boolean whenPeriodChanged) {
    if (resources.isEmpty()) {
      hideOverlap();
      return;
    }

    List<Long> opaqueResources = Lists.newArrayList();
    for (Long id : resources) {
      if (CalendarKeeper.isAttendeeOpaque(id)) {
        opaqueResources.add(id);
      }
    }
    if (opaqueResources.isEmpty()) {
      hideOverlap();
      return;
    }

    DateTime start = getStart();
    DateTime end = getEnd(start);
    if (start == null || end == null || TimeUtils.isLeq(end, start)) {
      hideOverlap();
      return;
    }
    
    if (whenPeriodChanged && start.equals(getLastCheckStart()) && end.equals(getLastCheckEnd())) {
      return;
    }
    setLastCheckStart(start);
    setLastCheckEnd(end);

    ParameterList params = CalendarKeeper.createRequestParameters(SVC_GET_OVERLAPPING_APPOINTMENTS);
    if (!isNew) {
      params.addQueryItem(PARAM_APPOINTMENT_ID, getFormView().getActiveRow().getId());
    }
    params.addQueryItem(PARAM_APPOINTMENT_START, start.getTime());
    params.addQueryItem(PARAM_APPOINTMENT_END, end.getTime());
    params.addQueryItem(PARAM_ATTENDEES, DataUtils.buildIdList(opaqueResources));

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        overlappingAppointments.clear();

        if (response.hasResponse(BeeRowSet.class)) {
          BeeRowSet rowSet = BeeRowSet.restore((String) response.getResponse());
          for (BeeRow row : rowSet.getRows()) {
            Appointment app = new Appointment(row);
            overlappingAppointments.add(app);
          }
        }
        showOverlap(!overlappingAppointments.isEmpty());
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

  private DateTime getEnd(DateTime start) {
    HasDateValue datePart = BeeUtils.isEmpty(getEndDateWidgetId()) 
        ? null : getInputDate(getEndDateWidgetId()).getDate();
    DateTime timePart = BeeUtils.isEmpty(getEndTimeWidgetId()) 
        ? null : getInputTime(getEndTimeWidgetId()).getDateTime();

    if (datePart == null && timePart != null && TimeUtils.minutesSinceDayStarted(timePart) > 0) {
      datePart = start;
    }

    if (datePart != null) {
      return TimeUtils.combine(datePart, timePart);
    } else {
      int duration = getDuration();
      if (start != null && duration > 0) {
        return new DateTime(start.getTime() + duration * TimeUtils.MILLIS_PER_MINUTE);
      } else {
        return null;
      }
    }
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

  private DateTime getLastCheckEnd() {
    return lastCheckEnd;
  }

  private DateTime getLastCheckStart() {
    return lastCheckStart;
  }

  private BeeListBox getListBox(String id) {
    if (BeeUtils.isEmpty(id)) {
      return null;
    }
    Widget widget = getWidget(id);
    return (widget instanceof BeeListBox) ? (BeeListBox) widget : null;
  }

  private String getMinuteWidgetId() {
    return minuteWidgetId;
  }

  private String getOverlapWidgetId() {
    return overlapWidgetId;
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
    HasDateValue datePart = BeeUtils.isEmpty(getStartDateWidgetId())
        ? null : getInputDate(getStartDateWidgetId()).getDate();
    if (datePart == null) {
      return null;
    }
    DateTime timePart = BeeUtils.isEmpty(getStartTimeWidgetId())
        ? null : getInputTime(getStartTimeWidgetId()).getDateTime();

    return TimeUtils.combine(datePart, timePart);
  }

  private String getStartDateWidgetId() {
    return startDateWidgetId;
  }

  private String getStartTimeWidgetId() {
    return startTimeWidgetId;
  }

  private Widget getWidget(String id) {
    Widget widget = DomUtils.getChildQuietly(getFormView().asWidget(), id);
    if (widget == null) {
      logger.warning("widget not found: id", id);
    }
    return widget;
  }

  private boolean hasValue(String widgetId) {
    Widget widget = getWidget(widgetId);

    if (widget instanceof BeeListBox) {
      return ((BeeListBox) widget).getSelectedIndex() >= 0;
    } else {
      return false;
    }
  }

  private void hideOverlap() {
    showOverlap(false);
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
      logger.warning("theme colors not found");
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
      logger.warning("theme", theme, "colors not found");
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

  private boolean isOverlapVisible() {
    return overlapVisible;
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
      logger.warning("extended properties not available");
      return;
    }

    String viewName = properties.getViewName();
    for (BeeRow row : properties.getRows()) {
      long id = row.getId();

      String groupName = Data.getString(viewName, row, COL_GROUP_NAME);
      boolean isDef = Objects.equal(Data.getLong(viewName, row, COL_DEFAULT_PROPERTY), id);

      if (BeeUtils.containsSame(groupName, "serv")) {
        serviceTypes.add(id);
        if (isDef) {
          defaultServiceType = id;
        }

      } else if (BeeUtils.containsSame(groupName, "rem")) {
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
      logger.warning("reminder types not available");
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
        String item = BeeUtils.joinItems(Data.getString(viewName, row, COL_NAME),
            Data.getString(viewName, row, COL_TYPE_NAME));
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
      if (isOverlapVisible()) {
        checkOverlap(false);
      }

    } else {
      getFormView().notifySevere("Resource widget not found");
    }
  }

  private void reset(BeeRow createdRow) {
    IsRow row = DataUtils.cloneRow(createdRow);
    row.setId(DataUtils.NEW_ROW_ID);
    row.setVersion(DataUtils.NEW_ROW_VERSION);

    StringBuilder info = new StringBuilder();
    String separator = BeeConst.DEFAULT_LIST_SEPARATOR;

    BeeListBox listBox = getListBox(getRepairTypeWidgetId());
    if (listBox != null) {
      int index = listBox.getSelectedIndex();
      if (index >= 0) {
        info.append(listBox.getItemText(index)).append(separator);
      }
      listBox.deselect();
      
      Long serviceType = getSelectedId(getServiceTypeWidgetId(), serviceTypes);
      if (serviceType == null) {
        row.clearProperty(VIEW_APPOINTMENT_PROPS);
      } else {
        row.setProperty(VIEW_APPOINTMENT_PROPS, serviceType.toString());
      }
    }
    
    boolean wasOpaque = false;

    if (!resources.isEmpty()) {
      BeeRowSet attendees = CalendarKeeper.getAttendees();
      for (long attId : resources) {
        info.append(attendees.getStringByRowId(attId, COL_NAME)).append(separator);
        if (!wasOpaque && CalendarKeeper.isAttendeeOpaque(attId)) {
          wasOpaque = true;
        }
      }
      
      resources.clear();
      refreshResourceWidget();
      hideOverlap();

      row.clearProperty(VIEW_APPOINTMENT_ATTENDEES);
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

      row.clearProperty(VIEW_APPOINTMENT_REMINDERS);
    }

    Widget widget = BeeUtils.isEmpty(getHourWidgetId()) ? null : getWidget(getHourWidgetId());
    if (widget instanceof Editor) {
      ((Editor) widget).setValue(BeeConst.STRING_ZERO);
    }
    widget = BeeUtils.isEmpty(getMinuteWidgetId()) ? null : getWidget(getMinuteWidgetId());
    if (widget instanceof Editor) {
      ((Editor) widget).setValue(BeeConst.STRING_ZERO);
    }

    widget = BeeUtils.isEmpty(getBuildInfoWidgetId()) ? null : getWidget(getBuildInfoWidgetId());
    if (widget instanceof HasItems) {
      ((HasItems) widget).addItem(info.toString());
    }

    if (wasOpaque && end != null) {
      Data.setValue(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME, end);
      Data.clearCell(VIEW_APPOINTMENTS, row, COL_END_DATE_TIME);
    }
    Data.clearCell(VIEW_APPOINTMENTS, row, COL_DESCRIPTION);
    
    getFormView().updateRow(row, false);
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

    DateTime end = getEnd(start);
    Data.setValue(viewName, row, COL_END_DATE_TIME, end);

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

    final String propList = DataUtils.buildIdList(getSelectedId(getServiceTypeWidgetId(),
        serviceTypes), getSelectedId(getRepairTypeWidgetId(), repairTypes));
    if (!BeeUtils.isEmpty(propList)) {
      rowSet.setTableProperty(COL_PROPERTY, propList);
    }

    final String attList = DataUtils.buildIdList(resources);
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

            if (!BeeUtils.isEmpty(attList)) {
              result.setProperty(VIEW_APPOINTMENT_ATTENDEES, attList);
            }
            if (!BeeUtils.isEmpty(propList)) {
              result.setProperty(VIEW_APPOINTMENT_PROPS, propList);
            }
            if (!BeeUtils.isEmpty(remindList)) {
              result.setProperty(VIEW_APPOINTMENT_REMINDERS, remindList);
            }

            if (isNew) {
              BeeKeeper.getBus().fireEvent(new RowInsertEvent(viewName, result));
            } else {
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(viewName, result));
            }
            
            Appointment appointment = new Appointment(result);
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

  private void setLastCheckEnd(DateTime lastCheckEnd) {
    this.lastCheckEnd = lastCheckEnd;
  }

  private void setLastCheckStart(DateTime lastCheckStart) {
    this.lastCheckStart = lastCheckStart;
  }

  private void setMinuteWidgetId(String minuteWidgetId) {
    this.minuteWidgetId = minuteWidgetId;
  }

  private void setModalCallback(ModalCallback modalCallback) {
    this.modalCallback = modalCallback;
  }

  private void setOverlapVisible(boolean overlapVisible) {
    this.overlapVisible = overlapVisible;
  }

  private void setOverlapWidgetId(String overlapWidgetId) {
    this.overlapWidgetId = overlapWidgetId;
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

  private void showOverlap(boolean show) {
    if (isOverlapVisible() != show && !BeeUtils.isEmpty(getOverlapWidgetId())) {
      Widget widget = getWidget(getOverlapWidgetId());
      if (widget != null) {
        widget.getElement().getStyle().setVisibility(show ? Visibility.VISIBLE : Visibility.HIDDEN);
        setOverlapVisible(show);
      }
    }
  }

  private void showOverlappingAppointments() {
    if (BeeUtils.isEmpty(overlappingAppointments)) {
      return;
    }

    Flow panel = new Flow();
    panel.addStyleName(CalendarStyleManager.MORE_PANEL);

    for (Appointment appointment : overlappingAppointments) {
      boolean multi = appointment.isMultiDay();
      AppointmentWidget widget = new AppointmentWidget(appointment, multi, BeeConst.UNDEF);
      widget.render();

      panel.add(widget);
    }
    
    DialogBox dialog = DialogBox.create(Global.CONSTANTS.overlappingAppointments(),
        CalendarStyleManager.MORE_POPUP);
    dialog.setWidget(panel);
    
    if (BeeUtils.isEmpty(getOverlapWidgetId())) {
      dialog.center();
    } else {
      dialog.showRelativeTo(getWidget(getOverlapWidgetId()));
    }
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
    if (!getFormView().validate(getFormView(), true)) {
      return false;
    }

    IsRow row = getFormView().getActiveRow();
    if (row == null) {
      return false;
    }

    if (isRequired(COL_COMPANY) && isEmpty(row, COL_COMPANY)) {
      getFormView().notifySevere("Įveskite klientą");
      return false;
    }
    if (isRequired(COL_VEHICLE) && isEmpty(row, COL_VEHICLE)) {
      getFormView().notifySevere("Įveskite automobilį");
      return false;
    }

    if (!BeeUtils.isEmpty(getServiceTypeWidgetId()) && isRequired(NAME_SERVICE_TYPE) 
        && !hasValue(getServiceTypeWidgetId())) {
      getFormView().notifySevere("Pasirinkite serviso tipą");
      return false;
    }
    if (!BeeUtils.isEmpty(getRepairTypeWidgetId()) && isRequired(NAME_REPAIR_TYPE) 
        && !hasValue(getRepairTypeWidgetId())) {
      getFormView().notifySevere("Pasirinkite remonto tipą");
      return false;
    }
    
    if (isRequired(NAME_RESOURCES) && resources.isEmpty()) {
      getFormView().notifySevere("Nurodykite resursus");
      return false;
    }

    DateTime start = getStart();
    DateTime end = getEnd(start);

    if (start == null) {
      getFormView().notifySevere("Įveskite planuojamą pradžios laiką");
      return false;
    }
    if (end == null) {
      getFormView().notifySevere("Įveskite trukmę arba planuojamą pabaigos laiką");
      return false;
    }
    if (TimeUtils.isLeq(end, start)) {
      getFormView().notifySevere("Pabaigos laikas turi būti didesnis už pradžios laiką");
      return false;
    }

    return true;
  }
}
