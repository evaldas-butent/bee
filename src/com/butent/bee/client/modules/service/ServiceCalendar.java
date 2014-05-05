package com.butent.bee.client.modules.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.timeboard.TimeBoard;
import com.butent.bee.client.timeboard.TimeBoardHelper;
import com.butent.bee.client.timeboard.TimeBoardRowLayout;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskPriority;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ServiceCalendar extends TimeBoard {

  static final String SUPPLIER_KEY = "service_calendar";
  
//  private static final String COL_COMPANY_KIND = "CalendarCompanyKind";

  private static final String COL_PIXELS_PER_COMPANY = "CalendarPixelsPerCompany";
  private static final String COL_PIXELS_PER_INFO = "CalendarPixelsPerInfo";

  private static final String COL_SEPARATE_OBJECTS = "CalendarSeparateObjects";

  private static final String COL_PIXELS_PER_DAY = "CalendarPixelsPerDay";
  private static final String COL_PIXELS_PER_ROW = "CalendarPixelsPerRow";

  private static final String COL_HEADER_HEIGHT = "CalendarHeaderHeight";
  private static final String COL_FOOTER_HEIGHT = "CalendarFooterHeight";

  private static final String COL_ITEM_OPACITY = "CalendarItemOpacity";
  private static final String COL_STRIP_OPACITY = "CalendarStripOpacity";

  private static final String COL_TASK_COLOR = "CalendarTaskColor";
  private static final String COL_RT_COLOR = "CalendarRTColor";

  private static final String FORM_SETTINGS = "ServiceCalendarSettings";

  private static final String STYLE_PREFIX = ServiceKeeper.STYLE_PREFIX + "calendar-";

  private static final String STYLE_VIEW = STYLE_PREFIX + "view";

  private static final String STYLE_CUSTOMER_PREFIX = STYLE_PREFIX + "customer-";
  private static final String STYLE_CUSTOMER_ROW_SEPARATOR = STYLE_CUSTOMER_PREFIX + "row-sep";
  private static final String STYLE_CUSTOMER_PANEL = STYLE_CUSTOMER_PREFIX + "panel";
  private static final String STYLE_CUSTOMER_LABEL = STYLE_CUSTOMER_PREFIX + "label";

//  private static final String STYLE_INFO_PREFIX = STYLE_PREFIX + "info-";
//  private static final String STYLE_INFO_PANEL = STYLE_INFO_PREFIX + "panel";
//  private static final String STYLE_INFO_LABEL = STYLE_INFO_PREFIX + "label";

  private static final String STYLE_TASK_PREFIX = STYLE_PREFIX + "task-";
  private static final String STYLE_TASK_PANEL = STYLE_TASK_PREFIX + "panel";
  private static final String STYLE_TASK_STATUS = STYLE_TASK_PREFIX + "status";
  private static final String STYLE_TASK_PRIORITY = STYLE_TASK_PREFIX + "priority";

  private static final String STYLE_RT_PREFIX = STYLE_PREFIX + "rt-";
  private static final String STYLE_RT_PANEL = STYLE_RT_PREFIX + "panel";
  private static final String STYLE_RT_PRIORITY = STYLE_RT_PREFIX + "priority";

  private static final String DEFAULT_TASK_COLOR = Colors.YELLOW;
  private static final String DEFAULT_RT_COLOR = Colors.YELLOW;

  private static final Set<String> relevantDataViews = Sets.newHashSet(VIEW_SERVICE_OBJECTS,
      VIEW_SERVICE_DATES, TaskConstants.VIEW_TASKS, TaskConstants.VIEW_RECURRING_TASKS);

  static void open(final Callback<IdentifiableWidget> callback) {
    BeeKeeper.getRpc().makeRequest(ServiceKeeper.createArgs(SVC_GET_CALENDAR_DATA),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            ServiceCalendar sc = new ServiceCalendar();
            sc.onCreate(response, callback);
          }
        });
  }

  private final List<ServiceCustomerWrapper> customers = new ArrayList<>();
  private final Multimap<Long, ServiceObjectWrapper> objects = ArrayListMultimap.create();

  private final Multimap<Long, TaskWrapper> tasks = ArrayListMultimap.create();
  private final Multimap<Long, RecurringTaskWrapper> recurringTasks = ArrayListMultimap.create();

  private int customerWidth = BeeConst.UNDEF;
  private int infoWidth = BeeConst.UNDEF;

  private boolean separateObjects;

  private final Set<String> customerPanels = new HashSet<>();
  private final Set<String> infoPanels = new HashSet<>();

  private final List<Integer> customerIndexesByRow = new ArrayList<>();
  private final Map<Integer, Long> objectsByRow = new HashMap<>();

  private ServiceCalendar() {
    super();
    addStyleName(STYLE_VIEW);
  }

  @Override
  public String getCaption() {
    return Localized.getConstants().svcCalendar();
  }

  @Override
  public String getIdPrefix() {
    return "svc-cal";
  }

  @Override
  public String getSupplierKey() {
    return SUPPLIER_KEY;
  }

  @Override
  public void handleAction(Action action) {
    if (Action.ADD.equals(action)) {
      RowFactory.createRow(VIEW_SERVICE_OBJECTS);
    } else {
      super.handleAction(action);
    }
  }

  @Override
  protected void editSettings() {
    BeeRow oldSettings = getSettingsRow();
    Assert.notNull(oldSettings);

    RowEditor.openRow(FORM_SETTINGS, getSettings().getViewName(), oldSettings, true,
        new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            if (updateSetting(result)) {
              render(false);
            }
          }
        });
  }

  @Override
  protected Collection<? extends HasDateRange> getChartItems() {
    List<HasDateRange> items = Lists.newArrayList();

    items.addAll(tasks.values());
    items.addAll(recurringTasks.values());

    return items;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.ADD, Action.CONFIGURE);
  }

  @Override
  protected String getFooterHeightColumnName() {
    return COL_FOOTER_HEIGHT;
  }

  @Override
  protected String getHeaderHeightColumnName() {
    return COL_HEADER_HEIGHT;
  }

  @Override
  protected String getRowHeightColumnName() {
    return COL_PIXELS_PER_ROW;
  }

  @Override
  protected String getStripOpacityColumnName() {
    return COL_STRIP_OPACITY;
  }

  @Override
  protected boolean isDataEventRelevant(DataEvent event) {
    return event != null && relevantDataViews.contains(event.getViewName());
  }

  @Override
  protected void prepareChart(Size canvasSize) {
    setSeparateObjects(TimeBoardHelper.getBoolean(getSettings(), COL_SEPARATE_OBJECTS));

    int defWidth = Math.max(150, canvasSize.getWidth() / 10);

    int minWidth = TimeBoardHelper.DEFAULT_MOVER_WIDTH + 1;
    int maxWidth = canvasSize.getWidth() / 3;

    setCustomerWidth(TimeBoardHelper.getPixels(getSettings(), COL_PIXELS_PER_COMPANY,
        defWidth, minWidth, maxWidth));

    if (separateObjects()) {
      setInfoWidth(TimeBoardHelper.getPixels(getSettings(), COL_PIXELS_PER_INFO,
          defWidth, minWidth, maxWidth));
    } else {
      setInfoWidth(0);
    }

    setChartLeft(getCustomerWidth() + getInfoWidth());
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());

    setDayColumnWidth(TimeBoardHelper.getPixels(getSettings(), COL_PIXELS_PER_DAY,
        20, 1, getChartWidth()));
  }

  @Override
  protected void refresh() {
    BeeKeeper.getRpc().makeRequest(ServiceKeeper.createArgs(SVC_GET_CALENDAR_DATA),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (setData(response)) {
              render(false);
            }
          }
        });
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    customerPanels.clear();
    infoPanels.clear();

    customerIndexesByRow.clear();
    objectsByRow.clear();

    List<TimeBoardRowLayout> customerLayout = doLayout();

    int rc = TimeBoardRowLayout.countRows(customerLayout, 1);
    initContent(panel, rc);

    if (customerLayout.isEmpty()) {
      return;
    }

    int calendarWidth = getCalendarWidth();

    Double opacity = TimeBoardHelper.getOpacity(getSettings(), COL_ITEM_OPACITY);

    Edges margins = new Edges();
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    Widget itemWidget;
    int rowIndex = 0;

    for (TimeBoardRowLayout layout : customerLayout) {
      int customerIndex = layout.getDataIndex();

      int size = layout.getSize(1);
      int lastRow = rowIndex + size - 1;

      int top = rowIndex * getRowHeight();

      if (rowIndex > 0) {
        TimeBoardHelper.addRowSeparator(panel, STYLE_CUSTOMER_ROW_SEPARATOR, top, 0,
            getChartLeft() + calendarWidth);
      }

      ServiceCustomerWrapper customer = customers.get(customerIndex);
      Assert.notNull(customer, "customer not found");

      IdentifiableWidget customerWidget = createCustomerWidget(customer);
      addCustomerWidget(panel, customerWidget, rowIndex, lastRow);

      if (size > 1) {
        renderRowSeparators(panel, rowIndex, lastRow);
      }

      for (int i = 0; i < layout.getRows().size(); i++) {
        for (HasDateRange item : layout.getRows().get(i).getRowItems()) {

          if (item instanceof TaskWrapper) {
            itemWidget = createTaskWidget((TaskWrapper) item);
          } else if (item instanceof RecurringTaskWrapper) {
            itemWidget = createRecurringTaskWidget((RecurringTaskWrapper) item);
          } else {
            itemWidget = null;
          }

          if (itemWidget != null) {
            Rectangle rectangle = getRectangle(item.getRange(), rowIndex + i);
            TimeBoardHelper.apply(itemWidget, rectangle, margins);

            styleItemWidget(item, itemWidget);
            if (opacity != null) {
              StyleUtils.setOpacity(itemWidget, opacity);
            }

            panel.add(itemWidget);
          }
        }
      }

      for (int i = 0; i < size; i++) {
        customerIndexesByRow.add(customerIndex);
      }

      rowIndex += size;
    }
  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
    Mover customerMover = TimeBoardHelper.createHorizontalMover();
    StyleUtils.setLeft(customerMover, getCustomerWidth() - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(customerMover, height);

    customerMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onCustomerResize(event);
      }
    });

    panel.add(customerMover);

    if (separateObjects()) {
      Mover infoMover = TimeBoardHelper.createHorizontalMover();
      StyleUtils.setLeft(infoMover, getChartLeft() - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
      StyleUtils.setHeight(infoMover, height);

      infoMover.addMoveHandler(new MoveEvent.Handler() {
        @Override
        public void onMove(MoveEvent event) {
          onInfoResize(event);
        }
      });

      panel.add(infoMover);
    }
  }

  @Override
  protected boolean setData(ResponseObject response) {
    if (!Queries.checkResponse(getCaption(), VIEW_SERVICE_SETTINGS, response, BeeRowSet.class)) {
      return false;
    }

    BeeRowSet rowSet = BeeRowSet.restore((String) response.getResponse());
    setSettings(rowSet);

    initData(rowSet.getTableProperties());
    updateMaxRange();

    return true;
  }

  @Override
  protected void setItemWidgetColor(HasDateRange item, Widget widget) {
    if (item instanceof TaskWrapper) {
      setTaskColor((TaskWrapper) item, widget);
    } else if (item instanceof RecurringTaskWrapper) {
      setRecurringTaskColor(widget);
    }
  }
  
  private void addCustomerWidget(HasWidgets panel, IdentifiableWidget widget,
      int firstRow, int lastRow) {

    Rectangle rectangle = TimeBoardHelper.getRectangle(0, getCustomerWidth(), firstRow, lastRow,
        getRowHeight());

    Edges margins = new Edges();
    margins.setRight(TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    TimeBoardHelper.apply(widget.asWidget(), rectangle, margins);

    panel.add(widget.asWidget());

    customerPanels.add(widget.getId());
  }

  private boolean containsCustomer(Long id) {
    if (!DataUtils.isId(id)) {
      return false;
    }

    for (ServiceCustomerWrapper wrapper : customers) {
      if (id.equals(wrapper.getId())) {
        return true;
      }
    }
    return false;
  }

  private IdentifiableWidget createCustomerWidget(ServiceCustomerWrapper customer) {
    Simple panel = new Simple();
    panel.addStyleName(STYLE_CUSTOMER_PANEL);

    CustomDiv label = new CustomDiv(STYLE_CUSTOMER_LABEL);
    label.setText(customer.getName());

    bindOpener(label, ClassifierConstants.VIEW_COMPANIES, customer.getId());

    panel.add(label);
    return panel;
  }

  private void setRecurringTaskColor(Widget widget) {
    String color = BeeUtils.notEmpty(TimeBoardHelper.getString(getSettings(), COL_RT_COLOR),
        DEFAULT_RT_COLOR);
    StyleUtils.setBackgroundColor(widget, color);
  }
  
  private Widget createRecurringTaskWidget(RecurringTaskWrapper recurringTask) {
    Flow panel = new Flow(STYLE_RT_PANEL);

    TaskPriority priority = recurringTask.getPriority();
    if (priority != null) {
      panel.addStyleName(STYLE_RT_PRIORITY + BeeUtils.toString(priority.ordinal()));
    }
    
    setRecurringTaskColor(panel);

    panel.setTitle(recurringTask.getSummary());
    bindOpener(panel, TaskConstants.VIEW_RECURRING_TASKS, recurringTask.getId());

    return panel;
  }

  private Widget createTaskWidget(TaskWrapper task) {
    Flow panel = new Flow(STYLE_TASK_PANEL);
    
    TaskPriority priority = task.getPriority();
    if (priority != null) {
      panel.addStyleName(STYLE_TASK_PRIORITY + BeeUtils.toString(priority.ordinal()));
    }

    TaskStatus status = task.getStatus();
    if (status != null) {
      panel.addStyleName(STYLE_TASK_STATUS + BeeUtils.toString(status.ordinal()));
    }
    
    setTaskColor(task, panel);

    panel.setTitle(task.getSummary());
    bindOpener(panel, TaskConstants.VIEW_TASKS, task.getId());

    return panel;
  }
  
  private List<TimeBoardRowLayout> doLayout() {
    List<TimeBoardRowLayout> result = Lists.newArrayList();
    Range<JustDate> range = getVisibleRange();

    for (int customerIndex = 0; customerIndex < customers.size(); customerIndex++) {
      ServiceCustomerWrapper customer = customers.get(customerIndex);
      long customerId = customer.getId();

      TimeBoardRowLayout layout = new TimeBoardRowLayout(customerIndex);

      List<ServiceObjectWrapper> customerObjects = getObjectsForLayout(customerId);

      for (ServiceObjectWrapper object : customerObjects) {
        Long objectId = object.getId();

        if (tasks.containsKey(objectId)) {
          layout.addItems(objectId, TimeBoardHelper.getActiveItems(tasks.get(objectId), range),
              range);
        }
        if (recurringTasks.containsKey(objectId)) {
          layout.addItems(objectId, TimeBoardHelper.getActiveItems(recurringTasks.get(objectId),
              range), range);
        }
      }

      if (!layout.isEmpty()) {
        result.add(layout);
      }
    }

    return result;
  }

  private int getCustomerWidth() {
    return customerWidth;
  }

  private int getInfoWidth() {
    return infoWidth;
  }

  private List<ServiceObjectWrapper> getObjectsForLayout(Long customerId) {
    List<ServiceObjectWrapper> result = new ArrayList<>();

    if (objects.containsKey(customerId)) {
      result.addAll(objects.get(customerId));
    }
    return result;
  }

  private void setTaskColor(TaskWrapper task, Widget widget) {
    String color;
    TaskStatus status = task.getStatus();

    if (status != null) {
      String colName = COL_TASK_COLOR + BeeUtils.toString(status.ordinal());
      color = TimeBoardHelper.getString(getSettings(), colName);
    } else {
      color = null;
    }

    if (BeeUtils.isEmpty(color)) {
      color = TimeBoardHelper.getString(getSettings(), COL_TASK_COLOR);

      if (BeeUtils.isEmpty(color)) {
        color = DEFAULT_TASK_COLOR;
      }
    }

    StyleUtils.setBackgroundColor(widget, color);
  }

  private void initData(Map<String, String> properties) {
    customers.clear();
    objects.clear();

    tasks.clear();
    recurringTasks.clear();

    if (BeeUtils.isEmpty(properties)) {
      return;
    }

    SimpleRowSet objectData = SimpleRowSet.getIfPresent(properties, TBL_SERVICE_OBJECTS);

    SimpleRowSet relationData = SimpleRowSet.getIfPresent(properties,
        AdministrationConstants.TBL_RELATIONS);

    SimpleRowSet taskData = SimpleRowSet.getIfPresent(properties, TaskConstants.TBL_TASKS);
    SimpleRowSet rtData = SimpleRowSet.getIfPresent(properties, TaskConstants.TBL_RECURRING_TASKS);

    if (DataUtils.isEmpty(objectData) || DataUtils.isEmpty(relationData)
        || DataUtils.isEmpty(taskData) && DataUtils.isEmpty(rtData)) {
      return;
    }

    for (SimpleRow row : objectData) {
      Long customerId = row.getLong(COL_SERVICE_OBJECT_CUSTOMER);

      if (DataUtils.isId(customerId)) {
        if (!containsCustomer(customerId)) {
          customers.add(new ServiceCustomerWrapper(customerId,
              row.getValue(ALS_SERVICE_CUSTOMER_NAME)));
        }

        objects.put(customerId, new ServiceObjectWrapper(row));
      }
    }

    if (!DataUtils.isEmpty(taskData)) {
      for (SimpleRow taskRow : taskData) {
        TaskWrapper wrapper = new TaskWrapper(taskRow);
        Long taskId = wrapper.getId();

        if (DataUtils.isId(taskId)) {
          for (SimpleRow relationRow : relationData) {
            if (taskId.equals(relationRow.getLong(TaskConstants.COL_TASK))) {
              Long objId = relationRow.getLong(COL_SERVICE_OBJECT);
              if (DataUtils.isId(objId)) {
                tasks.put(objId, wrapper);
              }
            }
          }
        }
      }
    }

    if (!DataUtils.isEmpty(rtData)) {
      for (SimpleRow rtRow : rtData) {
        RecurringTaskWrapper wrapper = new RecurringTaskWrapper(rtRow);
        Long rtId = wrapper.getId();

        if (DataUtils.isId(rtId)) {
          for (SimpleRow relationRow : relationData) {
            if (rtId.equals(relationRow.getLong(TaskConstants.COL_RECURRING_TASK))) {
              Long objId = relationRow.getLong(COL_SERVICE_OBJECT);
              if (DataUtils.isId(objId)) {
                recurringTasks.put(objId, wrapper);
              }
            }
          }
        }
      }
    }
  }

  private void onCustomerResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft;
    if (separateObjects()) {
      maxLeft = getChartLeft() - TimeBoardHelper.DEFAULT_MOVER_WIDTH * 2 - 1;
    } else {
      maxLeft = getLastResizableColumnMaxLeft(0);
    }

    int newLeft = BeeUtils.clamp(oldLeft + delta, 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      int customerPx = newLeft + TimeBoardHelper.DEFAULT_MOVER_WIDTH;
      int infoPx = separateObjects() ? getChartLeft() - customerPx : BeeConst.UNDEF;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : customerPanels) {
          StyleUtils.setWidth(DomUtils.getElement(id),
              customerPx - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
        }

        if (separateObjects()) {
          for (String id : infoPanels) {
            Element element = Document.get().getElementById(id);
            if (element != null) {
              StyleUtils.setLeft(element, customerPx);
              StyleUtils.setWidth(element, infoPx - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
            }
          }
        }
      }

      if (event.isFinished()) {
        if (separateObjects()) {
          if (updateSettings(COL_PIXELS_PER_COMPANY, customerPx, COL_PIXELS_PER_INFO, infoPx)) {
            setCustomerWidth(customerPx);
            setInfoWidth(infoPx);
          }

        } else if (updateSetting(COL_PIXELS_PER_COMPANY, customerPx)) {
          setCustomerWidth(customerPx);
          render(false);
        }
      }
    }
  }

  private void onInfoResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft = getLastResizableColumnMaxLeft(getCustomerWidth());
    int newLeft = BeeUtils.clamp(oldLeft + delta, getCustomerWidth() + 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      int infoPx = newLeft - getCustomerWidth() + TimeBoardHelper.DEFAULT_MOVER_WIDTH;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : infoPanels) {
          StyleUtils.setWidth(DomUtils.getElement(id),
              infoPx - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
        }
      }

      if (event.isFinished() && updateSetting(COL_PIXELS_PER_INFO, infoPx)) {
        setInfoWidth(infoPx);
        render(false);
      }
    }
  }

  private void renderRowSeparators(ComplexPanel panel, int firstRow, int lastRow) {
    for (int rowIndex = firstRow; rowIndex < lastRow; rowIndex++) {
      TimeBoardHelper.addRowSeparator(panel, (rowIndex + 1) * getRowHeight(), getChartLeft(),
          getCalendarWidth());
    }
  }

  private boolean separateObjects() {
    return separateObjects;
  }

  private void setCustomerWidth(int customerWidth) {
    this.customerWidth = customerWidth;
  }

  private void setInfoWidth(int infoWidth) {
    this.infoWidth = infoWidth;
  }

  private void setSeparateObjects(boolean separateObjects) {
    this.separateObjects = separateObjects;
  }
}
