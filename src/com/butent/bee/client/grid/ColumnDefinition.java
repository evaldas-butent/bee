package com.butent.bee.client.grid;

import com.butent.bee.client.grid.property.ColumnProperty;
import com.butent.bee.client.grid.property.ColumnPropertyManager;
import com.butent.bee.client.grid.property.FooterProperty;
import com.butent.bee.client.grid.property.HeaderProperty;
import com.butent.bee.client.grid.property.HeaderPropertyBase;
import com.butent.bee.client.grid.property.MaximumWidthProperty;
import com.butent.bee.client.grid.property.MinimumWidthProperty;
import com.butent.bee.client.grid.property.PreferredWidthProperty;
import com.butent.bee.client.grid.property.SortableProperty;
import com.butent.bee.client.grid.property.TruncationProperty;
import com.butent.bee.client.grid.render.CellRenderer;
import com.butent.bee.client.grid.render.DefaultCellRenderer;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;

/**
 * Manages columns properties like sortable, header, footer, minimum width etc.
 */

public abstract class ColumnDefinition implements Comparable<ColumnDefinition> {
  private CellRenderer cellRenderer = new DefaultCellRenderer();

  private ColumnPropertyManager properties = new ColumnPropertyManager();

  private int columnId;
  private int columnOrder;

  public int compareTo(ColumnDefinition o) {
    return ((Integer) getColumnOrder()).compareTo(o.getColumnOrder());
  }

  public CellRenderer getCellRenderer() {
    return cellRenderer;
  }

  public abstract Object getCellValue(IsRow rowValue);

  public int getColumnId() {
    return columnId;
  }

  public int getColumnOrder() {
    return columnOrder;
  }

  public <P extends ColumnProperty> P getColumnProperty(String name) {
    return properties.getColumnProperty(name);
  }

  public Object getFooter() {
    return getFooter(HeaderPropertyBase.DEFAULT_ROW);
  }

  public Object getFooter(int row) {
    FooterProperty prop = getColumnProperty(FooterProperty.NAME);
    if (prop == null) {
      return null;
    }
    return prop.getFooter(row);
  }

  public int getFooterCount() {
    FooterProperty prop = getColumnProperty(FooterProperty.NAME);
    if (prop == null) {
      return 0;
    }
    return prop.getFooterCount();
  }

  public Object getHeader() {
    return getHeader(HeaderPropertyBase.DEFAULT_ROW);
  }

  public Object getHeader(int row) {
    HeaderProperty prop = getColumnProperty(HeaderProperty.NAME);
    if (prop == null) {
      return null;
    }
    return prop.getHeader(row);
  }

  public int getHeaderCount() {
    HeaderProperty prop = getColumnProperty(HeaderProperty.NAME);
    if (prop == null) {
      return 0;
    }
    return prop.getHeaderCount();
  }

