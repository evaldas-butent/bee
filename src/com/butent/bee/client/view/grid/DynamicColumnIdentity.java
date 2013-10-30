package com.butent.bee.client.view.grid;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.HasCaption;

public class DynamicColumnIdentity implements HasCaption, Comparable<DynamicColumnIdentity> {

  private final String id;

  private final String caption;
  private String label;

  private String source;
  private String property;

  public DynamicColumnIdentity(String id, String caption) {
    this.id = Assert.notNull(id);
    this.caption = caption;
  }

  @Override
  public int compareTo(DynamicColumnIdentity o) {
    return id.compareTo(o.id);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof DynamicColumnIdentity) && id.equals(((DynamicColumnIdentity) obj).id);
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getProperty() {
    return property;
  }

  public String getSource() {
    return source;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
