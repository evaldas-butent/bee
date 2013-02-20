package com.butent.bee.client.modules.mail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.Link;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageHandler extends AbstractFormInterceptor {

  private static final int ATTA_ID = 0;
  private static final int ATTA_NAME = 1;
  private static final int ATTA_SIZE = 2;

  private static final int ADDR_ID = 0;
  private static final int ADDR_EMAIL = 1;
  private static final int ADDR_LABEL = 2;

  private boolean isActive = false;

  private Label recipientsWidget;
  private final Multimap<String, String[]> recipients = HashMultimap.create();
  private Label attachmentsWidget;
  private final List<String[]> attachments = Lists.newArrayList();
  private Label partsWidget;
  private Widget waitingWidget;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, "Recipients") && widget instanceof Label) {
      recipientsWidget = (Label) widget;

      recipientsWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();
          final Popup popup = new Popup(OutsideClick.CLOSE, "bee-mail-RecipientsPopup");
          HtmlTable ft = new HtmlTable();
          ft.setBorderSpacing(5);
          List<Pair<String, String>> types = Lists.newArrayList();
          types.add(Pair.of(AddressType.TO.name(), "Skirta:"));
          types.add(Pair.of(AddressType.CC.name(), "Kopija:"));
          types.add(Pair.of(AddressType.BCC.name(), "Nematoma kopija:"));

          for (Pair<String, String> type : types) {
            if (recipients.containsKey(type.getA())) {
              int c = ft.getRowCount();
              ft.getCellFormatter().setStyleName(c, 0, "bee-mail-RecipientsType");
              ft.setText(c, 0, type.getB());
              FlowPanel fp = new FlowPanel();

              for (String[] address : recipients.get(type.getA())) {
                FlowPanel adr = new FlowPanel();
                adr.setStyleName("bee-mail-Recipient");
                InlineLabel nm =
                    new InlineLabel(BeeUtils.notEmpty(address[ADDR_LABEL], address[ADDR_EMAIL]));
                nm.setStyleName("bee-mail-RecipientLabel");
                adr.add(nm);

                if (!BeeUtils.isEmpty(address[ADDR_LABEL])) {
                  nm = new InlineLabel(address[ADDR_EMAIL]);
                  nm.setStyleName("bee-mail-RecipientEmail");
                  adr.add(nm);
                }
                fp.add(adr);
              }
              ft.setWidget(c, 1, fp);
            }
          }
          popup.setWidget(ft);
          popup.showRelativeTo(recipientsWidget.getElement());
        }
      });
    } else if (BeeUtils.same(name, "Attachments") && widget instanceof Label) {
      attachmentsWidget = (Label) widget;

      attachmentsWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();
          final Popup popup = new Popup(OutsideClick.CLOSE, "bee-mail-AttachmentsPopup");
          TabBar bar = new TabBar("bee-mail-AttachmentsMenu-", Orientation.VERTICAL);

          for (String[] item : attachments) {
            Link link = new Link(BeeUtils.joinWords(item[ATTA_NAME],
                BeeUtils.parenthesize(FileUtils.sizeToText(BeeUtils.toLong(item[ATTA_SIZE])))),
                FileUtils.getUrl(item[ATTA_NAME], BeeUtils.toLongOrNull(item[ATTA_ID])));

            link.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent ev) {
                popup.close();
              }
            });
            bar.addItem(link);
          }
          popup.setWidget(bar);
          popup.showRelativeTo(attachmentsWidget.getElement());
        }
      });
    } else if (widget instanceof Label && BeeUtils.same(name, "Parts")) {
      partsWidget = (Label) widget;

    } else if (BeeUtils.same(name, "WaitingForContent")) {
      waitingWidget = widget.asWidget();
    }
  }

  public void deactivate() {
    isActive = false;
    partsWidget.setVisible(isActive);
    waitingWidget.setVisible(!isActive);
    recipientsWidget.setText(null);
    attachmentsWidget.setText(null);
  }

  public Map<Long, NewFileInfo> getAttachments() {
    Map<Long, NewFileInfo> attach = Maps.newLinkedHashMap();

    for (final String[] att : attachments) {
      attach.put(BeeUtils.toLong(att[ATTA_ID]),
          new NewFileInfo(att[ATTA_NAME], BeeUtils.toLong(att[ATTA_SIZE]), null));
    }
    return attach;
  }

  public Set<Long> getBcc() {
    return getRecipients(AddressType.BCC.name());
  }

  public Set<Long> getCc() {
    return getRecipients(AddressType.CC.name());
  }

  public String getContent() {
    if (isActive) {
      return partsWidget.getElement().getInnerHTML();
    }
    return null;
  }

  @Override
  public FormInterceptor getInstance() {
    return new MessageHandler();
  }

  public String getRecipients() {
    StringBuilder sb = new StringBuilder();

    for (String[] recipient : recipients.values()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(BeeUtils.joinWords(recipient[ADDR_LABEL], "<" + recipient[ADDR_EMAIL] + ">"));
    }
    return sb.toString();
  }

  public Set<Long> getTo() {
    return getRecipients(AddressType.TO.name());
  }

  void requery(Long messageId, Long accountId, Long placeId, boolean showBcc, boolean isSeen) {
    Assert.state(DataUtils.isId(messageId));
    deactivate();

    ParameterList params = MailKeeper.createArgs(SVC_GET_MESSAGE);
    params.addDataItem(COL_MESSAGE, messageId);
    params.addDataItem(COL_ACCOUNT, accountId);
    params.addDataItem(COL_PLACE, placeId);
    params.addDataItem("showBcc", showBcc ? 1 : 0);
    params.addDataItem("markAsRead", isSeen ? 0 : 1);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getFormView());

        String[] data = Codec.beeDeserializeCollection((String) response.getResponse());

        Map<String, SimpleRowSet> packet = Maps.newHashMapWithExpectedSize(data.length / 2);

        for (int i = 0; i < data.length; i += 2) {
          packet.put(data[i], SimpleRowSet.restore(data[i + 1]));
        }
        recipients.clear();
        String txt = null;

        for (SimpleRow address : packet.get(TBL_RECIPIENTS)) {
          String[] info = new String[Ints.max(ADDR_ID, ADDR_EMAIL, ADDR_LABEL)];
          info[ADDR_ID] = address.getValue(COL_ADDRESS);
          info[ADDR_EMAIL] = address.getValue(CommonsConstants.COL_EMAIL_ADDRESS);
          info[ADDR_LABEL] = address.getValue(CommonsConstants.COL_EMAIL_LABEL);

          recipients.put(address.getValue(COL_ADDRESS_TYPE), info);
          txt = BeeUtils.join(", ", txt, BeeUtils.notEmpty(info[ADDR_LABEL], info[ADDR_EMAIL]));
        }
        recipientsWidget.setText(BeeUtils.joinWords("Skirta:", txt));

        attachments.clear();
        int cnt = 0;
        long size = 0;
        txt = null;

        for (SimpleRow attachment : packet.get(TBL_ATTACHMENTS)) {
          String[] info = new String[Ints.max(ATTA_ID, ATTA_NAME, ATTA_SIZE)];
          info[ATTA_ID] = attachment.getValue(COL_FILE);
          info[ATTA_NAME] = BeeUtils.notEmpty(attachment.getValue(COL_ATTACHMENT_NAME),
              attachment.getValue(CommonsConstants.COL_FILE_NAME));
          info[ATTA_SIZE] = attachment.getValue(CommonsConstants.COL_FILE_SIZE);

          attachments.add(info);
          cnt++;
          size += BeeUtils.toLong(info[ATTA_SIZE]);
        }
        if (cnt > 0) {
          txt = BeeUtils.joinWords(cnt,
              "prielip" + ((cnt % 10 == 0 || BeeUtils.betweenInclusive(cnt % 100, 11, 19))
                  ? "ų" : (cnt % 10 == 1 ? "as" : "ai")),
              BeeUtils.parenthesize(FileUtils.sizeToText(size)));
        }
        attachmentsWidget.setText(txt);

        String content = null;
        Element sep = Document.get().createHRElement();
        sep.setClassName("bee-mail-PartSeparator");

        for (SimpleRow part : packet.get(TBL_PARTS)) {
          txt = part.getValue(COL_HTML_CONTENT);

          if (txt == null && part.getValue(COL_CONTENT) != null) {
            Element pre = Document.get().createPreElement();
            pre.getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
            pre.setInnerHTML(part.getValue(COL_CONTENT));
            txt = pre.getString();
          }
          content = BeeUtils.join(sep.getString(), content, txt);
        }
        partsWidget.getElement().setInnerHTML(content);
        activate();
      }
    });
  }

  private void activate() {
    isActive = true;
    waitingWidget.setVisible(!isActive);
    partsWidget.setVisible(isActive);
  }

  private Set<Long> getRecipients(String type) {
    Set<Long> ids = Sets.newHashSet();

    if (isActive && recipients.containsKey(type)) {
      for (String[] r : recipients.get(type)) {
        ids.add(BeeUtils.toLongOrNull(r[ADDR_ID]));
      }
    }
    return ids;
  }
}
