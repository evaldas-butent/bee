package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.transport.TransportConstants.ImportType;
import com.butent.bee.shared.modules.transport.TransportConstants.ImportType.ImportProperty;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;

public class ImportOptionForm extends AbstractFormInterceptor {

  private GridInterceptor properties;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid && BeeUtils.same(name, TBL_IMPORT_PROPERTIES)) {
      this.properties = new ImportPropertiesGrid(this);
      ((ChildGrid) widget).setGridInterceptor(this.properties);

    } else if (widget instanceof GridPanel && BeeUtils.same(name, TBL_IMPORT_MAPPINGS)) {
      ((GridPanel) widget).setGridInterceptor(new ImportMappingsGrid(this));
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    getHeaderView().setCaption(BeeUtils.join(": ", form.getCaption(),
        EnumUtils.getEnumByIndex(ImportType.class,
            row.getInteger(form.getDataIndex(COL_IMPORT_TYPE))).getCaption()));
  }

  @Override
  public FormInterceptor getInstance() {
    return new ImportOptionForm();
  }

  public ImportProperty getMappingProperty() {
    ImportProperty target = null;

    if (properties.getGridView() != null && properties.getGridView().getActiveRow() != null) {
      ImportType type = getImportType();

      if (type != null) {
        target = type.getProperty(properties.getGridView().getActiveRow()
            .getString(properties.getDataIndex(COL_IMPORT_PROPERTY)));

        if (target != null && BeeUtils.isEmpty(target.getRelTable())) {
          target = null;
        }
      }
    }
    return target;
  }

  public Collection<ImportProperty> getProperties() {
    ImportType type = getImportType();

    if (type != null) {
      return type.getProperties();
    }
    return null;
  }

  public ImportProperty getProperty(String name) {
    ImportType type = getImportType();

    if (type != null) {
      return type.getProperty(name);
    }
    return null;
  }

  private ImportType getImportType() {
    FormView formView = getFormView();
    IsRow row = formView.getActiveRow();

    if (row != null) {
      return EnumUtils.getEnumByIndex(ImportType.class,
          row.getInteger(formView.getDataIndex(COL_IMPORT_TYPE)));
    }
    return null;
  }
}
