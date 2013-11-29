package com.butent.bee.client.modules.transport;

import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.cli.Shell;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.screen.ScreenImpl;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.UserInterface;

public class SelfServiceScreen extends ScreenImpl {

  public SelfServiceScreen() {
    super();
  }

  @Override
  public UserInterface getUserInterface() {
    return UserInterface.SELF_SERVICE;
  }
  
  @Override
  public void start() {
    super.start();
    
    Data.setVisibleViews(Sets.newHashSet(VIEW_CARGO_REQUESTS, VIEW_CARGO_REQUEST_FILES,
        VIEW_CARGO_REQUEST_TEMPLATES, VIEW_ORDERS, VIEW_ORDER_CARGO, VIEW_TRIPS));
    Data.setEditableViews(Sets.newHashSet(VIEW_CARGO_REQUESTS, VIEW_CARGO_REQUEST_FILES,
        VIEW_CARGO_REQUEST_TEMPLATES));
    
    GridFactory.hideColumn(VIEW_CARGO_REQUESTS, COL_CARGO_REQUEST_USER);
    GridFactory.hideColumn(VIEW_CARGO_REQUEST_TEMPLATES, COL_CARGO_REQUEST_TEMPLATE_USER);

    addCommandItem(new Button(Localized.getConstants().trSelfServiceCommandNewRequest(),
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            RowFactory.createRow(VIEW_CARGO_REQUESTS);
          }
        }));

    addCommandItem(new Button(Localized.getConstants().trSelfServiceCommandRequests(),
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            GridFactory.openGrid(VIEW_CARGO_REQUESTS,
                GridOptions.forCurrentUserFilter(COL_CARGO_REQUEST_USER));
          }
        }));

    addCommandItem(new Button(Localized.getConstants().trSelfServiceCommandTemplates(),
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            GridFactory.openGrid(VIEW_CARGO_REQUEST_TEMPLATES,
                GridOptions.forCurrentUserFilter(COL_CARGO_REQUEST_TEMPLATE_USER));
          }
        }));

    addCommandItem(new Button(Localized.getConstants().trSelfServiceCommandHistory()));
  }

  @Override
  public void updateMenu(IdentifiableWidget widget) {
  }

  @Override
  protected String getScreenStyle() {
    return "bee-tr-SelfService-screen";
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initEast() {
    return Pair.of(ClientLogManager.getLogPanel(), 0);
  }

  @Override
  protected Pair<? extends IdentifiableWidget, Integer> initWest() {
    Shell shell = new Shell("bee-tr-SelfService-shell");
    shell.restore();

    Simple wrapper = new Simple(shell);
    return Pair.of(wrapper, 0);
  }
}
