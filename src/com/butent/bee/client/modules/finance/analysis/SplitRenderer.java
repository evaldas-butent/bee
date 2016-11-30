package com.butent.bee.client.modules.finance.analysis;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplit;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

class SplitRenderer extends AbstractCellRenderer {

  SplitRenderer(CellSource cellSource) {
    super(cellSource);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    String value = getString(row);
    if (BeeUtils.isEmpty(value)) {
      return null;
    }

    AnalysisSplit split = EnumUtils.getEnumByName(AnalysisSplit.class, value);
    return (split == null) ? value : split.getCaption();
  }
}
