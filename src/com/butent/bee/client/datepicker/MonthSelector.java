package com.butent.bee.client.datepicker;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

import com.butent.bee.client.Global;
import com.butent.bee.client.datepicker.DatePicker.CssClasses;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;

class MonthSelector extends Component {
  
  private final class Navigation implements Scheduler.ScheduledCommand {
    private final int months;
    
    private Navigation(int months) {
      super();
      this.months = months;
    }

    @Override
    public void execute() {
      addMonths(months);
      getDatePicker().setFocus(true);
    }
  }

  private final Image prevYear;
  private final Image prevMonth;
  private final Image nextMonth;
  private final Image nextYear;
  
  private final Grid grid;
  
  MonthSelector(CssClasses cssClasses) {
    String styleDisabled = cssClasses.monthNavigationDisabled(); 

    this.prevYear = new Image(Global.getImages().rewind(), new Navigation(-12), styleDisabled);
    this.prevMonth = new Image(Global.getImages().previous(), new Navigation(-1), styleDisabled);
    this.nextMonth = new Image(Global.getImages().next(), new Navigation(1), styleDisabled);
    this.nextYear = new Image(Global.getImages().forward(), new Navigation(12), styleDisabled);
    
    this.grid = new Grid(1, 5);
    grid.setWidget(0, 0, prevYear);
    grid.setWidget(0, 1, prevMonth);
    grid.setWidget(0, 3, nextMonth);
    grid.setWidget(0, 4, nextYear);

    CellFormatter formatter = grid.getCellFormatter();
    for (int i = 0; i < 5; i++) {
      if (i != 2) {
        formatter.setStyleName(0, i, cssClasses.monthNavigation());
      }
    }

    formatter.setWidth(0, 2, "100%");
    formatter.setStyleName(0, 2, cssClasses.month());
    
    grid.setStyleName(cssClasses.monthSelector());
    initWidget(grid);
  }
 
  @Override
  protected void refresh() {
    YearMonth current = getModel().getCurrentMonth();
    grid.setText(0, 2, getModel().format(current));
    
    refresh(prevYear, current.previousYear());
    refresh(prevMonth, current.previousMonth());
    
    refresh(nextMonth, current.nextMonth());
    refresh(nextYear, current.nextYear());
  }

  @Override
  protected void setUp() {
  }
  
  private boolean isEnabled(YearMonth ym) {
    JustDate minDate = getDatePicker().getMinDate();
    if (minDate != null && TimeUtils.isMore(minDate, ym.getLast())) {
      return false;
    }

    JustDate maxDate = getDatePicker().getMaxDate();
    if (maxDate != null && TimeUtils.isLess(maxDate, ym)) {
      return false;
    }
    return true;
  }
  
  private void refresh(Image widget, YearMonth ym) {
    boolean enabled = isEnabled(ym);
    widget.setEnabled(enabled);
    
    if (enabled) {
      widget.setTitle(getModel().format(ym));
    } else {
      widget.setTitle(BeeConst.STRING_EMPTY);
    }
  }
}
