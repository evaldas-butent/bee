package com.butent.bee.egg.client.grid;

public class ColumnWidthInfo {
  private int minWidth;
  private int maxWidth;
  private int prefWidth;
  private int curWidth;
  
  private int dataWidth;
  private int headerWidth;
  private int footerWidth;

  private int newWidth = 0;
  private int requiredWidth;

  public ColumnWidthInfo(int minWidth, int maxWidth, int prefWidth,
      int curWidth, int dataWidth, int headerWidth, int footerWidth) {
    this.minWidth = minWidth;
    this.maxWidth = maxWidth;
    this.prefWidth = prefWidth;

    this.curWidth = curWidth;
    this.dataWidth = dataWidth;
    this.headerWidth = headerWidth;
    this.footerWidth = footerWidth;
  }

  public int getCurWidth() {
    return curWidth;
  }

  public int getDataWidth() {
    return dataWidth;
  }

  public int getFooterWidth() {
    return footerWidth;
  }

  public int getHeaderWidth() {
    return headerWidth;
  }

  public int getMaxWidth() {
    return maxWidth;
  }

  public int getMinWidth() {
    return minWidth;
  }

  public int getNewWidth() {
    return newWidth;
  }

  public double getPercentageDifference() {
    return (newWidth - prefWidth) / (double) prefWidth;
  }

  public int getPrefWidth() {
    return prefWidth;
  }

  public int getRequiredWidth() {
    return requiredWidth;
  }

  public boolean hasMaxWidth() {
    return maxWidth > 0;
  }

  public boolean hasMinWidth() {
    return minWidth > 0;
  }

  public void setCurWidth(int curWidth) {
    this.curWidth = curWidth;
  }

  public void setDataWidth(int dataWidth) {
    this.dataWidth = dataWidth;
  }

  public void setFooterWidth(int footerWidth) {
    this.footerWidth = footerWidth;
  }

  public void setHeaderWidth(int headerWidth) {
    this.headerWidth = headerWidth;
  }

  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public void setMinWidth(int minWidth) {
    this.minWidth = minWidth;
  }

  public void setNewWidth(int newWidth) {
    this.newWidth = newWidth;
  }

  public void setPrefWidth(int prefWidth) {
    this.prefWidth = prefWidth;
  }

  public void setRequiredWidth(int requiredWidth) {
    this.requiredWidth = requiredWidth;
  }
}
