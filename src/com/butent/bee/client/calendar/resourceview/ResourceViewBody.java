package com.butent.bee.client.calendar.resourceview;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import com.butent.bee.client.calendar.HasSettings;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Scroll;

public class ResourceViewBody extends Composite {

  private final Scroll scrollPanel = new Scroll();
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

    scrollPanel.addStyleName("scroll-area");

    layout.setCellSpacing(0);
    layout.setCellPadding(0);
    layout.setBorderWidth(0);

    layout.getColumnFormatter().setWidth(1, "99%");

    VerticalAlignmentConstant valign = HasVerticalAlignment.ALIGN_TOP;
    layout.getCellFormatter().setVerticalAlignment(0, 0, valign);
    layout.getCellFormatter().setVerticalAlignment(0, 1, valign);

    layout.getCellFormatter().setWidth(0, 0, "50px");
    StyleUtils.fixedTableLayout(layout);

    layout.setWidget(0, 0, timeline);
    layout.setWidget(0, 1, grid);

    scrollPanel.setWidget(layout);
  }

  public ResourceViewGrid getGrid() {
    return grid;
  }

  public Scroll getScrollPanel() {
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
