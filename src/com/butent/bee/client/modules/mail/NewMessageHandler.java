package com.butent.bee.client.modules.mail;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;
import java.util.Set;

public class NewMessageHandler extends AbstractFormCallback {

  private boolean saveMode = true;

  private final Map<String, String> defaultRecipients = Maps.newHashMap();
  private final String defaultSubject;
  private final String defaultContent;

  private final Map<String, MultiSelector> recipientWidgets = Maps.newHashMap();
  private Editor subjectWidget;
  private Editor contentWidget;

  private final Long sender;
  private final Long draftId;

  private final MailHandler mailHandler;

  public NewMessageHandler(Long sender, Long draftId, Set<Long> to, Set<Long> cc, Set<Long> bcc,
      String subject, String content, String attachments, MailHandler mailHandler) {

    Assert.notNull(sender);
    this.sender = sender;
    this.draftId = draftId;
    this.mailHandler = mailHandler;

    this.defaultSubject = subject;
    this.defaultContent = content;

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
  public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
    if (widget instanceof MultiSelector) {
      MultiSelector w = (MultiSelector) widget;
      w.setEditing(true);

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
    }
  }

  @Override
  public FormCallback getInstance() {
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
    }
    if (!BeeUtils.isEmpty(error)) {
      getFormView().notifySevere(error);
    }
    return BeeUtils.isEmpty(error);
  }
}
