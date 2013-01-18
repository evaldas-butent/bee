package com.butent.bee.client.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormViewCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.mail.MailConstants.MessageFlag;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MailPanel extends AbstractFormInterceptor {

  static class AccountInfo {
    private final long id;
    private final long address;
    private final String description;
    private final BiMap<SystemFolder, Long> sysFolders = HashBiMap.create();
    private MailFolder rootFolder;

    public AccountInfo(long id, long address, String description) {
      this.id = id;
      this.address = address;
      this.description = description;

      for (SystemFolder sysFolder : SystemFolder.values()) {
        setSystemFolderId(sysFolder, null);
      }
    }

    public long getAddress() {
      return address;
    }

    public String getDescription() {
      return description;
    }

    public MailFolder getFolder(Long folderId) {
      if (rootFolder != null) {
        return rootFolder.findFolder(folderId);
      }
      return null;
    }

    public long getId() {
      return id;
    }

    public MailFolder getRootFolder() {
      return rootFolder;
    }

    public SystemFolder getSystemFolder(Long folderId) {
      return sysFolders.inverse().get(folderId);
    }

    public Long getSystemFolderId(SystemFolder sysFolder) {
      return sysFolders.get(sysFolder);
    }

    private void setRootFolder(MailFolder folder) {
      this.rootFolder = folder;
      setSystemFolderId(SystemFolder.Inbox, folder.getId());
    }

    private void setSystemFolderId(SystemFolder sysFolder, Long folderId) {
      sysFolders.put(sysFolder, folderId == null ? sysFolder.ordinal() * (-1) : folderId);
    }
  }

  private class ContentHandler implements ActiveRowChangeEvent.Handler {
    private final int message;
    private final int sender;
    private final int senderLabel;
    private final int senderEmail;
    private final int date;
    private final int subject;
    private final int flags;

    public ContentHandler(List<BeeColumn> dataColumns) {
      message = DataUtils.getColumnIndex(COL_MESSAGE, dataColumns);
      sender = DataUtils.getColumnIndex(COL_SENDER, dataColumns);
      senderLabel = DataUtils.getColumnIndex("SenderLabel", dataColumns);
      senderEmail = DataUtils.getColumnIndex("SenderEmail", dataColumns);
      date = DataUtils.getColumnIndex(COL_DATE, dataColumns);
      subject = DataUtils.getColumnIndex(COL_SUBJECT, dataColumns);
      flags = DataUtils.getColumnIndex(COL_FLAGS, dataColumns);
    }

    @Override
    public void onActiveRowChange(ActiveRowChangeEvent event) {
      IsRow row = event.getRowValue();

      messageWidget.setVisible(false);
      emptySelectionWidget.setVisible(row == null);
      messageHandler.deactivate();
      currentMessage = row;

      if (row != null) {
        String lbl = row.getString(senderLabel);
        String mail = row.getString(senderEmail);
        senderLabelWidget.setText(BeeUtils.notEmpty(lbl, mail));
        senderEmailWidget.setText(BeeUtils.isEmpty(lbl) ? "" : mail);
        dateWidget.setValue(row.getDateTime(date));
        subjectWidget.setText(row.getString(subject));

        messageHandler.requery(row.getLong(message), getCurrentAccount().getAddress(),
            row.getId(), Objects.equal(row.getLong(sender), getCurrentAccount().getAddress()),
            MessageFlag.isSeen(row.getInteger(flags)));

        messageWidget.setVisible(true);
      }
    }
  }

  private class EnvelopeRenderer extends AbstractCellRenderer {
    private final int senderEmail;
    private final int senderLabel;
    private final int recipientEmail;
    private final int recipientLabel;
    private final int recipientCount;
    private final int dateIdx;
    private final int subjectIdx;
    private final int flagsIdx;
    private final int attachmentCount;

    public EnvelopeRenderer(List<? extends IsColumn> dataColumns) {
      super(null);

      senderEmail = DataUtils.getColumnIndex("SenderEmail", dataColumns);
      senderLabel = DataUtils.getColumnIndex("SenderLabel", dataColumns);
      recipientEmail = DataUtils.getColumnIndex("RecipientEmail", dataColumns);
      recipientLabel = DataUtils.getColumnIndex("RecipientLabel", dataColumns);
      recipientCount = DataUtils.getColumnIndex("RecipientCount", dataColumns);
      dateIdx = DataUtils.getColumnIndex(COL_DATE, dataColumns);
      subjectIdx = DataUtils.getColumnIndex(COL_SUBJECT, dataColumns);
      flagsIdx = DataUtils.getColumnIndex(COL_FLAGS, dataColumns);
      attachmentCount = DataUtils.getColumnIndex("AttachmentCount", dataColumns);
    }

    @Override
    public String render(final IsRow row) {
      Flow fp = new Flow();
      fp.setStyleName("bee-mail-Header");

      DomUtils.setDraggable(fp);

      TextLabel sender = new TextLabel(false);
      sender.setStyleName("bee-mail-HeaderAddress");
      String address;

      if (isSenderDisplayMode()) {
        address = BeeUtils.notEmpty(row.getString(recipientLabel), row.getString(recipientEmail));
        int cnt = BeeUtils.unbox(row.getInteger(recipientCount)) - 1;

        if (cnt > 0) {
          address += " (" + cnt + "+)";
        }
      } else {
        address = BeeUtils.notEmpty(row.getString(senderLabel), row.getString(senderEmail));
      }
      if (!MessageFlag.isSeen(row.getInteger(flagsIdx))) {
        fp.addStyleName("bee-mail-HeaderUnread");
        Widget image = new BeeImage(Global.getImages().greenSmall());
        image.setStyleName("bee-mail-UnreadImage");
        fp.add(image);
      }
      sender.setText(address);
      fp.add(sender);

      Integer att = row.getInteger(attachmentCount);

      if (BeeUtils.isPositive(att)) {
        Widget image = new BeeImage(Global.getImages().attachment());
        image.setStyleName("bee-mail-AttachmentImage");
        fp.add(image);

        if (att > 1) {
          TextLabel attachments = new TextLabel(false);
          attachments.setStyleName("bee-mail-AttachmentCount");
          attachments.setText(BeeUtils.toString(att));
          fp.add(attachments);
        }
      }
      DateTime date = row.getDateTime(dateIdx);
      DateTimeLabel dt = TimeUtils.isToday(date)
          ? new DateTimeLabel("TIME_SHORT", false) : new DateTimeLabel("DATE_SHORT", false);
      dt.setStyleName("bee-mail-HeaderDate");
      dt.setValue(date);
      fp.add(dt);

      TextLabel subject = new TextLabel(false);
      subject.setStyleName("bee-mail-HeaderSubject");
      subject.setText(row.getString(subjectIdx));
      fp.add(subject);

      return fp.toString();
    }
  }

  private class MailDialogCallback extends InputCallback {

    private final NewMessageHandler newMessageHandler;

    public MailDialogCallback(NewMessageHandler newMessageHandler) {
      Assert.notNull(newMessageHandler);
      this.newMessageHandler = newMessageHandler;
    }

    @Override
    public String getErrorMessage() {
      if (newMessageHandler.validate()) {
        return null;
      } else {
        return InputBoxes.SILENT_ERROR;
      }
    }

    @Override
    public void onClose(final CloseCallback closeCallback) {
      Assert.notNull(closeCallback);

      if (newMessageHandler.hasChanges()) {
        Global.getMsgBoxen().decide(null,
            Lists.newArrayList("Laiškas nebuvo išsiųstas",
                "Ar norite išsaugoti laišką juodraščiuose"),
            new DecisionCallback() {
              @Override
              public void onCancel() {
                UiHelper.focus(getFormView().asWidget());
              }

              @Override
              public void onConfirm() {
                closeCallback.onSave();
              }

              @Override
              public void onDeny() {
                closeCallback.onClose();
              }
            }, DialogConstants.DECISION_YES);
      } else {
        closeCallback.onClose();
      }
    }

    @Override
    public void onSuccess() {
      newMessageHandler.save(MailPanel.this);
    }
  }

  private class MessagesGrid extends AbstractGridInterceptor {
    @Override
    public Map<String, Filter> getInitialFilters() {
      return ImmutableMap.of(MESSAGES_FILTER,
          ComparisonFilter.isEqual(COL_FOLDER, new LongValue(getCurrentFolderId())));
    }

    @Override
    public AbstractCellRenderer getRenderer(String columnName,
        List<? extends IsColumn> dataColumns, ColumnDescription columnDescription) {

      if (BeeUtils.same(columnName, "Star")) {
        return new StarRenderer(dataColumns);
      } else if (BeeUtils.same(columnName, "Envelope")) {
        return new EnvelopeRenderer(dataColumns);
      }
      return null;
    }

    @Override
    public void afterCreate(final GridView gridView) {
      Binder.addDragStartHandler(gridView.getGrid(), new DragStartHandler() {
        @Override
        public void onDragStart(DragStartEvent event) {
          EventTarget target = event.getNativeEvent().getEventTarget();

          if (!Element.is(target)) {
            return;
          }
          Integer rowIdx = BeeUtils.toIntOrNull(DomUtils
              .getDataRow(Element.as(target).getParentElement()));

          if (rowIdx == null || !BeeUtils.isIndex(gridView.getGrid().getRowData(), rowIdx)) {
            return;
          }
          EventUtils.allowCopyMove(event);
          EventUtils.setDndData(event, gridView.getGrid().getRowData().get(rowIdx).getId());
        }
      });
    }

    @Override
    public void onEditStart(final EditStartEvent event) {
      if ("Star".equals(event.getColumnId())) {
        final IsRow row = event.getRowValue();

        if (row == null) {
          return;
        }
        final String viewName = getGridPresenter().getViewName();
        boolean toggle = !MessageFlag.isFlagged(Data.getInteger(viewName, row, COL_FLAGS));

        ParameterList params = MailKeeper.createArgs(SVC_FLAG_MESSAGE);
        params.addDataItem(COL_ADDRESS, getCurrentAccount().getAddress());
        params.addDataItem(COL_PLACE, row.getId());
        params.addDataItem("toggle", toggle ? 1 : 0);

        BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(getFormView());

            if (!response.hasErrors()) {
              Integer flags = BeeUtils.toIntOrNull((String) response.getResponse());
              Data.setValue(viewName, row, COL_FLAGS, flags);
              event.getSourceElement()
                  .setInnerHTML(StarRenderer.render(MessageFlag.isFlagged(flags)));
            }
          }
        });
      }
    }

    @Override
    public void onShow(GridPresenter presenter) {
      presenter.getGridView().getGrid()
          .addActiveRowChangeHandler(new ContentHandler(presenter.getDataColumns()));
    }
  }

  private static class StarRenderer extends AbstractCellRenderer {

    public static String render(boolean flagged) {
      return flagged ? Stars.getHtml(0) : Stars.getDefaultHeader();
    }

    private final int flags;

    public StarRenderer(List<? extends IsColumn> dataColumns) {
      super(null);
      flags = DataUtils.getColumnIndex(COL_FLAGS, dataColumns);
    }

    @Override
    public String render(IsRow row) {
      return render(MessageFlag.isFlagged(row.getInteger(flags)));
    }
  }

  private static final String MESSAGES_FILTER = "MessagesFilter";

  private static enum NewMailMode {
    CREATE, REPLY, REPLYALL, FORWARD
  }

  private int currentAccount = -1;
  private Long currentFolder = null;

  private IsRow currentMessage = null;

  private final MessagesGrid messagesHandler = new MessagesGrid();
  private final MessageHandler messageHandler = new MessageHandler();

  private final List<AccountInfo> accounts = Lists.newArrayList();

  private Panel messageWidget;
  private Label emptySelectionWidget;

  private Label senderLabelWidget;
  private Label senderEmailWidget;
  private DateTimeLabel dateWidget;
  private Label subjectWidget;

  public MailPanel() {
    ParameterList params = MailKeeper.createArgs(SVC_GET_ACCOUNTS);
    params.addDataItem(COL_USER, BeeKeeper.getUser().getUserId());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.isTrue(response.hasResponse(SimpleRowSet.class));
        SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());

        for (int i = 0; i < rs.getNumberOfRows(); i++) {
          SimpleRow row = rs.getRow(i);

          if (currentAccount < 0 || BeeUtils.isTrue(row.getBoolean(COL_ACCOUNT_DEFAULT))) {
            currentAccount = i;
          }
          AccountInfo account = new AccountInfo(row.getLong(COL_ACCOUNT),
              row.getLong(COL_ADDRESS), row.getValue(COL_ACCOUNT_DESCRIPTION));

          for (SystemFolder sysFolder : SystemFolder.values()) {
            account.setSystemFolderId(sysFolder, row.getLong(sysFolder.name() + "Folder"));
          }
          accounts.add(account);
        }
        if (!BeeUtils.isEmpty(accounts)) {
          FormFactory.openForm("Mail", MailPanel.this);
        } else {
          BeeKeeper.getScreen().notifyWarning("No accounts found");
        }
      }
    });
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof BeeListBox && BeeUtils.same(name, "Accounts")) {
      initAccounts((ListBox) widget);

    } else if (widget instanceof GridPanel && BeeUtils.same(name, "Messages")) {
      ((GridPanel) widget).setGridInterceptor(messagesHandler);

    } else if (widget instanceof Panel && BeeUtils.same(name, "Message")) {
      messageWidget = ((Panel) widget);

    } else if (widget instanceof Label) {
      Label lbl = (Label) widget;

      if (BeeUtils.same(name, "EmptySelection")) {
        emptySelectionWidget = lbl;

      } else if (BeeUtils.same(name, "SenderLabel")) {
        senderLabelWidget = lbl;

      } else if (BeeUtils.same(name, "SenderEmail")) {
        senderEmailWidget = lbl;

      } else if (widget instanceof DateTimeLabel && BeeUtils.same(name, "MessageDate")) {
        dateWidget = (DateTimeLabel) lbl;

      } else if (BeeUtils.same(name, "MessageSubject")) {
        subjectWidget = lbl;

      } else {
        messageHandler.afterCreateWidget(name, widget, callback);
      }
    } else if (widget instanceof HasClickHandlers) {
      NewMailMode mode = NameUtils.getEnumByName(NewMailMode.class, name);

      if (mode != null) {
        initCreateAction((HasClickHandlers) widget, mode);

      } else if (BeeUtils.same(name, "delete")) {
        initTrashAction((HasClickHandlers) widget);

      } else {
        messageHandler.afterCreateWidget(name, widget, callback);
      }
    } else {
      messageHandler.afterCreateWidget(name, widget, callback);
    }
  }

  @Override
  public Domain getDomain() {
    return Domain.MAIL;
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }

  @Override
  public void onShow(FormPresenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    initFolders(false, new ScheduledCommand() {
      @Override
      public void execute() {
        refresh(getCurrentAccount().getSystemFolderId(SystemFolder.Inbox));
      }
    });
    messageHandler.setFormView(getFormView());
  }

  @Override
  public void onStateChange(State state) {
    if (State.ACTIVATED.equals(state)) {
      MailKeeper.activateController(this);

    } else if (State.REMOVED.equals(state)) {
      MailKeeper.removeMailPanel(this);
    }
    LogUtils.getRootLogger().debug("MailPanel", state);
  }

  AccountInfo getCurrentAccount() {
    if (BeeUtils.isIndex(accounts, currentAccount)) {
      return accounts.get(currentAccount);
    }
    return null;
  }

  Long getCurrentFolderId() {
    return currentFolder;
  }

  void initFolders(boolean forced, final ScheduledCommand update) {
    final AccountInfo account = getCurrentAccount();
    if (account == null) {
      return;
    }
    if (account.getRootFolder() == null || forced) {
      ParameterList params = MailKeeper.createArgs(SVC_GET_FOLDERS);
      params.addDataItem(COL_ACCOUNT, account.getId());

      if (!DataUtils.isId(account.getSystemFolderId(SystemFolder.Inbox))) {
        params.addDataItem(COL_ADDRESS, account.getAddress());
      }
      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(getFormView());

          if (response.hasResponse(MailFolder.class)) {
            account.setRootFolder(MailFolder.restore((String) response.getResponse()));
          } else {
            String[] data = Codec.beeDeserializeCollection((String) response.getResponse());

            if (!ArrayUtils.isEmpty(data)) {
              Map<String, String> info = Maps.newHashMapWithExpectedSize(data.length / 2);

              for (int i = 0; i < data.length; i += 2) {
                info.put(data[i], data[i + 1]);
              }
              for (SystemFolder sysFolder : SystemFolder.values()) {
                String s = info.get(sysFolder.name());

                if (sysFolder == SystemFolder.Inbox) {
                  account.setRootFolder(MailFolder.restore(s));
                } else {
                  account.setSystemFolderId(sysFolder, BeeUtils.toLongOrNull(s));
                }
              }
            }
          }
          if (account == getCurrentAccount()) {
            MailKeeper.rebuildController();

            if (update != null) {
              update.execute();
            }
          }
        }
      });
    } else {
      MailKeeper.rebuildController();

      if (update != null) {
        update.execute();
      }
    }
  }

  void refresh(final Long folderId) {
    if (folderId != null) {
      final AccountInfo account = getCurrentAccount();
      if (account == null) {
        return;
      }
      if (Objects.equal(folderId, getCurrentFolderId())) {
        MailFolder folder = account.getFolder(folderId);

        if (folder != null && !folder.isConnected()) {
          return;
        }
        ParameterList params = MailKeeper.createArgs(SVC_CHECK_MAIL);
        params.addDataItem(COL_ADDRESS, account.getAddress());
        params.addDataItem(COL_FOLDER, folderId);

        BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(getFormView());

            if (!response.hasErrors()) {
              initFolders(true, new ScheduledCommand() {
                @Override
                public void execute() {
                  if (Objects.equal(folderId, getCurrentFolderId())) {
                    refresh(null);
                  }
                }
              });
              int msgCnt = BeeUtils.toInt((String) response.getResponse());

              if (msgCnt > 0) {
                getFormView().notifyInfo(BeeUtils.joinWords("Naujų žinučių:", msgCnt));
              }
            }
          }
        });
        return;

      } else {
        currentFolder = folderId;
        MailKeeper.refreshController();
      }
    }
    GridPresenter presenter = messagesHandler.getGridPresenter();

    if (presenter != null) {
      presenter.getGridView().getGrid().reset();
      presenter.getDataProvider().setParentFilter(MESSAGES_FILTER,
          ComparisonFilter.isEqual(COL_FOLDER, new LongValue(getCurrentFolderId())));
      presenter.refresh(false);
    }
  }

  private void initAccounts(final ListBox accountsWidget) {
    accountsWidget.clear();

    for (AccountInfo account : accounts) {
      accountsWidget.addItem(account.getDescription());
    }
    accountsWidget.setEnabled(accountsWidget.getItemCount() > 1);
    accountsWidget.setSelectedIndex(currentAccount);
    accountsWidget.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int selectedAccount = accountsWidget.getSelectedIndex();

        if (selectedAccount != currentAccount) {
          currentAccount = selectedAccount;

          initFolders(false, new ScheduledCommand() {
            @Override
            public void execute() {
              currentFolder = null;
              refresh(getCurrentAccount().getSystemFolderId(SystemFolder.Inbox));
            }
          });
        }
      }
    });
  }

  private void initCreateAction(HasClickHandlers widget, final NewMailMode mode) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent ev) {
        if (getCurrentAccount() == null || mode != NewMailMode.CREATE && currentMessage == null) {
          return;
        }
        Long draftId = null;
        Long sender = getCurrentAccount().getAddress();
        Set<Long> to = null;
        Set<Long> cc = null;
        Set<Long> bcc = null;
        String subject = null;
        String content = null;
        Map<Long, NewFileInfo> attachments = null;

        switch (mode) {
          case CREATE:
            if (getCurrentAccount().getSystemFolder(getCurrentFolderId()) == SystemFolder.Drafts
                && currentMessage != null) {
              draftId = currentMessage.getId();
              subject = subjectWidget.getText();
              to = messageHandler.getTo();
              cc = messageHandler.getCc();
              bcc = messageHandler.getBcc();
              content = messageHandler.getContent();
              attachments = messageHandler.getAttachments();
            }
            break;

          case REPLY:
          case REPLYALL:
            if (mode == NewMailMode.REPLYALL) {
              cc = messageHandler.getTo();
              cc.addAll(messageHandler.getCc());
              bcc = messageHandler.getBcc();
            }
            Element bq = Document.get().createBlockQuoteElement();
            bq.setAttribute("style",
                "border-left: 1px solid #039; margin: 0; padding: 10px; color: #039;");
            bq.setInnerHTML(messageHandler.getContent());
            content = BeeUtils.join("<br>", "<br>",
                dateWidget.getValue().toString() + ", "
                    + SafeHtmlUtils.htmlEscape((BeeUtils.isEmpty(senderEmailWidget.getText())
                        ? "<" + senderLabelWidget.getText() + ">"
                        : senderLabelWidget.getText() + " <" + senderEmailWidget.getText() + ">"))
                    + " rašė:",
                bq.getString());
            to = Sets.newHashSet(Data.getLong(messagesHandler.getGridPresenter().getViewName(),
                currentMessage, COL_SENDER));
            subject = subjectWidget.getText();

            if (!BeeUtils.isPrefix(subject, "Re:")) {
              subject = BeeUtils.joinWords("Re:", subject);
            }
            break;

          case FORWARD:
            content = BeeUtils.join("<br>", "<br>", "---------- Persiųstas laiškas ----------",
                "Nuo: " + SafeHtmlUtils.htmlEscape((BeeUtils.isEmpty(senderEmailWidget.getText())
                    ? "<" + senderLabelWidget.getText() + ">"
                    : senderLabelWidget.getText() + " <" + senderEmailWidget.getText() + ">")),
                "Data: " + dateWidget.getValue().toString(),
                "Tema: " + SafeHtmlUtils.htmlEscape(subjectWidget.getText()),
                "Kam: " + SafeHtmlUtils.htmlEscape(messageHandler.getRecipients()),
                "<br>" + messageHandler.getContent());

            attachments = messageHandler.getAttachments();
            subject = subjectWidget.getText();

            if (!BeeUtils.isPrefix(subject, "Fwd:")) {
              subject = BeeUtils.joinWords("Fwd:", subject);
            }
            break;
        }
        final NewMessageHandler newMessageHandler =
            new NewMessageHandler(sender, draftId, to, cc, bcc, subject, content, attachments);

        FormFactory.createFormView("NewMessage", null, null, false, newMessageHandler,
            new FormViewCallback() {
              @Override
              public void onSuccess(FormDescription formDescription, FormView result) {
                if (result != null) {
                  result.start(null);

                  Global.inputWidget(result.getCaption(), result,
                      new MailDialogCallback(newMessageHandler),
                      true, RowFactory.DIALOG_STYLE, false);
                }
              }
            });
      }
    });
  }

  private void initTrashAction(HasClickHandlers widget) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent ev) {
        if (currentMessage == null) {
          return;
        }
        List<String> options = Lists.newArrayList("Aktyvų laišką");
        final Collection<RowInfo> rows = messagesHandler.getGridPresenter().getGridView()
            .getSelectedRows();

        if (!BeeUtils.isEmpty(rows)) {
          options.add(BeeUtils.joinWords("Pažymėtus", rows.size(), "laiškus"));
        }
        final AccountInfo account = getCurrentAccount();
        final Long folderId = getCurrentFolderId();
        final boolean purge = (account.getSystemFolder(folderId) == SystemFolder.Trash);

        Global.choice(purge ? "Pašalinti" : "Perkelti į šiukšlinę", null, options,
            new ChoiceCallback() {
              @Override
              public void onSuccess(int value) {
                List<Long> ids = null;

                if (value == 0) {
                  ids = Lists.newArrayList(currentMessage.getId());
                } else if (value == 1) {
                  ids = Lists.newArrayList();

                  for (RowInfo info : rows) {
                    ids.add(info.getId());
                  }
                }
                ParameterList params = MailKeeper.createArgs(SVC_REMOVE_MESSAGES);
                params.addDataItem(COL_ADDRESS, getCurrentAccount().getAddress());
                params.addDataItem(COL_FOLDER, getCurrentFolderId());
                params.addDataItem(COL_PLACE, Codec.beeSerialize(ids));
                params.addDataItem("Purge", purge ? 1 : 0);

                BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    response.notify(getFormView());

                    if (!response.hasErrors() && Objects.equal(folderId, getCurrentFolderId())) {
                      refresh(null);
                    }
                  }
                });
              }
            }, options.size(), BeeConst.UNDEF, DialogConstants.CANCEL, new WidgetInitializer() {
              @Override
              public Widget initialize(Widget w, String nm) {
                if (BeeUtils.same(nm, DialogConstants.WIDGET_DIALOG)) {
                  w.addStyleName(purge ? StyleUtils.NAME_SUPER_SCARY : StyleUtils.NAME_SCARY);
                }
                return w;
              }
            });
      }
    });
  }

  private boolean isSenderDisplayMode() {
    SystemFolder mode = getCurrentAccount().getSystemFolder(getCurrentFolderId());
    return mode == SystemFolder.Sent || mode == SystemFolder.Drafts;
  }
}