  public int getMaximumColumnWidth(int def) {
    MaximumWidthProperty prop = getColumnProperty(MaximumWidthProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.getMaximumColumnWidth();
  }

  public int getMinimumColumnWidth(int def) {
    MinimumWidthProperty prop = getColumnProperty(MinimumWidthProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.getMinimumColumnWidth();
  }

  public int getPreferredColumnWidth(int def) {
    PreferredWidthProperty prop = getColumnProperty(PreferredWidthProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.getPreferredColumnWidth();
  }

  public boolean isColumnSortable(boolean def) {
    SortableProperty prop = getColumnProperty(SortableProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.isColumnSortable();
  }

  public boolean isColumnTruncatable(boolean def) {
    TruncationProperty prop = getColumnProperty(TruncationProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.isColumnTruncatable();
  }

  public boolean isFooterDynamic(boolean def) {
    FooterProperty prop = getColumnProperty(FooterProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.isDynamic();
  }

  public boolean isFooterTruncatable(boolean def) {
    TruncationProperty prop = getColumnProperty(TruncationProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.isFooterTruncatable();
  }

  public boolean isHeaderDynamic(boolean def) {
    HeaderProperty prop = getColumnProperty(HeaderProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.isDynamic();
  }

  public boolean isHeaderTruncatable(boolean def) {
    TruncationProperty prop = getColumnProperty(TruncationProperty.NAME);
    if (prop == null) {
      return def;
    }
    return prop.isHeaderTruncatable();
  }

  public <P extends ColumnProperty> P removeColumnProperty(String name) {
    return properties.removeColumnProperty(name);
  }

  public void setCellRenderer(CellRenderer cellRenderer) {
    Assert.notNull(cellRenderer, "cellRenderer cannot be null");
    this.cellRenderer = cellRenderer;
  }

  public abstract void setCellValue(IsRow rowValue, Object cellValue);

  public void setColumnId(int columnId) {
    this.columnId = columnId;
  }

  public void setColumnOrder(int columnOrder) {
    this.columnOrder = columnOrder;
  }

  public <P extends ColumnProperty> void setColumnProperty(String name, P property) {
    properties.setColumnProperty(name, property);
  }

  public void setColumnSortable(boolean sortable) {
    setColumnProperty(SortableProperty.NAME, new SortableProperty(sortable));
  }

  public void setColumnTruncatable(boolean truncatable) {
    TruncationProperty prop = properties.getColumnProperty(TruncationProperty.NAME);
    if (prop == null) {
      prop = new TruncationProperty(truncatable);
      setColumnProperty(TruncationProperty.NAME, prop);
    } else {
      prop.setColumnTruncatable(truncatable);
    }
  }

  public void setFooter(int row, Object footer) {
    FooterProperty prop = properties.getColumnProperty(FooterProperty.NAME);
    if (prop == null) {
      prop = new FooterProperty();
      setColumnProperty(FooterProperty.NAME, prop);
    }
    prop.setFooter(row, footer);
  }

  public void setFooter(Object footer) {
    setFooter(HeaderPropertyBase.DEFAULT_ROW, footer);
  }

  public void setFooterCount(int footerCount) {
    FooterProperty prop = properties.getColumnProperty(FooterProperty.NAME);
    if (prop == null) {
      prop = new FooterProperty();
      setColumnProperty(FooterProperty.NAME, prop);
    }
    prop.setFooterCount(footerCount);
  }

  public void setFooterTruncatable(boolean truncatable) {
    TruncationProperty prop = properties.getColumnProperty(TruncationProperty.NAME);
    if (prop == null) {
      prop = new TruncationProperty();
      setColumnProperty(TruncationProperty.NAME, prop);
    }
    prop.setFooterTruncatable(truncatable);
  }

  public void setHeader(int row, Object header) {
    HeaderProperty prop = properties.getColumnProperty(HeaderProperty.NAME);
    if (prop == null) {
      prop = new HeaderProperty();
      setColumnProperty(HeaderProperty.NAME, prop);
    }
    prop.setHeader(row, header);
  }

  public void setHeader(Object header) {
    setHeader(HeaderPropertyBase.DEFAULT_ROW, header);
  }

  public void setHeaderCount(int headerCount) {
    HeaderProperty prop = properties.getColumnProperty(HeaderProperty.NAME);
    if (prop == null) {
      prop = new HeaderProperty();
      setColumnProperty(HeaderProperty.NAME, prop);
    }
    prop.setHeaderCount(headerCount);
  }

  public void setHeaderTruncatable(boolean truncatable) {
    TruncationProperty prop = properties.getColumnProperty(TruncationProperty.NAME);
    if (prop == null) {
      prop = new TruncationProperty();
      setColumnProperty(TruncationProperty.NAME, prop);
    }
    prop.setHeaderTruncatable(truncatable);
  }

  public void setMaximumColumnWidth(int maxWidth) {
    setColumnProperty(MaximumWidthProperty.NAME, new MaximumWidthProperty(maxWidth));
  }

  public void setMinimumColumnWidth(int minWidth) {
    setColumnProperty(MinimumWidthProperty.NAME, new MinimumWidthProperty(minWidth));
  }

  public void setPreferredColumnWidth(int preferredWidth) {
    setColumnProperty(PreferredWidthProperty.NAME, new PreferredWidthProperty(preferredWidth));
  }
}
