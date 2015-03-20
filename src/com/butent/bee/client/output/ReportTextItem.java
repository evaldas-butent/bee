package com.butent.bee.client.output;

import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReportTextItem extends ReportItem {

  private MultiSelector filter;

  public ReportTextItem(String name, String caption) {
    super(name, caption);
  }

  @Override
  public void clearFilter() {
    if (filter != null) {
      filter.clearValue();
    }
  }

  @Override
  public ReportItem deserializeFilter(String data) {
    if (!BeeUtils.isEmpty(data)) {
      getFilterWidget().setValues(Arrays.asList(Codec.beeDeserializeCollection(data)));
    }
    return this;
  }

  @Override
  public String evaluate(SimpleRow row) {
    return row.getValue(getName());
  }

  @Override
  public MultiSelector getFilterWidget() {
    if (filter == null) {
      Relation relation = Relation.create(ClassifierConstants.TBL_COUNTRIES,
          Collections.singletonList(ClassifierConstants.COL_COUNTRY_NAME));
      relation.setStrict(false);
      relation.setFilter(Filter.isFalse());
      relation.disableNewRow();
      relation.disableEdit();
      relation.setValueSource(ClassifierConstants.COL_COUNTRY_NAME);

      filter = MultiSelector.autonomous(relation, (AbstractCellRenderer) null);
      filter.addStyleName(getStyle() + "-filter");
    }
    return filter;
  }

  @Override
  public String getStyle() {
    return STYLE_TEXT;
  }

  @Override
  public String serializeFilter() {
    if (filter == null) {
      return null;
    }
    return Codec.beeSerialize(filter.getValues());
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget().setValues(Arrays.asList(value));
    }
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    if (filter == null || !row.getRowSet().hasColumn(getName())) {
      return true;
    }
    List<String> values = filter.getValues();

    if (values.isEmpty()) {
      return true;
    }
    String value = row.getValue(getName());

    for (String opt : values) {
      if (BeeUtils.containsSame(value, opt)) {
        return true;
      }
    }
    return false;
  }
}
