package com.butent.bee.server;

import com.butent.bee.server.rest.CrudWorker;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

public class RsDataSource implements JRRewindableDataSource {
  private BeeRowSet rs;
  private int index;

  public RsDataSource(BeeRowSet rowSet) {
    rs = Assert.notNull(rowSet);
    moveFirst();
  }

  @Override
  public Object getFieldValue(JRField field) {
    int idx = DataUtils.getColumnIndex(field.getName(), rs.getColumns(), false);

    if (BeeConst.isUndef(idx)) {
      return rs.getRow(index).getProperty(field.getName());
    }
    return CrudWorker.getValue(rs, index, rs.getColumnIndex(field.getName()));
  }

  public BeeRowSet getRowSet() {
    return rs;
  }

  public Long getIdValue() {
    return getRowValue().getId();
  }

  public IsRow getRowValue() {
    return rs.getRow(index);
  }

  @Override
  public void moveFirst() {
    index = -1;
  }

  @Override
  public boolean next() {
    return ++index < rs.getNumberOfRows();
  }
}
