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
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
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

  public static void register() {
    FormFactory.registerFormInterceptor("ModuleRights", new ModuleRightsHandler());
  }

  private static Widget createModuleLabel(Module module) {
    Label widget = new Label(module.getCaption());
    widget.addStyleName(STYLE_PREFIX + "module-label");
    return widget;
  }

  /**
   * @param module  
   */
  private static Widget createModuleToggle(Module module) {
    Toggle widget = new Toggle();
    widget.addStyleName(STYLE_PREFIX + "module-toggle");
    return widget;
  }

  private static Widget createRightsToggle(boolean checked) {
    Toggle widget = new Toggle();
    widget.addStyleName(STYLE_PREFIX + "rights-toggle");

    widget.setChecked(checked);

    return widget;
  }

  private static Widget createRoleLabel(String roleName) {
    Label widget = new Label(roleName);
    widget.addStyleName(STYLE_PREFIX + "role-label");

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      }
    });

    return widget;
  }

  /**
   * @param roleId  
   */
  private static Widget createRoleToggle(Long roleId) {
    Toggle widget = new Toggle();
    widget.addStyleName(STYLE_PREFIX + "role-toggle");
    return widget;
  }

  private final BiMap<Long, String> roles = HashBiMap.create();
  private final Multimap<Module, Long> hiddenModules = HashMultimap.create();
  
  private final HtmlTable table = new HtmlTable(STYLE_PREFIX + "table");
  private Orientation roleOrientation = Orientation.HORIZONTAL;

  private ModuleRightsHandler() {
  }

  @Override
  public void afterCreate(final FormView form) {
    getData(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean input) {
        if (BeeUtils.isTrue(input) && form.getRootWidget() instanceof HasIndexedWidgets) {
          form.getRootWidget().addStyleName(STYLE_PREFIX + "panel");
          createUi((HasIndexedWidgets) form.getRootWidget());
        }
      }
    });
  }

  @Override
  public FormInterceptor getInstance() {
    return new ModuleRightsHandler();
  }

  private void createUi(HasIndexedWidgets panel) {
    if (!panel.isEmpty()) {
      panel.clear();
    }

    List<String> roleNames = new ArrayList<>();

    roleNames.addAll(roles.values());
    if (roleNames.size() > 1) {
      Collections.sort(roleNames);
    }

    Toggle orientationToggle = new Toggle();
    orientationToggle.addStyleName(STYLE_PREFIX + "orientation");
    
    orientationToggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        switchOrientation();
      }
    });
    
//    table.setWidget(0, 0, orientationToggle);
    
    int row = 0;
    int col = 2;

    for (String roleName : roleNames) {
      Long roleId = getRoleId(roleName);

      table.setWidget(row, col, createRoleLabel(roleName));
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

        boolean visible = !hiddenModules.containsEntry(module, roleId);
        table.setWidget(row, col++, createRightsToggle(visible));
      }
      
      row++;
    }
    
    panel.add(table);
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
  
  private Orientation getRoleOrientation() {
    return roleOrientation;
  }

  private void setRoleOrientation(Orientation roleOrientation) {
    this.roleOrientation = roleOrientation;
  }

  private void switchOrientation() {
    int rc = table.getRowCount();
    int cc = table.getCellCount(0);
    
    int max = Math.max(rc, cc);
    
    for (int row = 0; row < max - 1; row++) {
      for (int col = row + 1; col < max; col++) {
        Widget w1 = (row < rc && col < cc) ? table.getWidget(row, col) : null;
        if (w1 != null) {
          table.remove(w1);
        }

        Widget w2 = (col < rc && row < cc) ? table.getWidget(col, row) : null;
        if (w2 != null) {
          table.remove(w2);
        }
        
        if (w1 != null) {
          table.setWidget(col, row, w1);
        }
        if (w2 != null) {
          table.setWidget(row, col, w2);
        }
      }
    }
    
    setRoleOrientation(getRoleOrientation().isVertical() 
        ? Orientation.HORIZONTAL : Orientation.VERTICAL);
  }
}
