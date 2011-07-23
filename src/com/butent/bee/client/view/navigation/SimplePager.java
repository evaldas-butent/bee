package com.butent.bee.client.view.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.HasRows;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a user interface component, which enables organizing information on the screen into
 * several pages.
 */

public class SimplePager extends AbstractPagerImpl {

  /**
   * Specifies which CSS style resources to use.
   */
  public interface Resources extends ClientBundle {
    @Source("SimplePager.css")
    Style pagerStyle();
  }

  /**
   * Specifies which styling aspects have to be implemented on simple pager implementations.
   */
  public interface Style extends CssResource {
    String container();

    String disabledButton();

    String pageInfo();
  }

  private class GoCommand extends BeeCommand {
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
          getDisplay().setVisibleRange(0, getDisplay().getVisibleRange().getLength());
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
          int length = getDisplay().getVisibleRange().getLength();
          getDisplay().setVisibleRange(getDisplay().getRowCount() - length, length);
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

  public static NumberFormat numberFormat = NumberFormat.getFormat("#,###");
  public static String positionSeparator = " - ";
  public static String rowCountSeparator = " / ";

  public static int minRowCountForFastNavigation = 100;
  public static int minFastPages = 3;
  public static int maxFastPages = 20;

  private static Resources DEFAULT_RESOURCES = null;
  private static Style DEFAULT_STYLE = null;

  private static Resources getDefaultResources() {
    if (DEFAULT_RESOURCES == null) {
      DEFAULT_RESOURCES = GWT.create(Resources.class);
    }
    return DEFAULT_RESOURCES;
  }

  private static Style getDefaultStyle() {
    if (DEFAULT_STYLE == null) {
      DEFAULT_STYLE = getDefaultResources().pagerStyle();
      DEFAULT_STYLE.ensureInjected();
    }
    return DEFAULT_STYLE;
  }

  private final BeeImage widgetFirst;
  private final BeeImage widgetRewind;
  private final BeeImage widgetPrev;
  private final BeeImage widgetNext;
  private final BeeImage widgetForw;
  private final BeeImage widgetLast;

  private final Html widgetInfo = new Html();
  
  private final boolean showPageSize;

  public SimplePager(int maxRowCount) {
    this(maxRowCount, true);
  }
  
  public SimplePager(int maxRowCount, boolean showPageSize) {
    this(maxRowCount, showPageSize, maxRowCount >= minRowCountForFastNavigation);
  }

  public SimplePager(int maxRowCount, boolean showPageSize, boolean showFastNavigation) {
    this(maxRowCount, showPageSize, showFastNavigation, getDefaultStyle());
  }

  public SimplePager(int maxRowCount, boolean showPageSize, boolean showFastNavigation,
      Style style) {

    String s = style.disabledButton();
    widgetFirst = new BeeImage(Global.getImages().first(), new GoCommand(Navigation.FIRST), s);
    widgetPrev = new BeeImage(Global.getImages().previous(), new GoCommand(Navigation.PREV), s);
    widgetNext = new BeeImage(Global.getImages().next(), new GoCommand(Navigation.NEXT), s);
    widgetLast = new BeeImage(Global.getImages().last(), new GoCommand(Navigation.LAST), s);

    if (showFastNavigation) {
      widgetRewind = new BeeImage(Global.getImages().rewind(), new GoCommand(Navigation.REWIND), s);
      widgetForw = new BeeImage(Global.getImages().forward(), new GoCommand(Navigation.FORWARD), s);
    } else {
      widgetRewind = null;
      widgetForw = null;
    }
    
    this.showPageSize = showPageSize;

    Horizontal layout = new Horizontal();
    initWidget(layout);
    addStyleName(style.container());

    layout.setSpacing(2);

    layout.add(widgetFirst);
    if (widgetRewind != null) {
      layout.add(widgetRewind);
    }
    layout.add(widgetPrev);

    int maxWidth = Rulers.getLineWidth(createText(maxRowCount, maxRowCount, maxRowCount));
    DomUtils.setWidth(widgetInfo, maxWidth);
    widgetInfo.addStyleName(style.pageInfo());
    layout.add(widgetInfo);
    layout.setCellHorizontalAlignment(widgetInfo, HasHorizontalAlignment.ALIGN_CENTER);

    layout.add(widgetNext);
    if (widgetForw != null) {
      layout.add(widgetForw);
    }
    layout.add(widgetLast);
  }

  @Override
  protected void onRangeOrRowCountChanged() {
    HasRows display = getDisplay();
    if (display == null) {
      return;
    }

    int start = display.getVisibleRange().getStart();
    int length = display.getVisibleRange().getLength();
    int rowCount = display.getRowCount();

    widgetInfo.setText(createText(start + 1, Math.min(rowCount, start + length), rowCount));

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

  private String createText(int start, int end, int rowCount) {
    StringBuilder sb = new StringBuilder(format(start));
    if (showPageSize) {
      sb.append(positionSeparator).append(format(end));
    }
    sb.append(rowCountSeparator).append(format(rowCount));
    return sb.toString();
  }

  private String format(int x) {
    if (numberFormat == null) {
      return BeeUtils.toString(x);
    } else {
      return numberFormat.format(x);
    }
  }

  private void forward() {
    HasRows display = getDisplay();
    if (display == null) {
      return;
    }

    int start = display.getVisibleRange().getStart();
    int length = display.getVisibleRange().getLength();
    int rowCount = display.getRowCount();

    if (start < 0 || length <= 0 || rowCount <= length || start + length >= rowCount) {
      return;
    }
    display.setVisibleRange(getForwardPosition(start, length, rowCount), length);
  }

  private int getFastStep(int pageSize, int rowCount) {
    if (pageSize <= 0 || minFastPages <= 0 || maxFastPages <= 0
        || rowCount <= pageSize * minFastPages) {
      return pageSize;
    }
    return BeeUtils.limit((int) Math.sqrt(rowCount / pageSize), minFastPages, maxFastPages)
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
    HasRows display = getDisplay();
    if (display == null) {
      return;
    }

    int start = display.getVisibleRange().getStart();
    int length = display.getVisibleRange().getLength();
    int rowCount = display.getRowCount();

    if (start <= 0 || length <= 0 || rowCount <= length) {
      return;
    }
    display.setVisibleRange(getRewindPosition(start, length, rowCount), length);
  }
}
