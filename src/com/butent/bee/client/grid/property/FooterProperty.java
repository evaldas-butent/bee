package com.butent.bee.client.grid.property;

/**
 * Extends {@code HeaderPropertyBase} class, handles setting and getting column footer and footer
 * count.
 */

public class FooterProperty extends HeaderPropertyBase {
  public static final String NAME = "footer";

  public Object getFooter(int row) {
    return super.getHeader(row);
  }

  public int getFooterCount() {
    return super.getHeaderCount();
  }

  public void removeFooter(int row) {
    super.removeHeader(row);
  }

  public void setFooter(int row, Object footer) {
    super.setHeader(row, footer);
  }

  public void setFooterCount(int footerCount) {
    super.setHeaderCount(footerCount);
  }
}
