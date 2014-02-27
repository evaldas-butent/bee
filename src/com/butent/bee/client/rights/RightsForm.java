package com.butent.bee.client.rights;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public abstract class RightsForm extends AbstractFormInterceptor {

  private static BeeLogger logger = LogUtils.getLogger(RightsForm.class);

  protected static final String STYLE_PREFIX = "bee-Rights-";
  protected static final String STYLE_SUFFIX_CELL = "-cell";

  private static final String STYLE_PANEL = STYLE_PREFIX + "panel";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private static final String STYLE_OBJECT_LABEL = STYLE_PREFIX + "object-label";
  private static final String STYLE_OBJECT_LABEL_CELL = STYLE_OBJECT_LABEL + STYLE_SUFFIX_CELL;
  private static final String STYLE_OBJECT_TOGGLE = STYLE_PREFIX + "object-toggle";
  private static final String STYLE_OBJECT_TOGGLE_CELL = STYLE_OBJECT_TOGGLE + STYLE_SUFFIX_CELL;

  private static final String STYLE_VALUE_TOGGLE = STYLE_PREFIX + "value-toggle";
  private static final String STYLE_VALUE_CELL = STYLE_PREFIX + "value-cell";
  private static final String STYLE_VALUE_ROW = STYLE_PREFIX + "value-row";
  private static final String STYLE_VALUE_CHANGED = STYLE_PREFIX + "value-changed";
  private static final String STYLE_VALUE_DISABLED = STYLE_PREFIX + "value-disabled";

  private static final String STYLE_FILTER_NOT_MATCHED = STYLE_PREFIX + "filter-not-matched";

  private static final String STYLE_HOVER = STYLE_PREFIX + "hover";

  private static final String STYLE_OBJECT_LEVEL_PREFIX = STYLE_PREFIX + "object-level-";
  private static final String STYLE_OBJECT_HAS_CHILDREN = STYLE_PREFIX + "object-has-children";
  private static final String STYLE_OBJECT_LEAF = STYLE_PREFIX + "object-leaf";

  private static final String STYLE_OBJECT_OPEN = STYLE_PREFIX + "object-open";
  private static final String STYLE_OBJECT_CLOSED = STYLE_PREFIX + "object-closed";
  private static final String STYLE_OBJECT_HIDDEN = STYLE_PREFIX + "object-hidden";

  private static final String DATA_TYPE_OBJECT_LABEL = "ol";
  private static final String DATA_TYPE_OBJECT_TOGGLE = "ot";
  private static final String DATA_TYPE_VALUE = "v";

  private static final String DATA_KEY_TYPE = "rights-type";
  private static final String DATA_KEY_OBJECT = "rights-object";

  private static final int LABEL_ROW = 0;
  private static final int MULTI_TOGGLE_ROW = 1;
  private static final int LABEL_COL = 0;
  private static final int MULTI_TOGGLE_COL = 1;

  protected static final int VALUE_START_ROW = 2;
  protected static final int VALUE_START_COL = 2;

  public static void register() {
    FormFactory.registerFormInterceptor("ModuleRights", new ModuleRightsHandler());
    FormFactory.registerFormInterceptor("MenuRights", new MenuRightsHandler());
  }

  protected static Toggle createToggle(FontAwesome up, FontAwesome down, String styleName) {
    Toggle toggle = new Toggle(String.valueOf(up.getCode()), String.valueOf(down.getCode()),
        styleName);
    StyleUtils.setFontFamily(toggle, FontAwesome.FAMILY);
    return toggle;
  }

  protected static Toggle createValueToggle(String objectName) {
    Toggle toggle = createToggle(FontAwesome.TIMES, FontAwesome.CHECK, STYLE_VALUE_TOGGLE);

    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE);
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_OBJECT, objectName);

    return toggle;
  }

  protected static void enableValueWidet(Widget widget, boolean enabled) {
    widget.setStyleName(STYLE_VALUE_DISABLED, !enabled);
  }

  protected static String getObjectName(Widget widget) {
    return DomUtils.getDataProperty(widget.getElement(), DATA_KEY_OBJECT);
  }

  protected static void setNotMatched(Collection<? extends Element> elements) {
    if (!BeeUtils.isEmpty(elements)) {
      StyleUtils.addClassName(elements, STYLE_FILTER_NOT_MATCHED);
    }
  }

  protected static void updateValueCell(Widget widget, boolean isChanged) {
    TableCellElement cellElement = DomUtils.getParentCell(widget, false);

    if (cellElement == null) {
      logger.warning("cell not found");
    } else if (isChanged) {
      cellElement.addClassName(STYLE_VALUE_CHANGED);
    } else {
      cellElement.removeClassName(STYLE_VALUE_CHANGED);
    }
  }

  private static void doClose(Presenter presenter) {
    BeeKeeper.getScreen().closeWidget(presenter.getMainView());
  }

  private static void removeClassName(UIObject root, String className) {
    NodeList<Element> nodes = Selectors.getNodes(root, Selectors.classSelector(className));
    if (!DomUtils.isEmpty(nodes)) {
      StyleUtils.removeClassName(nodes, className);
    }
  }

  private List<RightsObject> objects = Lists.newArrayList();

  private HtmlTable table;

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
        if (hasChanges()) {
          onClose(presenter);
          return false;
        } else {
          return true;
        }

      case SAVE:
        save(null);
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  protected void addColumnLabel(int col, Widget widget, String cellStyleName) {
    table.setWidget(LABEL_ROW, col, widget, cellStyleName);
  }

  protected void addColumnToggle(int col, Widget widget, String cellStyleName) {
    table.setWidget(MULTI_TOGGLE_ROW, col, widget, cellStyleName);
  }

  protected void addObjectLabel(int row, RightsObject object) {
    table.setWidget(row, LABEL_COL, createObjectLabel(object), STYLE_OBJECT_LABEL_CELL);
  }

  protected void addObjectToggle(int row, RightsObject object) {
    table.setWidget(row, MULTI_TOGGLE_COL, createObjectToggle(object.getName()),
        STYLE_OBJECT_TOGGLE_CELL);
  }

  protected void addValueToggle(int row, int col, Widget widget) {
    table.setWidget(row, col, widget, STYLE_VALUE_CELL);
  }

  protected void afterCreateValueRow(int row, RightsObject object) {
    if (object.hasParent()) {
      StyleUtils.addClassName(table.getRowCells(row), STYLE_OBJECT_HIDDEN);
    }
    table.getRowFormatter().addStyleName(row, STYLE_VALUE_ROW);
  }

  protected void debug(Object... messages) {
    logger.debug(messages);
  }

  protected RightsObject findObject(String objectName) {
    for (RightsObject object : objects) {
      if (object.getName().equals(objectName)) {
        return object;
      }
    }

    logger.severe("object", objectName, "not found");
    return null;
  }

  protected abstract Multimap<String, ?> getChanges();

  protected List<TableCellElement> getObjectCells(String objectName) {
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

  protected List<RightsObject> getObjects() {
    return objects;
  }

  protected Toggle getObjectToggle(String objectName) {
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

  protected List<Toggle> getObjectToggles() {
    List<Toggle> toggles = Lists.newArrayList();

    for (Widget widget : table) {
      if (widget instanceof Toggle
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_OBJECT_TOGGLE)) {
        toggles.add((Toggle) widget);
      }
    }

    return toggles;
  }

  protected abstract RightsObjectType getObjectType();

  protected HtmlTable getTable() {
    return table;
  }

  protected abstract boolean hasChanges();

  protected boolean hasObject(Widget widget, String objectName) {
    return DomUtils.dataEquals(widget.getElement(), DATA_KEY_OBJECT, objectName);
  }

  protected abstract void initData(final Consumer<Boolean> callback);

  protected abstract void initObjects(Consumer<List<RightsObject>> consumer);

  protected boolean isDataType(Widget widget, String type) {
    return DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, type);
  }

  protected abstract boolean isObjectChecked(String objectName);

  protected boolean isValueWidget(Widget widget) {
    return DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE);
  }

  protected void markValueRows() {
    for (int row = VALUE_START_ROW; row < table.getRowCount(); row++) {
      table.getRowFormatter().addStyleName(row, STYLE_VALUE_ROW);
    }
  }

  protected void onClearChanges() {
    removeClassName(table, STYLE_VALUE_CHANGED);
  }

  protected abstract void onObjectToggle(boolean checked);

  protected abstract void populateTable();

  protected void resetFilter() {
    removeClassName(table, STYLE_FILTER_NOT_MATCHED);
  }

  protected abstract void save(final Consumer<Boolean> callback);

  protected void setDataType(Widget widget, String type) {
    DomUtils.setDataProperty(widget.getElement(), DATA_KEY_TYPE, type);
  }

  protected void setObjects(List<RightsObject> objects) {
    this.objects = objects;
  }

  protected void severe(Object... messages) {
    logger.severe(messages);
  }

  protected abstract void updateObjectValueToggles(String objectName, boolean checked);

  protected void warning(Object... messages) {
    logger.warning(messages);
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

    if (isObjectChecked(name)) {
      toggle.setChecked(true);
    }

    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_OBJECT, name);
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_TYPE, DATA_TYPE_OBJECT_TOGGLE);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          Toggle ot = (Toggle) event.getSource();
          updateObjectValueToggles(getObjectName(ot), ot.isChecked());

          onObjectToggle(ot.isChecked());
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

            } else if (table.getId().equals(el.getId())) {
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

    populateTable();

    panel.add(table);
  }

  private int getHoverColumn() {
    return hoverColumn;
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

  private void onClose(final Presenter presenter) {
    String message = BeeUtils.joinWords(Localized.getConstants().changedValues(),
        BeeUtils.bracket(getChanges().size()));
    List<String> messages = Lists.newArrayList(message);

    List<RightsObject> changedObjects = Lists.newArrayList();
    for (RightsObject object : objects) {
      if (getChanges().containsKey(object.getName())) {
        changedObjects.add(object);
      }
    }

    int limit = BeeUtils.resize(BeeKeeper.getScreen().getHeight(), 200, 1000, 2, 10);
    int count = (changedObjects.size() > limit * 3 / 2) ? limit : changedObjects.size();

    for (int i = 0; i < count; i++) {
      RightsObject object = changedObjects.get(i);
      messages.add(BeeUtils.joinWords(object.getCaption(),
          BeeUtils.bracket(getChanges().get(object.getName()).size())));
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
}
