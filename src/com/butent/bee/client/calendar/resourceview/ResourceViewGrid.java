package com.butent.bee.client.calendar.resourceview;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.calendar.HasSettings;
import com.butent.bee.client.dom.StyleUtils;

public class ResourceViewGrid extends Composite {

  private static final int HOURS_PER_DAY = 24;

  private final AbsolutePanel grid = new AbsolutePanel();
  private final SimplePanel gridOverlay = new SimplePanel();

  private final HasSettings settings;

  public ResourceViewGrid(HasSettings settings) {
    initWidget(grid);
    this.settings = settings;
  }

  public void build(int workingHourStart, int workingHourStop, int cc) {
    grid.clear();
    if (cc <= 0) {
      return;
    }

    int intervalsPerHour = settings.getSettings().getIntervalsPerHour();
    int intervalSize = settings.getSettings().getPixelsPerInterval();
    
    StyleUtils.setHeight(this, intervalsPerHour * intervalSize * 24);

    for (int i = 0; i < HOURS_PER_DAY; i++) {
      boolean isWorkingHours = (i >= workingHourStart && i < workingHourStop);

      SimplePanel sp1 = new SimplePanel();
      sp1.setStyleName("major-time-interval");
      sp1.addStyleName(isWorkingHours ? "working-hours" : "non-working");

      StyleUtils.setHeight(sp1, intervalSize - 1);
      grid.add(sp1);

      for (int x = 0; x < intervalsPerHour - 1; x++) {
        SimplePanel sp2 = new SimplePanel();
        sp2.setStyleName("minor-time-interval");
        sp2.addStyleName(isWorkingHours ? "working-hours" : "non-working");

        StyleUtils.setHeight(sp2, intervalSize - 1);
        grid.add(sp2);
      }
    }

    int width = 100 / cc;
    int left = 0;
    
    for (int i = 0; i < cc; i++) {
      left = width * i;

      SimplePanel panel = new SimplePanel();
      panel.setStyleName("day-separator");
      grid.add(panel);
      panel.getElement().getStyle().setLeft(left, Unit.PCT);
    }
    
    StyleUtils.fullWidth(gridOverlay);
    StyleUtils.fullHeight(gridOverlay);

    StyleUtils.makeAbsolute(gridOverlay);
    StyleUtils.setLeft(gridOverlay, 0);
    StyleUtils.setTop(gridOverlay, 0);

    grid.add(gridOverlay);
  }

  public AbsolutePanel getGrid() {
    return grid;
  }

  public SimplePanel getGridOverlay() {
    return gridOverlay;
  }
}
