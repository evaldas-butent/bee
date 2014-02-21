package com.butent.bee.client.rights;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleRightsHandler extends AbstractFormInterceptor {

  private static BeeLogger logger = LogUtils.getLogger(ModuleRightsHandler.class);

  private static final String STYLE_PREFIX = "bee-ModuleRights-";

  private static final String DATA_KEY_ROLE = "rights-role";
  private static final String DATA_KEY_MODULE = "rights-module";

  public static void register() {
    FormFactory.registerFormInterceptor("ModuleRights", new ModuleRightsHandler());
  }

  private static Widget createModuleLabel(Module module) {
    Label widget = new Label(module.getCaption());
    widget.addStyleName(STYLE_PREFIX + "module-label");
    return widget;
  }

  private static Widget createRoleLabel(final long roleId, String roleName) {
    Label widget = new Label(roleName);
    widget.addStyleName(STYLE_PREFIX + "role-label");

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RowEditor.openRow(VIEW_ROLES, roleId, true, null);
      }
    });

    return widget;
  }

  private static Toggle createToggle(FontAwesome up, FontAwesome down, String styleSuffix) {
    Toggle toggle = new Toggle(String.valueOf(up.getCode()), String.valueOf(down.getCode()),
        STYLE_PREFIX + styleSuffix);
    StyleUtils.setFontFamily(toggle, FontAwesome.FAMILY);
    return toggle;
  }

  private final BiMap<Long, String> roles = HashBiMap.create();

  private final Multimap<Module, Long> hiddenModules = HashMultimap.create();

  private Orientation roleOrientation = Orientation.HORIZONTAL;
  private Orientation columnLabelOrientation = Orientation.HORIZONTAL;

  private final HtmlTable table;
  private final FaLabel roleOrientationToggle;

  private final Toggle columnLabelOrientationToggle;

  private ModuleRightsHandler() {
    this.table = new HtmlTable(STYLE_PREFIX + "table");

    this.roleOrientationToggle = new FaLabel(FontAwesome.EXCHANGE,
        STYLE_PREFIX + "role-orientation");
    this.columnLabelOrientationToggle = createToggle(FontAwesome.ELLIPSIS_H,
        FontAwesome.ELLIPSIS_V, "column-label-orientation");
  }

  @Override
  public void afterCreate(final FormView form) {
    getData(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean input) {
        if (BeeUtils.isTrue(input) && form.getRootWidget() instanceof HasIndexedWidgets) {
          IdentifiableWidget panel = form.getRootWidget();
          panel.addStyleName(STYLE_PREFIX + "panel");
          createUi((HasIndexedWidgets) panel);
        }
      }
    });
  }

  @Override
  public FormInterceptor getInstance() {
    return new ModuleRightsHandler();
  }

  private void addColumnLabelOrientationToggle() {
    table.setWidget(0, 1, columnLabelOrientationToggle);
  }

  private void addRoleOrientationToggle() {
    table.setWidget(0, 0, roleOrientationToggle);
  }

  private Widget createModuleToggle(final Module module) {
    Toggle toggle = createToggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O, "module-toggle");
    if (!hiddenModules.containsKey(module)) {
      toggle.setChecked(true);
    }

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean checked = ((Toggle) event.getSource()).isChecked();

        List<Toggle> toggles = getToggles(module, null);
        for (Toggle t : toggles) {
          t.setChecked(checked);
        }
      }
    });

    return toggle;
  }

  private Widget createRightsToggle(Module module, long roleId) {
    Toggle toggle = createToggle(FontAwesome.TIMES, FontAwesome.CHECK, "rights-toggle");
    if (!hiddenModules.containsEntry(module, roleId)) {
      toggle.setChecked(true);
    }
    
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_MODULE, module.ordinal());
    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_ROLE, roleId);

    return toggle;
  }

  private Widget createRoleToggle(final Long roleId) {
    Toggle toggle = createToggle(FontAwesome.CIRCLE_O, FontAwesome.CHECK_CIRCLE_O, "role-toggle");
    if (!hiddenModules.containsValue(roleId)) {
      toggle.setChecked(true);
    }

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean checked = ((Toggle) event.getSource()).isChecked();

        List<Toggle> toggles = getToggles(null, roleId);
        for (Toggle t : toggles) {
          t.setChecked(checked);
        }
      }
    });

    return toggle;
  }

  private void createUi(HasIndexedWidgets panel) {
    if (!panel.isEmpty()) {
      panel.clear();
    }

    if (!table.isEmpty()) {
      table.clear();
    }

    List<String> roleNames = new ArrayList<>();

    roleNames.addAll(roles.values());
    if (roleNames.size() > 1) {
      Collections.sort(roleNames);
    }

    roleOrientationToggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        switchRoleOrientation();
      }
    });

    table.addStyleName(getRoleOrientationStyleName());
    addRoleOrientationToggle();

    columnLabelOrientationToggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        switchColumnLabelOrientation();
      }
    });

    table.addStyleName(getColumnLabelOrientationStyleName());
    addColumnLabelOrientationToggle();

    int row = 0;
    int col = 2;

    for (String roleName : roleNames) {
      Long roleId = getRoleId(roleName);

      table.setWidget(row, col, createRoleLabel(roleId, roleName));
      table.setWidget(row + 1, col, createRoleToggle(roleId));
      col++;
    }

    row += 2;

    for (Module module : Module.values()) {
      col = 0;

      table.setWidget(row, col++, createModuleLabel(module));
      table.setWidget(row, col++, createModuleToggle(module));

      for (String roleName : roleNames) {
        Long roleId = getRoleId(roleName);
        table.setWidget(row, col++, createRightsToggle(module, roleId));
      }

      row++;
    }

    panel.add(table);
  }

  private String getColumnLabelOrientationStyleName() {
    return STYLE_PREFIX + "column-label-" + columnLabelOrientation.name().toLowerCase();
  }

  private void getData(final Consumer<Boolean> callback) {
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

        Filter filter = Filter.and(
            Filter.isEqual(ALS_OBJECT_TYPE, IntegerValue.of(RightsObjectType.MODULE)),
            Filter.isEqual(COL_STATE, IntegerValue.of(RightsState.VISIBLE)));

        Queries.getRowSet(VIEW_RIGHTS, Lists.newArrayList(ALS_OBJECT_NAME, COL_ROLE), filter,
            new Queries.RowSetCallback() {
              @Override
              public void onSuccess(BeeRowSet rightsData) {
                if (!hiddenModules.isEmpty()) {
                  hiddenModules.clear();
                }

                if (!DataUtils.isEmpty(rightsData)) {
                  int nameIndex = rightsData.getColumnIndex(ALS_OBJECT_NAME);
                  int roleIndex = rightsData.getColumnIndex(COL_ROLE);

                  for (BeeRow rightsRow : rightsData) {
                    String name = rightsRow.getString(nameIndex);
                    Module module = EnumUtils.getEnumByName(Module.class, name);

                    if (module == null) {
                      logger.warning(VIEW_RIGHTS, ALS_OBJECT_NAME, name, "module not recognized");
                    } else {
                      hiddenModules.put(module, rightsRow.getLong(roleIndex));
                    }
                  }
                }

                callback.accept(true);
              }
            });
      }
    });
  }

  private Long getRoleId(String roleName) {
    return roles.inverse().get(roleName);
  }

  private String getRoleOrientationStyleName() {
    return STYLE_PREFIX + "role-" + roleOrientation.name().toLowerCase();
  }

  private List<Toggle> getToggles(Module module, Long roleId) {
    List<Toggle> toggles = Lists.newArrayList();

    for (Widget widget : table) {
      if (widget instanceof Toggle) {
        if (module != null
            && !DomUtils.dataEquals(widget.getElement(), DATA_KEY_MODULE, module.ordinal())) {
          continue;
        }
        if (roleId != null && !DomUtils.dataEquals(widget.getElement(), DATA_KEY_ROLE, roleId)) {
          continue;
        }

        toggles.add((Toggle) widget);
      }
    }

    return toggles;
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
}
