package com.butent.bee.client.imports;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class ImportMappingsGrid extends AbstractGridInterceptor {

  private final String viewName;

  public ImportMappingsGrid(String viewName) {
    this.viewName = viewName;
  }

  @Override
  public ColumnDescription beforeCreateColumn(GridView gridView, ColumnDescription description) {
    if (BeeUtils.same(description.getId(), AdministrationConstants.COL_IMPORT_MAPPING)) {
      Relation relation = Data.getRelation(viewName);

      if (relation == null) {
        List<String> columns = new ArrayList<>();

        for (BeeColumn column : Data.getColumns(viewName)) {
          columns.add(column.getId());
        }
        relation = Relation.create(viewName, columns);
        LogUtils.getRootLogger().warning("Missing relation info:", viewName);
      }
      description.setRelation(relation);
    }
    return super.beforeCreateColumn(gridView, description);
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }
}
