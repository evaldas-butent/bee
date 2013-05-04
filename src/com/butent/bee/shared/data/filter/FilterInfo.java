package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.Codec;

public class FilterInfo implements BeeSerializable, HasCaption {
  
  public static FilterInfo restore(String s) {
    Assert.notEmpty(s);
    
    FilterInfo filterInfo = new FilterInfo();
    filterInfo.deserialize(s);
    
    return filterInfo;
  }
  
  private String id;
  private String caption;
  private String label;
  private Filter filter;

  public FilterInfo(String id, String caption, String label, Filter filter) {
    this.id = id;
    this.caption = caption;
    this.label = label;
    this.filter = filter;
  }

  private FilterInfo() {
    super();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 4);
    
    int i = 0;
    setId(arr[i++]);
    setCaption(arr[i++]);
    setLabel(arr[i++]);
    setFilter(Filter.restore(arr[i++]));
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public Filter getFilter() {
    return filter;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(new Object[] {getId(), getCaption(), getLabel(), getFilter()});
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setFilter(Filter filter) {
    this.filter = filter;
  }

  private void setId(String id) {
    this.id = id;
  }

  private void setLabel(String label) {
    this.label = label;
  }
}
