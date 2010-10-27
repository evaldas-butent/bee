package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.shared.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ColumnResizer {
  private static final int RESOLUTION = 1;

  static class ColumnWidthInfo {
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

    public int getPreferredWidth() {
      return preferredWidth;
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

    public void setPreferredWidth(int preferredWidth) {
      this.preferredWidth = preferredWidth;
    }

    double getPercentageDifference() {
      return (newWidth - preferredWidth) / (double) preferredWidth;
    }

    int getRequiredWidth() {
      return requiredWidth;
    }

    void setNewWidth(int newWidth) {
      this.newWidth = newWidth;
    }

    void setRequiredWidth(int requiredWidth) {
      this.requiredWidth = requiredWidth;
    }
  }

  public int distributeWidth(List<ColumnWidthInfo> columns, int width) {
    Assert.notNull(columns);

    for (ColumnWidthInfo info : columns) {
      int curWidth = info.getCurrentWidth();
      if (info.hasMinimumWidth() && curWidth < info.getMinimumWidth()) {
        curWidth = info.getMinimumWidth();
      } else if (info.hasMaximumWidth() && curWidth > info.getMaximumWidth()) {
        curWidth = info.getMaximumWidth();
      }
      width -= (curWidth - info.getCurrentWidth());
      info.setNewWidth(curWidth);
    }

    if (width == 0) {
      return 0;
    }

    List<ColumnWidthInfo> orderedColumns = new ArrayList<ColumnWidthInfo>(columns);

    if (width > 0) {
      Comparator<ColumnWidthInfo> comparator = new Comparator<ColumnWidthInfo>() {
        public int compare(ColumnWidthInfo o1, ColumnWidthInfo o2) {
          double diff1 = o1.getPercentageDifference();
          double diff2 = o2.getPercentageDifference();
          if (diff1 < diff2) {
            return -1;
          } else if (diff1 == diff2) {
            return 0;
          } else {
            return 1;
          }
        }
      };
      Collections.sort(orderedColumns, comparator);

    } else if (width < 0) {
      Comparator<ColumnWidthInfo> comparator = new Comparator<ColumnWidthInfo>() {
        public int compare(ColumnWidthInfo o1, ColumnWidthInfo o2) {
          double diff1 = o1.getPercentageDifference();
          double diff2 = o2.getPercentageDifference();
          if (diff1 > diff2) {
            return -1;
          } else if (diff1 == diff2) {
            return 0;
          } else {
            return 1;
          }
        }
      };
      Collections.sort(orderedColumns, comparator);
    }

    return distributeWidthImpl(orderedColumns, width);
  }

  private int distributeWidthImpl(List<ColumnWidthInfo> columns, int width) {
    boolean growing = (width > 0);
    boolean fullySynced = false;
    int syncedColumns = 1;

    while (columns.size() > 0 && width != 0) {
      double targetDiff = getTargetDiff(columns, syncedColumns, width);

      int totalRequired = 0;
      for (int curIndex = 0; curIndex < syncedColumns; curIndex++) {
        ColumnWidthInfo curInfo = columns.get(curIndex);
        int preferredWidth = curInfo.getPreferredWidth();
        int newWidth = (int) (targetDiff * preferredWidth) + preferredWidth;

        if (growing) {
          newWidth = Math.max(newWidth, curInfo.getCurrentWidth());
          if (curInfo.hasMaximumWidth()) {
            newWidth = Math.min(newWidth, curInfo.getMaximumWidth());
          }
        } else {
          newWidth = Math.min(newWidth, curInfo.getCurrentWidth());
          if (curInfo.hasMinimumWidth()) {
            newWidth = Math.max(newWidth, curInfo.getMinimumWidth());
          }
        }

        curInfo.setRequiredWidth(newWidth - curInfo.getNewWidth());
        totalRequired += curInfo.getRequiredWidth();
      }

      double percentAvailable = 1.0;
      if (totalRequired != 0) {
        percentAvailable = Math.min(1.0, width / (double) totalRequired);
      }
      for (int curIndex = 0; curIndex < syncedColumns; curIndex++) {
        ColumnWidthInfo curInfo = columns.get(curIndex);
        int required = (int) (percentAvailable * curInfo.getRequiredWidth());

        if (fullySynced) {
          if (growing) {
            required = Math.max(RESOLUTION, required);
          } else {
            required = Math.min(-RESOLUTION, required);
          }
        }

        if (growing && required > width) {
          required = width;
        } else if (!growing && required < width) {
          required = width;
        }

        curInfo.setNewWidth(curInfo.getNewWidth() + required);
        width -= required;

        boolean maxedOut = false;
        if (growing && curInfo.hasMaximumWidth()) {
          maxedOut = (curInfo.getNewWidth() >= curInfo.getMaximumWidth());
        } else if (!growing && curInfo.hasMinimumWidth()) {
          maxedOut = (curInfo.getNewWidth() <= curInfo.getMinimumWidth());
        }
        if (maxedOut) {
          columns.remove(curIndex);
          curIndex--;
          syncedColumns--;
        }
      }

      if (!fullySynced && syncedColumns < columns.size()) {
        syncedColumns++;
      } else {
        fullySynced = true;
      }
    }

    return width;
  }

  private double getTargetDiff(List<ColumnWidthInfo> columns,
      int syncedColumns, int width) {
    if (syncedColumns < columns.size()) {
      return columns.get(syncedColumns).getPercentageDifference();
    } else {
      int totalNewWidth = width;
      int totalPreferredWidth = 0;
      for (ColumnWidthInfo info : columns) {
        totalNewWidth += info.getNewWidth();
        totalPreferredWidth += info.getPreferredWidth();
      }
      return (totalNewWidth - totalPreferredWidth) / (double) totalPreferredWidth;
    }
  }
}
