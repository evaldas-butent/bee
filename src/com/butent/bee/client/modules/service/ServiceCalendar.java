package com.butent.bee.client.modules.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
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
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.layout.Flow;
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
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ServiceCalendar extends TimeBoard {

  static final String SUPPLIER_KEY = "service_calendar";

  private static final String COL_COMPANY_KIND = "CalendarCompanyKind";

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

  private static final String STYLE_COMPANY_PREFIX = STYLE_PREFIX + "company-";
  private static final String STYLE_COMPANY_ROW_SEPARATOR = STYLE_COMPANY_PREFIX + "row-sep";
  private static final String STYLE_COMPANY_PANEL = STYLE_COMPANY_PREFIX + "panel";
  private static final String STYLE_COMPANY_LABEL = STYLE_COMPANY_PREFIX + "label";

  private static final String STYLE_INFO_PREFIX = STYLE_PREFIX + "info-";
  private static final String STYLE_INFO_ROW_SEPARATOR = STYLE_INFO_PREFIX + "row-sep";
  private static final String STYLE_INFO_PANEL = STYLE_INFO_PREFIX + "panel";
  private static final String STYLE_INFO_LABEL = STYLE_INFO_PREFIX + "label";

  private static final String STYLE_TASK_PREFIX = STYLE_PREFIX + "task-";
  private static final String STYLE_TASK_PANEL = STYLE_TASK_PREFIX + "panel";
  private static final String STYLE_TASK_STATUS = STYLE_TASK_PREFIX + "status";
  private static final String STYLE_TASK_PRIORITY = STYLE_TASK_PREFIX + "priority";
  private static final String STYLE_TASK_STARRED = STYLE_TASK_PREFIX + "starred";

  private static final String STYLE_RT_PREFIX = STYLE_PREFIX + "rt-";
  private static final String STYLE_RT_PANEL = STYLE_RT_PREFIX + "panel";
  private static final String STYLE_RT_PRIORITY = STYLE_RT_PREFIX + "priority";

  private static final String STYLE_DATE_PREFIX = STYLE_PREFIX + "date-";
  private static final String STYLE_DATE_PANEL = STYLE_DATE_PREFIX + "panel";

  private static final String DEFAULT_TASK_COLOR = Colors.GOLD;
  private static final String DEFAULT_RT_COLOR = Colors.YELLOW;
  private static final String DEFAULT_DATE_COLOR = Colors.RED;

  private static final Set<String> relevantDataViews = Sets.newHashSet(VIEW_SERVICE_OBJECTS,
      VIEW_SERVICE_DATES, TaskConstants.VIEW_TASKS, TaskConstants.VIEW_RECURRING_TASKS,
      TaskConstants.VIEW_TASK_TYPES);

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

  private static void setDateColor(ServiceDateWrapper date, Widget widget) {
    String color = BeeUtils.notEmpty(date.getColor(), DEFAULT_DATE_COLOR);
    StyleUtils.setBackgroundColor(widget, color);
  }
  private final List<ServiceCompanyWrapper> companies = new ArrayList<>();

  private final Multimap<Long, ServiceObjectWrapper> objects = ArrayListMultimap.create();
  private final Multimap<Long, TaskWrapper> tasks = ArrayListMultimap.create();

  private final Multimap<Long, RecurringTaskWrapper> recurringTasks = ArrayListMultimap.create();

  private final Multimap<Long, ServiceDateWrapper> dates = ArrayListMultimap.create();
  private int companyWidth = BeeConst.UNDEF;

  private int infoWidth = BeeConst.UNDEF;

  private boolean separateObjects;
  private final Set<String> companyPanels = new HashSet<>();

  private final Set<String> infoPanels = new HashSet<>();
  private final List<Integer> companyIndexesByRow = new ArrayList<>();

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
    switch (action) {
      case ADD:
        RowFactory.createRow(VIEW_SERVICE_OBJECTS);
        break;

      case EXPORT:
        export();
        break;

      default:
        super.handleAction(action);
    }
  }

  protected void addInfoWidget(HasWidgets panel, IdentifiableWidget widget,
      int firstRow, int lastRow) {

    Rectangle rectangle = TimeBoardHelper.getRectangle(getCompanyWidth(), getInfoWidth(),
        firstRow, lastRow, getRowHeight());

    Edges margins = new Edges();
    margins.setRight(TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    TimeBoardHelper.apply(widget.asWidget(), rectangle, margins);

    panel.add(widget.asWidget());

    infoPanels.add(widget.getId());
  }

  @Override
  protected void editSettings() {
    final BeeRow oldRow = getSettingsRow();
    Assert.notNull(oldRow);

    RowEditor.openRow(FORM_SETTINGS, getSettings().getViewName(), oldRow, true,
        new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            if (updateSetting(result)) {
              int typesIndex = getSettings().getColumnIndex(COL_SERVICE_CALENDAR_TASK_TYPES);
              int kindIndex = getSettings().getColumnIndex(COL_COMPANY_KIND);

              if (Objects.equals(oldRow.getString(typesIndex), result.getString(typesIndex))
                  && Objects.equals(oldRow.getInteger(kindIndex), result.getInteger(kindIndex))) {
                render(false);
              } else {
                refresh();
              }
            }
          }
        });
  }

  @Override
  protected Collection<? extends HasDateRange> getChartItems() {
    List<HasDateRange> items = Lists.newArrayList();

    items.addAll(tasks.values());
    items.addAll(recurringTasks.values());

    items.addAll(dates.values());

    return items;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.ADD, Action.EXPORT, Action.CONFIGURE);
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

    int defWidth = Math.max(120, canvasSize.getWidth() / 10);

    int minWidth = TimeBoardHelper.DEFAULT_MOVER_WIDTH + 1;
    int maxWidth = canvasSize.getWidth() / 3;

    setCompanyWidth(TimeBoardHelper.getPixels(getSettings(), COL_PIXELS_PER_COMPANY,
        defWidth, minWidth, maxWidth));

    if (separateObjects()) {
      setInfoWidth(TimeBoardHelper.getPixels(getSettings(), COL_PIXELS_PER_INFO,
          defWidth, minWidth, maxWidth));
    } else {
      setInfoWidth(0);
    }

    setChartLeft(getCompanyWidth() + getInfoWidth());
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
    companyPanels.clear();
    infoPanels.clear();

    companyIndexesByRow.clear();
    objectsByRow.clear();

    List<TimeBoardRowLayout> boardLayout = doLayout();

    int rc = TimeBoardRowLayout.countRows(boardLayout, 1);
    initContent(panel, rc);

    if (boardLayout.isEmpty()) {
      return;
    }

    int calendarWidth = getCalendarWidth();

    Double opacity = TimeBoardHelper.getOpacity(getSettings(), COL_ITEM_OPACITY);

    Edges margins = new Edges();
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    Widget itemWidget;
    int rowIndex = 0;

    for (TimeBoardRowLayout layout : boardLayout) {
      int companyIndex = layout.getDataIndex();

      int size = layout.getSize(1);
      int lastRow = rowIndex + size - 1;

      int top = rowIndex * getRowHeight();

      if (rowIndex > 0) {
        TimeBoardHelper.addRowSeparator(panel, STYLE_COMPANY_ROW_SEPARATOR, top, 0,
            getChartLeft() + calendarWidth);
      }

      ServiceCompanyWrapper company = companies.get(companyIndex);
      Assert.notNull(company, "company not found");

      IdentifiableWidget companyWidget = createCompanyWidget(company);
      addCompanyWidget(panel, companyWidget, rowIndex, lastRow);

      if (separateObjects()) {
        for (TimeBoardRowLayout.GroupLayout group : layout.getGroups()) {
          ServiceObjectWrapper object = findObject(group.getGroupId());

          if (object != null) {
            IdentifiableWidget infoWidget = createInfoWidget(object);

            int first = rowIndex + group.getFirstRow();
            int last = rowIndex + group.getLastRow();
            addInfoWidget(panel, infoWidget, first, last);

            for (int r = first; r <= last; r++) {
              objectsByRow.put(r, object.getId());
            }
          }
        }
      }

      if (size > 1) {
        renderRowSeparators(panel, rowIndex, lastRow);
      }

      for (int i = 0; i < layout.getRows().size(); i++) {
        for (HasDateRange item : layout.getRows().get(i).getRowItems()) {

          if (item instanceof TaskWrapper) {
            itemWidget = createTaskWidget((TaskWrapper) item);
          } else if (item instanceof RecurringTaskWrapper) {
            itemWidget = createRecurringTaskWidget((RecurringTaskWrapper) item);
          } else if (item instanceof ServiceDateWrapper) {
            itemWidget = createDateWidget((ServiceDateWrapper) item);
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
        companyIndexesByRow.add(companyIndex);
      }

      rowIndex += size;
    }
  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
    Mover companyMover = TimeBoardHelper.createHorizontalMover();
    StyleUtils.setLeft(companyMover, getCompanyWidth() - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(companyMover, height);

    companyMover.addMoveHandler(new MoveEvent.Handler() {
      @Override
      public void onMove(MoveEvent event) {
        onCompanyResize(event);
      }
    });

    panel.add(companyMover);

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

    TimeBoardHelper.getInteger(rowSet, COL_COMPANY_KIND);

    ServiceCompanyKind companyKind = EnumUtils.getEnumByIndex(ServiceCompanyKind.class,
        TimeBoardHelper.getInteger(rowSet, COL_COMPANY_KIND));
    if (companyKind == null) {
      companyKind = ServiceCompanyKind.DETAULT;
    }

    initData(companyKind, rowSet.getTableProperties());
    updateMaxRange();

    return true;
  }

  @Override
  protected void setItemWidgetColor(HasDateRange item, Widget widget) {
    if (item instanceof TaskWrapper) {
      setTaskColor((TaskWrapper) item, widget);
    } else if (item instanceof RecurringTaskWrapper) {
      setRecurringTaskColor((RecurringTaskWrapper) item, widget);
    } else if (item instanceof ServiceDateWrapper) {
      setDateColor((ServiceDateWrapper) item, widget);
    }
  }

  private void addCompanyWidget(HasWidgets panel, IdentifiableWidget widget,
      int firstRow, int lastRow) {

    Rectangle rectangle = TimeBoardHelper.getRectangle(0, getCompanyWidth(), firstRow, lastRow,
        getRowHeight());

    Edges margins = new Edges();
    margins.setRight(TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    TimeBoardHelper.apply(widget.asWidget(), rectangle, margins);

    panel.add(widget.asWidget());

    companyPanels.add(widget.getId());
  }

  private boolean containsCompany(Long id) {
    if (id == null) {
      return false;
    }

    for (ServiceCompanyWrapper wrapper : companies) {
      if (id.equals(wrapper.getId())) {
        return true;
      }
    }
    return false;
  }

  private IdentifiableWidget createCompanyWidget(ServiceCompanyWrapper company) {
    Flow panel = new Flow(STYLE_COMPANY_PANEL);

    CustomDiv label = new CustomDiv(STYLE_COMPANY_LABEL);

    if (DataUtils.isId(company.getId())) {
      label.setText(company.getName());
      bindOpener(label, ClassifierConstants.VIEW_COMPANIES, company.getId());
    }

    panel.add(label);
    return panel;
  }

  private Widget createDateWidget(ServiceDateWrapper date) {
    Flow panel = new Flow(STYLE_DATE_PANEL);
    setDateColor(date, panel);

    Long objectId = date.getObjectId();
    String title = date.getTitle();

    ServiceObjectWrapper object = findObject(objectId);

    if (object == null) {
      if (!BeeUtils.isEmpty(title)) {
        panel.setTitle(title);
      }

    } else {
      panel.setTitle(BeeUtils.buildLines(date.getTitle(), BeeConst.STRING_EMPTY,
          object.getTitle()));
      bindOpener(panel, VIEW_SERVICE_OBJECTS, objectId);
    }

    return panel;
  }

  private IdentifiableWidget createInfoWidget(ServiceObjectWrapper object) {
    Flow panel = new Flow(STYLE_INFO_PANEL);

    CustomDiv label = new CustomDiv(STYLE_INFO_LABEL);
    label.setText(object.getAddress());

    label.setTitle(object.getTitle());

    bindOpener(label, VIEW_SERVICE_OBJECTS, object.getId());

    panel.add(label);
    return panel;
  }

  private Widget createRecurringTaskWidget(RecurringTaskWrapper recurringTask) {
    Flow panel = new Flow(STYLE_RT_PANEL);

    TaskPriority priority = recurringTask.getPriority();
    if (priority != null) {
      panel.addStyleName(STYLE_RT_PRIORITY + BeeUtils.toString(priority.ordinal()));
    }

    setRecurringTaskColor(recurringTask, panel);

    panel.setTitle(recurringTask.getTitle());
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
    
    if (task.getStar() != null) {
      ImageResource imageResource = Stars.get(task.getStar());

      if (imageResource != null) {
        StyleUtils.setBackgroundImage(panel, imageResource.getSafeUri().asString());
        panel.addStyleName(STYLE_TASK_STARRED);
      }
    }

    panel.setTitle(task.getTitle());
    bindOpener(panel, TaskConstants.VIEW_TASKS, task.getId());

    return panel;
  }

  private List<TimeBoardRowLayout> doLayout() {
    List<TimeBoardRowLayout> result = Lists.newArrayList();
    Range<JustDate> range = getVisibleRange();

    List<HasDateRange> items;

    for (int companyIndex = 0; companyIndex < companies.size(); companyIndex++) {
      ServiceCompanyWrapper company = companies.get(companyIndex);
      long companyId = company.getId();

      TimeBoardRowLayout layout = new TimeBoardRowLayout(companyIndex);

      List<ServiceObjectWrapper> companyObjects = getObjectsForLayout(companyId);

      for (ServiceObjectWrapper object : companyObjects) {
        Long objectId = object.getId();
        Long groupId = separateObjects() ? objectId : companyId;

        if (tasks.containsKey(objectId)) {
          items = TimeBoardHelper.getActiveItems(tasks.get(objectId), range);
          layout.addItems(groupId, items, range);
        }
        if (recurringTasks.containsKey(objectId)) {
          items = TimeBoardHelper.getActiveItems(recurringTasks.get(objectId), range);
          layout.addItems(groupId, items, range);
        }

        if (dates.containsKey(objectId)) {
          items = TimeBoardHelper.getActiveItems(dates.get(objectId), range);
          layout.addItems(groupId, items, range);
        }
      }

      if (!layout.isEmpty()) {
        result.add(layout);
      }
    }

    return result;
  }

  private void export() {
  }

  private ServiceObjectWrapper findObject(Long id) {
    if (DataUtils.isId(id)) {
      for (ServiceObjectWrapper object : objects.values()) {
        if (id.equals(object.getId())) {
          return object;
        }
      }
    }
    return null;
  }

  private int getCompanyWidth() {
    return companyWidth;
  }

  private int getInfoWidth() {
    return infoWidth;
  }

  private List<ServiceObjectWrapper> getObjectsForLayout(Long companyId) {
    List<ServiceObjectWrapper> result = new ArrayList<>();

    if (objects.containsKey(companyId)) {
      result.addAll(objects.get(companyId));
    }
    return result;
  }

  private void initData(ServiceCompanyKind companyKind, Map<String, String> properties) {
    companies.clear();
    objects.clear();

    tasks.clear();
    recurringTasks.clear();

    dates.clear();

    if (BeeUtils.isEmpty(properties)) {
      return;
    }

    SimpleRowSet objectData = SimpleRowSet.getIfPresent(properties, TBL_SERVICE_OBJECTS);
    if (DataUtils.isEmpty(objectData)) {
      return;
    }

    SimpleRowSet relationData = SimpleRowSet.getIfPresent(properties,
        AdministrationConstants.TBL_RELATIONS);

    SimpleRowSet taskData = SimpleRowSet.getIfPresent(properties, TaskConstants.TBL_TASKS);
    SimpleRowSet rtData = SimpleRowSet.getIfPresent(properties, TaskConstants.TBL_RECURRING_TASKS);

    SimpleRowSet datesData = SimpleRowSet.getIfPresent(properties, TBL_SERVICE_DATES);

    String cId = (companyKind == ServiceCompanyKind.CONTRACTOR)
        ? COL_SERVICE_OBJECT_CONTRACTOR : COL_SERVICE_OBJECT_CUSTOMER;
    String cName = (companyKind == ServiceCompanyKind.CONTRACTOR)
        ? ALS_SERVICE_CONTRACTOR_NAME : ALS_SERVICE_CUSTOMER_NAME;

    for (SimpleRow row : objectData) {
      long companyId = BeeUtils.unbox(row.getLong(cId));
      if (!containsCompany(companyId)) {
        companies.add(new ServiceCompanyWrapper(companyId, row.getValue(cName)));
      }

      objects.put(companyId, new ServiceObjectWrapper(row));
    }

    if (companies.size() > 1) {
      Collections.sort(companies);
    }

    if (!DataUtils.isEmpty(taskData) && !DataUtils.isEmpty(relationData)) {
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

    if (!DataUtils.isEmpty(rtData) && !DataUtils.isEmpty(relationData)) {
      for (SimpleRow rtRow : rtData) {
        Long rtId = rtRow.getLong(RecurringTaskWrapper.ID_COLUMN);

        if (DataUtils.isId(rtId)) {
          for (SimpleRow relationRow : relationData) {
            if (rtId.equals(relationRow.getLong(TaskConstants.COL_RECURRING_TASK))) {
              Long objId = relationRow.getLong(COL_SERVICE_OBJECT);

              if (DataUtils.isId(objId)) {
                List<RecurringTaskWrapper> rts = RecurringTaskWrapper.spawn(rtRow);
                if (!rts.isEmpty()) {
                  recurringTasks.putAll(objId, rts);
                }
              }
            }
          }
        }
      }
    }

    if (!DataUtils.isEmpty(datesData)) {
      for (SimpleRow dateRow : datesData) {
        Long objId = dateRow.getLong(COL_SERVICE_OBJECT);

        if (DataUtils.isId(objId)) {
          ServiceDateWrapper wrapper = new ServiceDateWrapper(dateRow);
          dates.put(objId, wrapper);
        }
      }
    }
  }
  
  private void onCompanyResize(MoveEvent event) {
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
      int companyPx = newLeft + TimeBoardHelper.DEFAULT_MOVER_WIDTH;
      int infoPx = separateObjects() ? getChartLeft() - companyPx : BeeConst.UNDEF;

      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);

        for (String id : companyPanels) {
          StyleUtils.setWidth(DomUtils.getElement(id),
              companyPx - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
        }

        if (separateObjects()) {
          for (String id : infoPanels) {
            Element element = Document.get().getElementById(id);
            if (element != null) {
              StyleUtils.setLeft(element, companyPx);
              StyleUtils.setWidth(element, infoPx - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
            }
          }
        }
      }

      if (event.isFinished()) {
        if (separateObjects()) {
          if (updateSettings(COL_PIXELS_PER_COMPANY, companyPx, COL_PIXELS_PER_INFO, infoPx)) {
            setCompanyWidth(companyPx);
            setInfoWidth(infoPx);
          }

        } else if (updateSetting(COL_PIXELS_PER_COMPANY, companyPx)) {
          setCompanyWidth(companyPx);
          render(false);
        }
      }
    }
  }

  private void onInfoResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();
    int oldLeft = StyleUtils.getLeft(resizer);

    int maxLeft = getLastResizableColumnMaxLeft(getCompanyWidth());
    int newLeft = BeeUtils.clamp(oldLeft + delta, getCompanyWidth() + 1, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      int infoPx = newLeft - getCompanyWidth() + TimeBoardHelper.DEFAULT_MOVER_WIDTH;

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
    Long lastObject = objectsByRow.get(firstRow);

    for (int rowIndex = firstRow + 1; rowIndex <= lastRow; rowIndex++) {
      int top = rowIndex * getRowHeight();
      Long currentObject = objectsByRow.get(rowIndex);

      if (Objects.equals(lastObject, currentObject)) {
        TimeBoardHelper.addRowSeparator(panel, top, getChartLeft(), getCalendarWidth());
      } else {
        TimeBoardHelper.addRowSeparator(panel, STYLE_INFO_ROW_SEPARATOR, top,
            getCompanyWidth(), getInfoWidth() + getCalendarWidth());
        lastObject = currentObject;
      }
    }
  }

  private boolean separateObjects() {
    return separateObjects;
  }

  private void setCompanyWidth(int companyWidth) {
    this.companyWidth = companyWidth;
  }

  private void setInfoWidth(int infoWidth) {
    this.infoWidth = infoWidth;
  }

  private void setRecurringTaskColor(RecurringTaskWrapper recurringTask, Widget widget) {
    String color = BeeUtils.notEmpty(recurringTask.getTypeColor(),
        TimeBoardHelper.getString(getSettings(), COL_RT_COLOR), DEFAULT_RT_COLOR);
    StyleUtils.setBackgroundColor(widget, color);
  }

  private void setSeparateObjects(boolean separateObjects) {
    this.separateObjects = separateObjects;
  }

  private void setTaskColor(TaskWrapper task, Widget widget) {
    String typeColor = task.getTypeColor();

    String statusColor;
    if (task.getStatus() != null) {
      String colName = COL_TASK_COLOR + BeeUtils.toString(task.getStatus().ordinal());
      statusColor = TimeBoardHelper.getString(getSettings(), colName);
    } else {
      statusColor = null;
    }
    
    String color;
    
    if (BeeUtils.allEmpty(typeColor, statusColor)) {
      color = TimeBoardHelper.getString(getSettings(), COL_TASK_COLOR);
      if (BeeUtils.isEmpty(color)) {
        color = DEFAULT_TASK_COLOR;
      }

    } else if (BeeUtils.isEmpty(typeColor)) {
      color = statusColor;
    } else if (BeeUtils.isEmpty(statusColor)) {
      color = typeColor;

    } else {
      color = Color.blend(typeColor, statusColor);
    }

    StyleUtils.setBackgroundColor(widget, color);
  }
}
