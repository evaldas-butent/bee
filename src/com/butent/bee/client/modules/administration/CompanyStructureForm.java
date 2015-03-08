package com.butent.bee.client.modules.administration;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.PhotoRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.DndDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Line;
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
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.treelayout.Configuration;
import com.butent.bee.shared.treelayout.NodeExtentProvider;
import com.butent.bee.shared.treelayout.TreeLayout;
import com.butent.bee.shared.treelayout.util.DefaultConfiguration;
import com.butent.bee.shared.treelayout.util.DefaultTreeForTreeLayout;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class CompanyStructureForm extends AbstractFormInterceptor implements HandlesAllDataEvents {

  private static final BeeLogger logger = LogUtils.getLogger(CompanyStructureForm.class);

  private static final List<String> viewNames = Lists.newArrayList(VIEW_DEPARTMENTS,
      VIEW_DEPARTMENT_EMPLOYEES, VIEW_DEPARTMENT_POSITIONS);

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "orgChart-";

  private static final String STYLE_LEVEL_PREFIX = STYLE_PREFIX + "level-";

  private static final String STYLE_DEPARTMENT_PREFIX = STYLE_PREFIX + "department-";
  private static final String STYLE_DEPARTMENT_PANEL = STYLE_DEPARTMENT_PREFIX + "panel";
  private static final String STYLE_DEPARTMENT_LABEL = STYLE_DEPARTMENT_PREFIX + "label";
  private static final String STYLE_DEPARTMENT_CONNECT = STYLE_DEPARTMENT_PREFIX + "connect";

  private static final String STYLE_DEPARTMENT_DRAG = STYLE_DEPARTMENT_PREFIX + "drag";
  private static final String STYLE_DEPARTMENT_DRAG_OVER = STYLE_DEPARTMENT_PREFIX + "dragOver";

  private static final String STYLE_BOSS_PREFIX = STYLE_PREFIX + "boss-";
  private static final String STYLE_BOSS_CONTAINER = STYLE_BOSS_PREFIX + "container";
  private static final String STYLE_BOSS_LABEL = STYLE_BOSS_PREFIX + "label";
  private static final String STYLE_BOSS_PHOTO = STYLE_BOSS_PREFIX + "photo";
  private static final String STYLE_BOSS_DRAG = STYLE_BOSS_PREFIX + "drag";

  private static final String STYLE_EMPLOYEE_PREFIX = STYLE_PREFIX + "employee-";
  private static final String STYLE_EMPLOYEE_PANEL = STYLE_EMPLOYEE_PREFIX + "panel";
  private static final String STYLE_EMPLOYEE_CONTAINER = STYLE_EMPLOYEE_PREFIX + "container";
  private static final String STYLE_EMPLOYEE_LABEL = STYLE_EMPLOYEE_PREFIX + "label";
  private static final String STYLE_EMPLOYEE_PHOTO = STYLE_EMPLOYEE_PREFIX + "photo";
  private static final String STYLE_EMPLOYEE_DRAG = STYLE_EMPLOYEE_PREFIX + "drag";

  private static final String STYLE_TOGGLE_POSITIONS = STYLE_PREFIX + "toggle-positions";
  private static final String STYLE_TOGGLE_EMPLOYEES = STYLE_PREFIX + "toggle-employees";

  private static final String DATA_TYPE_DEPARTMENT = "OrgChartDepartment";
  private static final String DATA_TYPE_BOSS = "OrgChartBoss";
  private static final String DATA_TYPE_EMPLOYEE = "OrgChartEmployee";

  private static final Set<String> DND_TYPES = ImmutableSet.of(DATA_TYPE_DEPARTMENT,
      DATA_TYPE_BOSS, DATA_TYPE_EMPLOYEE);

  private static final Long ROOT = 0L;

  private static final String NAME_MARGIN_LEFT = "MarginLeft";
  private static final String NAME_MARGIN_TOP = "MarginTop";
  private static final String NAME_NODE_WIDTH = "NodeWidth";
  private static final String NAME_NODE_HEIGHT = "NodeHeight";
  private static final String NAME_NODE_GAP = "NodeGap";
  private static final String NAME_LEVEL_GAP = "LevelGap";

  private static final String NAME_SHOW_POSITIONS = "ShowPositions";
  private static final String NAME_SHOW_EMPLOYEES = "ShowEmployees";
  private static final String NAME_AUTO_FIT = "AutoFit";

  private static final int DEFAULT_MARGIN_LEFT = 10;
  private static final int DEFAULT_MARGIN_TOP = 10;
  private static final int DEFAULT_NODE_WIDTH = 150;
  private static final int DEFAULT_NODE_HEIGHT = 100;
  private static final int DEFAULT_NODE_GAP = 20;
  private static final int DEFAULT_LEVEL_GAP = 30;

  private static final boolean DEFAULT_SHOW_POSITIONS = false;
  private static final boolean DEFAULT_SHOW_EMPLOYEES = false;
  private static final boolean DEFAULT_AUTO_FIT = false;

  private static String storagePrefix() {
    Long userId = BeeKeeper.getUser().getUserId();
    return "CompanyStructure-" + (userId == null ? "" : BeeUtils.toString(userId)) + "-";
  }

  private static String storageKey(String name) {
    return storagePrefix() + name;
  }

  private final List<HandlerRegistration> handlerRegistry = new ArrayList<>();

  private BeeRowSet departments;
  private BeeRowSet employees;
  @SuppressWarnings("unused")
  private BeeRowSet positions;

  private int marginLeft;
  private int marginTop;
  private int nodeWidth;
  private int nodeHeight;
  private int nodeGap;
  private int levelGap;

  private boolean showPositions;
  private boolean showEmployees;
  private boolean autoFit;

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

      case AUTO_FIT:
        autoFit = !autoFit;
        store(NAME_AUTO_FIT, autoFit);

        refresh();
        return false;

      case CONFIGURE:

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

    readSettings();

    if (form.getViewPresenter() != null) {
      HeaderView header = getHeaderView();
      if (header != null) {
        addCommands(header);
      }
    }

    refresh();

    super.onLoad(form);
  }

  private static boolean readBoolean(String name, boolean defValue) {
    String key = storageKey(name);
    if (BeeKeeper.getStorage().hasItem(key)) {
      return BeeKeeper.getStorage().getBoolean(key);
    } else {
      return defValue;
    }
  }

  private static int readInt(String name) {
    Integer value = BeeKeeper.getStorage().getInteger(storageKey(name));
    return (value == null) ? BeeConst.UNDEF : value;
  }

  private static void store(String name, Boolean value) {
    BeeKeeper.getStorage().set(storageKey(name), value);
  }

  @SuppressWarnings("unused")
  private static void store(String name, Integer value) {
    BeeKeeper.getStorage().set(storageKey(name), value);
  }

  private void readSettings() {
    int value = readInt(NAME_MARGIN_LEFT);
    marginLeft = (value >= 0) ? value : DEFAULT_MARGIN_LEFT;

    value = readInt(NAME_MARGIN_TOP);
    marginTop = (value >= 0) ? value : DEFAULT_MARGIN_TOP;

    value = readInt(NAME_NODE_WIDTH);
    nodeWidth = (value > 0) ? value : DEFAULT_NODE_WIDTH;

    value = readInt(NAME_NODE_HEIGHT);
    nodeHeight = (value > 0) ? value : DEFAULT_NODE_HEIGHT;

    value = readInt(NAME_NODE_GAP);
    nodeGap = (value > 0) ? value : DEFAULT_NODE_GAP;

    value = readInt(NAME_LEVEL_GAP);
    levelGap = (value > 0) ? value : DEFAULT_LEVEL_GAP;

    showPositions = readBoolean(NAME_SHOW_POSITIONS, DEFAULT_SHOW_POSITIONS);
    showEmployees = readBoolean(NAME_SHOW_EMPLOYEES, DEFAULT_SHOW_EMPLOYEES);

    autoFit = readBoolean(NAME_AUTO_FIT, DEFAULT_AUTO_FIT);
  }

  private void addCommands(HeaderView header) {
    CheckBox positionToggle = new CheckBox(Localized.getConstants().personPositions());
    positionToggle.addStyleName(STYLE_TOGGLE_POSITIONS);

    positionToggle.setValue(showPositions);

    positionToggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        showPositions = event.getValue();
        store(NAME_SHOW_POSITIONS, showPositions);

        refresh();
      }
    });

    header.addCommandItem(positionToggle);

    CheckBox employeeToggle = new CheckBox(Localized.getConstants().employees());
    employeeToggle.addStyleName(STYLE_TOGGLE_EMPLOYEES);

    employeeToggle.setValue(showPositions);

    employeeToggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        showEmployees = event.getValue();
        store(NAME_SHOW_EMPLOYEES, showEmployees);

        refresh();
      }
    });

    header.addCommandItem(employeeToggle);
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

  private TreeLayout<Long> layoutTree() {
    DefaultTreeForTreeLayout<Long> tree = new DefaultTreeForTreeLayout<>(ROOT);

    Multimap<Long, Long> children = ArrayListMultimap.create();
    List<Long> parents = new ArrayList<>();

    List<Long> rowIds = departments.getRowIds();
    int parentIndex = departments.getColumnIndex(COL_DEPARTMENT_PARENT);

    for (BeeRow row : departments) {
      Long id = row.getId();
      Long parent = row.getLong(parentIndex);

      if (DataUtils.isId(parent) && !Objects.equals(id, parent) && rowIds.contains(parent)) {
        children.put(parent, id);
      } else {
        tree.addChild(ROOT, id);
        parents.add(id);
      }
    }

    if (!children.isEmpty()) {
      for (int i = 0; i < departments.getNumberOfRows(); i++) {
        List<Long> nodes = new ArrayList<>(parents);
        parents.clear();

        for (Long node : nodes) {
          if (children.containsKey(node)) {
            for (Long child : children.get(node)) {
              tree.addChild(node, child);
              if (children.containsKey(child)) {
                parents.add(child);
              }
            }
          }
        }

        if (parents.isEmpty()) {
          break;
        }
      }
    }

    NodeExtentProvider<Long> nodeExtentProvider = new NodeExtentProvider<Long>() {
      @Override
      public double getWidth(Long treeNode) {
        return nodeWidth;
      }

      @Override
      public double getHeight(Long treeNode) {
        return nodeHeight;
      }
    };

    Configuration<Long> configuration = new DefaultConfiguration<>(levelGap, nodeGap);

    TreeLayout<Long> treeLayout = new TreeLayout<>(tree, nodeExtentProvider, configuration);
    return treeLayout;
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

            case VIEW_DEPARTMENT_POSITIONS:
              positions = rowSet;
              break;
          }
        }

        Flow panel = getPanel();
        if (panel != null) {
          panel.clear();

          if (!DataUtils.isEmpty(departments)) {
            render(panel, layoutTree());
          }
        }
      }
    });
  }

  private void render(Flow panel, TreeLayout<Long> treeLayout) {
    Map<Long, Rectangle2D.Double> nodeBounds = new HashMap<>(treeLayout.getNodeBounds());

    int shiftX = Math.max(marginLeft, 0);
    int shiftY = Math.max(marginTop, 0) - nodeHeight - levelGap;

    if (shiftX != 0 || shiftY != 0) {
      for (Rectangle2D.Double rectangle : nodeBounds.values()) {
        rectangle.setRect(rectangle.getX() + shiftX, rectangle.getY() + shiftY,
            rectangle.getWidth(), rectangle.getHeight());
      }
    }

    for (Map.Entry<Long, Rectangle2D.Double> entry : nodeBounds.entrySet()) {
      Long node = entry.getKey();
      if (!ROOT.equals(node)) {
        int level = treeLayout.getTree().getLevel(node) - 1;
        panel.add(renderDepartment(node, level, entry.getValue()));
      }
    }

    for (Long parent : nodeBounds.keySet()) {
      if (!ROOT.equals(parent)) {
        List<Long> children = treeLayout.getTree().getChildren(parent);

        if (!BeeUtils.isEmpty(children)) {
          Rectangle2D.Double parentBounds = nodeBounds.get(parent);
          for (Long child : children) {
            Widget widget = connect(parentBounds, nodeBounds.get(child), STYLE_DEPARTMENT_CONNECT);
            if (widget != null) {
              panel.add(widget);
            }
          }
        }
      }
    }
  }

  private static Widget connect(Rectangle2D.Double parent, Rectangle2D.Double child,
      String styleName) {

    if (parent == null || child == null) {
      return null;
    }

    double x1 = parent.getCenterX();
    double y1 = parent.getMaxY();

    double x2 = child.getCenterX();
    double y2 = child.getY();

    return new Line(x1, y1, x2, y2, styleName);
  }

  private Widget renderDepartment(final long id, int level, Rectangle2D.Double bounds) {
    final Flow panel = new Flow(STYLE_DEPARTMENT_PANEL);

    if (level >= 0) {
      panel.addStyleName(STYLE_LEVEL_PREFIX + BeeUtils.toString(level));
    }

    if (bounds != null) {
      StyleUtils.makeAbsolute(panel);
      StyleUtils.setRectangle(panel, BeeUtils.round(bounds.getX()), BeeUtils.round(bounds.getY()),
          BeeUtils.round(bounds.getWidth()), BeeUtils.round(bounds.getHeight()));
    }

    BeeRow department = departments.getRowById(id);

    DndDiv label = new DndDiv(STYLE_DEPARTMENT_LABEL);

    String name = DataUtils.getString(departments, department, COL_DEPARTMENT_NAME);
    label.setText(name);

    String fullName = department.getProperty(PROP_DEPARTMENT_FULL_NAME);
    if (!BeeUtils.isEmpty(fullName) && !BeeUtils.equalsTrim(name, fullName)) {
      label.setTitle(fullName);
    }

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
    if (DataUtils.isId(head) && !DataUtils.isEmpty(employees)) {
      BeeRow employee = employees.getRowById(head);
      if (employee != null) {
        panel.add(renderEmployee(employee, true));
      }
    }

    List<BeeRow> depEmployees = filterEmployees(id, head);
    if (!depEmployees.isEmpty()) {
      panel.add(renderEmployees(depEmployees));
    }

    DndHelper.makeTarget(panel, DND_TYPES, STYLE_DEPARTMENT_DRAG_OVER,
        new Predicate<Object>() {
          @Override
          public boolean apply(Object input) {
            if (validateDataType(DndHelper.getDataType()) && validateDndContent(input)) {
              return isTarget(DndHelper.getDataType(), (Long) input, id);
            } else {
              return false;
            }
          }
        },
        new BiConsumer<DropEvent, Object>() {
          @Override
          public void accept(DropEvent t, Object u) {
            panel.removeStyleName(STYLE_DEPARTMENT_DRAG_OVER);

            if (validateDataType(DndHelper.getDataType()) && validateDndContent(u)) {
              acceptDrop(DndHelper.getDataType(), (Long) u, id);
            }
          }
        });

    return panel;
  }

  private static boolean validateDataType(String dataType) {
    return DND_TYPES.contains(dataType);
  }

  private static boolean validateDndContent(Object content) {
    if (content instanceof Long) {
      return DataUtils.isId((Long) content);
    } else {
      return false;
    }
  }

  private BeeRow findEmployee(long depId, long persId) {
    if (DataUtils.isEmpty(employees)) {
      return null;
    }

    int departmentIndex = employees.getColumnIndex(COL_DEPARTMENT);
    int personIndex = employees.getColumnIndex(COL_COMPANY_PERSON);

    for (BeeRow row : employees) {
      if (Objects.equals(row.getLong(departmentIndex), depId)
          && Objects.equals(row.getLong(personIndex), persId)) {
        return row;
      }
    }
    return null;
  }

  private boolean hasEmployee(long depId, long persId) {
    return findEmployee(depId, persId) != null;
  }

  private Long getEmployeeRelation(long emplId, String colName) {
    if (DataUtils.isEmpty(employees)) {
      return null;
    }

    BeeRow row = employees.getRowById(emplId);
    if (row == null) {
      return null;
    } else {
      return DataUtils.getLong(employees, row, colName);
    }
  }

  private Long getDepartmentParent(long depId) {
    if (DataUtils.isEmpty(departments)) {
      return null;
    }

    BeeRow row = departments.getRowById(depId);
    if (row == null) {
      return null;
    } else {
      return DataUtils.getLong(departments, row, COL_DEPARTMENT_PARENT);
    }
  }

  private boolean isTarget(String dataType, long source, long target) {
    switch (dataType) {
      case DATA_TYPE_DEPARTMENT:
        return !Objects.equals(source, target)
            && !Objects.equals(getDepartmentParent(source), target);

      case DATA_TYPE_BOSS:
      case DATA_TYPE_EMPLOYEE:
        Long persId = getEmployeeRelation(source, COL_COMPANY_PERSON);
        return DataUtils.isId(persId) && !hasEmployee(target, persId);

      default:
        return false;
    }
  }

  private void acceptDrop(String dataType, final long source, final long target) {
    switch (dataType) {
      case DATA_TYPE_DEPARTMENT:
        Long unbind = null;
        if (!DataUtils.isEmpty(departments)) {
          Long parent = target;

          for (int i = 0; i < departments.getNumberOfRows(); i++) {
            Long p = getDepartmentParent(parent);

            if (p == null) {
              break;
            } else if (Objects.equals(p, source)) {
              unbind = parent;
              break;
            } else {
              parent = p;
            }
          }
        }

        final IntCallback callback = new IntCallback() {
          @Override
          public void onSuccess(Integer result) {
            if (BeeUtils.isPositive(result)) {
              fireRefresh(VIEW_DEPARTMENTS);
            }
          }
        };

        if (unbind == null) {
          updateDepartmentParent(source, target, callback);

        } else {
          updateDepartmentParent(unbind, getDepartmentParent(source), new IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              if (BeeUtils.isPositive(result)) {
                updateDepartmentParent(source, target, callback);
              }
            }
          });
        }

        break;

      case DATA_TYPE_BOSS:
        Long oldDep = getEmployeeRelation(source, COL_DEPARTMENT);
        Queries.update(VIEW_DEPARTMENTS, oldDep, COL_DEPARTMENT_HEAD, LongValue.getNullValue(),
            new IntCallback() {
              @Override
              public void onSuccess(Integer result) {
                if (BeeUtils.isPositive(result)) {
                  fireRefresh(VIEW_DEPARTMENTS);
                  updateEmployeeDepartment(source, target);
                }
              }
            });

        break;

      case DATA_TYPE_EMPLOYEE:
        updateEmployeeDepartment(source, target);
        break;
    }
  }

  private static void updateDepartmentParent(long depId, Long parent, IntCallback callback) {
    Queries.update(VIEW_DEPARTMENTS, depId, COL_DEPARTMENT_PARENT, new LongValue(parent), callback);
  }

  private static void updateEmployeeDepartment(long emplId, long depId) {
    Queries.update(VIEW_DEPARTMENT_EMPLOYEES, emplId, COL_DEPARTMENT, new LongValue(depId),
        new IntCallback() {
          @Override
          public void onSuccess(Integer result) {
            if (BeeUtils.isPositive(result)) {
              fireRefresh(VIEW_DEPARTMENT_EMPLOYEES);
            }
          }
        });
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

  private Widget renderEmployee(BeeRow employee, boolean boss) {
    Flow container = new Flow();
    container.addStyleName(boss ? STYLE_BOSS_CONTAINER : STYLE_EMPLOYEE_CONTAINER);

    final long emplId = employee.getId();

    String fullName = BeeUtils.joinWords(
        DataUtils.getString(employees, employee, COL_FIRST_NAME),
        DataUtils.getString(employees, employee, COL_LAST_NAME));

    String positionName = DataUtils.getString(employees, employee, ALS_POSITION_NAME);
    String companyName = DataUtils.getString(employees, employee, ALS_COMPANY_NAME);

    String photo = DataUtils.getString(employees, employee, COL_PHOTO);
    if (!BeeUtils.isEmpty(photo)) {
      Image image = new Image(PhotoRenderer.getUrl(photo));
      image.addStyleName(boss ? STYLE_BOSS_PHOTO : STYLE_EMPLOYEE_PHOTO);

      image.setTitle(BeeUtils.buildLines(fullName, positionName, companyName));

      image.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Long person = getEmployeeRelation(emplId, COL_PERSON);
          RowEditor.open(VIEW_PERSONS, person, Opener.MODAL, new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              refresh();
            }
          });
        }
      });

      container.add(image);
    }

    Label label = new Label(fullName);
    label.addStyleName(boss ? STYLE_BOSS_LABEL : STYLE_EMPLOYEE_LABEL);

    label.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Long cp = getEmployeeRelation(emplId, COL_COMPANY_PERSON);
        RowEditor.open(VIEW_COMPANY_PERSONS, cp, Opener.MODAL, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            refresh();
          }
        });
      }
    });

    container.add(label);

    if (boss) {
      DndHelper.makeSource(container, DATA_TYPE_BOSS, emplId, STYLE_BOSS_DRAG);
    } else {
      DndHelper.makeSource(container, DATA_TYPE_EMPLOYEE, emplId, STYLE_EMPLOYEE_DRAG);
    }

    return container;
  }

  private Widget renderEmployees(List<BeeRow> depEmployees) {
    Flow panel = new Flow(STYLE_EMPLOYEE_PANEL);

    for (BeeRow employee : depEmployees) {
      panel.add(renderEmployee(employee, false));
    }

    return panel;
  }

  private static void fireRefresh(String viewName) {
    DataChangeEvent.fireRefresh(BeeKeeper.getBus(), viewName);
  }
}
