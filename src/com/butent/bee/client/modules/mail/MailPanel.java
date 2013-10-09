package com.butent.bee.client.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.crm.RequestBuilder;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.mail.MailConstants.MessageFlag;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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

    public long getSystemFolderId(SystemFolder sysFolder) {
      return sysFolders.get(sysFolder);
    }

    private void setRootFolder(MailFolder folder) {
      this.rootFolder = folder;
    }

    private void setSystemFolderId(SystemFolder sysFolder, Long folderId) {
      sysFolders.put(sysFolder, folderId == null ? sysFolder.ordinal() * (-1) : folderId);
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
      return render(MessageFlag.FLAGGED.isSet(row.getInteger(flags)));
    }
  }

  private class ContentHandler implements ActiveRowChangeEvent.Handler {
    private final int messageId;
    private final int sender;
    private final int flags;

    public ContentHandler(List<BeeColumn> dataColumns) {
      messageId = DataUtils.getColumnIndex(COL_MESSAGE, dataColumns);
      sender = DataUtils.getColumnIndex(COL_SENDER, dataColumns);
      flags = DataUtils.getColumnIndex(COL_FLAGS, dataColumns);
    }

    @Override
    public void onActiveRowChange(ActiveRowChangeEvent event) {
      currentMessage = event.getRowValue();
      showMessage(currentMessage != null);

      if (currentMessage != null) {
        message.requery(currentMessage.getLong(messageId),
            Objects.equal(currentMessage.getLong(sender), getCurrentAccount().getAddress()));

        if (!MessageFlag.SEEN.isSet(currentMessage.getInteger(flags))) {
          flagMessage(getCurrentAccount().getId(), currentMessage, flags, MessageFlag.SEEN, null);
        }
      }
    }

    private void showMessage(boolean show) {
      if (!show) {
        message.reset();
      }
      messageWidget.setVisible(show);
      emptySelectionWidget.setVisible(!show);
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

      if (!MessageFlag.SEEN.isSet(row.getInteger(flagsIdx))) {
        fp.addStyleName("bee-mail-HeaderUnread");
      }
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
      sender.setHtml(address);
      fp.add(sender);

      Integer att = row.getInteger(attachmentCount);

      if (BeeUtils.isPositive(att)) {
        Widget image = new Image(Global.getImages().attachment());
        image.setStyleName("bee-mail-AttachmentImage");
        fp.add(image);

        if (att > 1) {
          TextLabel attachments = new TextLabel(false);
          attachments.setStyleName("bee-mail-AttachmentCount");
          attachments.setHtml(BeeUtils.toString(att));
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
      subject.setHtml(row.getString(subjectIdx));
      fp.add(subject);

      return fp.toString();
    }
  }

  private class MessagesGrid extends AbstractGridInterceptor {
    private final Horizontal dummy = new Horizontal();

    @Override
    public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
      return Data.createRowSet(gridDescription.getViewName());
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
          Long placeId = gridView.getGrid().getRowData().get(rowIdx).getId();

          EventUtils.allowCopyMove(event);
          EventUtils.setDndData(event, placeId);

          Collection<RowInfo> rows =
              getGridPresenter().getGridView().getSelectedRows(SelectedRows.ALL);
          Set<Long> ids = Sets.newHashSet();

          if (!BeeUtils.isEmpty(rows)) {
            for (RowInfo info : rows) {
              ids.add(info.getId());
            }
          }
          Label dragLabel = new Label();

          if (ids.contains(placeId)) {
            int cnt = ids.size();
            String ending = (cnt % 10 == 0 || BeeUtils.betweenInclusive(cnt % 100, 11, 19))
                ? "ų" : (cnt % 10 == 1 ? "as" : "ai");
            dragLabel = new Label(BeeUtils.joinWords(cnt, "pažymėt" + ending, "laišk" + ending));
          } else {
            dragLabel = new Label("1 laiškas");
            ids.clear();
          }
          dummy.add(dragLabel);
          RootPanel.get().add(dummy);
          dragLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
          dragLabel.getElement().getStyle().setBackgroundColor("whiteSmoke");

          event.getDataTransfer().setDragImage(dragLabel.getElement(), 0,
              dummy.getElement().getOffsetHeight());

          DndHelper.fillContent(DATA_TYPE_MESSAGE, placeId, getCurrentFolderId(),
              Codec.beeSerialize(ids));
        }
      });

      Binder.addDragEndHandler(gridView.getGrid(), new DragEndHandler() {
        @Override
        public void onDragEnd(DragEndEvent event) {
          dummy.clear();
          RootPanel.get().remove(dummy);
          DndHelper.reset();
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
        final int flagIdx = Data.getColumnIndex(getGridPresenter().getViewName(), COL_FLAGS);
        event.getSourceElement()
            .setInnerHTML(StarRenderer.render(!MessageFlag.FLAGGED.isSet(row.getInteger(flagIdx))));

        flagMessage(getCurrentAccount().getId(), row, flagIdx, MessageFlag.FLAGGED,
            new ScheduledCommand() {
              @Override
              public void execute() {
                event.getSourceElement().setInnerHTML(StarRenderer
                    .render(MessageFlag.FLAGGED.isSet(row.getInteger(flagIdx))));
              }
            });
      }
    }

    @Override
    public void onShow(GridPresenter presenter) {
      presenter.getGridView().getGrid()
          .addActiveRowChangeHandler(new ContentHandler(presenter.getDataColumns()));

      initFolders(false, new ScheduledCommand() {
        @Override
        public void execute() {
          refresh(getCurrentAccount().getSystemFolderId(SystemFolder.Inbox));
        }
      });
    }
  }

  private static final String MESSAGES_FILTER = "MessagesFilter";

  private int currentAccount = -1;
  private Long currentFolder;

  private IsRow currentMessage;

  private final MessagesGrid messages = new MessagesGrid();
  private final MailMessage message = new MailMessage();

  private final List<AccountInfo> accounts = Lists.newArrayList();

  private Widget messageWidget;
  private Widget emptySelectionWidget;

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
            account.setSystemFolderId(sysFolder, row.getLong(sysFolder.name() + COL_FOLDER));
          }
          accounts.add(account);
        }
        if (!BeeUtils.isEmpty(accounts)) {
          message.setAccountInfo(getCurrentAccount().getAddress(), accounts);
          FormFactory.openForm("Mail", MailPanel.this);
        } else {
          BeeKeeper.getScreen().notifyWarning("No accounts found");
          MailKeeper.removeMailPanel(MailPanel.this);
        }
      }
    });
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof GridPanel && BeeUtils.same(name, "Messages")) {
      ((GridPanel) widget).setGridInterceptor(messages);

    } else if (BeeUtils.same(name, "Message")) {
      messageWidget = widget.asWidget();

    } else if (BeeUtils.same(name, "EmptySelection")) {
      emptySelectionWidget = widget.asWidget();
    }
    message.afterCreateWidget(name, widget, callback);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case ADD:
        createMessage();
        break;

      case DELETE:
        removeMessages();
        break;

      default:
        return true;
    }
    return false;
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
  public void onShow(Presenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    BeeListBox accountsWidget = new BeeListBox();
    initAccounts(accountsWidget);
    header.addCommandItem(accountsWidget);

    header.addCommandItem(new Button(Localized.getConstants().crmRequest(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        createRequest(currentMessage != null
            ? currentMessage.getLong(DataUtils.getColumnIndex(COL_MESSAGE,
                messages.getGridPresenter().getDataColumns())) : null);
      }
    }));

    message.setFormView(getFormView());
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

      for (SystemFolder sysFolder : SystemFolder.values()) {
        if (!DataUtils.isId(account.getSystemFolderId(sysFolder))) {
          params.addDataItem("refreshAccount", 1);
          break;
        }
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
              account.setRootFolder(MailFolder.restore(info.get(COL_FOLDER)));

              for (SystemFolder sysFolder : SystemFolder.values()) {
                account.setSystemFolderId(sysFolder,
                    BeeUtils.toLongOrNull(info.get(sysFolder.name())));
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
      if (Objects.equal(folderId, getCurrentFolderId())
          || Objects.equal(folderId, MailKeeper.CHECK_ALL_FOLDERS)) {

        ParameterList params = MailKeeper.createArgs(SVC_CHECK_MAIL);
        params.addDataItem(COL_ACCOUNT, getCurrentAccount().getId());

        if (!Objects.equal(folderId, MailKeeper.CHECK_ALL_FOLDERS)) {
          params.addDataItem(COL_FOLDER, folderId);
        }
        BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(getFormView());

            if (!response.hasErrors()) {
              initFolders(true, new ScheduledCommand() {
                @Override
                public void execute() {
                  if (Objects.equal(folderId, getCurrentFolderId()) || folderId == 0) {
                    refresh(null);
                  }
                }
              });
            }
          }
        });
        return;

      } else {
        currentFolder = folderId;
        MailKeeper.refreshController();
      }
    }
    GridPresenter presenter = messages.getGridPresenter();

    if (presenter != null) {
      presenter.getGridView().getGrid().reset();
      presenter.getDataProvider().setParentFilter(MESSAGES_FILTER,
          ComparisonFilter.isEqual(COL_FOLDER, new LongValue(getCurrentFolderId())));
      presenter.refresh(false);
    }
  }

  private void createMessage() {
    if (getCurrentAccount() == null) {
      return;
    }
    Long draftId = null;
    Set<Long> to = null;
    Set<Long> cc = null;
    Set<Long> bcc = null;
    String subject = null;
    String content = null;
    Map<Long, NewFileInfo> attachments = null;

    if (getCurrentAccount().getSystemFolder(getCurrentFolderId()) == SystemFolder.Drafts
        && currentMessage != null) {
      draftId = currentMessage.getId();
      to = message.getTo();
      cc = message.getCc();
      bcc = message.getBcc();
      subject = message.getSubject();
      content = message.getContent();
      attachments = message.getAttachments();
    }
    NewMailMessage newMessage = NewMailMessage.create(getCurrentAccount().getAddress(),
        DataUtils.isId(draftId) ? Lists.newArrayList(getCurrentAccount()) : accounts,
        to, cc, bcc, subject, content, attachments, draftId);

    newMessage.setScheduled(new ScheduledCommand() {
      @Override
      public void execute() {
        if (getCurrentAccount().getSystemFolder(getCurrentFolderId()) == SystemFolder.Drafts) {
          refresh(null);
        }
      }
    });
  }

  private void createRequest(Long messageId) {
    if (!DataUtils.isId(messageId)) {
      return;
    }
    ParameterList params = MailKeeper.createArgs(SVC_GET_USABLE_CONTENT);
    params.addDataItem(COL_MESSAGE, messageId);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getFormView());

        if (response.hasErrors()) {
          return;
        }
        String[] data = Codec.beeDeserializeCollection((String) response.getResponse());
        Map<String, String> packet = Maps.newHashMapWithExpectedSize(data.length / 2);

        for (int i = 0; i < data.length; i += 2) {
          packet.put(data[i], data[i + 1]);
        }
        DataInfo dataInfo = Data.getDataInfo(CrmConstants.TBL_REQUESTS);
        BeeRow row = RowFactory.createEmptyRow(dataInfo, true);

        row.setValue(dataInfo.getColumnIndex("Customer"),
            BeeUtils.toLongOrNull(packet.get(CommonsConstants.COL_COMPANY)));
        row.setValue(dataInfo.getColumnIndex("CustomerName"),
            packet.get(CommonsConstants.COL_COMPANY + CommonsConstants.COL_NAME));
        row.setValue(dataInfo.getColumnIndex("CustomerPerson"),
            BeeUtils.toLongOrNull(packet.get(CommonsConstants.COL_PERSON)));
        row.setValue(dataInfo.getColumnIndex("PersonFirstName"),
            packet.get(CommonsConstants.COL_FIRST_NAME));
        row.setValue(dataInfo.getColumnIndex("PersonLastName"),
            packet.get(CommonsConstants.COL_LAST_NAME));
        row.setValue(dataInfo.getColumnIndex(COL_CONTENT), packet.get(COL_CONTENT));

        Map<Long, NewFileInfo> files = Maps.newHashMap();
        SimpleRowSet rs = SimpleRowSet.restore(packet.get(TBL_ATTACHMENTS));

        for (SimpleRow attach : rs) {
          files.put(attach.getLong(COL_FILE),
              new NewFileInfo(BeeUtils.notEmpty(attach.getValue(COL_ATTACHMENT_NAME),
                  attach.getValue(CommonsConstants.COL_FILE_NAME)),
                  attach.getLong(CommonsConstants.COL_FILE_SIZE), null));
        }
        RowFactory.createRow(dataInfo.getNewRowForm(), null, dataInfo, row, null,
            new RequestBuilder(files), null);
      }
    });
  }

  private void flagMessage(long accountId, final IsRow row, final int flagIdx, MessageFlag flag,
      final ScheduledCommand command) {

    ParameterList params = MailKeeper.createArgs(SVC_FLAG_MESSAGE);
    params.addDataItem(COL_ACCOUNT, accountId);
    params.addDataItem(COL_PLACE, row.getId());
    params.addDataItem(COL_FLAGS, flag.name());
    params.addDataItem("on", Codec.pack(!flag.isSet(row.getInteger(flagIdx))));

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getFormView());

        if (!response.hasErrors()) {
          Integer flags = BeeUtils.toIntOrNull((String) response.getResponse());
          row.setValue(flagIdx, flags);

          if (command != null) {
            command.execute();
          }
        }
      }
    });
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
          message.setAccountInfo(getCurrentAccount().getAddress(), accounts);

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

  private boolean isSenderDisplayMode() {
    SystemFolder mode = getCurrentAccount().getSystemFolder(getCurrentFolderId());
    return mode == SystemFolder.Sent || mode == SystemFolder.Drafts;
  }

  private void removeMessages() {
    if (currentMessage == null) {
      return;
    }
    final List<String> options = Lists.newArrayList(Localized.getConstants().mailCurrentMessage());
    final Collection<RowInfo> rows = messages.getGridPresenter().getGridView()
        .getSelectedRows(SelectedRows.ALL);

    if (!BeeUtils.isEmpty(rows)) {
      options.add(Localized.getMessages().mailSelectedMessages(rows.size()));
    }
    final AccountInfo account = getCurrentAccount();
    final Long folderId = getCurrentFolderId();
    final boolean purge = account.getSystemFolder(folderId) == SystemFolder.Trash;

    options.add(Localized.getConstants().cancel());
    Icon icon = purge ? Icon.ALARM : Icon.WARNING;

    Global.messageBox(purge ? Localized.getConstants().actionDelete() : Localized.getConstants()
        .mailActionMoveToTrash(),
        icon, null, options,
        options.size() - 1, new ChoiceCallback() {
          @Override
          public void onSuccess(int value) {
            if (value == options.size() - 1) {
              return;
            }
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
            params.addDataItem(COL_ACCOUNT, getCurrentAccount().getId());
            params.addDataItem(COL_FOLDER, getCurrentFolderId());
            params.addDataItem(COL_PLACE, Codec.beeSerialize(ids));
            params.addDataItem("Purge", Codec.pack(purge));

            BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                response.notify(getFormView());

                if (!response.hasErrors() && Objects.equal(folderId, getCurrentFolderId())) {
                  getFormView().notifyInfo(
                      purge ? Localized.getMessages().mailDeletedMessages(
                          (String) response.getResponse()) : Localized.getMessages()
                          .mailMovedMessagesToTrash((String) response.getResponse()));
                  refresh(null);
                }
              }
            });
          }
        });
  }
}
