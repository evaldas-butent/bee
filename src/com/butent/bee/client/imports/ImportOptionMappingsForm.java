package com.butent.bee.client.imports;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.imports.ImportProperty;
import com.butent.bee.shared.imports.ImportType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ImportOptionMappingsForm extends AbstractFormInterceptor {

  private final String viewName;

  public ImportOptionMappingsForm(String viewName) {
    this.viewName = viewName;
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      if (BeeUtils.same(name, TBL_IMPORT_MAPPINGS)) {
        ((ChildGrid) widget).setGridInterceptor(new ImportMappingsGrid(viewName));

      } else if (BeeUtils.same(name, TBL_IMPORT_CONDITIONS)) {
        ((ChildGrid) widget).setGridInterceptor(new ImportConditionsGrid());
      }
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    getHeaderView().setCaption(BeeUtils.join(": ", EnumUtils.getEnumByIndex(ImportType.class,
        getIntegerValue(COL_IMPORT_TYPE)).getCaption(),
        Data.getViewCaption(getStringValue(COL_IMPORT_DATA))));
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }

  public static Map<String, ImportProperty> getDataProperties(String viewName) {
    Map<String, ImportProperty> props = new LinkedHashMap<>();

    for (ViewColumn col : Data.getDataInfo(viewName).getViewColumns()) {
      if (col.isHidden() || col.isReadOnly() || !col.isEditable()
          && BeeUtils.isPositive(col.getLevel())) {
        continue;
      }
      String name = col.getName();
      ImportProperty prop = new ImportProperty(name, Data.getColumnLabel(viewName, name), true);

      if (!BeeUtils.isEmpty(col.getRelation())) {
        if (Objects.isNull(Data.getDataInfo(col.getRelation(), false))) {
          continue;
        }
        prop.setRelation(col.getRelation());
      }
      props.put(prop.getName(), prop);
    }
    return props;
  }
}