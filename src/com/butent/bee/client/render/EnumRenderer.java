package com.butent.bee.client.render;

import com.google.common.collect.Lists;

import com.butent.bee.client.Global;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class EnumRenderer extends AbstractCellRenderer implements HasValueStartIndex {

  private static final BeeLogger logger = LogUtils.getLogger(EnumRenderer.class);
  
  public static final int DEFAULT_VALUE_START_INDEX = 0;
  
  private final List<String> captions = Lists.newArrayList();
  private int valueStartIndex;

  public EnumRenderer(int dataIndex, IsColumn dataColumn, String key) {
    this(dataIndex, dataColumn, key, DEFAULT_VALUE_START_INDEX);
  }

  public EnumRenderer(int dataIndex, IsColumn dataColumn, String key, int valueStartIndex) {
    this(dataIndex, dataColumn, Global.getCaptions(key), valueStartIndex);
  }

  public EnumRenderer(int dataIndex, IsColumn dataColumn, Class<? extends Enum<?>> clazz) {
    this(dataIndex, dataColumn, clazz, DEFAULT_VALUE_START_INDEX);
  }

  public EnumRenderer(int dataIndex, IsColumn dataColumn, Class<? extends Enum<?>> clazz,
      int valueStartIndex) {
    this(dataIndex, dataColumn, UiHelper.getCaptions(clazz), valueStartIndex);
  }

  private EnumRenderer(int dataIndex, IsColumn dataColumn, List<String> captions,
      int valueStartIndex) {
    super(dataIndex, dataColumn);
    
    if (BeeUtils.isEmpty(captions)) {
      logger.severe(NameUtils.getName(this), ": no captions available");
    } else {
      this.captions.addAll(captions);
    }

    this.valueStartIndex = valueStartIndex;
  }

  public List<String> getCaptions() {
    return captions;
  }

  public int getValueStartIndex() {
    return valueStartIndex;
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    Integer value = row.getInteger(getDataIndex());
    if (value == null) {
      return null;
    }

    int index = value - getValueStartIndex();
    if (index >= 0 && index < captions.size()) {
      return captions.get(index);
    } else {
      return value.toString();
    }
  }

  public void setValueStartIndex(int valueStartIndex) {
    this.valueStartIndex = valueStartIndex;
  }
}
