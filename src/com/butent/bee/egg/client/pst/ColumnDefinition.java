package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.shared.Assert;

public abstract class ColumnDefinition<RowType, ColType> {
  private CellEditor<ColType> cellEditor = null;

  private CellRenderer<RowType, ColType> cellRenderer = new DefaultCellRenderer<RowType, ColType>();

  private ColumnPropertyManager properties = new ColumnPropertyManager();

  public CellEditor<ColType> getCellEditor() {
    return cellEditor;
  }

  public CellRenderer<RowType, ColType> getCellRenderer() {
    return cellRenderer;
  }

  public abstract ColType getCellValue(RowType rowValue);

  public <P extends ColumnProperty> P getColumnProperty(ColumnProperty.Type<P> type) {
    return properties.getColumnProperty(type);
  }

  public Object getFooter(int row) {
    return getColumnProperty(FooterProperty.TYPE).getFooter(row);
  }

  public int getFooterCount() {
    return getColumnProperty(FooterProperty.TYPE).getFooterCount();
  }

  public Object getHeader(int row) {
    return getColumnProperty(HeaderProperty.TYPE).getHeader(row);
  }

  public int getHeaderCount() {
    return getColumnProperty(HeaderProperty.TYPE).getHeaderCount();
  }

  public int getMaximumColumnWidth() {
    return getColumnProperty(MaximumWidthProperty.TYPE).getMaximumColumnWidth();
  }

  public int getMinimumColumnWidth() {
    return getColumnProperty(MinimumWidthProperty.TYPE).getMinimumColumnWidth();
  }

  public int getPreferredColumnWidth() {
    return getColumnProperty(PreferredWidthProperty.TYPE).getPreferredColumnWidth();
  }

  public boolean isColumnSortable() {
    return getColumnProperty(SortableProperty.TYPE).isColumnSortable();
  }

  public boolean isColumnTruncatable() {
    return getColumnProperty(TruncationProperty.TYPE).isColumnTruncatable();
  }

  public boolean isFooterTruncatable() {
    return getColumnProperty(TruncationProperty.TYPE).isFooterTruncatable();
  }

  public boolean isHeaderTruncatable() {
    return getColumnProperty(TruncationProperty.TYPE).isHeaderTruncatable();
  }

  public <P extends ColumnProperty> P removeColumnProperty(ColumnProperty.Type<P> type) {
    return properties.removeColumnProperty(type);
  }

  public void setCellEditor(CellEditor<ColType> cellEditor) {
    this.cellEditor = cellEditor;
  }

  public void setCellRenderer(CellRenderer<RowType, ColType> cellRenderer) {
    Assert.notNull(cellRenderer, "cellRenderer cannot be null");
    this.cellRenderer = cellRenderer;
  }

  public abstract void setCellValue(RowType rowValue, ColType cellValue);

  public <P extends ColumnProperty> void setColumnProperty(
      ColumnProperty.Type<P> type, P property) {
    properties.setColumnProperty(type, property);
  }

  public void setColumnSortable(boolean sortable) {
    setColumnProperty(SortableProperty.TYPE, new SortableProperty(sortable));
  }

  public void setColumnTruncatable(boolean truncatable) {
    TruncationProperty prop = properties.getColumnProperty(
        TruncationProperty.TYPE, false);
    if (prop == null) {
      prop = new TruncationProperty(truncatable);
      setColumnProperty(TruncationProperty.TYPE, prop);
    } else {
      prop.setColumnTruncatable(truncatable);
    }
  }

  public void setFooter(int row, Object footer) {
    FooterProperty prop = properties.getColumnProperty(FooterProperty.TYPE,
        false);
    if (prop == null) {
      prop = new FooterProperty();
      setColumnProperty(FooterProperty.TYPE, prop);
    }
    prop.setFooter(row, footer);
  }

  public void setFooterCount(int footerCount) {
    FooterProperty prop = properties.getColumnProperty(FooterProperty.TYPE,
        false);
    if (prop == null) {
      prop = new FooterProperty();
      setColumnProperty(FooterProperty.TYPE, prop);
    }
    prop.setFooterCount(footerCount);
  }

  public void setFooterTruncatable(boolean truncatable) {
    TruncationProperty prop = properties.getColumnProperty(
        TruncationProperty.TYPE, false);
    if (prop == null) {
      prop = new TruncationProperty();
      setColumnProperty(TruncationProperty.TYPE, prop);
    }
    prop.setFooterTruncatable(truncatable);
  }

  public void setHeader(int row, Object header) {
    HeaderProperty prop = properties.getColumnProperty(HeaderProperty.TYPE, false);
    if (prop == null) {
      prop = new HeaderProperty();
      setColumnProperty(HeaderProperty.TYPE, prop);
    }
    prop.setHeader(row, header);
  }

  public void setHeaderCount(int headerCount) {
    HeaderProperty prop = properties.getColumnProperty(HeaderProperty.TYPE, false);
    if (prop == null) {
      prop = new HeaderProperty();
      setColumnProperty(HeaderProperty.TYPE, prop);
    }
    prop.setHeaderCount(headerCount);
  }

  public void setHeaderTruncatable(boolean truncatable) {
    TruncationProperty prop = properties.getColumnProperty(
        TruncationProperty.TYPE, false);
    if (prop == null) {
      prop = new TruncationProperty();
      setColumnProperty(TruncationProperty.TYPE, prop);
    }
    prop.setHeaderTruncatable(truncatable);
  }

  public void setMaximumColumnWidth(int maxWidth) {
    setColumnProperty(MaximumWidthProperty.TYPE, new MaximumWidthProperty(maxWidth));
  }

  public void setMinimumColumnWidth(int minWidth) {
    setColumnProperty(MinimumWidthProperty.TYPE, new MinimumWidthProperty(minWidth));
  }

  public void setPreferredColumnWidth(int preferredWidth) {
    setColumnProperty(PreferredWidthProperty.TYPE, new PreferredWidthProperty(preferredWidth));
  }
}
