package com.butent.bee.client.modules.mail;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
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
  private final FlowPanel foldersPanel;

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

    final BeeImage create = new BeeImage(Global.getImages().add());
    create.addStyleName("bee-mail-FolderAction");
    create.setTitle("Sukurti naują aplanką");

    create.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        MailKeeper.createFolder(create.getTitle());
      }
    });
    caption.add(create);
    panel.add(caption);

    foldersPanel = new FlowPanel();
    foldersPanel.setStyleName("bee-mail-Folders");
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

    if (folders.containsKey(folderId)) {
      folders.get(folderId).addStyleDependentName(STYLE_SELECTED);
    }
  }

  private void buildTree(final AccountInfo account, MailFolder folder, int margin) {
    for (final MailFolder subFolder : folder.getSubFolders()) {
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
        if (BeeUtils.isEmpty(subFolder.getSubFolders())) {
          label.addStyleDependentName("leaf");
        } else {
          label.addStyleDependentName("branch");
        }

        Binder.addDragEnterHandler(label, new DragEnterHandler() {
          @Override
          public void onDragEnter(DragEnterEvent event) {
            label.addStyleDependentName("dragOver");
          }
        });
        Binder.addDragOverHandler(label, new DragOverHandler() {
          @Override
          public void onDragOver(DragOverEvent event) {
            event.preventDefault();

            if (event.getNativeEvent().getCtrlKey() || event.getNativeEvent().getShiftKey()
                || event.getNativeEvent().getAltKey()) {
              EventUtils.selectDropCopy(event);
            } else {
              EventUtils.selectDropMove(event);
            }
          }
        });
        Binder.addDragLeaveHandler(label, new DragLeaveHandler() {
          @Override
          public void onDragLeave(DragLeaveEvent event) {
            label.removeStyleDependentName("dragOver");
          }
        });
        Binder.addDropHandler(label, new DropHandler() {
          @Override
          public void onDrop(DropEvent event) {
            label.removeStyleDependentName("dragOver");
            event.stopPropagation();

            BeeKeeper.getScreen().notifyInfo((event.getNativeEvent().getCtrlKey()
                || event.getNativeEvent().getShiftKey()
                || event.getNativeEvent().getAltKey() ? "Copy" : "Move:")
                + EventUtils.getDndData(event));
          }
        });

        row.add(label);

        if (subFolder.isConnected()) {
          final BeeImage disconnect = new BeeImage(Global.getImages().disconnect());
          disconnect.addStyleName("bee-mail-FolderAction");
          disconnect.setTitle(BeeUtils.joinWords("Nutraukti aplanko",
              BeeUtils.bracket(label.getText()), "sinchronizaciją su pašto serveriu"));

          disconnect.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              Global.confirm(disconnect.getTitle(), new ConfirmationCallback() {
                @Override
                public void onConfirm() {
                  MailKeeper.disconnectFolder(account, folderId);
                }
              });
            }
          });
          row.add(disconnect);
        } else {
          label.addStyleDependentName("disconnected");
        }
        final BeeImage edit = new BeeImage(Global.getImages().edit());
        edit.addStyleName("bee-mail-FolderAction");
        edit.setTitle((BeeUtils.joinWords("Pakeisti aplanko", BeeUtils.bracket(label.getText()),
            "pavadinimą")));

        edit.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.inputString(edit.getTitle(), null, new StringCallback() {
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

        final BeeImage delete = new BeeImage(Global.getImages().delete());
        delete.addStyleName("bee-mail-FolderAction");
        delete.setTitle("Pašalinti aplanką " + BeeUtils.bracket(label.getText()));

        delete.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.confirm(BeeUtils.joinWords(delete.getTitle(), subFolder.isConnected()
                ? "(APLANKO TURINYS BUS PAŠALINAS IR IŠ PAŠTO SERVERIO !)" : null),
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
