package com.butent.bee.client;

import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.layout.TilePanel;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.BeeResource;

/**
 * manages the main browser window and it's main containing elements (f.e. panels).
 */

public interface Screen extends Module, NotificationListener {

  void activatePanel(TilePanel np);
  
  void closeView(View view);

  TilePanel getActivePanel();

  int getActivePanelHeight();

  int getActivePanelWidth();

  String getDsn();

  Split getScreenPanel();

  boolean isTemporaryDetach();

  void setRootPanel(LayoutPanel rootPanel);

  void showGrid(Object data, String... cols);

  void showResource(BeeResource resource);

  void updateActivePanel(Widget w);

  void updateActivePanel(Widget w, ScrollBars scroll);

  void updateActiveQuietly(Widget w, ScrollBars scroll);

  void updateData(Widget w);

  void updateMenu(Widget w);

  void updateSignature();
}
