package com.butent.bee.client.modules.mail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.SVC_GET_MESSAGE;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_ATTACHMENTS;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_PARTS;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_RECIPIENTS;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

public class MessageHandler extends AbstractFormCallback {

  private static final int NAME = 0;
  private static final int SIZE = 1;
  private static final int HASH = 2;

  private Label recipientsWidget;
  private final Multimap<String, Pair<String, String>> recipients = HashMultimap.create();
  private Label attachmentsWidget;
  private final List<String[]> attachments = Lists.newArrayList();
  private Widget partsWidget;

  @Override
  public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, "Recipients") && widget instanceof Label) {
      recipientsWidget = (Label) widget;

      recipientsWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();
          final Popup popup = new Popup(true, true, "bee-Message-RecipientsPopup");
          FlexTable ft = new FlexTable();
          ft.setCellSpacing(5);
          List<Pair<String, String>> types = Lists.newArrayList();
          types.add(Pair.of("to", "Skirta:"));
          types.add(Pair.of("cc", "Kopija:"));
          types.add(Pair.of("bcc", "Nematoma kopija:"));

          for (Pair<String, String> type : types) {
            if (recipients.containsKey(type.getA())) {
              int c = ft.getRowCount();
              ft.getCellFormatter().setStyleName(c, 0, "bee-Message-RecipientsCaption");
              ft.setText(c, 0, type.getB());
              FlowPanel fp = new FlowPanel();

              for (Pair<String, String> address : recipients.get(type.getA())) {
                FlowPanel adr = new FlowPanel();
                adr.setStyleName("bee-Message-Recipient");
                InlineLabel nm = new InlineLabel(BeeUtils.notEmpty(address.getA(), address.getB()));
                nm.setStyleName("bee-Message-AddressName");
                adr.add(nm);

                if (!BeeUtils.isEmpty(address.getA())) {
                  nm = new InlineLabel(address.getB());
                  nm.setStyleName("bee-Message-AddressMail");
                  adr.add(nm);
                }
                fp.add(adr);
              }
              ft.setWidget(c, 1, fp);
            }
          }
          popup.setWidget(ft);
          popup.showRelativeTo(recipientsWidget);
        }
      });
    } else if (BeeUtils.same(name, "Attachments") && widget instanceof Label) {
      attachmentsWidget = (Label) widget;

      attachmentsWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();
          final Popup popup = new Popup(true, true, "bee-Message-AttachmentsPopup");
          TabBar bar = new TabBar("bee-Message-AttachmentsMenu-", Orientation.VERTICAL);

          for (String[] item : attachments) {
            bar.addItem(BeeUtils.joinWords(item[NAME], BeeUtils.parenthesize(item[SIZE])));
          }
          bar.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> ev) {
              popup.hide();
              String[] info = attachments.get(ev.getSelectedItem());

              Window.open(GWT.getModuleBaseURL() + "file/"
                  + Codec.encodeBase64(Codec.beeSerialize(Pair.of(info[NAME], info[HASH]))),
                  null, null);
            }
          });
          popup.setWidget(bar);
          popup.showRelativeTo(attachmentsWidget);
        }
      });
    } else if (BeeUtils.same(name, "Parts")) {
      partsWidget = widget;
    }
  }

  @Override
  public FormCallback getInstance() {
    return new MessageHandler();
  }

  @Override
  public void onStartEdit(FormView form, IsRow row) {
    recipientsWidget.setText(null);
    attachmentsWidget.setText(null);
    partsWidget.getElement().setInnerText("Waiting for content...");

    ParameterList params = MailKeeper.createArgs(SVC_GET_MESSAGE);
    params.addDataItem("id", row.getId());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.isTrue(response.hasResponse());

        String[] data = Codec.beeDeserializeCollection((String) response.getResponse());

        Map<String, SimpleRowSet> packet = Maps.newHashMapWithExpectedSize(data.length / 2);

        for (int i = 0; i < data.length; i += 2) {
          packet.put(data[i], SimpleRowSet.restore(data[i + 1]));
        }
        recipients.clear();
        String txt = null;

        for (Map<String, String> recipient : packet.get(TBL_RECIPIENTS)) {
          Pair<String, String> names = Pair.of(recipient.get("Label"), recipient.get("Email"));
          recipients.put(BeeUtils.normalize(recipient.get("Type")), names);
          txt = BeeUtils.join(", ", txt, BeeUtils.notEmpty(names.getA(), names.getB()));
        }
        recipientsWidget.setText(BeeUtils.joinWords("Skirta:", txt));

        attachments.clear();
        int cnt = 0;
        long size = 0;
        txt = null;

        for (Map<String, String> attachment : packet.get(TBL_ATTACHMENTS)) {
          long sz = BeeUtils.toLong(attachment.get("Size"));

          String[] info = new String[Ints.max(NAME, SIZE, HASH)];
          info[NAME] = BeeUtils.notEmpty(attachment.get("FileName"), attachment.get("Name"));
          info[SIZE] = sizeToText(sz);
          info[HASH] = attachment.get("Hash");

          attachments.add(info);
          cnt++;
          size += sz;
        }
        if (cnt > 0) {
          txt = BeeUtils.joinWords(cnt,
              "prielip" + ((cnt % 10 == 0 || BeeUtils.betweenInclusive(cnt % 100, 11, 19))
                  ? "ų" : (cnt % 10 == 1 ? "as" : "ai")),
              BeeUtils.parenthesize(sizeToText(size)));
        }
        attachmentsWidget.setText(txt);

        String content = null;

        for (Map<String, String> part : packet.get(TBL_PARTS)) {
          txt = part.get("HtmlContent");

          if (txt == null && part.get("Content") != null) {
            txt = "<pre>" + part.get("Content") + "</pre>";
          }
          content = BeeUtils.join("<hr class=\"bee-Message-PartSeparator\">", content, txt);
        }
        partsWidget.getElement().setInnerHTML(content);
      }
    });
  }

  private String sizeToText(long size) {
    String prfx = "MB";
    long c = 1;

    for (int i = 1; i < 3; i++) {
      if (size < c * 1024) {
        prfx = (i == 1) ? "B" : "KB";
        break;
      }
      c *= 1024;
    }
    if (c == 1) {
      return size + prfx;
    }
    return Math.round(size * 10d / c) / 10d + prfx;
  }
}
