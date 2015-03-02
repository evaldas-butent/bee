package com.butent.bee.client.modules.administration;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.DndDiv;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class CompanyStructureForm extends AbstractFormInterceptor implements HandlesAllDataEvents {

  private static final BeeLogger logger = LogUtils.getLogger(CompanyStructureForm.class);

  private static final List<String> viewNames = Lists.newArrayList(VIEW_DEPARTMENTS,
      VIEW_DEPARTMENT_EMPLOYEES);

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "orgChart-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_NODE_ROW = STYLE_PREFIX + "node-row";
  // private static final String STYLE_CONN_ROW = STYLE_PREFIX + "conn-row";

  private static final String STYLE_LEVEL_PREFIX = STYLE_PREFIX + "level-";

  private static final String STYLE_DEPARTMENT_PREFIX = STYLE_PREFIX + "department-";
  private static final String STYLE_DEPARTMENT_CELL = STYLE_DEPARTMENT_PREFIX + "cell";
  private static final String STYLE_DEPARTMENT_PANEL = STYLE_DEPARTMENT_PREFIX + "panel";
  private static final String STYLE_DEPARTMENT_LABEL = STYLE_DEPARTMENT_PREFIX + "label";

  private static final String STYLE_DEPARTMENT_DRAG = STYLE_DEPARTMENT_PREFIX + "drag";
  private static final String STYLE_DEPARTMENT_DRAG_OVER = STYLE_DEPARTMENT_PREFIX + "dragOver";

  private static final String STYLE_DEPARTMENT_BOSS = STYLE_DEPARTMENT_PREFIX + "boss";
  private static final String STYLE_BOSS_DRAG = STYLE_DEPARTMENT_BOSS + "-drag";

  private static final String STYLE_EMPLOYEE_PREFIX = STYLE_PREFIX + "employee-";
  private static final String STYLE_EMPLOYEE_PANEL = STYLE_EMPLOYEE_PREFIX + "panel";
  private static final String STYLE_EMPLOYEE_LABEL = STYLE_EMPLOYEE_PREFIX + "label";
  private static final String STYLE_EMPLOYEE_DRAG = STYLE_EMPLOYEE_PREFIX + "drag";

  private static final String DATA_TYPE_DEPARTMENT = "OrgChartDepartment";
  private static final String DATA_TYPE_BOSS = "OrgChartBoss";
  private static final String DATA_TYPE_EMPLOYEE = "OrgChartEmployee";

  private static final Set<String> DND_TYPES = ImmutableSet.of(DATA_TYPE_DEPARTMENT,
      DATA_TYPE_BOSS, DATA_TYPE_EMPLOYEE);

  private final List<HandlerRegistration> handlerRegistry = new ArrayList<>();

  private BeeRowSet departments;
  private BeeRowSet employees;

  CompanyStructureForm() {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case ADD:
        RowFactory.createRow(VIEW_DEPARTMENTS, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            refresh();
          }
        });
        return false;

      case REFRESH:
        refresh();
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CompanyStructureForm();
  }

  @Override
  public void onLoad(FormView form) {
    if (handlerRegistry.isEmpty()) {
      handlerRegistry.addAll(BeeKeeper.getBus().registerDataHandler(this, false));
    }
    refresh();

    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    EventUtils.clearRegistry(handlerRegistry);
    super.onUnload(form);
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (isEventRelevant(event)) {
      refresh();
    }
  }

  private static boolean isEventRelevant(DataEvent event) {
    for (String viewName : viewNames) {
      if (event.hasView(viewName)) {
        return true;
      }
    }
    return false;
  }

  private Flow getPanel() {
    IdentifiableWidget widget = getFormView().getRootWidget();
    if (widget instanceof Flow) {
      return (Flow) widget;
    } else {
      logger.severe(NameUtils.getName(this), "panel not found");
      return null;
    }
  }

  private Multimap<Integer, Long> layout() {
    List<Long> rowIds = departments.getRowIds();

    Multimap<Long, Long> children = ArrayListMultimap.create();
    Multimap<Integer, Long> levels = ArrayListMultimap.create();

    int parentIndex = departments.getColumnIndex(COL_DEPARTMENT_PARENT);
    for (BeeRow row : departments) {
      Long id = row.getId();
      Long parent = row.getLong(parentIndex);

      if (DataUtils.isId(parent) && !Objects.equals(id, parent) && rowIds.contains(id)) {
        children.put(parent, id);
      } else {
        levels.put(0, id);
      }
    }

    for (int level = 0; level < departments.getNumberOfRows(); level++) {
      List<Long> parents = new ArrayList<>(levels.get(level));
      if (parents.isEmpty()) {
        break;
      }

      for (Long parent : parents) {
        if (children.containsKey(parent)) {
          for (Long child : children.get(parent)) {
            if (!levels.containsValue(child)) {
              levels.put(level + 1, child);
            }
          }
        }
      }
    }

    return levels;
  }

  private void refresh() {
    Queries.getData(viewNames, CachingPolicy.NONE, new Queries.DataCallback() {
      @Override
      public void onSuccess(Collection<BeeRowSet> result) {
        for (BeeRowSet rowSet : result) {
          switch (rowSet.getViewName()) {
            case VIEW_DEPARTMENTS:
              departments = rowSet;
              break;

            case VIEW_DEPARTMENT_EMPLOYEES:
              employees = rowSet;
              break;
          }
        }

        Flow panel = getPanel();
        if (panel != null) {
          panel.clear();

          if (!DataUtils.isEmpty(departments)) {
            render(panel, layout());
          }
        }
      }
    });
  }

  private void render(Flow panel, Multimap<Integer, Long> levels) {
    HtmlTable table = new HtmlTable(STYLE_TABLE);

    int row = 0;
    int col = 0;

    for (int level : levels.keySet()) {
      col = 0;

      for (long depId : levels.get(level)) {
        table.setWidget(row, col++, renderDepartment(depId), STYLE_DEPARTMENT_CELL);
      }

      table.getRowFormatter().addStyleName(row, STYLE_NODE_ROW);
      table.getRowFormatter().addStyleName(row, STYLE_LEVEL_PREFIX + BeeUtils.toString(level));

      row++;
    }

    panel.add(table);
  }

  private Widget renderDepartment(final long id) {
    final Flow panel = new Flow(STYLE_DEPARTMENT_PANEL);

    BeeRow department = departments.getRowById(id);

    DndDiv label = new DndDiv(STYLE_DEPARTMENT_LABEL);
    label.setText(DataUtils.getString(departments, department, COL_DEPARTMENT_NAME));

    label.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RowEditor.open(VIEW_DEPARTMENTS, id, Opener.MODAL, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            refresh();
          }
        });
      }
    });

    DndHelper.makeSource(label, DATA_TYPE_DEPARTMENT, id, STYLE_DEPARTMENT_DRAG);

    panel.add(label);

    Long head = DataUtils.getLong(departments, department, COL_DEPARTMENT_HEAD);
    if (DataUtils.isId(head)) {
      panel.add(renderBoss(department));
    }

    List<BeeRow> depEmployees = filterEmployees(id, head);
    if (!depEmployees.isEmpty()) {
      panel.add(renderEmployees(depEmployees));
    }

    DndHelper.makeTarget(panel, DND_TYPES, STYLE_DEPARTMENT_DRAG_OVER,
        new Predicate<Object>() {
          @Override
          public boolean apply(Object input) {
            return true;
          }
        }, new BiConsumer<DropEvent, Object>() {
          @Override
          public void accept(DropEvent t, Object u) {
            panel.removeStyleName(STYLE_DEPARTMENT_DRAG_OVER);
          }
        });

    return panel;
  }

  private Widget renderBoss(BeeRow department) {
    DndDiv widget = new DndDiv(STYLE_DEPARTMENT_BOSS);
    widget.setText(join(DataUtils.getString(departments, department, COL_FIRST_NAME),
        DataUtils.getString(departments, department, COL_LAST_NAME),
        DataUtils.getString(departments, department, ALS_POSITION_NAME)));

    addEmployeeHandlers(widget, DataUtils.getLong(departments, department, COL_COMPANY_PERSON),
        DATA_TYPE_BOSS);

    return widget;
  }

  private List<BeeRow> filterEmployees(Long depId, Long exclude) {
    if (DataUtils.isEmpty(employees)) {
      return Collections.emptyList();

    } else {
      List<BeeRow> rows = DataUtils.filterRows(employees, COL_DEPARTMENT, depId);

      if (!rows.isEmpty() && DataUtils.isId(exclude)) {
        int index = BeeConst.UNDEF;
        for (int i = 0; i < rows.size(); i++) {
          if (DataUtils.idEquals(rows.get(i), exclude)) {
            index = i;
            break;
          }
        }

        if (!BeeConst.isUndef(index)) {
          rows.remove(index);
        }
      }
      return rows;
    }
  }

  private Widget renderEmployee(BeeRow employee) {
    DndDiv widget = new DndDiv(STYLE_EMPLOYEE_LABEL);
    widget.setText(join(DataUtils.getString(employees, employee, COL_FIRST_NAME),
        DataUtils.getString(employees, employee, COL_LAST_NAME),
        DataUtils.getString(employees, employee, ALS_POSITION_NAME)));

    addEmployeeHandlers(widget, DataUtils.getLong(employees, employee, COL_COMPANY_PERSON),
        DATA_TYPE_EMPLOYEE);

    return widget;
  }

  private Widget renderEmployees(List<BeeRow> depEmployees) {
    Flow panel = new Flow(STYLE_EMPLOYEE_PANEL);

    for (BeeRow employee : depEmployees) {
      panel.add(renderEmployee(employee));
    }

    return panel;
  }

  private void addEmployeeHandlers(DndDiv widget, final long id, String dataType) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RowEditor.open(VIEW_COMPANY_PERSONS, id, Opener.MODAL, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            refresh();
          }
        });
      }
    });

    switch (dataType) {
      case DATA_TYPE_BOSS:
        DndHelper.makeSource(widget, dataType, id, STYLE_BOSS_DRAG);
        break;

      case DATA_TYPE_EMPLOYEE:
        DndHelper.makeSource(widget, dataType, id, STYLE_EMPLOYEE_DRAG);
        break;
    }
  }

  private static String join(String firstName, String lastName, String position) {
    return BeeUtils.joinItems(BeeUtils.joinWords(firstName, lastName), position);
  }
}
