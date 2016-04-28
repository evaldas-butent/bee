package com.butent.bee.client.modules.transport.charts;

import com.butent.bee.client.modules.transport.charts.ChartData.Type;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.List;

class ChartFilter implements BeeSerializable {

  private static final class FilterValue implements BeeSerializable {

    private static FilterValue restore(String s) {
      if (!BeeUtils.isEmpty(s)) {
        FilterValue fv = new FilterValue();
        fv.deserialize(s);

        if (fv.isValid()) {
          return fv;
        }
      }
      return null;
    }

    private Type type;
    private String name;
    private Long id;

    private FilterValue() {
    }

    private FilterValue(Type type, String name, Long id) {
      this.type = type;
      this.name = name;
      this.id = id;
    }

    @Override
    public void deserialize(String s) {
      String[] arr = Codec.beeDeserializeCollection(s);
      Assert.lengthEquals(arr, 3);

      int i = 0;

      this.type = EnumUtils.getEnumByName(Type.class, arr[i++]);
      this.name = arr[i++];
      this.id = BeeUtils.toLongOrNull(arr[i++]);
    }

    @Override
    public String serialize() {
      List<String> list = new ArrayList<>();
      list.add(type.name());
      list.add(name);
      list.add((id == null) ? null : id.toString());

      return Codec.beeSerialize(list);
    }

    private boolean isValid() {
      return type != null && (!BeeUtils.isEmpty(name) || id != null);
    }
  }

  static List<ChartFilter> restoreList(String s) {
    List<ChartFilter> result = new ArrayList<>();

    String[] arr = Codec.beeDeserializeCollection(s);
    if (!ArrayUtils.isEmpty(arr)) {
      for (String cfv : arr) {
        ChartFilter cf = new ChartFilter();
        cf.deserialize(cfv);
        if (cf.isValid()) {
          result.add(cf);
        }
      }
    }

    return result;
  }

  private String label;
  private boolean initial;

  private final List<FilterValue> values = new ArrayList<>();

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    int i = 0;
    setLabel(arr[i++]);
    setInitial(Codec.unpack(arr[i++]));
    String[] fvarr = Codec.beeDeserializeCollection(arr[i++]);

    if (!values.isEmpty()) {
      values.clear();
    }
    if (!ArrayUtils.isEmpty(fvarr)) {
      for (String fvs : fvarr) {
        FilterValue fv = FilterValue.restore(fvs);
        if (fv != null) {
          values.add(fv);
        }
      }
    }
  }

  @Override
  public String serialize() {
    List<String> list = new ArrayList<>();
    list.add(getLabel());
    list.add(Codec.pack(isInitial()));
    list.add(Codec.beeSerialize(values));

    return Codec.beeSerialize(list);
  }

  String getLabel() {
    return label;
  }

  boolean isInitial() {
    return initial;
  }

  boolean isValid() {
    return !BeeUtils.isEmpty(getLabel()) && !values.isEmpty();
  }

  private void setInitial(boolean initial) {
    this.initial = initial;
  }

  private void setLabel(String label) {
    this.label = label;
  }
}
