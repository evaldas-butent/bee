package com.butent.bee.client.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.modules.mail.MailPanel.AccountInfo;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.HandlesStateChange;
import com.butent.bee.client.screen.HasDomain;
import com.butent.bee.client.tree.HasTreeItems;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.State;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;

import java.util.Map;
import java.util.Map.Entry;

public class MailController extends Flow implements HasDomain, HandlesStateChange {

  private final Map<SystemFolder, Widget> sysFolders = Maps.newHashMap();
  private final Tree folders;

  MailController() {
    super();

    Vertical panel = new Vertical();
    panel.setStyleName("bee-mail-DisplayMode-panel");
    add(panel);

    for (final SystemFolder sysFolder : SystemFolder.values()) {
      BeeButton button = new BeeButton(sysFolder.name(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          MailKeeper.clickSystemFolder(sysFolder);
        }
      });
      button.addStyleName("bee-mail-DisplayMode-item");
      panel.add(button);
      sysFolders.put(sysFolder, button);
    }
    folders = new Tree("Aplankai");
    folders.addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem item = event.getSelectedItem();

        if (item != null) {
          Object obj = item.getUserObject();

          if (obj != null && obj instanceof MailFolder) {
            MailKeeper.clickFolder(((MailFolder) obj).getId());
          }
        }
      }
    });
    add(folders);
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
    folders.removeItems();

    if (account != null) {
      MailFolder folder = account.getRootFolder();

      if (folder != null) {
        buildTree(account, folder, folders);
      }
    }
  }

  void refresh(SystemFolder sysFolder) {
    folders.setSelectedItem(null, false);

    String selected = "bee-mail-DisplayMode-item-selected";

    for (Entry<SystemFolder, Widget> folder : sysFolders.entrySet()) {
      if (folder.getKey() == sysFolder) {
        folder.getValue().addStyleName(selected);
      } else {
        folder.getValue().removeStyleName(selected);
      }
    }
  }

  void refresh(Long folderId) {
    refresh((SystemFolder) null);
    folders.setSelectedItem(findItem(folders, folderId), false);
  }

  private void buildTree(AccountInfo account, MailFolder folder, HasTreeItems parent) {
    for (MailFolder subFolder : folder.getSubFolders()) {
      if (account.getSystemFolder(subFolder.getId()) == null) {
        TreeItem item = new TreeItem(subFolder.getName(), subFolder);
        parent.addItem(item);
        buildTree(account, subFolder, item);
      }
    }
  }

  private TreeItem findItem(HasTreeItems parent, Long folderId) {
    TreeItem item = null;

    if (parent.getItemCount() > 0) {
      for (TreeItem branch : parent.getTreeItems()) {
        Object obj = branch.getUserObject();

        if (obj != null && obj instanceof MailFolder
            && Objects.equal(((MailFolder) obj).getId(), folderId)) {
          item = branch;
        } else {
          item = findItem(branch, folderId);
        }
        if (item != null) {
          break;
        }
      }
    }
    return item;
  }
}
