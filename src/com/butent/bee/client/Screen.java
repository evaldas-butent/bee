package com.butent.bee.client;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.LayoutPanel;

import com.butent.bee.client.layout.Split;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.Workspace;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.UserData;

/**
 * manages the main browser window and it's main containing elements (f.e. panels).
 */

public interface Screen extends Module, NotificationListener {

  boolean activateDomainEntry(Domain domain, Long key);

  void activateWidget(IdentifiableWidget widget);
  
  void addCommandItem(IdentifiableWidget widget);  

  void addDomainEntry(Domain domain, IdentifiableWidget widget, Long key, String caption);
  
  void closeProgress(String id);
  
  void closeWidget(IdentifiableWidget widget);

  boolean containsDomainEntry(Domain domain, Long key);
  
  String createProgress(String caption, double max);

  int getActivePanelHeight();

  int getActivePanelWidth();

  IdentifiableWidget getActiveWidget();
  
  HasWidgets getCommandPanel();

  int getHeight();

  Split getScreenPanel();

  int getWidth();
  
  Workspace getWorkspace();

  boolean removeDomainEntry(Domain domain, Long key);
  
  void setRootPanel(LayoutPanel rootPanel);
  
  void showInfo();

  void showWidget(IdentifiableWidget widget, boolean newPlace);

  void updateActivePanel(IdentifiableWidget widget);

  void updateCommandPanel(IdentifiableWidget widget);
  
  void updateMenu(IdentifiableWidget widget);

  void updateProgress(String id, double value);
  
  void updateSignature(UserData userData);
}
