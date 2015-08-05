package com.butent.bee.client.modules.projects;

import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.HasDomain;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.html.builder.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProjectTemplateController extends Flow implements HasDomain {

  private Flow content;
  private ChildGrid stages;

  public ProjectTemplateController() {
    content = new Flow(BeeConst.CSS_CLASS_PREFIX + "prj-Template-Controller");
    add(content);
  }

  @Override
  public Domain getDomain() {
    return Domain.ADMIN;
  }

//  public void setContent(Widget content) {
//    this.content.clear();
//    this.content.add(content);
//  }

  public void setStages(ChildGrid stages) {
    this.stages = stages;
    content.add(stages);
    StyleUtils.setVisible(this.stages, false);
  }

  public void showStages() {
    clearContent();
    StyleUtils.setVisible(stages, true);
  }


  public void clearContent() {
    StyleUtils.setVisible(stages, false);
  }
}
