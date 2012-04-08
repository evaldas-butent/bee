package com.butent.bee.client.datepicker;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

import com.butent.bee.client.datepicker.DatePicker.CssClasses;

public class MonthSelector extends Component {

  private final CssClasses cssClasses;
  
  private PushButton backwards;
  private PushButton forwards;
  private Grid grid;

  public MonthSelector(CssClasses cssClasses) {
    this.cssClasses = cssClasses;
  }
 
  @Override
  protected void refresh() {
    String formattedMonth = getModel().formatCurrentMonth();
    grid.setText(0, 1, formattedMonth);
  }

  @Override
  protected void setup() {
    backwards = new PushButton();
    backwards.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addMonths(-1);
      }
    });

    backwards.getUpFace().setHTML("&laquo;");
    backwards.setStyleName(cssClasses.previousButton());

    forwards = new PushButton();
    forwards.getUpFace().setHTML("&raquo;");
    forwards.setStyleName(cssClasses.nextButton());
    forwards.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addMonths(+1);
      }
    });

    grid = new Grid(1, 3);
    grid.setWidget(0, 0, backwards);
    grid.setWidget(0, 2, forwards);

    CellFormatter formatter = grid.getCellFormatter();
    formatter.setStyleName(0, 1, cssClasses.month());
    formatter.setWidth(0, 0, "1");
    formatter.setWidth(0, 1, "100%");
    formatter.setWidth(0, 2, "1");
    grid.setStyleName(cssClasses.monthSelector());
    initWidget(grid);
  }
}
