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
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.mail.MailConstants.MessageStatus;
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

public class MailHandler extends AbstractFormInterceptor {

  private class ContentHandler implements ActiveRowChangeEvent.Handler {
    private final int unread;
    private final int sender;
    private final int senderLabel;
    private final int senderEmail;
    private final int date;
    private final int subject;

    public ContentHandler(List<BeeColumn> dataColumns) {
      unread = DataUtils.getColumnIndex(COL_UNREAD, dataColumns);
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
        currentMessage = row.getId();

        String lbl = row.getString(senderLabel);
        String mail = row.getString(senderEmail);
        senderLabelWidget.setText(BeeUtils.notEmpty(lbl, mail));
        senderEmailWidget.setText(BeeUtils.isEmpty(lbl) ? "" : mail);
        dateWidget.setValue(row.getDateTime(date));
        subjectWidget.setText(row.getString(subject));

        messageHandler.requery(currentMessage, isSenderDisplayMode(currentMode) ? null
            : BeeUtils.isTrue(row.getBoolean(unread)) ? accounts.get(currentAccount) : 0);

        messageWidget.setVisible(true);
      } else {
        currentSender = -1;
        currentMessage = -1;
      }
    }
  }

  private class EnvelopeRenderer extends AbstractCellRenderer {
    private final int unread;
    private final int senderEmail;
    private final int senderLabel;
    private final int recipientEmail;
    private final int recipientLabel;
    private final int recipientCount;
    private final int dateIdx;
    private final int subjectIdx;
    private final int attachmentCount;

    public EnvelopeRenderer(List<? extends IsColumn> dataColumns) {
      unread = DataUtils.getColumnIndex(COL_UNREAD, dataColumns);
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

      if (isSenderDisplayMode(currentMode)) {
        address = BeeUtils.notEmpty(row.getString(recipientLabel), row.getString(recipientEmail));
        int cnt = BeeUtils.unbox(row.getInteger(recipientCount)) - 1;

        if (cnt > 0) {
          address += " (" + cnt + "+)";
        }
      } else {
        if (BeeUtils.isTrue(row.getBoolean(unread))) {
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
      return ImmutableMap.of(MESSAGES_FILTER, Filter.isFalse());
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
      refresh();
    }
  }

  static final String MESSAGES_FILTER = "MessagesFilter";

  private static enum NewMailMode {
    NEW, REPLY, REPLY_ALL, FORWARD
  }

  private static enum DisplayMode {
    INBOX, SENT, DRAFTS, TRASH
  }

  private static Value NEUTRAL = new IntegerValue(MessageStatus.NEUTRAL.ordinal());
  private static Value DRAFT = new IntegerValue(MessageStatus.DRAFT.ordinal());
  private static Value DELETED = new IntegerValue(MessageStatus.DELETED.ordinal());

  private DisplayMode currentMode = DisplayMode.INBOX;

  private TabBar modeWidget;
  private GridPresenter messagesPresenter;
  private Panel messageWidget;

  private final MessageHandler messageHandler = new MessageHandler();

  private List<Long> accounts;
  private int currentAccount = -1;
  private long currentSender = -1;
  private long currentMessage = -1;

  private Label emptySelectionWidget;

  private Label senderLabelWidget;
  private Label senderEmailWidget;
  private DateTimeLabel dateWidget;
  private Label subjectWidget;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof BeeListBox && BeeUtils.same(name, "Accounts")) {
      initAccounts((ListBox) widget);

    } else if (widget instanceof TabBar && BeeUtils.same(name, "DisplayMode")) {
      initDisplayModes((TabBar) widget);

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
    return new MailHandler();
  }

  void refresh() {
    modeWidget.selectTab(currentMode.ordinal(), true);
  }

  private void initAccounts(final ListBox accountsWidget) {
    accountsWidget.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        currentAccount = accountsWidget.getSelectedIndex();
        refresh();
      }
    });
    accounts = Lists.newArrayList();
    currentAccount = -1;
    accountsWidget.clear();

    ParameterList params = MailKeeper.createArgs(SVC_GET_ACCOUNTS);
    params.addDataItem(COL_USER, BeeKeeper.getUser().getUserId());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.isTrue(response.hasResponse(SimpleRowSet.class));
        SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());

        for (int i = 0; i < rs.getNumberOfRows(); i++) {
          if (currentAccount < 0 || BeeUtils.isTrue(rs.getBoolean(i, "Main"))) {
            currentAccount = i;
          }
          accountsWidget.addItem(rs.getValue(i, "Description"));
          accounts.add(rs.getLong(i, COL_ADDRESS));
        }
        accountsWidget.setEnabled(rs.getNumberOfRows() > 1);
        accountsWidget.setSelectedIndex(currentAccount);
      }
    });
  }

  private void initCreateAction(HasClickHandlers widget, final NewMailMode mode) {
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent ev) {
        if (currentAccount < 0 || mode != NewMailMode.NEW && currentMessage < 0) {
          return;
        }
        Long draftId = null;
        Long sender = accounts.get(currentAccount);
        Set<Long> to = null;
        Set<Long> cc = null;
        Set<Long> bcc = null;
        String subject = null;
        String content = null;
        Map<Long, NewFileInfo> attachments = null;

        switch (mode) {
          case NEW:
            if (currentMode == DisplayMode.DRAFTS && currentMessage > 0) {
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
            new NewMessageHandler(sender, draftId, to, cc, bcc, subject, content, attachments,
                isSenderDisplayMode(currentMode) ? MailHandler.this : null);

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

  private void initDisplayModes(TabBar widget) {
    modeWidget = widget;

    modeWidget.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> ev) {
        Filter flt = null;
        currentMode = NameUtils.getEnumByIndex(DisplayMode.class, ev.getSelectedItem());

        if (messagesPresenter != null) {
          messagesPresenter.getGridView().getGrid().reset();

          if (currentAccount < 0) {
            flt = Filter.isFalse();
          } else {
            final Value address = new LongValue(accounts.get(currentAccount));

            switch (currentMode) {
              case INBOX:
                ParameterList params = MailKeeper.createArgs(SVC_CHECK_MAIL);
                params.addDataItem(COL_ADDRESS, address.getLong());

                BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    messagesPresenter.getDataProvider().setParentFilter(MESSAGES_FILTER,
                        Filter.and(ComparisonFilter.isEqual(COL_ADDRESS, address),
                            ComparisonFilter.isEqual(COL_STATUS, NEUTRAL)));
                    messagesPresenter.refresh(false);

                    if (response.hasErrors()) {
                      getFormView().notifySevere(response.getErrors());
                    } else {
                      int msgCnt = BeeUtils.toInt((String) response.getResponse());

                      if (msgCnt > 0) {
                        getFormView()
                            .notifyInfo(BeeUtils.joinWords("Gauta naujų žinučių:", msgCnt));
                      }
                    }
                  }
                });
                return;

              case SENT:
                flt = Filter.and(ComparisonFilter.isEqual(COL_SENDER, address),
                    ComparisonFilter.isEqual("SenderStatus", NEUTRAL));
                break;

              case DRAFTS:
                flt = Filter.and(ComparisonFilter.isEqual(COL_SENDER, address),
                    ComparisonFilter.isEqual("SenderStatus", DRAFT));
                break;

              case TRASH:
                flt = Filter.and(ComparisonFilter.isEqual(COL_ADDRESS, address),
                    ComparisonFilter.isEqual(COL_STATUS, DELETED));
                break;
            }
          }
          messagesPresenter.getDataProvider().setParentFilter(MESSAGES_FILTER, flt);
          messagesPresenter.refresh(false);
        }
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
        final boolean purge = (currentMode != DisplayMode.INBOX);

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
                params.addDataItem(isSenderDisplayMode(currentMode) ? COL_SENDER : "Recipient",
                    accounts.get(currentAccount));
                params.addDataItem("Messages", Codec.beeSerialize(ids));
                params.addDataItem("Purge", purge ? 1 : 0);

                BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    response.notify(getFormView());

                    if (!response.hasErrors()) {
                      refresh();
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

  private boolean isSenderDisplayMode(DisplayMode mode) {
    return mode == DisplayMode.SENT || mode == DisplayMode.DRAFTS;
  }
}
