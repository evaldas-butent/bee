package com.butent.bee.client.modules.mail;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.mail.MailPanel.AccountInfo;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.HandlesStateChange;
import com.butent.bee.client.screen.HasDomain;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.State;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;
import java.util.Map.Entry;

public class MailController extends Flow implements HasDomain, HandlesStateChange {

  private static final String STYLE_SELECTED = "selected";

  private final Map<SystemFolder, Widget> sysFolders = Maps.newHashMap();
  private final Map<Long, Widget> folders = Maps.newHashMap();
  private final FlowPanel foldersPanel = new FlowPanel();

  MailController() {
    super();

    FlowPanel panel = new FlowPanel();
    panel.setStyleName("bee-mail-Controller");
    add(panel);

    for (final SystemFolder sysFolder : SystemFolder.values()) {
      BeeLabel label = null;

      switch (sysFolder) {
        case Drafts:
          label = new BeeLabel("Juodraščiai");
          break;
        case Inbox:
          label = new BeeLabel("Gautieji");
          break;
        case Sent:
          label = new BeeLabel("Siųstieji");
          break;
        case Trash:
          label = new BeeLabel("Šiukšlinė");
          break;
      }
      label.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          MailKeeper.clickSystemFolder(sysFolder);
        }
      });
      label.setStyleName("bee-mail-SysFolder");
      panel.add(label);
      sysFolders.put(sysFolder, label);
    }
    Horizontal caption = new Horizontal();
    caption.setStyleName("bee-mail-FolderRow");

    BeeLabel label = new BeeLabel("Aplankai");
    label.setStyleName("bee-mail-FolderCaption");
    caption.add(label);

    BeeImage create = new BeeImage(Global.getImages().add());
    create.addStyleName("bee-mail-FolderAdd");
    create.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Global.inputString("Sukurti aplanką", null, new StringCallback() {
          @Override
          public void onSuccess(String value) {
            MailKeeper.createFolder(value);
          }
        });
      }
    });
    caption.add(create);

    panel.add(caption);
    panel.add(foldersPanel);
  }

  @Override
  public Domain getDomain() {
    return Domain.MAIL;
  }

  @Override
  public void onStateChange(State state) {
    if (State.ACTIVATED.equals(state)) {
      MailKeeper.activateMailPanel();
    } else if (State.REMOVED.equals(state)) {
      MailKeeper.removeMailPanels();
    }
    LogUtils.getRootLogger().debug("MailController", state);
  }

  void rebuild(AccountInfo account) {
    foldersPanel.clear();

    if (account != null) {
      MailFolder folder = account.getRootFolder();

      if (folder != null) {
        buildTree(account, folder, 0);
      }
    }
  }

  void refresh(SystemFolder sysFolder) {
    for (Widget widget : folders.values()) {
      widget.removeStyleDependentName(STYLE_SELECTED);
    }
    for (Entry<SystemFolder, Widget> folder : sysFolders.entrySet()) {
      folder.getValue().setStyleDependentName(STYLE_SELECTED, folder.getKey() == sysFolder);
    }
  }

  void refresh(Long folderId) {
    refresh((SystemFolder) null);
    folders.get(folderId).addStyleDependentName(STYLE_SELECTED);
  }

  private void buildTree(final AccountInfo account, MailFolder folder, int margin) {
    for (MailFolder subFolder : folder.getSubFolders()) {
      final Long folderId = subFolder.getId();

      if (account.getSystemFolder(folderId) == null) {
        Horizontal row = new Horizontal();
        row.setStyleName("bee-mail-FolderRow");
        row.setDefaultCellStyles("padding: 0px;");

        final BeeLabel label = new BeeLabel(subFolder.getName());
        label.setStyleName("bee-mail-Folder");
        label.getElement().getStyle().setMarginLeft(margin, Unit.PX);
        label.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            MailKeeper.clickFolder(folderId);
          }
        });
        row.add(label);

        BeeImage edit = new BeeImage(Global.getImages().edit());
        edit.addStyleName("bee-mail-FolderEdit");
        edit.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.inputString("Pakeisti aplanko pavadinimą", null, new StringCallback() {
              @Override
              public void onSuccess(String value) {
                if (!label.getText().equals(value)) {
                  MailKeeper.renameFolder(account, folderId, value);
                }
              }
            }, label.getText());
          }
        });
        row.add(edit);

        BeeImage delete = new BeeImage(Global.getImages().delete());
        delete.addStyleName("bee-mail-FolderDelete");
        delete.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.confirm(BeeUtils.joinWords("Pašalinti aplanką", label.getText()),
                new ConfirmationCallback() {
                  @Override
                  public void onConfirm() {
                    MailKeeper.removeFolder(account, folderId);
                  }
                });
          }
        });
        row.add(delete);

        foldersPanel.add(row);
        folders.put(folderId, label);
        buildTree(account, subFolder, margin + 10);
      }
    }
  }
}
