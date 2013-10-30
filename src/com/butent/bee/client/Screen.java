package com.butent.bee.client;

import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.client.layout.Split;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.Workspace;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.ui.UserInterface;

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
  
  String createProgress(String caption, Double max, IdentifiableWidget closeBox);

  int getActivePanelHeight();

  int getActivePanelWidth();

  IdentifiableWidget getActiveWidget();
  
  HasWidgets getCommandPanel();

  int getHeight();

  Split getScreenPanel();
  
  UserInterface getUserInterface();

  int getWidth();
  
  Workspace getWorkspace();
  
  void onLoad();

  boolean removeDomainEntry(Domain domain, Long key);
  
  void showInfo();

  void showWidget(IdentifiableWidget widget, boolean newPlace);

  void updateActivePanel(IdentifiableWidget widget);

  void updateCommandPanel(IdentifiableWidget widget);
  
  void updateMenu(IdentifiableWidget widget);

  void updateProgress(String id, double value);
  
  void updateUserData(UserData userData);
}
