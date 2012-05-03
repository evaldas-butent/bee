package com.butent.bee.client.datepicker;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.datepicker.DatePicker.CssClasses;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.shared.time.YearMonth;

class MonthSelector extends Component {
  
  private class Navigation implements Scheduler.ScheduledCommand {
    private final int months;
    
    private Navigation(int months) {
      super();
      this.months = months;
    }

    public void execute() {
      addMonths(months);
      getDatePicker().setFocus(true);
    }
  }

  private final Widget prevYear;
  private final Widget prevMonth;
  private final Widget nextMonth;
  private final Widget nextYear;
  
  private final Grid grid;
  
  MonthSelector(CssClasses cssClasses) {
    this.prevYear = new BeeImage(Global.getImages().rewind(), new Navigation(-12));
    this.prevMonth = new BeeImage(Global.getImages().previous(), new Navigation(-1));
    this.nextMonth = new BeeImage(Global.getImages().next(), new Navigation(1));
    this.nextYear = new BeeImage(Global.getImages().forward(), new Navigation(12));
    
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
    grid.setText(0, 2, getModel().formatCurrentMonth());
    
    YearMonth ym = getModel().getCurrentMonth();

    prevYear.setTitle(ym.previousYear().format());
    prevMonth.setTitle(ym.previousMonth().format());
    nextMonth.setTitle(ym.nextMonth().format());
    nextYear.setTitle(ym.nextYear().format());
  }

  @Override
  protected void setup() {
  }
}
