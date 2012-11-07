package com.butent.bee.client;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Split;
import com.butent.bee.shared.Resource;

/**
 * manages the main browser window and it's main containing elements (f.e. panels).
 */

public interface Screen extends Module, NotificationListener {

  void addCommandItem(Widget widget);  

  void closeProgress(String id);
  
  void closeWidget(Widget widget);
  
  String createProgress(String caption, double max);

  int getActivePanelHeight();

  int getActivePanelWidth();

  Widget getActiveWidget();
  
  HasWidgets getCommandPanel();
  
  Split getScreenPanel();

  void setRootPanel(LayoutPanel rootPanel);
  
  void showGrid(Widget grid);

  void showInfo();

  void showResource(Resource resource);
  
  void updateActivePanel(Widget w);

  void updateActivePanel(Widget w, ScrollBars scroll);

  void updateCommandPanel(Widget w);
  
  void updateMenu(Widget w);

  void updateProgress(String id, double value);
  
  void updateSignature(String userSign);
}
