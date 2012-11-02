package com.butent.bee.client.view.navigation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.logical.ScopeChangeEvent;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.utils.BeeUtils;

public class SimplePager extends AbstractPager {

  private class GoCommand extends Command {
    private Navigation goTo;

    private GoCommand(Navigation goTo) {
      super();
      this.goTo = goTo;
    }

    @Override
    public void execute() {
      if (!isEnabled() || getDisplay() == null) {
        return;
      }

      switch (goTo) {
        case FIRST:
          setPageStart(0);
          break;
        case REWIND:
          rewind();
          break;
        case PREV:
          previousPage();
          break;
        case NEXT:
          nextPage();
          break;
        case FORWARD:
          forward();
          break;
        case LAST:
          setPageStart(getRowCount() - getPageSize());
          break;
        default:
          Assert.untouchable();
      }
    }
  }

  /**
   * Contains possible navigation options like first, previous, next, last etc.
   */
  private static enum Navigation {
    FIRST, REWIND, PREV, NEXT, FORWARD, LAST
  }

  private static final String STYLE_PREFIX = "bee-SimplePager-";
  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";
  private static final String STYLE_DISABLED_BUTTON = STYLE_PREFIX + "disabledButton";
  private static final String STYLE_INFO = STYLE_PREFIX + "info";
  
  private static final NumberFormat NUMBER_FORMAT = NumberFormat.getFormat("#,###");
  private static final String POSITION_SEPARATOR = " - ";
  private static final String ROW_COUNT_SEPARATOR = " / ";

  private static final int MIN_ROW_COUNT_FOR_FAST_NAVIGATION = 100;
  private static final int MIN_FAST_PAGES = 3;
  private static final int MAX_FAST_PAGES = 20;

  private final BeeImage widgetFirst;
  private final BeeImage widgetRewind;
  private final BeeImage widgetPrev;
  private final BeeImage widgetNext;
  private final BeeImage widgetForw;
  private final BeeImage widgetLast;

  private final Html widgetInfo;
  
  private final boolean showPageSize;
  
  private int maxRowCount;

  public SimplePager(int maxRowCount) {
    this(maxRowCount, true);
  }
  
  public SimplePager(int maxRowCount, boolean showPageSize) {
    this(maxRowCount, showPageSize, maxRowCount >= MIN_ROW_COUNT_FOR_FAST_NAVIGATION);
  }

  public SimplePager(int maxRowCount, boolean showPageSize, boolean showFastNavigation) {
    this.maxRowCount = maxRowCount;
    this.showPageSize = showPageSize;

    this.widgetFirst = new BeeImage(Global.getImages().first(), new GoCommand(Navigation.FIRST),
        STYLE_DISABLED_BUTTON);
    this.widgetPrev = new BeeImage(Global.getImages().previous(), new GoCommand(Navigation.PREV),
        STYLE_DISABLED_BUTTON);
    this.widgetNext = new BeeImage(Global.getImages().next(), new GoCommand(Navigation.NEXT),
        STYLE_DISABLED_BUTTON);
    this.widgetLast = new BeeImage(Global.getImages().last(), new GoCommand(Navigation.LAST),
        STYLE_DISABLED_BUTTON);

    if (showFastNavigation) {
      this.widgetRewind = new BeeImage(Global.getImages().rewind(), new GoCommand(Navigation.REWIND),
          STYLE_DISABLED_BUTTON);
      this.widgetForw = new BeeImage(Global.getImages().forward(), new GoCommand(Navigation.FORWARD),
          STYLE_DISABLED_BUTTON);
    } else {
      this.widgetRewind = null;
      this.widgetForw = null;
    }

    Horizontal container = new Horizontal();
    initWidget(container);
    addStyleName(STYLE_CONTAINER);

    container.add(widgetFirst);
    if (widgetRewind != null) {
      container.add(widgetRewind);
    }
    container.add(widgetPrev);

    this.widgetInfo = new Html();
    StyleUtils.setWidth(widgetInfo, getMaxInfoWidth(maxRowCount));
    widgetInfo.addStyleName(STYLE_INFO);

    container.add(widgetInfo);
    container.setCellHorizontalAlignment(widgetInfo, HasHorizontalAlignment.ALIGN_CENTER);

    container.add(widgetNext);
    if (widgetForw != null) {
      container.add(widgetForw);
    }
    container.add(widgetLast);
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    return !DomUtils.isImageElement(source);
  }

