package com.butent.bee.client.modules.mail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.ModalForm;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.mail.AccountInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.WindowType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class NewMailMessage extends AbstractFormInterceptor
    implements SelectionHandler<FileInfo> {

  public static void create(String to, String subject, String content,
      Collection<FileInfo> attachments, BiConsumer<Long, Boolean> callback) {

    create(Collections.singleton(to), null, null, subject, content, attachments, null, false,
        callback);
  }

  public static void create(Set<String> to, Set<String> cc, Set<String> bcc, String subject,
      String content, Collection<FileInfo> attachments, Long relatedId, boolean isDraft,
      BiConsumer<Long, Boolean> callback) {

    MailKeeper.getAccounts((availableAccounts, defaultAccount) -> {
      if (BeeUtils.isEmpty(availableAccounts)) {
        BeeKeeper.getScreen().notifyWarning(Localized.dictionary().mailNoAccountsFound());
      } else {
        create(availableAccounts, defaultAccount, to, cc, bcc, subject, content, attachments,
            relatedId, isDraft).setCallback(callback);
      }
    });
  }

  public static NewMailMessage create(List<AccountInfo> availableAccounts,
      AccountInfo defaultAccount, Set<String> to, Set<String> cc, Set<String> bcc, String subject,
      String content, Collection<FileInfo> attachments, Long relatedId, boolean isDraft) {

    NewMailMessage newMessage = new NewMailMessage(availableAccounts, defaultAccount,
        to, cc, bcc, subject, content, attachments, relatedId, isDraft);

    newMessage.isSignatureAbove =
        BeeUtils.unbox(Global.getParameterBoolean(PRM_SIGNATURE_POSITION));

    FormFactory.openForm(FORM_NEW_MAIL_MESSAGE, newMessage, presenter -> {
      if (presenter instanceof FormPresenter) {
        newMessage.initHeader(presenter.getHeader());

        FormView formView = ((FormPresenter) presenter).getFormView();
        WindowType windowType = getWindowType();

        if (windowType.isPopup()) {
          ModalForm dialog = new ModalForm(presenter, formView, false);

          dialog.setPreviewEnabled(windowType == WindowType.MODAL);
          dialog.center();

        } else {
          PresenterCallback.SHOW_IN_NEW_TAB.onCreate(presenter);
        }
      }
    });

    return newMessage;
  }

  private static WindowType getWindowType() {
    if (Popup.hasEventPreview()) {
      return WindowType.MODAL;

    } else {
      String wtp = BeeKeeper.getUser().getNewMailMessageWindow();
      if (BeeUtils.isEmpty(wtp)) {
        wtp = Settings.getNewMailMessageWindow();
      }

      WindowType windowType = WindowType.parse(wtp);
      return (windowType == null) ? WindowType.DEFAULT_NEW_MAIL_MESSAGE : windowType;
    }
  }

  private AccountInfo account;
  private final List<AccountInfo> accounts = new ArrayList<>();
  private final Map<Long, String> signatures = new HashMap<>();
  private final Long relatedId;
  private final boolean isDraft;
  private final Div signature = new Div().id(BeeUtils.randomString(5));
  private boolean isSignatureAbove;

  private final Multimap<AddressType, String> recipients = HashMultimap.create();
  private final String subject;
  private final String content;
  private final Collection<FileInfo> defaultAttachments;

  private final Map<AddressType, MultiSelector> recipientWidgets = new HashMap<>();
  private Editor subjectWidget;
  private Editor contentWidget;
  private final ListBox signaturesWidget = new ListBox();
  private FileCollector attachmentsWidget;

  private BiConsumer<Long, Boolean> actionCallback;

  private NewMailMessage(List<AccountInfo> availableAccounts, AccountInfo defaultAccount,
      Set<String> to, Set<String> cc, Set<String> bcc, String subject, String content,
      Collection<FileInfo> attachments, Long relatedId, boolean isDraft) {

    this.account = Assert.notNull(defaultAccount);
    this.accounts.addAll(availableAccounts);
    this.relatedId = relatedId;
    this.isDraft = isDraft;

    this.subject = subject;
    this.content = content;

    this.defaultAttachments = attachments;

    Map<AddressType, Set<String>> recs = new HashMap<>();
    recs.put(AddressType.TO, to);
    recs.put(AddressType.CC, cc);
    recs.put(AddressType.BCC, bcc);

    for (AddressType type : recs.keySet()) {
      Collection<String> emails = recs.get(type);

      if (!BeeUtils.isEmpty(emails)) {
        recipients.putAll(type,
            emails.stream().filter(s -> !BeeUtils.isEmpty(s)).collect(Collectors.toSet()));

        if (!isDraft) {
          recipients.remove(type, defaultAccount.getAddress());
        }
      }
    }
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    AddressType type = EnumUtils.getEnumByName(AddressType.class, name);

    if (widget instanceof MultiSelector && type != null) {
      MultiSelector input = (MultiSelector) widget;

      Collection<String> lst = recipients.get(type);
      if (!BeeUtils.isEmpty(lst)) {
        input.setValues(lst);
      }
      recipientWidgets.put(type, input);

    } else if (widget instanceof Editor && BeeUtils.same(name, COL_SUBJECT)) {
      subjectWidget = (Editor) widget;
      subjectWidget.setValue(subject);

    } else if (widget instanceof Editor && BeeUtils.same(name, COL_MESSAGE)) {
      contentWidget = (Editor) widget;
      contentWidget.setValue(content);

    } else if (widget instanceof FileCollector && BeeUtils.same(name, TBL_ATTACHMENTS)) {
      attachmentsWidget = (FileCollector) widget;
      attachmentsWidget.addFiles(defaultAttachments);
      attachmentsWidget.bindDnd(getFormView());
      attachmentsWidget.addSelectionHandler(this);
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.CLOSE && hasChanges()) {
      Global.decide(null, Lists.newArrayList(Localized.dictionary().mailMessageWasNotSent(),
          Localized.dictionary().mailQuestionSaveToDraft()), new DecisionCallback() {
        @Override
        public void onCancel() {
          UiHelper.focus(getFormView().asWidget());
        }

        @Override
        public void onConfirm() {
          onSave(true);
        }

        @Override
        public void onDeny() {
          close();
        }
      }, DialogConstants.DECISION_YES);

      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    Assert.unsupported();
    return null;
  }

  @Override
  public void onSelection(SelectionEvent<FileInfo> event) {
    FileInfo file = event.getSelectedItem();

    if (attachmentsWidget.contains(file)) {
      FileUtils.uploadFile(file, new Callback<FileInfo>() {
        @Override
        public void onFailure(String... reason) {
          attachmentsWidget.removeFile(file);
          Callback.super.onFailure(reason);
        }

        @Override
        public void onSuccess(FileInfo info) {
          attachmentsWidget.refreshFile(file);
        }
      });
    }
  }

  @Override
  public void onStart(FormView form) {
    super.onStart(form);

    Scheduler.get().scheduleDeferred(() -> {
      Widget focusWidget = getFocusWidget();

      if (focusWidget == null) {
        UiHelper.focus(form.asWidget());
      } else {
        UiHelper.focus(focusWidget);
      }
    });
  }

  public void setCallback(BiConsumer<Long, Boolean> callback) {
    this.actionCallback = callback;
  }

  private void applySignature(Long signatureId) {
    String value = null;
    String currentContent = contentWidget.getValue();
    String oldSignature = signature.toString();
    signature.clearChildren();

    if (DataUtils.isId(signatureId)) {
      value = BeeUtils.toString(signatureId);
      signature.text(signatures.get(signatureId));
    }
    if (!isDraft || DataUtils.isId(signatureId)) {
      String newSignature = signature.toString();

      if (currentContent.contains(oldSignature)) {
        currentContent = currentContent.replace(oldSignature, newSignature);
      } else {
        if (isSignatureAbove) {
          currentContent = SIGNATURE_SEPARATOR + newSignature + currentContent;
        } else {
          currentContent = currentContent + SIGNATURE_SEPARATOR + newSignature;
        }
      }
    }
    contentWidget.setValue(currentContent);
    signaturesWidget.setValue(value);
  }

  private void close() {
    BeeKeeper.getScreen().closeWidget(getPresenter().getMainView());
  }

  private Widget getFocusWidget() {
    Editor w = null;

    if (BeeUtils.isEmpty(recipients.get(AddressType.TO))) {
      w = recipientWidgets.get(AddressType.TO);
    }
    if (Objects.isNull(w) && BeeUtils.isEmpty(subject)) {
      w = subjectWidget;
    }
    if (Objects.isNull(w)) {
      w = contentWidget;
    }
    return Objects.isNull(w) ? null : w.asWidget();
  }

  private boolean hasChanges() {
    for (AddressType type : recipientWidgets.keySet()) {
      Set<String> values = new HashSet<>(recipientWidgets.get(type).getValues());
      if (!BeeUtils.sameElements(recipients.get(type), values)) {
        return true;
      }
    }
    if (subjectWidget == null || !BeeUtils.same(subject, subjectWidget.getValue())) {
      return true;
    }
    if (contentWidget == null || !BeeUtils.same(content,
        contentWidget.getValue().replace(SIGNATURE_SEPARATOR + signature.toString(), ""))) {
      return true;
    }
    return !BeeUtils.sameElements(defaultAttachments, attachmentsWidget.getFiles());
  }

  private void initHeader(HeaderView header) {
    final ListBox accountsWidget = new ListBox();

    for (int i = 0; i < accounts.size(); i++) {
      AccountInfo accountInfo = accounts.get(i);
      accountsWidget.addItem(accountInfo.getDescription() + " <" + accountInfo.getAddress() + ">");

      if (Objects.equals(accountInfo, account)) {
        accountsWidget.setSelectedIndex(i);
      }
    }
    accountsWidget.setEnabled(accountsWidget.getItemCount() > 1);
    accountsWidget.addChangeHandler(event -> {
      AccountInfo selectedAccount = accounts.get(accountsWidget.getSelectedIndex());

      if (!Objects.equals(selectedAccount, account)) {
        account = selectedAccount;
        applySignature(account.getSignatureId());
      }
    });

    header.addCommandItem(accountsWidget);

    Queries.getRowSet(TBL_SIGNATURES, null,
        Filter.equals(COL_USER, BeeKeeper.getUser().getUserId()), result -> {
          for (int i = 0; i < result.getNumberOfRows(); i++) {
            Long signatureId = result.getRow(i).getId();

            signatures.put(signatureId, result.getString(i, COL_SIGNATURE_CONTENT));
            signaturesWidget.addItem(result.getString(i, COL_SIGNATURE_NAME),
                BeeUtils.toString(signatureId));
          }
          signaturesWidget.setEnabled(signaturesWidget.getItemCount() > 0);
          signaturesWidget.addChangeHandler(
              event -> applySignature(BeeUtils.toLongOrNull(signaturesWidget.getValue())));

          applySignature(isDraft ? null : account.getSignatureId());
        });

    header.addCommandItem(new Label(Localized.dictionary().mailSignature() + ":"));
    header.addCommandItem(signaturesWidget);

    FaLabel send = new FaLabel(FontAwesome.PAPER_PLANE);
    send.setTitle(Localized.dictionary().send());
    send.addClickHandler(event -> onSave(false));

    header.addCommandItem(send);

    FaLabel save = new FaLabel(FontAwesome.SAVE);
    save.setTitle(Localized.dictionary().actionSave());
    save.addClickHandler(event -> onSave(true));

    header.addCommandItem(save);
  }

  private void onSave(boolean saveMode) {
    if (validate()) {
      close();
      save(saveMode);
    }
  }

  private void save(final boolean saveMode) {
    if (saveMode && !hasChanges()) {
      return;
    }
    ParameterList params = MailKeeper.createArgs(SVC_SEND_MAIL);
    params.addDataItem(COL_ACCOUNT, account.getAccountId());

    for (AddressType type : recipientWidgets.keySet()) {
      Set<String> values = new HashSet<>(recipientWidgets.get(type).getValues());
      if (!BeeUtils.isEmpty(values)) {
        params.addDataItem(type.name(), Codec.beeSerialize(values));
      }
    }

    if (relatedId != null) {
      params.addDataItem(AdministrationConstants.COL_RELATION, relatedId);
    }
    params.addDataItem("Save", saveMode ? 1 : 0);
    params.addDataItem(COL_SUBJECT, subjectWidget.getValue());
    params.addDataItem(COL_CONTENT, contentWidget.getValue());

    Map<Long, String> attachments = new LinkedHashMap<>();

    for (FileInfo file : attachmentsWidget.getFiles()) {
      attachments.put(file.getId(), BeeUtils.notEmpty(file.getCaption(), file.getName()));
    }
    if (!BeeUtils.isEmpty(attachments)) {
      params.addDataItem(TBL_ATTACHMENTS, Codec.beeSerialize(attachments));
    }
    BeeKeeper.getRpc().makePostRequest(params, response -> {
      response.notify(BeeKeeper.getScreen());

      if (actionCallback != null && !response.hasErrors()) {
        actionCallback.accept(response.getResponseAsLong(), saveMode);
      }
    });
  }

  private boolean validate() {
    boolean hasRecipients = false;
    String error = null;

    for (MultiSelector r : recipientWidgets.values()) {
      if (!BeeUtils.isEmpty(r.getValues())) {
        hasRecipients = true;
        break;
      }
    }
    if (!hasRecipients) {
      error = Localized.dictionary().mailSpecifyRecipient();

    } else if (subjectWidget == null || BeeUtils.isEmpty(subjectWidget.getValue())) {
      error = Localized.dictionary().mailSpecifySubject();

    } else if (contentWidget == null || BeeUtils.isEmpty(contentWidget.getValue())) {
      error = Localized.dictionary().mailMessageBodyIsEmpty();

    } else {
      for (FileInfo file : attachmentsWidget.getFiles()) {
        if (!DataUtils.isId(file.getId())) {
          error = Localized.dictionary().mailThereIsStackOfUnfinishedAttachments();
          break;
        }
      }
    }
    if (!BeeUtils.isEmpty(error)) {
      getFormView().notifySevere(error);
    }
    return BeeUtils.isEmpty(error);
  }
}
