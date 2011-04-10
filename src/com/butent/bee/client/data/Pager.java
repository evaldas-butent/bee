package com.butent.bee.client.data;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.HasRows;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class Pager extends AbstractPager {

  public interface Resources extends ClientBundle {
    @Source("Pager.css")
    Style pagerStyle();
  }

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
      switch (goTo) {
        case FIRST:
          firstPage();
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
          lastPage();
          break;
        default:
          Assert.untouchable();
      }
    }
  }

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

  private final int maxRowCount;

  public Pager(int maxRowCount) {
    this(maxRowCount, maxRowCount >= minRowCountForFastNavigation);
  }

  public Pager(int maxRowCount, boolean showFastNavigation) {
    this(maxRowCount, showFastNavigation, getDefaultStyle());
  }

  public Pager(int maxRowCount, boolean showFastNavigation, Style style) {
    this.maxRowCount = maxRowCount;

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

    Horizontal layout = new Horizontal();
    initWidget(layout);
    addStyleName(style.container());

    layout.setSpacing(2);

    layout.add(widgetFirst);
    if (widgetRewind != null) {
      layout.add(widgetRewind);
    }
    layout.add(widgetPrev);

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
  protected void onLoad() {
    super.onLoad();

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        String text = widgetInfo.getText();
        int oldWidth = widgetInfo.getOffsetWidth();
        widgetInfo.setText(createText(maxRowCount, maxRowCount, maxRowCount));
        int newWidth = widgetInfo.getOffsetWidth();
        widgetInfo.setText(text);
        if (newWidth > oldWidth) {
          DomUtils.setWidth(widgetInfo, newWidth);
        }
      }
    });
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
    return format(start) + positionSeparator + format(end) + rowCountSeparator + format(rowCount);
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
