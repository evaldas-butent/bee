package com.butent.bee.client.modules.classifiers;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.TreeGridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class VehiclesGrid extends TreeGridInterceptor {

  @Override
  public VehiclesGrid getInstance() {
    return new VehiclesGrid();
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    IsRow model = getSelectedTreeItem();

    if (model != null) {
      List<BeeColumn> cols = getGridPresenter().getDataColumns();
      newRow.setValue(DataUtils.getColumnIndex(COL_MODEL, cols), model.getId());
      newRow.setValue(DataUtils.getColumnIndex(COL_PARENT_MODEL_NAME, cols),
          getModelValue(model, "ParentName"));
      newRow.setValue(DataUtils.getColumnIndex(COL_MODEL_NAME, cols),
          getModelValue(model, "Name"));
    }
    return true;
  }

  @Override
  protected Filter getFilter(Long modelId) {
    if (DataUtils.isId(modelId)) {
      return Filter.equals(COL_MODEL, modelId);
    }
    return null;
  }

  private String getModelValue(IsRow model, String colName) {
    if (BeeUtils.allNotNull(model, getTreeDataColumns())) {
      return model.getString(getTreeColumnIndex(colName));
    }
    return null;
  }
}
