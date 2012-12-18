package com.butent.bee.client.modules.mail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
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
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
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
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MailPanel extends AbstractFormInterceptor {

  private class ContentHandler implements ActiveRowChangeEvent.Handler {
    private final int message;
    private final int sender;
    private final int senderLabel;
    private final int senderEmail;
    private final int date;
    private final int subject;

    public ContentHandler(List<BeeColumn> dataColumns) {
      message = DataUtils.getColumnIndex(COL_MESSAGE, dataColumns);
      sender = DataUtils.getColumnIndex(COL_SENDER, dataColumns);
      senderLabel = DataUtils.getColumnIndex("SenderLabel", dataColumns);
      senderEmail = DataUtils.getColumnIndex("SenderEmail", dataColumns);
      date = DataUtils.getColumnIndex(COL_DATE, dataColumns);
      subject = DataUtils.getColumnIndex(COL_SUBJECT, dataColumns);
    }

    @Override
    public void onActiveRowChange(ActiveRowChangeEvent event) {
      IsRow row = event.getRowValue();

      messageWidget.setVisible(false);
      emptySelectionWidget.setVisible(row == null);
      messageHandler.deactivate();

      if (row != null) {
        currentSender = row.getLong(sender);
        currentMessage = row.getLong(message);

        String lbl = row.getString(senderLabel);
        String mail = row.getString(senderEmail);
        senderLabelWidget.setText(BeeUtils.notEmpty(lbl, mail));
        senderEmailWidget.setText(BeeUtils.isEmpty(lbl) ? "" : mail);
        dateWidget.setValue(row.getDateTime(date));
        subjectWidget.setText(row.getString(subject));

        messageHandler.requery(currentMessage, isSenderDisplayMode(currentFolder) ? null
            : (/* unread */false ? accounts.get(currentAccount).getLong(COL_ADDRESS) : 0));

        messageWidget.setVisible(true);
      } else {
        currentSender = -1;
        currentMessage = -1;
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
    private final int attachmentCount;

    public EnvelopeRenderer(List<? extends IsColumn> dataColumns) {
      super(null);

      senderEmail = DataUtils.getColumnIndex("SenderEmail", dataColumns);
      senderLabel = DataUtils.getColumnIndex("SenderLabel", dataColumns);
      recipientEmail = DataUtils.getColumnIndex("RecipientEmail", dataColumns);
      recipientLabel = DataUtils.getColumnIndex("RecipientLabel", dataColumns);
      recipientCount = DataUtils.getColumnIndex("RecipientCount", dataColumns);
      dateIdx = DataUtils.getColumnIndex(COL_DATE, dataColumns);
      subjectIdx = DataUtils.getColumnIndex("Subject", dataColumns);
      attachmentCount = DataUtils.getColumnIndex("AttachmentCount", dataColumns);
    }

    @Override
    public String render(IsRow row) {
      Panel fp = new FlowPanel();
      fp.setStyleName("bee-mail-Header");

      TextLabel sender = new TextLabel(false);
      sender.setStyleName("bee-mail-HeaderAddress");
      String address;

      if (isSenderDisplayMode(currentFolder)) {
        address = BeeUtils.notEmpty(row.getString(recipientLabel), row.getString(recipientEmail));
        int cnt = BeeUtils.unbox(row.getInteger(recipientCount)) - 1;

        if (cnt > 0) {
          address += " (" + cnt + "+)";
        }
      } else {
        if (false/* Unread */) {
          fp.addStyleName("bee-mail-HeaderUnread");
          Widget image = new BeeImage(Global.getImages().greenSmall());
          image.setStyleName("bee-mail-UnreadImage");
          fp.add(image);
        }
        address = BeeUtils.notEmpty(row.getString(senderLabel), row.getString(senderEmail));
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
      newMessageHandler.save(getFormView());
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

      if (BeeUtils.same(columnName, "Envelope")) {
        return new EnvelopeRenderer(dataColumns);
      }
      return null;
    }

    @Override
    public void onShow(GridPresenter presenter) {
      messagesPresenter = presenter;
      messagesPresenter.getGridView().getGrid()
          .addActiveRowChangeHandler(new ContentHandler(messagesPresenter.getDataColumns()));
    }
  }

  private static final String MESSAGES_FILTER = "MessagesFilter";

  private static enum NewMailMode {
    NEW, REPLY, REPLY_ALL, FORWARD
  }

  private SystemFolder currentFolder = SystemFolder.Inbox;
  private int currentAccount = -1;
  private long currentMessage = -1;
  private long currentSender = -1;

  private GridPresenter messagesPresenter;
  private final MessageHandler messageHandler = new MessageHandler();

  private final List<SimpleRow> accounts = Lists.newArrayList();

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
          SimpleRow account = rs.getRow(i);

          if (currentAccount < 0 || BeeUtils.isTrue(account.getBoolean(COL_ACCOUNT_DEFAULT))) {
            currentAccount = i;
          }
          accounts.add(account);
        }
        FormFactory.openForm("Mail", MailPanel.this);
      }
    });
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof BeeListBox && BeeUtils.same(name, "Accounts")) {
      initAccounts((ListBox) widget);

    } else if (widget instanceof TabBar && BeeUtils.same(name, "DisplayMode")) {
      initFolders((TabBar) widget);

    } else if (widget instanceof GridPanel && BeeUtils.same(name, "Messages")) {
      ((GridPanel) widget).setGridInterceptor(new MessagesGrid());

    } else if (widget instanceof Panel && BeeUtils.same(name, "Message")) {
      messageWidget = ((Panel) widget);

    } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Create")) {
      initCreateAction((HasClickHandlers) widget, NewMailMode.NEW);

    } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Reply")) {
      initCreateAction((HasClickHandlers) widget, NewMailMode.REPLY);

    } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "ReplyAll")) {
      initCreateAction((HasClickHandlers) widget, NewMailMode.REPLY_ALL);

    } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Forward")) {
      initCreateAction((HasClickHandlers) widget, NewMailMode.FORWARD);

    } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Trash")) {
      initTrashAction((HasClickHandlers) widget);

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
    } else {
      messageHandler.afterCreateWidget(name, widget, callback);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }

  @Override
  public void onStateChange(State state) {
    if (State.ACTIVATED.equals(state)) {
      MailKeeper.activateController(this);

    } else if (State.REMOVED.equals(state)) {
      MailKeeper.removeMailPanel(this);
    }
    LogUtils.getRootLogger().warning("MailPanel", state);
  }

  private Long getCurrentFolderId() {
    Long folderId = null;
    SimpleRow account = accounts.get(currentAccount);

    if (account != null) {
      folderId = account.getLong(currentFolder.name() + "Folder");
    }
    return folderId;
  }

  private void initAccounts(final ListBox accountsWidget) {
    accountsWidget.clear();

    for (SimpleRow account : accounts) {
      accountsWidget.addItem(account.getValue(COL_ACCOUNT_DESCRIPTION));
    }
    accountsWidget.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int selectedAccount = accountsWidget.getSelectedIndex();

        if (selectedAccount != currentAccount) {
          currentAccount = accountsWidget.getSelectedIndex();
          currentFolder = null;

          ((TabBar) MailPanel.this.getFormView().getWidgetByName("DisplayMode"))
              .selectTab(SystemFolder.Inbox.ordinal());

          initFolders();
        }
      }
    });
    accountsWidget.setEnabled(accounts.size() > 1);
    accountsWidget.setSelectedIndex(currentAccount);
    initFolders();
  }

  private void initCreateAction(HasClickHandlers widget, final NewMailMode mode) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent ev) {
        if (currentAccount < 0 || mode != NewMailMode.NEW && currentMessage < 0) {
          return;
        }
        Long draftId = null;
        Long sender = accounts.get(currentAccount).getLong(COL_ADDRESS);
        Set<Long> to = null;
        Set<Long> cc = null;
        Set<Long> bcc = null;
        String subject = null;
        String content = null;
        Map<Long, NewFileInfo> attachments = null;

        switch (mode) {
          case NEW:
            if (currentFolder == SystemFolder.Drafts && currentMessage > 0) {
              draftId = currentMessage;
              subject = subjectWidget.getText();
              to = messageHandler.getTo();
              cc = messageHandler.getCc();
              bcc = messageHandler.getBcc();
              content = messageHandler.getContent();
              attachments = messageHandler.getAttachments();
            }
            break;

          case REPLY:
          case REPLY_ALL:
            if (mode == NewMailMode.REPLY_ALL) {
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
            to = Sets.newHashSet(currentSender);
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

  private void initFolders() {
    if (getFormView() != null) {
      // getFormView().notifyInfo(BeeUtils.joinWords("Folder:", getCurrentFolderId()));
    }
  }

  private void initFolders(TabBar widget) {
    widget.selectTab(currentFolder.ordinal(), false);

    widget.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> ev) {
        refresh(NameUtils.getEnumByIndex(SystemFolder.class, ev.getSelectedItem()));
      }
    });
  }

  private void initTrashAction(HasClickHandlers widget) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent ev) {
        if (currentMessage < 0) {
          return;
        }
        List<String> options = Lists.newArrayList("Aktyvų laišką");
        final Collection<RowInfo> rows = messagesPresenter.getGridView().getSelectedRows();

        if (!BeeUtils.isEmpty(rows)) {
          options.add(BeeUtils.joinWords("Pažymėtus", rows.size(), "laiškus"));
        }
        final boolean purge = (currentFolder != SystemFolder.Inbox);

        Global.choice(purge ? "Pašalinti" : "Perkelti į šiukšlinę", null, options,
            new ChoiceCallback() {
              @Override
              public void onSuccess(int value) {
                List<Long> ids = null;

                if (value == 0) {
                  ids = Lists.newArrayList(currentMessage);
                } else if (value == 1) {
                  ids = Lists.newArrayList();

                  for (RowInfo info : rows) {
                    ids.add(info.getId());
                  }
                }
                ParameterList params = MailKeeper.createArgs(SVC_REMOVE_MESSAGES);
                params.addDataItem(isSenderDisplayMode(currentFolder) ? COL_SENDER : "Recipient",
                    accounts.get(currentAccount).getLong(COL_ADDRESS));
                params.addDataItem("Messages", Codec.beeSerialize(ids));
                params.addDataItem("Purge", purge ? 1 : 0);

                BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    response.notify(getFormView());

                    if (!response.hasErrors()) {
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

  private boolean isSenderDisplayMode(SystemFolder mode) {
    return mode == SystemFolder.Sent || mode == SystemFolder.Drafts;
  }

  private void refresh(final SystemFolder folder) {
    if (messagesPresenter == null) {
      return;
    }
    if (folder != null) {
      if (folder == currentFolder) {
        ParameterList params = MailKeeper.createArgs(SVC_CHECK_MAIL);
        params.addDataItem(COL_ADDRESS, accounts.get(currentAccount).getLong(COL_ADDRESS));
        Long folderId = getCurrentFolderId();

        if (DataUtils.isId(folderId)) {
          params.addDataItem(COL_FOLDER, getCurrentFolderId());
        } else {
          params.addDataItem(COL_FOLDER_NAME, currentFolder.name());
        }
        BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (response.hasErrors()) {
              getFormView().notifySevere(response.getErrors());
            } else {
              if (folder == currentFolder) {
                refresh(null);
              }
              int msgCnt = BeeUtils.toInt((String) response.getResponse());

              if (msgCnt > 0) {
                getFormView().notifyInfo(BeeUtils.joinWords(folder, "naujų žinučių:", msgCnt));
              }
            }
          }
        });
        return;

      } else {
        currentFolder = folder;
      }
    }
    messagesPresenter.getGridView().getGrid().reset();
    messagesPresenter.getDataProvider().setParentFilter(MESSAGES_FILTER,
        ComparisonFilter.isEqual(COL_FOLDER, new LongValue(getCurrentFolderId())));
    messagesPresenter.refresh(false);
  }
}
