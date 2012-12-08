package com.butent.bee.client.modules.mail;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;
import java.util.Set;

public class NewMessageHandler extends AbstractFormInterceptor {

  private boolean saveMode = true;

  private final Map<String, String> defaultRecipients = Maps.newHashMap();
  private final String defaultSubject;
  private final String defaultContent;
  private final Map<Long, NewFileInfo> defaultAttachments;

  private final Map<String, MultiSelector> recipientWidgets = Maps.newHashMap();
  private Editor subjectWidget;
  private Editor contentWidget;

  private final Long sender;
  private final Long draftId;

  private final Map<String, Long> attachments = Maps.newLinkedHashMap();

  private final MailHandler mailHandler;

  public NewMessageHandler(Long sender, Long draftId, Set<Long> to, Set<Long> cc, Set<Long> bcc,
      String subject, String content, Map<Long, NewFileInfo> attach, MailHandler mailHandler) {

    Assert.notNull(sender);
    this.sender = sender;
    this.draftId = draftId;
    this.mailHandler = mailHandler;

    this.defaultSubject = subject;
    this.defaultContent = content;

    if (!BeeUtils.isEmpty(attach)) {
      this.defaultAttachments = attach;
    } else {
      this.defaultAttachments = Maps.newHashMap();
    }
    for (Long id : defaultAttachments.keySet()) {
      attachments.put(defaultAttachments.get(id).getName(), id);
    }
    if (draftId == null) {
      if (to != null) {
        to.remove(sender);
      }
      if (cc != null) {
        cc.remove(sender);
      }
      if (bcc != null) {
        bcc.remove(sender);
      }
    }
    defaultRecipients.put(AddressType.TO.name(), DataUtils.buildIdList(to));
    defaultRecipients.put(AddressType.CC.name(), DataUtils.buildIdList(cc));
    defaultRecipients.put(AddressType.BCC.name(), DataUtils.buildIdList(bcc));
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof MultiSelector) {
      MultiSelector w = (MultiSelector) widget;

      for (AddressType type : AddressType.values()) {
        if (BeeUtils.same(name, type.name())) {
          w.render(defaultRecipients.get(type.name()));
          recipientWidgets.put(type.name(), w);
          break;
        }
      }
    } else if (widget instanceof Editor && BeeUtils.same(name, "Subject")) {
      subjectWidget = (Editor) widget;
      subjectWidget.setValue(defaultSubject);

    } else if (widget instanceof Editor && BeeUtils.same(name, "Message")) {
      contentWidget = (Editor) widget;
      contentWidget.setValue(defaultContent);

    } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Send")) {
      ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent ev) {
          Popup dialog = DomUtils.getParentPopup(getFormView().asWidget());
          saveMode = false;
          dialog.getOnSave().execute();
          saveMode = true;
        }
      });
    } else if (widget instanceof FileCollector && BeeUtils.same(name, "Attachments")) {
      if (!BeeUtils.isEmpty(defaultAttachments)) {
        ((FileCollector) widget).addFiles(defaultAttachments.values());
      }
      ((FileCollector) widget).bindDnd(getFormView(), getFormView().asWidget().getElement());
      ((FileCollector) widget).addSelectionHandler(new SelectionHandler<NewFileInfo>() {
        @Override
        public void onSelection(SelectionEvent<NewFileInfo> ev) {
          final String fileName = ev.getSelectedItem().getName();

          if (attachments.containsKey(fileName)) {
            attachments.remove(fileName);
          } else {
            attachments.put(fileName, 0L);

            FileUtils.upload(ev.getSelectedItem(), new Callback<Long>() {
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
      });
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new NewMessageHandler(sender, null, null, null, null, null, null, null, null);
  }

  public boolean hasChanges() {
    if (defaultRecipients.size() != recipientWidgets.size()) {
      return true;
    }
    for (String type : defaultRecipients.keySet()) {
      if (!BeeUtils.same(defaultRecipients.get(type), recipientWidgets.get(type).getValue())) {
        return true;
      }
    }
    if (!BeeUtils.same(defaultSubject, subjectWidget.getValue())) {
      return true;
    }
    if (!BeeUtils.same(defaultContent, contentWidget.getValue())) {
      return true;
    }
    if (!BeeUtils.same(DataUtils.buildIdList(defaultAttachments.keySet()),
        DataUtils.buildIdList(attachments.values()))) {
      return true;
    }
    return false;
  }

  public void save(final NotificationListener notificator) {
    if (saveMode && !hasChanges()) {
      return;
    }
    ParameterList params = MailKeeper.createArgs(SVC_SEND_MAIL);
    params.addDataItem(COL_SENDER, sender);

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
      params.addDataItem("Attachments", ids);
    }
    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(notificator);

        if (mailHandler != null) {
          mailHandler.refresh();
        }
      }
    });
  }

  public boolean validate() {
    boolean hasRecipients = false;
    String error = null;

    for (MultiSelector r : recipientWidgets.values()) {
      if (!BeeUtils.isEmpty(r.getValue())) {
        hasRecipients = true;
        break;
      }
    }
    if (!hasRecipients) {
      error = "Nurodykite gavėją(-us)";

    } else if (BeeUtils.isEmpty(subjectWidget.getValue())) {
      error = "Nurodykite temą";

    } else if (BeeUtils.isEmpty(contentWidget.getValue())) {
      error = "Laiško turinys tuščias";

    } else if (attachments.values().contains(0L)) {
      error = "Yra nebaigtų krauti prielipų";
    }
    if (!BeeUtils.isEmpty(error)) {
      getFormView().notifySevere(error);
    }
    return BeeUtils.isEmpty(error);
  }
}
