package com.butent.bee.client.modules.mail;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.HandlesStateChange;
import com.butent.bee.client.screen.HasDomain;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.State;
import com.butent.bee.shared.logging.LogUtils;

public class MailController extends Flow implements HasDomain, HandlesStateChange {

  private MailPanel activePanel;

  public MailController() {
    super();
    add(new BeeLabel("Pašto kontroleris"));
  }

  public MailPanel getActivePanel() {
    return activePanel;
  }

  @Override
  public Domain getDomain() {
    return Domain.MAIL;
  }

  @Override
  public void onStateChange(State state) {
    if (State.ACTIVATED.equals(state) && activePanel != null) {
      BeeKeeper.getScreen()
          .activateWidget(activePanel.getFormView().getViewPresenter().getMainView());
    } else if (State.REMOVED.equals(state)) {
      MailKeeper.removeMailPanels();
    }
    LogUtils.getRootLogger().warning("MailController", state);
  }

  public void setActivePanel(MailPanel mailPanel) {
    if (mailPanel == activePanel) {
      return;
    }
    activePanel = mailPanel;
  }
}
