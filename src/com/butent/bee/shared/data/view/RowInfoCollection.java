package com.butent.bee.shared.data.view;

import com.google.common.base.Splitter;

import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.HashSet;

@SuppressWarnings("serial")
public class RowInfoCollection extends HashSet<RowInfo> implements BeeSerializable, Transformable {
  
  private static final char ROW_INFO_SEPARATOR = ';';
  private static final Splitter ROW_INFO_SPLITTER = 
    Splitter.on(ROW_INFO_SEPARATOR).omitEmptyStrings().trimResults();

  public static final RowInfoCollection restore(String s) {
    RowInfoCollection ric = new RowInfoCollection();
    if (!BeeUtils.isEmpty(s)) {
      ric.deserialize(s);
    }
    return ric;
  }
  
  public RowInfoCollection() {
    super();
  }

  public RowInfoCollection(Collection<Long> rowIds) {
    super();
    if (rowIds != null) {
      for (Long id : rowIds) {
        if (id != null) {
          add(new RowInfo(id));
        }
      }
    }
  }
  
  public RowInfoCollection(long rowId) {
    this(new RowInfo(rowId));
  }

  public RowInfoCollection(RowInfo rowInfo) {
    super();
    if (rowInfo != null) {
      add(rowInfo);
    }
  }
  
  @Override
  public void deserialize(String s) {
    clear();
    if (!BeeUtils.isEmpty(s)) {
      for (String z : ROW_INFO_SPLITTER.split(s)) {
        add(RowInfo.restore(z));
      }
    }
  }

  @Override
  public String serialize() {
    StringBuilder sb = new StringBuilder();
    for (RowInfo rowInfo : this) {
      if (rowInfo == null) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(ROW_INFO_SEPARATOR);
      }
      sb.append(rowInfo.serialize());
    }
    return sb.toString();
  }

  @Override
  public String transform() {
    return serialize();
  }
}
