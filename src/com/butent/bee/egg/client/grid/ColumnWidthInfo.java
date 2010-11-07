package com.butent.bee.egg.client.grid;

public class ColumnWidthInfo {
  private int minWidth;
  private int maxWidth;
  private int preferredWidth;
  private int curWidth;

  private int newWidth = 0;

  private int requiredWidth;

  public ColumnWidthInfo(int minWidth, int maxWidth, int preferredWidth, int curWidth) {
    this.minWidth = minWidth;
    this.maxWidth = maxWidth;
    this.preferredWidth = preferredWidth;
    this.curWidth = curWidth;
  }

  public int getCurrentWidth() {
    return curWidth;
  }

  public int getMaximumWidth() {
    if (hasMaximumWidth()) {
      return Math.max(maxWidth, minWidth);
    }
    return maxWidth;
  }

  public int getMinimumWidth() {
    return minWidth;
  }

  public int getNewWidth() {
    return newWidth;
  }

  public double getPercentageDifference() {
    return (newWidth - preferredWidth) / (double) preferredWidth;
  }

  public int getPreferredWidth() {
    return preferredWidth;
  }

  public int getRequiredWidth() {
    return requiredWidth;
  }

  public boolean hasMaximumWidth() {
    return maxWidth >= 0;
  }

  public boolean hasMinimumWidth() {
    return minWidth >= 0;
  }

  public void setCurrentWidth(int curWidth) {
    this.curWidth = curWidth;
  }

  public void setMaximumWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public void setMinimumWidth(int minWidth) {
    this.minWidth = minWidth;
  }

  public void setNewWidth(int newWidth) {
    this.newWidth = newWidth;
  }

  public void setPreferredWidth(int preferredWidth) {
    this.preferredWidth = preferredWidth;
  }

  public void setRequiredWidth(int requiredWidth) {
    this.requiredWidth = requiredWidth;
  }
}
