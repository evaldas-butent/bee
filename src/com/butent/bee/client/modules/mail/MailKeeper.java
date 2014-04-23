package com.butent.bee.client.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.MenuHandler;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.mail.AccountInfo;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Set;

public final class MailKeeper {

  private static MailController controller;
  private static MailPanel activePanel;
  private static final Set<MailPanel> mailPanels = Sets.newHashSet();

  public static void register() {
    MenuService.RESTART_PROXY.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        BeeKeeper.getRpc().makeGetRequest(createArgs(SVC_RESTART_PROXY));
      }
    });

    MenuService.OPEN_MAIL.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        mailPanels.add(new MailPanel());
      }
    });

    FormFactory.registerFormInterceptor(FORM_ACCOUNT, new AccountEditor());
    FormFactory.registerFormInterceptor(FORM_NEW_ACCOUNT, new AccountEditor());
  }

  static void activateController(MailPanel mailPanel) {
    if (controller == null) {
      controller = new MailController();
      BeeKeeper.getScreen().addDomainEntry(Domain.MAIL, controller, null,
          Localized.getConstants().mails());
    }
    activePanel = mailPanel;
    rebuildController();
    BeeKeeper.getScreen().activateDomainEntry(Domain.MAIL, null);
  }

  static void activateMailPanel() {
    BeeKeeper.getScreen()
        .activateWidget(activePanel.getFormView().getViewPresenter().getMainView());
  }

  static void clickFolder(Long folderId) {
    activePanel.refresh(folderId);
  }

  static void copyMessage(final Long folderFrom, final Long folderTo, String[] places,
      final boolean move) {
    final MailPanel panel = activePanel;
    ParameterList params = createArgs(SVC_COPY_MESSAGES);
    params.addDataItem(COL_ACCOUNT, panel.getCurrentAccount().getAccountId());
    params.addDataItem(COL_FOLDER_PARENT, folderFrom);
    params.addDataItem(COL_FOLDER, folderTo);
    params.addDataItem(COL_PLACE, Codec.beeSerialize(places));
    params.addDataItem("move", move ? 1 : 0);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(panel.getFormView());

        if (!response.hasErrors() && Objects.equal(folderFrom, panel.getCurrentFolderId())) {
          panel.getFormView().notifyInfo(
              move ? Localized.getMessages().mailMovedMessagesToFolder(
                  (String) response.getResponse()) : Localized.getMessages()
                  .mailCopiedMessagesToFolder((String) response.getResponse()),
              BeeUtils.bracket(panel.getCurrentAccount().findFolder(folderTo).getName()));
          panel.refreshMessages();
        }
      }
    });
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(Module.MAIL.getName());
    args.addQueryItem(AdministrationConstants.METHOD, name);
    return args;
  }

  static void createFolder(String title) {
    final MailPanel panel = activePanel;
    final AccountInfo account = panel.getCurrentAccount();
    final Long parentId = panel.getCurrentFolderId();
    final boolean isParent = !account.isSystemFolder(parentId);
    String caption = null;

    if (isParent) {
      caption = Localized.getConstants().mailInFolder() + " "
          + BeeUtils.bracket(account.findFolder(parentId).getName());
    }
    Global.inputString(title, caption, new StringCallback() {
      @Override
      public void onSuccess(String value) {
        ParameterList params = createArgs(SVC_CREATE_FOLDER);
        params.addDataItem(COL_ACCOUNT, account.getAccountId());
        params.addDataItem(COL_FOLDER_NAME, value);

        if (isParent) {
          params.addDataItem(COL_FOLDER, parentId);
        }
        BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(panel.getFormView());

            if (!response.hasErrors()) {
              panel.initFolders();
            }
          }
        });
      }
    });
  }

  static void disconnectFolder(final AccountInfo account, final Long folderId) {
    final MailPanel panel = activePanel;
    ParameterList params = createArgs(SVC_DISCONNECT_FOLDER);
    params.addDataItem(COL_ACCOUNT, account.getAccountId());
    params.addDataItem(COL_FOLDER, folderId);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(panel.getFormView());

        if (!response.hasErrors()) {
          panel.initFolders();
        }
      }
    });
  }

  static void rebuildController() {
    controller.rebuild(activePanel.getCurrentAccount());
    refreshController();
  }

  static void refreshController() {
    controller.refresh(activePanel.getCurrentFolderId());
  }

  static void removeFolder(final AccountInfo account, final Long folderId) {
    final MailPanel panel = activePanel;
    ParameterList params = createArgs(SVC_DROP_FOLDER);
    params.addDataItem(COL_ACCOUNT, account.getAccountId());
    params.addDataItem(COL_FOLDER, folderId);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(panel.getFormView());

        if (!response.hasErrors()) {
          panel.initFolders(new ScheduledCommand() {
            @Override
            public void execute() {
              if (Objects.equal(folderId, panel.getCurrentFolderId())) {
                panel.refresh(account.getInboxId());
              }
            }
          });
        }
      }
    });
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

  static void renameFolder(AccountInfo account, final Long folderId, String name) {
    final MailPanel panel = activePanel;
    ParameterList params = createArgs(SVC_RENAME_FOLDER);
    params.addDataItem(COL_ACCOUNT, account.getAccountId());
    params.addDataItem(COL_FOLDER, folderId);
    params.addDataItem(COL_FOLDER_NAME, name);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(panel.getFormView());

        if (!response.hasErrors()) {
          panel.initFolders(new ScheduledCommand() {
            @Override
            public void execute() {
              panel.checkFolder(folderId);
            }
          });
        }
      }
    });
  }

  private MailKeeper() {
  }
}
