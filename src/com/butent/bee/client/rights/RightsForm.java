package com.butent.bee.client.rights;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

  private static final String STYLE_ROLE_ORIENTATION = STYLE_PREFIX + "role-orientation";
  private static final String STYLE_ROLE_ORIENTATION_CELL = STYLE_ROLE_ORIENTATION
      + STYLE_SUFFIX_CELL;
  private static final String STYLE_COLUMN_LABEL_ORIENTATION = STYLE_PREFIX
      + "column-label-orientation";
  private static final String STYLE_COLUMN_LABEL_ORIENTATION_CELL =
      STYLE_COLUMN_LABEL_ORIENTATION + STYLE_SUFFIX_CELL;

  private static final String DATA_KEY_ROLE = "rights-role";
  private static final String DATA_KEY_OBJECT = "rights-object";
  private static final String DATA_KEY_TYPE = "rights-type";

  private static final String DATA_TYPE_ROLE_TOGGLE = "rt";
  private static final String DATA_TYPE_OBJECT_TOGGLE = "ot";
  private static final String DATA_TYPE_VALUE = "v";

  public static void register() {
    FormFactory.registerFormInterceptor("ModuleRights", new ModuleRightsHandler());
    FormFactory.registerFormInterceptor("MenuRights", new MenuRightsHandler());
  }

  private static Widget createObjectLabel(String caption) {
    Label widget = new Label(caption);
    widget.addStyleName(STYLE_OBJECT_LABEL);
    return widget;
  }

  private static Widget createRoleLabel(final long roleId, String roleName) {
    Label widget = new Label(roleName);
    widget.addStyleName(STYLE_ROLE_LABEL);

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RowEditor.openRow(VIEW_ROLES, roleId, true, null);
      }
    });

    return widget;
  }

  private static Toggle createToggle(FontAwesome up, FontAwesome down, String styleName) {
    Toggle toggle = new Toggle(String.valueOf(up.getCode()), String.valueOf(down.getCode()),
        styleName);
    StyleUtils.setFontFamily(toggle, FontAwesome.FAMILY);
    return toggle;
  }

  private static String getObjectName(Widget widget) {
    return DomUtils.getDataProperty(widget.getElement(), DATA_KEY_OBJECT);
  }

  private static Long getRoleId(Widget widget) {
    return DomUtils.getDataPropertyLong(widget.getElement(), DATA_KEY_ROLE);
  }

  private final BiMap<Long, String> roles = HashBiMap.create();
  private final List<String> objectNames = Lists.newArrayList();

  private final Multimap<String, Long> initialValues = HashMultimap.create();
  private final Multimap<String, Long> changes = HashMultimap.create();

  private Orientation roleOrientation = Orientation.HORIZONTAL;

  private Orientation columnLabelOrientation = Orientation.HORIZONTAL;
  private HtmlTable table;

  private FaLabel roleOrientationToggle;

  private Toggle columnLabelOrientationToggle;

  @Override
  public void afterCreate(final FormView form) {
    initObjects(new Consumer<List<String>>() {
      @Override
      public void accept(List<String> names) {
        if (BeeUtils.isEmpty(names)) {
          logger.severe(getObjectType(), "objects not available");
          return;
        }

        objectNames.addAll(names);

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

  protected abstract String getObjectCaption(String name);

  protected abstract RightsObjectType getObjectType();

  protected abstract RightsState getRightsState();

  protected abstract void initObjects(Consumer<List<String>> consumer);

  private void addColumnLabelOrientationToggle() {
    table.setWidget(0, 1, columnLabelOrientationToggle, STYLE_COLUMN_LABEL_ORIENTATION_CELL);
  }

  private void addRoleOrientationToggle() {
    table.setWidget(0, 0, roleOrientationToggle, STYLE_ROLE_ORIENTATION_CELL);
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

  private Widget createRoleToggle(final long roleId) {
    Toggle toggle = createToggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
        STYLE_ROLE_TOGGLE);

    boolean checked = true;
    for (String objectName : objectNames) {
      if (!initialValues.containsEntry(objectName, roleId)) {
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

    int row = 0;
    int col = 2;

    for (String roleName : roleNames) {
      long roleId = getRoleId(roleName);

      table.setWidget(row, col, createRoleLabel(roleId, roleName), STYLE_ROLE_LABEL_CELL);
      table.setWidget(row + 1, col, createRoleToggle(roleId), STYLE_ROLE_TOGGLE_CELL);
      col++;
    }

    row += 2;

    for (String objectName : objectNames) {
      col = 0;

      table.setWidget(row, col++, createObjectLabel(getObjectCaption(objectName)),
          STYLE_OBJECT_LABEL_CELL);
      table.setWidget(row, col++, createObjectToggle(objectName), STYLE_OBJECT_TOGGLE_CELL);

      for (String roleName : roleNames) {
        long roleId = getRoleId(roleName);
        table.setWidget(row, col++, createValueToggle(objectName, roleId), STYLE_VALUE_CELL);
      }

      table.getRowFormatter().addStyleName(row, STYLE_VALUE_ROW);
      row++;
    }

    if (roleOrientation.isVertical()) {
      table.transpose();
    }

    table.addStyleName(getRoleOrientationStyleName());
    table.addStyleName(getColumnLabelOrientationStyleName());

    panel.add(table);
  }

  private Widget createValueToggle(String objectName, long roleId) {
    Toggle toggle = createToggle(FontAwesome.TIMES, FontAwesome.CHECK, STYLE_VALUE_TOGGLE);
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

          String obj = getObjectName(t);
          Long role = getRoleId(t);

          toggleValue(obj, role);

          Toggle objectToggle = getObjectToggle(obj);
          Toggle roleToggle = getRoleToggle(role);

          if (t.isChecked()) {
            objectToggle.setChecked(isObjectChecked(obj));
            roleToggle.setChecked(isRoleChecked(role));

          } else {
            objectToggle.setChecked(false);
            roleToggle.setChecked(false);
          }
        }
      }
    });

    return toggle;
  }

  private String getColumnLabelOrientationStyleName() {
    return STYLE_PREFIX + "column-label-" + columnLabelOrientation.name().toLowerCase();
  }

  private Toggle getObjectToggle(String objectName) {
    for (Widget widget : table) {
      if (widget instanceof Toggle
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_OBJECT, objectName)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_OBJECT_TOGGLE)) {
        return (Toggle) widget;
      }
    }

    logger.severe("object", objectName, "toggle not found");
    return null;
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
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_ROLE, roleId)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_ROLE_TOGGLE)) {
        return (Toggle) widget;
      }
    }

    logger.severe("role", roleId, "toggle not found");
    return null;
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
                  for (String objectName : objectNames) {
                    Set<Long> ids = Sets.newHashSet(roles.keySet());
                    if (rights.containsKey(objectName)) {
                      ids.removeAll(DataUtils.parseIdSet(rights.get(objectName)));
                    }

                    if (!ids.isEmpty()) {
                      initialValues.putAll(objectName, ids);
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
    for (String objectName : objectNames) {
      if (!isChecked(objectName, roleId)) {
        return false;
      }
    }
    return true;
  }

  private void switchColumnLabelOrientation() {
    table.removeStyleName(getColumnLabelOrientationStyleName());
    this.columnLabelOrientation = columnLabelOrientation.invert();
    table.addStyleName(getColumnLabelOrientationStyleName());
  }

  private void switchRoleOrientation() {
    table.remove(roleOrientationToggle);
    table.remove(columnLabelOrientationToggle);

    table.transpose();

    addRoleOrientationToggle();
    addColumnLabelOrientationToggle();

    table.removeStyleName(getRoleOrientationStyleName());
    this.roleOrientation = roleOrientation.invert();
    table.addStyleName(getRoleOrientationStyleName());
  }

  private void toggleValue(String objectName, Long roleId) {
    if (BeeUtils.isEmpty(objectName)) {
      logger.severe("toggle value: object name not specified");
    } else if (roleId == null) {
      logger.severe("toggle value: role id not specified");

    } else if (changes.containsEntry(objectName, roleId)) {
      changes.remove(objectName, roleId);
    } else {
      changes.put(objectName, roleId);
    }
  }

  private void updateObjectValueToggles(String objectName, boolean checked) {
    for (Widget widget : table) {
      if (widget instanceof Toggle && ((Toggle) widget).isChecked() != checked
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_OBJECT, objectName)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE)) {

        ((Toggle) widget).setChecked(checked);
        toggleValue(objectName, getRoleId(widget));
      }
    }
  }

  private void updateRoleValueToggles(long roleId, boolean checked) {
    for (Widget widget : table) {
      if (widget instanceof Toggle && ((Toggle) widget).isChecked() != checked
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_ROLE, roleId)
          && DomUtils.dataEquals(widget.getElement(), DATA_KEY_TYPE, DATA_TYPE_VALUE)) {

        ((Toggle) widget).setChecked(checked);
        toggleValue(getObjectName(widget), roleId);
      }
    }
  }
}
