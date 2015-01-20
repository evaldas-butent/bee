package com.butent.bee.client.output;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.EnumUtils;

public class ReportEnumItem extends ReportItem {

  private Class<Enum<? extends HasCaption>> enumClass;

  public ReportEnumItem(String name, String caption, Class<Enum<? extends HasCaption>> en) {
    super(name, caption);
    enumClass = Assert.notNull(en);
  }

  @Override
  public ReportItem create() {
    return new ReportEnumItem(getName(), getCaption(), enumClass);
  }

  @Override
  public String evaluate(SimpleRow row) {
    Enum<? extends HasCaption> e = EnumUtils.getEnumByIndex(enumClass, row.getInt(getName()));
    return e != null ? ((HasCaption) e).getCaption() : null;
  }

}
