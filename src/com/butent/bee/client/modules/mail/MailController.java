package com.butent.bee.client.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
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

import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.DndHelper;
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
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;
import java.util.Map.Entry;

public class MailController extends Flow implements HasDomain, HandlesStateChange {

  private static final String STYLE_SELECTED = "selected";
  private static final String STYLE_DND_TARGET = "dragOver";

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
      setDndTarget(label, sysFolder, null);
      panel.add(label);
      sysFolders.put(sysFolder, label);
    }
    Horizontal caption = new Horizontal();
    caption.setStyleName("bee-mail-FolderRow");

    BeeLabel label = new BeeLabel("Aplankai");
    label.setStyleName("bee-mail-FolderCaption");
    caption.add(label);

    final BeeImage refresh = new BeeImage(Global.getImages().refresh());
    refresh.addStyleName("bee-mail-FolderAction");
    refresh.setTitle("Sinchronizuoti aplankus");

    refresh.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        MailKeeper.clickFolder(MailKeeper.CHECK_ALL_FOLDERS);
      }
    });
    caption.add(refresh);
    panel.add(caption);

    final BeeImage create = new BeeImage(Global.getImages().silverPlus());
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
      final long folderId = subFolder.getId();
      int mrg = margin;

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
        setDndTarget(label, null, folderId);
        row.add(label);

        if (subFolder.isConnected()) {
          final BeeImage disconnect = new BeeImage(Global.getImages().disconnect());
          disconnect.addStyleName("bee-mail-FolderAction");
          disconnect.setTitle(BeeUtils.joinWords("Nutraukti aplanko",
              BeeUtils.bracket(label.getText()), "sinchronizaciją su pašto serveriu?"));

          disconnect.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              Global.confirmDelete(Settings.getAppName(), Icon.WARNING,
                  Lists.newArrayList(disconnect.getTitle(),
                      subFolder.isConnected()
                          ? "(Aplanko turinys bus pašalintas iš pašto serverio)" : null),
                  new ConfirmationCallback() {
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
        final BeeImage edit = new BeeImage(Global.getImages().silverEdit());
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

        final BeeImage delete = new BeeImage(Global.getImages().silverMinus());
        delete.addStyleName("bee-mail-FolderAction");
        delete.setTitle("Pašalinti aplanką " + BeeUtils.bracket(label.getText()) + "?");

        delete.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.confirmDelete(Settings.getAppName(), Icon.ALARM,
                Lists.newArrayList(delete.getTitle(),
                    subFolder.isConnected()
                        ? "(Aplanko turinys bus pašalintas ir iš pašto serverio!)" : null),
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
        mrg += 10;
      }
      buildTree(account, subFolder, mrg);
    }
  }

  private void setDndTarget(final Widget label, final SystemFolder sysFolder, final Long folderId) {
    Binder.addDragEnterHandler(label, new DragEnterHandler() {
      @Override
      public void onDragEnter(DragEnterEvent event) {
        if (BeeUtils.same(DndHelper.getDataType(), MailConstants.DATA_TYPE_MESSAGE)) {
          label.addStyleDependentName(STYLE_DND_TARGET);
        }
      }
    });
    Binder.addDragOverHandler(label, new DragOverHandler() {
      @Override
      public void onDragOver(DragOverEvent event) {
        if (DndHelper.isDataType(MailConstants.DATA_TYPE_MESSAGE)) {
          Long id = folderId;

          if (!DataUtils.isId(id)) {
            id = MailKeeper.getCurrentSystemFolderId(sysFolder);
          }
          if (EventUtils.hasModifierKey(event.getNativeEvent())) {
            EventUtils.selectDropCopy(event);
          } else if (!Objects.equal(DndHelper.getRelatedId(), id)) {
            EventUtils.selectDropMove(event);
          } else {
            EventUtils.selectDropNone(event);
          }
        } else {
          EventUtils.selectDropNone(event);
        }
      }
    });
    Binder.addDragLeaveHandler(label, new DragLeaveHandler() {
      @Override
      public void onDragLeave(DragLeaveEvent event) {
        if (DndHelper.isDataType(MailConstants.DATA_TYPE_MESSAGE)) {
          label.removeStyleDependentName(STYLE_DND_TARGET);
        }
      }
    });
    Binder.addDropHandler(label, new DropHandler() {
      @Override
      public void onDrop(DropEvent event) {
        if (DndHelper.isDataType(MailConstants.DATA_TYPE_MESSAGE)) {
          label.removeStyleDependentName(STYLE_DND_TARGET);
          Long folderTo = folderId;

          if (!DataUtils.isId(folderTo)) {
            folderTo = MailKeeper.getCurrentSystemFolderId(sysFolder);
          }
          String[] places = Codec.beeDeserializeCollection((String) DndHelper.getData());

          if (ArrayUtils.isEmpty(places)) {
            places = new String[] {BeeUtils.toString(DndHelper.getDataId())};
          }

          MailKeeper.copyMessage(DndHelper.getRelatedId(), folderTo, places,
              !EventUtils.hasModifierKey(event.getNativeEvent()));

          event.stopPropagation();
        }
      }
    });
  }
}