  @Override
  public void onScopeChange(ScopeChangeEvent event) {
    if (event == null) {
      return;
    }

    int start = BeeUtils.toNonNegativeInt(event.getStart());
    int length = BeeUtils.toNonNegativeInt(event.getLength());
    int rowCount = BeeUtils.toNonNegativeInt(event.getTotal());
    
    if (start >= rowCount) {
      start = Math.max(rowCount - 1, 0);
    }
    if (start + length > rowCount) {
      length = Math.max(rowCount - start, 0);
    }
    
    if (rowCount > getMaxRowCount()) {
      setMaxRowCount(rowCount);
      StyleUtils.setWidth(widgetInfo, getMaxInfoWidth(rowCount));
    }

    widgetInfo.setText(createText(Math.min(start + 1, rowCount),
        Math.min(rowCount, start + length), rowCount));

    widgetFirst.setEnabled(start > 0);
    widgetPrev.setEnabled(start > 0);

    widgetNext.setEnabled(start + length < rowCount);
    widgetLast.setEnabled(start + length < rowCount);

    if (widgetRewind != null && widgetForw != null) {
      if (start > 0) {
        widgetRewind.setEnabled(true);
        widgetRewind.setTitle(format(getRewindPosition(start, length, rowCount) + 1));
      } else {
        widgetRewind.setEnabled(false);
        DomUtils.clearTitle(widgetRewind);
      }

      if (start + length < rowCount) {
        widgetForw.setEnabled(true);
        widgetForw.setTitle(format(getForwardPosition(start, length, rowCount) + 1));
      } else {
        widgetForw.setEnabled(false);
        DomUtils.clearTitle(widgetForw);
      }
    }
  }

  @Override
  protected NavigationOrigin getNavigationOrigin() {
    return NavigationOrigin.PAGER;
  }

  private String createText(int start, int end, int rowCount) {
    StringBuilder sb = new StringBuilder(format(start));
    if (showPageSize) {
      sb.append(POSITION_SEPARATOR).append(format(end));
    }
    sb.append(ROW_COUNT_SEPARATOR).append(format(rowCount));
    return sb.toString();
  }

  private String format(int x) {
    if (NUMBER_FORMAT == null) {
      return BeeUtils.toString(x);
    } else {
      return NUMBER_FORMAT.format(x);
    }
  }

  private void forward() {
    if (getDisplay() == null) {
      return;
    }

    int start = getPageStart();
    int length = getPageSize();
    int rowCount = getRowCount();

    if (start < 0 || length <= 0 || rowCount <= length || start + length >= rowCount) {
      return;
    }
    setPageStart(getForwardPosition(start, length, rowCount));
  }

  private int getFastStep(int pageSize, int rowCount) {
    if (pageSize <= 0 || MIN_FAST_PAGES <= 0 || MAX_FAST_PAGES <= 0
        || rowCount <= pageSize * MIN_FAST_PAGES) {
      return pageSize;
    }
    return BeeUtils.clamp((int) Math.sqrt(rowCount / pageSize), MIN_FAST_PAGES, MAX_FAST_PAGES)
        * pageSize;
  }

  private int getForwardPosition(int pageStart, int pageSize, int rowCount) {
    int step = getFastStep(pageSize, rowCount);
    if (pageStart + step + pageSize >= rowCount) {
      return rowCount - pageSize;
    } else {
      int pos = pageStart + step;
      return pos - pos % pageSize;
    }
  }
  
  private int getMaxInfoWidth(int rowCount) {
    return Rulers.getLineWidth(null, createText(rowCount, rowCount, rowCount), false);
  }

  private int getMaxRowCount() {
    return maxRowCount;
  }

  private int getRewindPosition(int pageStart, int pageSize, int rowCount) {
    int step = getFastStep(pageSize, rowCount);
    if (step >= pageStart + pageSize) {
      return 0;
    } else {
      int pos = pageStart - step;
      return pos - pos % pageSize;
    }
  }

  private void rewind() {
    if (getDisplay() == null) {
      return;
    }

    int start = getPageStart();
    int length = getPageSize();
    int rowCount = getRowCount();

    if (start <= 0 || length <= 0 || rowCount <= length) {
      return;
    }
    setPageStart(getRewindPosition(start, length, rowCount));
  }

  private void setMaxRowCount(int maxRowCount) {
    this.maxRowCount = maxRowCount;
  }
}
