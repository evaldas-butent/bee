package com.butent.bee.client.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.modules.mail.MailPanel.AccountInfo;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormViewCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NewMailMessage extends AbstractFormInterceptor
    implements ClickHandler, SelectionHandler<NewFileInfo> {

  private class DialogCallback extends InputCallback {
    @Override
    public String getErrorMessage() {
      if (validate()) {
        return null;
      } else {
        return InputBoxes.SILENT_ERROR;
      }
    }

    @Override
    public void onClose(final CloseCallback closeCallback) {
      Assert.notNull(closeCallback);

      if (hasChanges()) {
        Global.decide(null, Lists.newArrayList(Localized.getConstants().mailMessageWasNotSent(),
            Localized.getConstants().mailQuestionSaveToDraft()), new DecisionCallback() {
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
      save(true);
    }
  }

  public static void create(final Set<Long> to, final Set<Long> cc, final Set<Long> bcc,
      final String subject, final String content, final Map<Long, NewFileInfo> attach) {

    ParameterList params = MailKeeper.createArgs(SVC_GET_ACCOUNTS);
    params.addDataItem(COL_USER, BeeKeeper.getUser().getUserId());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        Assert.isTrue(response.hasResponse(SimpleRowSet.class));
        SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());
        List<AccountInfo> availableAccounts = Lists.newArrayList();
        Long defaultAccount = null;

        for (SimpleRow row : rs) {
          if (defaultAccount == null || BeeUtils.unbox(row.getBoolean(COL_ACCOUNT_DEFAULT))) {
            defaultAccount = row.getLong(COL_ADDRESS);
          }
          availableAccounts.add(new AccountInfo(row));
        }
        if (BeeUtils.isEmpty(availableAccounts)) {
          BeeKeeper.getScreen().notifyWarning("No accounts found");
          return;
        }
        create(defaultAccount, availableAccounts, to, cc, bcc, subject, content, attach, null);
      }
    });
  }

  public static NewMailMessage create(Long defaultAccount, List<AccountInfo> availableAccounts,
      Set<Long> to, Set<Long> cc, Set<Long> bcc, String subject, String content,
      Map<Long, NewFileInfo> attach, Long draftId) {

    final NewMailMessage newMessage = new NewMailMessage(defaultAccount, availableAccounts,
        to, cc, bcc, subject, content, attach, draftId);

    FormFactory.createFormView(FORM_NEW_MAIL, null, null, false, newMessage,
        new FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView formView) {
            if (formView != null) {
              formView.start(null);

              Global.inputWidget(formView.getCaption(), formView,
                  newMessage.new DialogCallback(), RowFactory.DIALOG_STYLE);
            }
          }
        });
    return newMessage;
  }

  private Long account;
  private final List<AccountInfo> accounts;
  private final Long draftId;

  private final Map<String, String> recipients = Maps.newHashMap();
  private final String subject;
  private final String content;
  private final Map<Long, NewFileInfo> defaultAttachments;

  private final Map<String, MultiSelector> recipientWidgets = Maps.newHashMap();
  private Editor subjectWidget;
  private Editor contentWidget;
  private final Map<String, Long> attachments = Maps.newLinkedHashMap();

  private ScheduledCommand scheduled;

  private NewMailMessage(Long defaultAccount, List<AccountInfo> availableAccounts,
      Set<Long> to, Set<Long> cc, Set<Long> bcc, String subject, String content,
      Map<Long, NewFileInfo> attach, Long draftId) {

    Assert.notNull(defaultAccount);

    this.account = defaultAccount;
    this.accounts = availableAccounts;
    this.draftId = draftId;

    this.subject = subject;
    this.content = content;

    if (!BeeUtils.isEmpty(attach)) {
      this.defaultAttachments = attach;
    } else {
      this.defaultAttachments = Maps.newHashMap();
    }
    for (Long id : defaultAttachments.keySet()) {
      attachments.put(defaultAttachments.get(id).getName(), id);
    }
    if (!DataUtils.isId(draftId)) {
      if (to != null) {
        to.remove(defaultAccount);
      }
      if (cc != null) {
        cc.remove(defaultAccount);
      }
      if (bcc != null) {
        bcc.remove(defaultAccount);
      }
    }
    recipients.put(AddressType.TO.name(), DataUtils.buildIdList(to));
    recipients.put(AddressType.CC.name(), DataUtils.buildIdList(cc));
    recipients.put(AddressType.BCC.name(), DataUtils.buildIdList(bcc));
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ListBox && BeeUtils.same(name, TBL_ACCOUNTS)) {
      if (BeeUtils.isEmpty(accounts)) {
        widget.asWidget().setVisible(false);
      } else {
        final ListBox accountsWidget = (ListBox) widget;

        for (AccountInfo accountInfo : accounts) {
          accountsWidget.addItem(accountInfo.getDescription());

          if (Objects.equal(account, accountInfo.getAddress())) {
            accountsWidget.setSelectedIndex(accountsWidget.getItemCount() - 1);
          }
        }
        accountsWidget.setEnabled(accountsWidget.getItemCount() > 1);
        accountsWidget.addChangeHandler(new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            account = accounts.get(accountsWidget.getSelectedIndex()).getAddress();
          }
        });
      }
    } else if (widget instanceof MultiSelector) {
      MultiSelector w = (MultiSelector) widget;

      for (AddressType type : AddressType.values()) {
        if (BeeUtils.same(name, type.name())) {
          w.render(recipients.get(type.name()));
          recipientWidgets.put(type.name(), w);
          break;
        }
      }
    } else if (widget instanceof Editor && BeeUtils.same(name, COL_SUBJECT)) {
      subjectWidget = (Editor) widget;
      subjectWidget.setValue(subject);

    } else if (widget instanceof Editor && BeeUtils.same(name, COL_MESSAGE)) {
      contentWidget = (Editor) widget;
      contentWidget.setValue(content);

    } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Send")) {
      ((HasClickHandlers) widget).addClickHandler(this);

    } else if (widget instanceof FileCollector && BeeUtils.same(name, TBL_ATTACHMENTS)) {
      if (!BeeUtils.isEmpty(defaultAttachments)) {
        ((FileCollector) widget).addFiles(defaultAttachments.values());
      }
      ((FileCollector) widget).bindDnd(getFormView());
      ((FileCollector) widget).addSelectionHandler(this);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }

  @Override
  public void onClick(ClickEvent event) {
    if (validate()) {
      Popup dialog = UiHelper.getParentPopup(getFormView().asWidget());
      if (dialog != null) {
        dialog.close();
      }
      save(false);
    }
  }

  @Override
  public void onSelection(SelectionEvent<NewFileInfo> event) {
    final String fileName = event.getSelectedItem().getName();

    if (attachments.containsKey(fileName)) {
      attachments.remove(fileName);
    } else {
      attachments.put(fileName, 0L);

      FileUtils.uploadFile(event.getSelectedItem(), new Callback<Long>() {
        @Override
        public void onFailure(String... reason) {
          if (attachments.containsKey(fileName)) {
            attachments.put(fileName, -1L);
            super.onFailure(reason);
          }
        }

        @Override
        public void onSuccess(Long id) {
          if (attachments.containsKey(fileName)) {
            attachments.put(fileName, id);
          }
        }
      });
    }
  }

  public void setScheduled(ScheduledCommand scheduled) {
    this.scheduled = scheduled;
  }

  private boolean hasChanges() {
    if (recipients.size() != recipientWidgets.size()) {
      return true;
    }
    for (String type : recipients.keySet()) {
      if (!BeeUtils.same(recipients.get(type), recipientWidgets.get(type).getValue())) {
        return true;
      }
    }
    if (subjectWidget == null || !BeeUtils.same(subject, subjectWidget.getValue())) {
      return true;
    }
    if (contentWidget == null || !BeeUtils.same(content, contentWidget.getValue())) {
      return true;
    }
    if (!BeeUtils.same(DataUtils.buildIdList(defaultAttachments.keySet()),
        DataUtils.buildIdList(attachments.values()))) {
      return true;
    }
    return false;
  }

  private void save(boolean saveMode) {
    if (saveMode && !hasChanges()) {
      return;
    }
    ParameterList params = MailKeeper.createArgs(SVC_SEND_MAIL);
    params.addDataItem(COL_SENDER, account);

    for (String type : recipientWidgets.keySet()) {
      String recipient = recipientWidgets.get(type).getValue();

      if (!BeeUtils.isEmpty(recipient)) {
        params.addDataItem(type, recipient);
      }
    }
    if (draftId != null) {
      params.addDataItem("DraftId", draftId);
    }
    params.addDataItem("Save", saveMode ? 1 : 0);
    params.addDataItem(COL_SUBJECT, subjectWidget.getValue());
    params.addDataItem(COL_CONTENT, contentWidget.getValue());

    String ids = DataUtils.buildIdList(attachments.values());

    if (!BeeUtils.isEmpty(ids)) {
      params.addDataItem(TBL_ATTACHMENTS, ids);
    }
    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (scheduled != null) {
          scheduled.execute();
        }
      }
    });
  }

  private boolean validate() {
    boolean hasRecipients = false;
    String error = null;

    for (MultiSelector r : recipientWidgets.values()) {
      if (!BeeUtils.isEmpty(r.getValue())) {
        hasRecipients = true;
        break;
      }
    }
    if (!hasRecipients) {
      error = Localized.getConstants().mailSpecifyRecipient();

    } else if (subjectWidget == null || BeeUtils.isEmpty(subjectWidget.getValue())) {
      error = Localized.getConstants().mailSpecifySubject();

    } else if (contentWidget == null || BeeUtils.isEmpty(contentWidget.getValue())) {
      error = Localized.getConstants().mailMessageBodyIsEmpty();

    } else if (attachments.values().contains(0L)) {
      error = Localized.getConstants().mailThereIsStackOfUnfinishedAttachments();
    }
    if (!BeeUtils.isEmpty(error)) {
      getFormView().notifySevere(error);
    }
    return BeeUtils.isEmpty(error);
  }
}
