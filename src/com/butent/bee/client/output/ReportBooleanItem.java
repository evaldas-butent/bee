package com.butent.bee.client.output;

import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class ReportBooleanItem extends ReportItem {

  private RadioGroup filter;

  public ReportBooleanItem(String name, String caption) {
    super(name, caption);
  }

  @Override
  public void clearFilter() {
    if (filter != null) {
      filter.setSelectedIndex(0);
    }
  }

  @Override
  public void deserialize(String data) {
    if (data != null) {
      String flt = Codec.deserializeMap(data).get(Service.VAR_DATA);
      getFilterWidget().setSelectedIndex(BeeUtils.isEmpty(flt) ? 0 : Codec.unpack(flt) ? 1 : 2);
    }
  }

  @Override
  public String evaluate(SimpleRow row) {
    return BeeUtils.unbox(row.getBoolean(getName()))
        ? Localized.getConstants().yes() : Localized.getConstants().no();
  }

  @Override
  public RadioGroup getFilterWidget() {
    if (filter == null) {
      LocalizableConstants loc = Localized.getConstants();
      filter = new RadioGroup(Orientation.HORIZONTAL, 0,
          Arrays.asList(loc.noMatter(), loc.yes(), loc.no()));
      filter.addStyleName(getStyle() + "-filter");
    }
    return filter;
  }

  @Override
  public String getStyle() {
    return STYLE_BOOLEAN;
  }

  @Override
  public String serialize() {
    String data = serializeFilter();

    if (data != null) {
      data = Codec.beeSerialize(Collections.singletonMap(Service.VAR_DATA, data));
    }
    return super.serialize(data);
  }

  @Override
  public String serializeFilter() {
    if (filter == null) {
      return null;
    }
    return filter.getSelectedIndex() == 0 ? ""
        : Codec.pack(BeeUtils.toBoolean(filter.getSelectedIndex()));
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      getFilterWidget().setSelectedIndex(Objects.equals(value, Localized.getConstants().yes())
          ? 1 : 2);
    }
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    if (filter == null || filter.getSelectedIndex() == 0 || !row.getRowSet().hasColumn(getName())) {
      return true;
    }
    return BeeUtils.unbox(row.getBoolean(getName())) == BeeUtils.toBoolean(filter
        .getSelectedIndex());
  }
}
