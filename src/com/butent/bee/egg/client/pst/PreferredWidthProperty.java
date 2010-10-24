package com.butent.bee.egg.client.pst;

public class PreferredWidthProperty extends ColumnProperty {
  public static final Type<PreferredWidthProperty> TYPE = new Type<PreferredWidthProperty>() {
    private PreferredWidthProperty instance;

    @Override
    public PreferredWidthProperty getDefault() {
      if (instance == null) {
        instance = new PreferredWidthProperty(80);
      }
      return instance;
    }
  };

  private int preferredWidth;

  public PreferredWidthProperty(int preferredWidth) {
    this.preferredWidth = preferredWidth;
  }
  
  public int getPreferredColumnWidth() {
    return preferredWidth;
  }
}
