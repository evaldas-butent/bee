package com.butent.bee.client;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.LayoutPanel;

import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.Resource;

/**
 * manages the main browser window and it's main containing elements (f.e. panels).
 */

public interface Screen extends Module, NotificationListener {

  void addCommandItem(IdentifiableWidget widget);  

  void closeProgress(String id);
  
  void closeWidget(IdentifiableWidget widget);
  
  String createProgress(String caption, double max);

  int getActivePanelHeight();

  int getActivePanelWidth();

  IdentifiableWidget getActiveWidget();
  
  HasWidgets getCommandPanel();
  
  Split getScreenPanel();

  void setRootPanel(LayoutPanel rootPanel);
  
  void showGrid(IdentifiableWidget grid);

  void showInfo();

  void showResource(Resource resource);
  
  void showWidget(IdentifiableWidget widget, ScrollBars scroll, boolean newPanel);

  void updateActivePanel(IdentifiableWidget widget);

  void updateCommandPanel(IdentifiableWidget widget);
  
  void updateMenu(IdentifiableWidget widget);

  void updateProgress(String id, double value);
  
  void updateSignature(String userSign);
}
