package com.butent.bee.client.calendar.resourceview;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.HasSettings;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;

public class ResourceViewBody extends Composite {

  private final ScrollPanel scrollPanel = new ScrollPanel();
  private final FlexTable layout = new FlexTable();

  private final HasSettings settings;

  private final ResourceViewTimeline timeline;
  private final ResourceViewGrid grid;

  public ResourceViewBody(HasSettings settings) {
    initWidget(scrollPanel);

    this.settings = settings;
    
    this.timeline = new ResourceViewTimeline(settings);

    this.grid = new ResourceViewGrid(settings);
    grid.setStyleName("bee-appointment-panel");

    scrollPanel.setStylePrimaryName("scroll-area");
    StyleUtils.hideScroll(scrollPanel, ScrollBars.HORIZONTAL);
    StyleUtils.alwaysScroll(scrollPanel, ScrollBars.VERTICAL);

    layout.setCellPadding(0);
    layout.setBorderWidth(0);
    layout.setCellSpacing(0);

    layout.getColumnFormatter().setWidth(1, "99%");

    VerticalAlignmentConstant valign = HasVerticalAlignment.ALIGN_TOP;
    layout.getCellFormatter().setVerticalAlignment(0, 0, valign);
    layout.getCellFormatter().setVerticalAlignment(0, 1, valign);

    layout.getCellFormatter().setWidth(0, 0, "50px");
    StyleUtils.fixedTableLayout(layout);

    layout.setWidget(0, 0, timeline);
    layout.setWidget(0, 1, grid);

    scrollPanel.add(layout);
  }

  public void add(Widget w) {
    scrollPanel.add(w);
  }

  public ResourceViewGrid getDayViewGrid() {
    return grid;
  }

  public ResourceViewTimeline getDayViewTimeline() {
    return timeline;
  }

  public ResourceViewGrid getGrid() {
    return grid;
  }

  public ScrollPanel getScrollPanel() {
    return scrollPanel;
  }

  public ResourceViewTimeline getTimeline() {
    return timeline;
  }

  public void setColumns(int count) {
    grid.build(settings.getSettings().getWorkingHourStart(),
        settings.getSettings().getWorkingHourEnd(), count);
  }
}
