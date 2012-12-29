package com.butent.bee.client.modules.mail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.MenuManager.MenuCallback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;

import java.util.Map;
import java.util.Set;

public class MailKeeper {

  private static MailController controller = null;
  private static MailPanel activePanel;
  private static final Set<MailPanel> mailPanels = Sets.newHashSet();

  public static void register() {
    BeeKeeper.getMenu().registerMenuCallback("mail_parameters", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Parameters", new ParametersHandler(parameters));
      }
    });
    BeeKeeper.getMenu().registerMenuCallback(SVC_RESTART_PROXY,
        new MenuManager.MenuCallback() {
          @Override
          public void onSelection(String parameters) {
            BeeKeeper.getRpc().makeGetRequest(createArgs(SVC_RESTART_PROXY));
          }
        });

    BeeKeeper.getMenu().registerMenuCallback("open_mail", new MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        mailPanels.add(new MailPanel());
      }
    });

    GridFactory.registerGridInterceptor(TBL_ACCOUNTS, new AbstractGridInterceptor() {
      @Override
      public Map<String, Filter> getInitialFilters() {
        return ImmutableMap.of("UserFilter",
            ComparisonFilter.isEqual(COL_USER, new LongValue(BeeKeeper.getUser().getUserId())));
      }

      @Override
      public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
        newRow.setValue(DataUtils.getColumnIndex(COL_USER, gridView.getDataColumns()),
            BeeKeeper.getUser().getUserId());
        return true;
      }
    });
  }

  static void activateController(MailPanel mailPanel) {
    if (controller == null) {
      controller = new MailController();
      BeeKeeper.getScreen().addDomainEntry(Domain.MAIL, controller, null, "Laiškai");
    }
    activePanel = mailPanel;
    rebuildController();
    BeeKeeper.getScreen().activateDomainEntry(Domain.MAIL, null);
  }

  static void activateMailPanel() {
    if (activePanel != null) {
      BeeKeeper.getScreen()
          .activateWidget(activePanel.getFormView().getViewPresenter().getMainView());
    }
  }

  static void clickFolder(Long folderId) {
    if (activePanel != null) {
      activePanel.refresh(folderId);
    }
  }

  static void clickSystemFolder(SystemFolder sysFolder) {
    if (activePanel != null) {
      clickFolder(activePanel.getCurrentAccount().getSystemFolderId(sysFolder));
    }
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(MAIL_MODULE);
    args.addQueryItem(MAIL_METHOD, name);
    return args;
  }

  static void rebuildController() {
    if (controller != null && activePanel != null) {
      controller.rebuild(activePanel.getCurrentAccount());
      refreshController();
    }
  }

  static void refreshController() {
    if (controller != null && activePanel != null) {
      Long folderId = activePanel.getCurrentFolderId();
      SystemFolder sysFolder = activePanel.getCurrentAccount().getSystemFolder(folderId);

      if (sysFolder != null) {
        controller.refresh(sysFolder);
      } else {
        controller.refresh(folderId);
      }
    }
  }

  static void removeMailPanel(MailPanel mailPanel) {
    mailPanels.remove(mailPanel);

    if (mailPanels.size() > 0) {
      if (mailPanel == activePanel) {
        activePanel = mailPanels.iterator().next();
        rebuildController();
      }
    } else {
      activePanel = null;
      controller = null;
      BeeKeeper.getScreen().removeDomainEntry(Domain.MAIL, null);
    }
  }

  static void removeMailPanels() {
    for (MailPanel mailPanel : mailPanels) {
      BeeKeeper.getScreen().closeWidget(mailPanel.getFormView().getViewPresenter().getMainView());
    }
    mailPanels.clear();
    activePanel = null;
    controller = null;
  }

  private MailKeeper() {
  }
}
