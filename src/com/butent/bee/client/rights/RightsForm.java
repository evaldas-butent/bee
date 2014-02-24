package com.butent.bee.client.rights;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class RightsForm extends AbstractFormInterceptor {

  private static BeeLogger logger = LogUtils.getLogger(RightsForm.class);

  private static final String STYLE_PREFIX = "bee-Rights-";

  private static final String STYLE_PANEL = STYLE_PREFIX + "panel";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private static final String STYLE_SUFFIX_CELL = "-cell";

  private static final String STYLE_ROLE_LABEL = STYLE_PREFIX + "role-label";
  private static final String STYLE_ROLE_LABEL_CELL = STYLE_ROLE_LABEL + STYLE_SUFFIX_CELL;
  private static final String STYLE_ROLE_TOGGLE = STYLE_PREFIX + "role-toggle";
  private static final String STYLE_ROLE_TOGGLE_CELL = STYLE_ROLE_TOGGLE + STYLE_SUFFIX_CELL;

  private static final String STYLE_OBJECT_LABEL = STYLE_PREFIX + "object-label";
  private static final String STYLE_OBJECT_LABEL_CELL = STYLE_OBJECT_LABEL + STYLE_SUFFIX_CELL;
  private static final String STYLE_OBJECT_TOGGLE = STYLE_PREFIX + "object-toggle";
  private static final String STYLE_OBJECT_TOGGLE_CELL = STYLE_OBJECT_TOGGLE + STYLE_SUFFIX_CELL;

  private static final String STYLE_VALUE_TOGGLE = STYLE_PREFIX + "value-toggle";
  private static final String STYLE_VALUE_CELL = STYLE_PREFIX + "value-cell";
  private static final String STYLE_VALUE_ROW = STYLE_PREFIX + "value-row";
  private static final String STYLE_VALUE_CHANGED = STYLE_PREFIX + "value-changed";
  private static final String STYLE_VALUE_DISABLED = STYLE_PREFIX + "value-disabled";

  private static final String STYLE_ROLE_ORIENTATION = STYLE_PREFIX + "role-orientation";
  private static final String STYLE_ROLE_ORIENTATION_CELL = STYLE_ROLE_ORIENTATION
      + STYLE_SUFFIX_CELL;
  private static final String STYLE_COLUMN_LABEL_ORIENTATION = STYLE_PREFIX
      + "column-label-orientation";
  private static final String STYLE_COLUMN_LABEL_ORIENTATION_CELL =
      STYLE_COLUMN_LABEL_ORIENTATION + STYLE_SUFFIX_CELL;

  private static final String STYLE_USER_COMMAND = STYLE_PREFIX + "user-command";
  private static final String STYLE_USER_CLEAR = STYLE_PREFIX + "user-clear";
  private static final String STYLE_USER_SELECTOR = STYLE_PREFIX + "user-selector";
  private static final String STYLE_USER_EMPTY = STYLE_PREFIX + "user-empty";
  private static final String STYLE_USER_NOT_EMPTY = STYLE_PREFIX + "user-not-empty";

  private static final String STYLE_USER_CHECKED = STYLE_PREFIX + "user-checked";
  private static final String STYLE_USER_UNCHECKED = STYLE_PREFIX + "user-unchecked";

  private static final String STYLE_HOVER = STYLE_PREFIX + "hover";

  private static final String STYLE_OBJECT_LEVEL_PREFIX = STYLE_PREFIX + "object-level-";
  private static final String STYLE_OBJECT_HAS_CHILDREN = STYLE_PREFIX + "object-has-children";
  private static final String STYLE_OBJECT_LEAF = STYLE_PREFIX + "object-leaf";

  private static final String STYLE_OBJECT_OPEN = STYLE_PREFIX + "object-open";
  private static final String STYLE_OBJECT_CLOSED = STYLE_PREFIX + "object-closed";
  private static final String STYLE_OBJECT_HIDDEN = STYLE_PREFIX + "object-hidden";

  private static final String DATA_KEY_ROLE = "rights-role";
  private static final String DATA_KEY_OBJECT = "rights-object";
  private static final String DATA_KEY_TYPE = "rights-type";

  private static final String DATA_TYPE_ROLE_LABEL = "rl";
  private static final String DATA_TYPE_ROLE_TOGGLE = "rt";
  private static final String DATA_TYPE_OBJECT_LABEL = "ol";
  private static final String DATA_TYPE_OBJECT_TOGGLE = "ot";
  private static final String DATA_TYPE_VALUE = "v";

  private static final int ROLE_ORIENTATION_ROW = 0;
  private static final int ROLE_ORIENTATION_COL = 0;

  private static final int COLUMN_LABEL_ORIENTATION_ROW = 0;
  private static final int COLUMN_LABEL_ORIENTATION_COL = 1;

  private static final int LABEL_ROW = 0;
  private static final int MULTI_TOGGLE_ROW = 1;
  private static final int LABEL_COL = 0;
  private static final int MULTI_TOGGLE_COL = 1;

  private static final int VALUE_START_ROW = 2;
  private static final int VALUE_START_COL = 2;

  public static void register() {
    FormFactory.registerFormInterceptor("ModuleRights", new ModuleRightsHandler());
    FormFactory.registerFormInterceptor("MenuRights", new MenuRightsHandler());
  }

  private static Toggle createToggle(FontAwesome up, FontAwesome down, String styleName) {
    Toggle toggle = new Toggle(String.valueOf(up.getCode()), String.valueOf(down.getCode()),
        styleName);
    StyleUtils.setFontFamily(toggle, FontAwesome.FAMILY);
    return toggle;
  }

  private static void doClose(Presenter presenter) {
    BeeKeeper.getScreen().closeWidget(presenter.getMainView());
  }

  private static String getObjectName(Widget widget) {
    return DomUtils.getDataProperty(widget.getElement(), DATA_KEY_OBJECT);
  }

  private static Long getRoleId(Widget widget) {
    return DomUtils.getDataPropertyLong(widget.getElement(), DATA_KEY_ROLE);
  }

  private static void removeClassName(UIObject root, String className) {
    NodeList<Element> nodes = Selectors.getNodes(root, Selectors.classSelector(className));
    if (!DomUtils.isEmpty(nodes)) {
      StyleUtils.removeClassName(nodes, className);
    }
  }

  private static void updateValueCell(Widget widget, boolean isChanged) {
    TableCellElement cellElement = DomUtils.getParentCell(widget, false);

    if (cellElement == null) {
      logger.warning("cell not found");
    } else if (isChanged) {
      cellElement.addClassName(STYLE_VALUE_CHANGED);
    } else {
      cellElement.removeClassName(STYLE_VALUE_CHANGED);
    }
  }

  private final BiMap<Long, String> roles = HashBiMap.create();

  private final List<RightsObject> objects = Lists.newArrayList();

  private final Multimap<String, Long> initialValues = HashMultimap.create();
  private final Multimap<String, Long> changes = HashMultimap.create();

  private Orientation roleOrientation = Orientation.HORIZONTAL;
  private Orientation columnLabelOrientation = Orientation.HORIZONTAL;

  private HtmlTable table;

  private FaLabel roleOrientationToggle;
  private Toggle columnLabelOrientationToggle;

  private Long userId;

  private int hoverColumn = BeeConst.UNDEF;

  @Override
  public void afterCreate(final FormView form) {
    initObjects(new Consumer<List<RightsObject>>() {
      @Override
      public void accept(List<RightsObject> typeObjects) {
        if (BeeUtils.isEmpty(typeObjects)) {
          logger.severe(getObjectType(), "objects not available");
          return;
        }

        if (!objects.isEmpty()) {
          objects.clear();
        }
        objects.addAll(typeObjects);

        initData(new Consumer<Boolean>() {
          @Override
          public void accept(Boolean input) {
            if (BeeUtils.isTrue(input) && form.getRootWidget() instanceof HasIndexedWidgets) {
              IdentifiableWidget panel = form.getRootWidget();
              panel.addStyleName(STYLE_PANEL);
              createUi((HasIndexedWidgets) panel);
            }
          }
        });
      }
    });
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case CLOSE:
        if (changes.isEmpty()) {
          return true;
        } else {
          onClose(presenter);
          return false;
        }

      case SAVE:
        save(null);
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public void onShow(Presenter presenter) {
    HeaderView header = presenter.getHeader();

    if (header != null && !header.hasCommands()) {
      List<String> columns = Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME, ALS_COMPANY_NAME);
      Relation relation = Relation.create(VIEW_USERS, columns);

      relation.disableEdit();
      relation.disableNewRow();

      final DataSelector userSelector = new DataSelector(relation, true);
      userSelector.setEditing(true);
      userSelector.addStyleName(STYLE_USER_SELECTOR);

      final Button userCommand = new Button(Localized.getConstants().user());
      userCommand.addStyleName(STYLE_USER_COMMAND);
      userCommand.addStyleName(STYLE_USER_EMPTY);

      final CustomDiv userClear = new CustomDiv(STYLE_USER_CLEAR);
      userClear.addStyleName(STYLE_USER_EMPTY);
      userClear.setText(String.valueOf(BeeConst.CHAR_TIMES));

      userSelector.addSelectorHandler(new SelectorEvent.Handler() {
        @Override
        public void onDataSelector(SelectorEvent event) {
          if (event.isChanged()) {
            long value = event.getValue();

            if (DataUtils.isId(value) && !Objects.equals(value, getUserId())) {
              setUserId(value);
              userCommand.setHtml(Global.getUsers().getSignature(value));

              userCommand.removeStyleName(STYLE_USER_EMPTY);
              userCommand.addStyleName(STYLE_USER_NOT_EMPTY);

              userClear.removeStyleName(STYLE_USER_EMPTY);
              userClear.addStyleName(STYLE_USER_NOT_EMPTY);

              removeClassName(table, STYLE_USER_CHECKED);
              removeClassName(table, STYLE_USER_UNCHECKED);

              checkUserRights();
            }
          }
        }
      });

      userCommand.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          userSelector.clearValue();
          userSelector.setFocus(true);

          userSelector.startEdit(null, DataSelector.SHOW_SELECTOR, null, null);
        }
      });

      userClear.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (getUserId() != null) {
            setUserId(null);

            userCommand.setHtml(Localized.getConstants().user());
            userCommand.removeStyleName(STYLE_USER_NOT_EMPTY);
            userCommand.addStyleName(STYLE_USER_EMPTY);

            userClear.removeStyleName(STYLE_USER_NOT_EMPTY);
            userClear.addStyleName(STYLE_USER_EMPTY);

            removeClassName(table, STYLE_USER_CHECKED);
            removeClassName(table, STYLE_USER_UNCHECKED);
          }
        }
      });

      header.addCommandItem(userCommand);
      header.addCommandItem(userClear);
      header.addCommandItem(userSelector);
    }
  }

  protected abstract RightsObjectType getObjectType();

  protected abstract RightsState getRightsState();

  protected abstract void initObjects(Consumer<List<RightsObject>> consumer);

  private void addColumnLabelOrientationToggle() {
    table.setWidget(COLUMN_LABEL_ORIENTATION_ROW, COLUMN_LABEL_ORIENTATION_COL,
        columnLabelOrientationToggle, STYLE_COLUMN_LABEL_ORIENTATION_CELL);
  }

  private void addRoleOrientationToggle() {
    table.setWidget(ROLE_ORIENTATION_ROW, ROLE_ORIENTATION_COL, roleOrientationToggle,
        STYLE_ROLE_ORIENTATION_CELL);
  }

  private void checkUserRights() {
    Queries.getRowSet(VIEW_USER_ROLES, Lists.newArrayList(COL_ROLE),
        Filter.equals(COL_USER, getUserId()), new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            if (DataUtils.isEmpty(result)) {
              BeeKeeper.getScreen().notifyWarning("user has no roles");

            } else {
              int index = result.getColumnIndex(COL_ROLE);

              Set<Long> userRoles = Sets.newHashSet();
              for (BeeRow row : result) {
                userRoles.add(row.getLong(index));
              }

              markUserValues(userRoles);
            }
          }
        });
  }

  private Widget createObjectLabel(RightsObject object) {
    Label widget = new Label(object.getCaption());
    widget.addStyleName(STYLE_OBJECT_LABEL);

    widget.addStyleName(STYLE_OBJECT_LEVEL_PREFIX + object.getLevel());
    widget.addStyleName(object.hasChildren() ? STYLE_OBJECT_HAS_CHILDREN : STYLE_OBJECT_LEAF);

    widget.setTitle(object.getName());

    DomUtils.setDataProperty(widget.getElement(), DATA_KEY_OBJECT, object.getName());
    DomUtils.setDataProperty(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_OBJECT_LABEL);

    if (object.hasChildren()) {
      widget.addStyleName(STYLE_OBJECT_CLOSED);

      widget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (event.getSource() instanceof Widget) {
            Widget source = (Widget) event.getSource();
            String objectName = getObjectName(source);
            boolean wasOpen = source.getElement().hasClassName(STYLE_OBJECT_OPEN);

            if (!BeeUtils.isEmpty(objectName)) {
              source.setStyleName(STYLE_OBJECT_OPEN, !wasOpen);
              source.setStyleName(STYLE_OBJECT_CLOSED, wasOpen);

              setChildrenVisibility(objectName, !wasOpen);
            }
          }
        }
      });
    }

    return widget;
  }

  private Widget createObjectToggle(String name) {
    Toggle toggle = createToggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
        STYLE_OBJECT_TOGGLE);

    if (initialValues.containsKey(name) && initialValues.get(name).containsAll(roles.keySet())) {
      toggle.setChecked(true);
    }

    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_OBJECT, name);
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_TYPE, DATA_TYPE_OBJECT_TOGGLE);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          Toggle t = (Toggle) event.getSource();
          updateObjectValueToggles(getObjectName(t), t.isChecked());
        }
      }
    });

    return toggle;
  }

  private Widget createRoleLabel(final long roleId, String roleName) {
    final Label widget = new Label(roleName);
    widget.addStyleName(STYLE_ROLE_LABEL);

    DomUtils.setDataProperty(widget.getElement(), DATA_KEY_ROLE, roleId);
    DomUtils.setDataProperty(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_ROLE_LABEL);

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RowEditor.openRow(VIEW_ROLES, roleId, true, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            String name = Data.getString(VIEW_ROLES, result, COL_ROLE_NAME);

            if (!BeeUtils.equalsTrimRight(name, roles.get(roleId))) {
              roles.put(roleId, name);
              widget.setHtml(name);
            }
          }
        });
      }
    });

    return widget;
  }

  private Widget createRoleToggle(final long roleId) {
    Toggle toggle = createToggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
        STYLE_ROLE_TOGGLE);

    boolean checked = true;
    for (RightsObject object : objects) {
      if (!initialValues.containsEntry(object.getName(), roleId)) {
        checked = false;
        break;
      }
    }

    if (checked) {
      toggle.setChecked(true);
    }

    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_ROLE, roleId);
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_TYPE, DATA_TYPE_ROLE_TOGGLE);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          Toggle t = (Toggle) event.getSource();
          updateRoleValueToggles(getRoleId(t), t.isChecked());
        }
      }
    });

    return toggle;
  }

  private void createUi(HasIndexedWidgets panel) {
    if (!panel.isEmpty()) {
      panel.clear();
    }

    if (table == null) {
      this.table = new HtmlTable(STYLE_TABLE);

      table.addMouseMoveHandler(new MouseMoveHandler() {
        @Override
        public void onMouseMove(MouseMoveEvent event) {
          Element target = EventUtils.getTargetElement(event.getNativeEvent().getEventTarget());

          for (Element el = target; el != null; el = el.getParentElement()) {
            if (TableCellElement.is(el)) {
              int col = ((TableCellElement) el.cast()).getCellIndex();

              if (getHoverColumn() != col) {
                onColumnHover(col);
              }

            } else if (DomUtils.idEquals(el, table.getId())) {
              break;
            }
          }
        }
      });

      table.addMouseOutHandler(new MouseOutHandler() {
        @Override
        public void onMouseOut(MouseOutEvent event) {
          onColumnHover(BeeConst.UNDEF);
        }
      });

    } else if (!table.isEmpty()) {
      table.clear();
    }

    List<String> roleNames = new ArrayList<>();

    roleNames.addAll(roles.values());
    if (roleNames.size() > 1) {
      Collections.sort(roleNames);
    }

    if (roleOrientationToggle == null) {
      this.roleOrientationToggle = new FaLabel(FontAwesome.EXCHANGE, STYLE_ROLE_ORIENTATION);

      roleOrientationToggle.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          switchRoleOrientation();
        }
      });
    }

    addRoleOrientationToggle();

    if (columnLabelOrientationToggle == null) {
      this.columnLabelOrientationToggle = createToggle(FontAwesome.ELLIPSIS_H,
          FontAwesome.ELLIPSIS_V, STYLE_COLUMN_LABEL_ORIENTATION);

      if (columnLabelOrientation.isVertical()) {
        columnLabelOrientationToggle.setChecked(true);
      }

      columnLabelOrientationToggle.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          switchColumnLabelOrientation();
        }
      });
    }

    addColumnLabelOrientationToggle();

    int col = VALUE_START_COL;

    for (String roleName : roleNames) {
      long roleId = getRoleId(roleName);

      table.setWidget(LABEL_ROW, col, createRoleLabel(roleId, roleName), STYLE_ROLE_LABEL_CELL);
      table.setWidget(MULTI_TOGGLE_ROW, col, createRoleToggle(roleId), STYLE_ROLE_TOGGLE_CELL);
      col++;
    }

    int row = VALUE_START_ROW;

    for (RightsObject object : objects) {
      String objectName = object.getName();

      table.setWidget(row, LABEL_COL, createObjectLabel(object), STYLE_OBJECT_LABEL_CELL);
      table.setWidget(row, MULTI_TOGGLE_COL, createObjectToggle(objectName),
          STYLE_OBJECT_TOGGLE_CELL);

      col = VALUE_START_COL;
      String objectCaption = object.getCaption();

      for (String roleName : roleNames) {
        long roleId = getRoleId(roleName);
        String title = BeeUtils.joinWords(objectCaption, roleName);

        table.setWidget(row, col++, createValueToggle(objectName, roleId, title),
            STYLE_VALUE_CELL);
      }

      if (object.hasParent()) {
        StyleUtils.addClassName(table.getRowCells(row), STYLE_OBJECT_HIDDEN);
      }

      table.getRowFormatter().addStyleName(row, STYLE_VALUE_ROW);
      row++;
    }

    if (roleOrientation.isVertical()) {
      transposeTable();
    }

    table.addStyleName(getRoleOrientationStyleName());
    table.addStyleName(getColumnLabelOrientationStyleName());

    panel.add(table);
  }

  private Widget createValueToggle(String objectName, long roleId, String title) {
    Toggle toggle = createToggle(FontAwesome.TIMES, FontAwesome.CHECK, STYLE_VALUE_TOGGLE);
    toggle.setTitle(title);

    if (initialValues.containsEntry(objectName, roleId)) {
      toggle.setChecked(true);
    }

    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_OBJECT, objectName);
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_ROLE, roleId);
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          Toggle t = (Toggle) event.getSource();

          String name = getObjectName(t);
          Long role = getRoleId(t);

          boolean isChanged = toggleValue(name, role);
          updateValueCell(t, isChanged);

          Toggle objectToggle = getObjectToggle(name);
          Toggle roleToggle = getRoleToggle(role);

          if (t.isChecked()) {
            objectToggle.setChecked(isObjectChecked(name));
            roleToggle.setChecked(isRoleChecked(role));

          } else {
            objectToggle.setChecked(false);
            roleToggle.setChecked(false);
          }

          RightsObject object = findObject(name);
          if (object != null && object.hasChildren()) {
            Set<String> childrenNames = Sets.newHashSet();
            for (RightsObject ro : objects) {
              if (object.getName().equals(ro.getParent())) {
                childrenNames.add(ro.getName());
              }
            }
            
            for (Widget widget : table) {
              if (DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE)
                  && DomUtils.dataEquals(widget.getElement(), DATA_KEY_ROLE, role)
                  && childrenNames.contains(getObjectName(widget))) {
                widget.setStyleName(STYLE_VALUE_DISABLED, !t.isChecked());
              }
            }
          }
        }
      }
    });

    return toggle;
  }

  private RightsObject findObject(String objectName) {
    for (RightsObject object : objects) {
      if (object.getName().equals(objectName)) {
        return object;
      }
    }

    logger.severe("object", objectName, "not found");
    return null;
  }

  private String getColumnLabelOrientationStyleName() {
    return STYLE_PREFIX + "column-label-" + columnLabelOrientation.name().toLowerCase();
  }

  private int getHoverColumn() {
    return hoverColumn;
  }

  private List<TableCellElement> getObjectCells(String objectName) {
    List<TableCellElement> cells = Lists.newArrayList();

    NodeList<Element> nodes = Selectors.getNodes(table,
        Selectors.attributeEquals(Attributes.DATA_PREFIX + DATA_KEY_OBJECT, objectName));

    if (DomUtils.isEmpty(nodes)) {
      logger.warning("object", objectName, "nodes nof found");

    } else {
      for (int i = 0; i < nodes.getLength(); i++) {
        TableCellElement cell = DomUtils.getParentCell(nodes.getItem(i), true);
        if (cell != null) {
          cells.add(cell);
        }
      }
    }

    return cells;
  }

  private Widget getObjectLabel(String objectName) {
    for (Widget widget : table) {
      if (DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_OBJECT_LABEL)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_OBJECT, objectName)) {
        return widget;
      }
    }

    logger.severe("object", objectName, "label not found");
    return null;
  }

  private Toggle getObjectToggle(String objectName) {
    for (Widget widget : table) {
      if (widget instanceof Toggle
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_OBJECT_TOGGLE)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_OBJECT, objectName)) {
        return (Toggle) widget;
      }
    }

    logger.severe("object", objectName, "toggle not found");
    return null;
  }

  private List<TableCellElement> getRoleCells(Long roleId) {
    List<TableCellElement> cells = Lists.newArrayList();

    NodeList<Element> nodes = Selectors.getNodes(table,
        Selectors.attributeEquals(Attributes.DATA_PREFIX + DATA_KEY_ROLE, roleId));

    if (DomUtils.isEmpty(nodes)) {
      logger.warning("role", roleId, "nodes nof found");

    } else {
      for (int i = 0; i < nodes.getLength(); i++) {
        TableCellElement cell = DomUtils.getParentCell(nodes.getItem(i), true);
        if (cell != null) {
          cells.add(cell);
        }
      }
    }

    return cells;
  }

  private Long getRoleId(String roleName) {
    return roles.inverse().get(roleName);
  }

  private String getRoleOrientationStyleName() {
    return STYLE_PREFIX + "role-" + roleOrientation.name().toLowerCase();
  }

  private Toggle getRoleToggle(long roleId) {
    for (Widget widget : table) {
      if (widget instanceof Toggle
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_ROLE_TOGGLE)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_ROLE, roleId)) {
        return (Toggle) widget;
      }
    }

    logger.severe("role", roleId, "toggle not found");
    return null;
  }

  private Long getUserId() {
    return userId;
  }

  private void initData(final Consumer<Boolean> callback) {
    Queries.getRowSet(VIEW_ROLES, Lists.newArrayList(COL_ROLE_NAME), new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet roleData) {
        if (DataUtils.isEmpty(roleData)) {
          logger.severe("roles not available");
          callback.accept(false);
          return;
        }

        if (!roles.isEmpty()) {
          roles.clear();
        }

        for (BeeRow roleRow : roleData) {
          roles.put(roleRow.getId(), DataUtils.getString(roleData, roleRow, COL_ROLE_NAME));
        }

        ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RIGHTS);
        params.addQueryItem(COL_OBJECT_TYPE, getObjectType().name());
        params.addQueryItem(COL_STATE, getRightsState().name());

        BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (response.hasErrors()) {
              response.notify(BeeKeeper.getScreen());
              callback.accept(false);

            } else {
              if (!initialValues.isEmpty()) {
                initialValues.clear();
              }
              if (!changes.isEmpty()) {
                changes.clear();
              }

              if (response.hasResponse()) {
                Map<String, String> rights =
                    Codec.beeDeserializeMap(response.getResponseAsString());

                if (getRightsState().isChecked()) {
                  for (RightsObject object : objects) {
                    Set<Long> ids = Sets.newHashSet(roles.keySet());

                    String name = BeeUtils.normalize(object.getName());
                    if (rights.containsKey(name)) {
                      ids.removeAll(DataUtils.parseIdSet(rights.get(name)));
                    }

                    if (!ids.isEmpty()) {
                      initialValues.putAll(object.getName(), ids);
                    }
                  }

                } else if (!rights.isEmpty()) {
                  for (Map.Entry<String, String> entry : rights.entrySet()) {
                    Set<Long> ids = DataUtils.parseIdSet(entry.getValue());
                    for (Long id : ids) {
                      initialValues.put(entry.getKey(), id);
                    }
                  }
                }
              }

              callback.accept(true);
            }
          }
        });
      }
    });
  }

  private boolean isChecked(String objectName, long roleId) {
    return initialValues.containsEntry(objectName, roleId)
        != changes.containsEntry(objectName, roleId);
  }

  private boolean isObjectChecked(String objectName) {
    for (Long roleId : roles.keySet()) {
      if (!isChecked(objectName, roleId)) {
        return false;
      }
    }
    return true;
  }

  private boolean isRoleChecked(long roleId) {
    for (RightsObject object : objects) {
      if (!isChecked(object.getName(), roleId)) {
        return false;
      }
    }
    return true;
  }

  private void markUserValues(Set<Long> userRoles) {
    for (Long roleId : roles.keySet()) {
      if (!userRoles.contains(roleId)) {
        List<TableCellElement> cells = getRoleCells(roleId);
        if (!cells.isEmpty()) {
          StyleUtils.addClassName(cells, STYLE_USER_UNCHECKED);
        }
      }
    }

    for (RightsObject object : objects) {
      String objectName = object.getName();

      boolean checked = false;
      for (Long roleId : userRoles) {
        if (isChecked(objectName, roleId)) {
          checked = true;
          break;
        }
      }

      if (!checked) {
        List<TableCellElement> cells = getObjectCells(objectName);
        if (!cells.isEmpty()) {
          StyleUtils.addClassName(cells, STYLE_USER_UNCHECKED);
        }
      }
    }
  }

  private void onClose(final Presenter presenter) {
    String message = BeeUtils.joinWords(Localized.getConstants().changedValues(),
        BeeUtils.bracket(changes.size()));
    List<String> messages = Lists.newArrayList(message);

    List<RightsObject> changedObjects = Lists.newArrayList();
    for (RightsObject object : objects) {
      if (changes.containsKey(object.getName())) {
        changedObjects.add(object);
      }
    }

    int limit = BeeUtils.resize(BeeKeeper.getScreen().getHeight(), 200, 1000, 2, 10);
    int count = (changedObjects.size() > limit * 3 / 2) ? limit : changedObjects.size();

    for (int i = 0; i < count; i++) {
      RightsObject object = changedObjects.get(i);
      messages.add(BeeUtils.joinWords(object.getCaption(),
          BeeUtils.bracket(changes.get(object.getName()).size())));
    }

    if (count < changedObjects.size()) {
      messages.add(BeeUtils.joinWords(BeeUtils.parenthesize(changedObjects.size() - count),
          BeeConst.ELLIPSIS));
    }

    messages.add(Localized.getConstants().saveChanges());

    DecisionCallback callback = new DecisionCallback() {
      @Override
      public void onConfirm() {
        save(new Consumer<Boolean>() {
          @Override
          public void accept(Boolean input) {
            if (BeeUtils.isTrue(input)) {
              doClose(presenter);
            }
          }
        });
      }

      @Override
      public void onDeny() {
        doClose(presenter);
      }
    };

    Global.decide(getFormView().getCaption(), messages, callback, DialogConstants.DECISION_YES);
  }

  private void onColumnHover(int col) {
    if (getHoverColumn() >= VALUE_START_COL) {
      List<TableCellElement> cells = table.getColumnCells(getHoverColumn());
      for (TableCellElement cell : cells) {
        cell.removeClassName(STYLE_HOVER);
      }
    }

    setHoverColumn(col);

    if (col >= VALUE_START_COL) {
      List<TableCellElement> cells = table.getColumnCells(col);
      for (TableCellElement cell : cells) {
        cell.addClassName(STYLE_HOVER);
      }
    }
  }

  private void save(final Consumer<Boolean> callback) {
    if (changes.isEmpty()) {
      BeeKeeper.getScreen().notifyInfo("no changes");
      if (callback != null) {
        callback.accept(false);
      }

    } else {
      ParameterList params = BeeKeeper.getRpc().createParameters(Service.SET_RIGHTS);
      params.addQueryItem(COL_OBJECT_TYPE, getObjectType().name());
      params.addQueryItem(COL_STATE, getRightsState().name());

      Map<String, String> diff = Maps.newHashMap();
      for (String objectName : changes.keySet()) {
        diff.put(objectName, DataUtils.buildIdList(changes.get(objectName)));
      }
      params.addDataItem(COL_OBJECT, Codec.beeSerialize(diff));

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasErrors()) {
            response.notify(BeeKeeper.getScreen());
            if (callback != null) {
              callback.accept(false);
            }

          } else {
            int size = changes.size();

            for (Map.Entry<String, Long> entry : changes.entries()) {
              if (initialValues.containsEntry(entry.getKey(), entry.getValue())) {
                initialValues.remove(entry.getKey(), entry.getValue());
              } else {
                initialValues.put(entry.getKey(), entry.getValue());
              }
            }

            changes.clear();

            removeClassName(table, STYLE_VALUE_CHANGED);

            String message = BeeUtils.joinWords(getObjectType(), getRightsState(),
                "saved", size, "changes");
            logger.debug(message);
            BeeKeeper.getScreen().notifyInfo(message);

            if (callback != null) {
              callback.accept(true);
            }
          }
        }
      });
    }
  }

  private void setChildrenVisibility(String parent, boolean visible) {
    for (RightsObject object : objects) {
      if (parent.equals(object.getParent())) {
        List<TableCellElement> cells = getObjectCells(object.getName());

        if (!cells.isEmpty()) {
          if (visible) {
            StyleUtils.removeClassName(cells, STYLE_OBJECT_HIDDEN);
          } else {
            StyleUtils.addClassName(cells, STYLE_OBJECT_HIDDEN);
          }
        }

        if (!visible && object.hasChildren()) {
          Widget labelWidget = getObjectLabel(object.getName());

          if (labelWidget != null && labelWidget.getElement().hasClassName(STYLE_OBJECT_OPEN)) {
            labelWidget.removeStyleName(STYLE_OBJECT_OPEN);
            labelWidget.addStyleName(STYLE_OBJECT_CLOSED);

            setChildrenVisibility(object.getName(), visible);
          }
        }
      }
    }
  }

  private void setHoverColumn(int hoverColumn) {
    this.hoverColumn = hoverColumn;
  }

  private void setUserId(Long userId) {
    this.userId = userId;
  }

  private void switchColumnLabelOrientation() {
    table.removeStyleName(getColumnLabelOrientationStyleName());
    this.columnLabelOrientation = columnLabelOrientation.invert();
    table.addStyleName(getColumnLabelOrientationStyleName());
  }

  private void switchRoleOrientation() {
    transposeTable();

    table.removeStyleName(getRoleOrientationStyleName());
    this.roleOrientation = roleOrientation.invert();
    table.addStyleName(getRoleOrientationStyleName());
  }

  private boolean toggleValue(String objectName, Long roleId) {
    if (BeeUtils.isEmpty(objectName)) {
      logger.severe("toggle value: object name not specified");
      return false;
    } else if (roleId == null) {
      logger.severe("toggle value: role id not specified");
      return false;

    } else if (changes.containsEntry(objectName, roleId)) {
      changes.remove(objectName, roleId);
      return false;
    } else {
      changes.put(objectName, roleId);
      return true;
    }
  }

  private void transposeTable() {
    table.remove(roleOrientationToggle);
    table.remove(columnLabelOrientationToggle);

    table.transpose();

    addRoleOrientationToggle();
    addColumnLabelOrientationToggle();

    for (int row = VALUE_START_ROW; row < table.getRowCount(); row++) {
      table.getRowFormatter().addStyleName(row, STYLE_VALUE_ROW);
    }
  }

  private void updateObjectValueToggles(String objectName, boolean checked) {
    for (Widget widget : table) {
      if (widget instanceof Toggle && ((Toggle) widget).isChecked() != checked
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_OBJECT, objectName)) {

        ((Toggle) widget).setChecked(checked);

        boolean isChanged = toggleValue(objectName, getRoleId(widget));
        updateValueCell(widget, isChanged);
      }
    }
  }

  private void updateRoleValueToggles(long roleId, boolean checked) {
    for (Widget widget : table) {
      if (widget instanceof Toggle && ((Toggle) widget).isChecked() != checked
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_ROLE, roleId)) {

        ((Toggle) widget).setChecked(checked);

        boolean isChanged = toggleValue(getObjectName(widget), roleId);
        updateValueCell(widget, isChanged);
      }
    }
  }
}
