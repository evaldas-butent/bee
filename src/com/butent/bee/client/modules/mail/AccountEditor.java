package com.butent.bee.client.modules.mail;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AccountEditor extends AbstractFormInterceptor implements SelectorEvent.Handler {

  @Override
  public void afterCreateWidget(final String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof DataSelector
        && BeeUtils.inListSame(name,
            SystemFolder.Sent.name() + COL_FOLDER,
            SystemFolder.Drafts.name() + COL_FOLDER,
            SystemFolder.Trash.name() + COL_FOLDER)) {

      ((DataSelector) widget).addSelectorHandler(this);

    } else if (widget instanceof HasClickHandlers) {
      if (BeeUtils.inListSame(name, COL_STORE_PASSWORD, COL_TRANSPORT_PASSWORD)) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.inputString(Localized.getConstants().mailNewAccountPassword(), null,
                new StringCallback(false) {
                  @Override
                  public void onSuccess(String value) {
                    getFormView().getActiveRow().setValue(getFormView().getDataIndex(name),
                        BeeUtils.isEmpty(value) ? null : Codec.encodeBase64(value));
                  }
                }, null, BeeConst.UNDEF, BeeConst.DOUBLE_UNDEF, null, BeeConst.UNDEF,
                Localized.getConstants().ok(), Localized.getConstants().cancel(),
                new WidgetInitializer() {
                  @Override
                  public Widget initialize(Widget inputWidget, String widgetName) {
                    if (BeeUtils.same(widgetName, DialogConstants.WIDGET_INPUT)) {
                      inputWidget.getElement().setPropertyString("type", "password");
                    }
                    return inputWidget;
                  }
                });
          }
        });
      } else if (BeeUtils.inListSame(name, COL_STORE_PROPERTIES, COL_TRANSPORT_PROPERTIES)) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            editMap(name, BeeUtils.same(name, COL_TRANSPORT_PROPERTIES)
                ? Protocol.SMTP.name() : getActiveRow().getString(getDataIndex(COL_STORE_TYPE)));
          }
        });
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new AccountEditor();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isOpened()) {
      IsRow activeRow = getFormView().getActiveRow();
      Set<Long> exclusions = Sets.newHashSet();

      for (SystemFolder folder : SystemFolder.values()) {
        exclusions.add(activeRow.getLong(getFormView().getDataIndex(folder + COL_FOLDER)));
      }
      event.consume();
      event.getSelector().getOracle().setExclusions(exclusions);
      event.getSelector().setAdditionalFilter(ComparisonFilter
          .isEqual(COL_ACCOUNT, new LongValue(activeRow.getId())));
    }
  }

  private void editMap(String name, final String protocol) {
    final int index = getDataIndex(name);

    if (index == BeeConst.UNDEF) {
      return;
    }
    final HtmlTable table = new HtmlTable();
    Map<String, String> properties = Codec.beeDeserializeMap(getActiveRow().getString(index));
    int row = 0;

    for (String property : properties.keySet()) {
      table.setWidget(row, 0, new CheckBox());
      table.setText(row, 1, property);
      InputText input = new InputText();
      input.setValue(properties.get(property));
      table.setWidget(row, 2, input);
      row++;
    }
    FlowPanel widget = new FlowPanel();
    widget.add(table);
    SimplePanel panel = new SimplePanel(new Image(Global.getImages().silverAdd(),
        new ScheduledCommand() {
          @Override
          public void execute() {
            Global.inputString(BeeUtils.joinWords(Localized.getConstants().newProperty(),
                BeeUtils.parenthesize(protocol)), new StringCallback(true) {
              @Override
              public void onSuccess(String value) {
                int idx = table.getRowCount();
                table.setWidget(idx, 0, new CheckBox());
                table.setText(idx, 1, value);
                table.setWidget(idx, 2, new InputText());
              }

              @Override
              public boolean validate(String value) {
                boolean ok = super.validate(value);

                if (ok) {
                  for (int i = 0; i < table.getRowCount(); i++) {
                    if (BeeUtils.same(value,
                        table.getCellFormatter().getElement(i, 1).getInnerText())) {
                      ok = false;
                      break;
                    }
                  }
                }
                return ok;
              }
            });
          }
        }));
    StyleUtils.setTextAlign(panel.getElement(), TextAlign.CENTER);
    widget.add(panel);

    Global.inputWidget(BeeUtils.joinWords(Localized.getConstants().properties(),
        BeeUtils.parenthesize(protocol)), widget, new InputCallback() {
      @Override
      public void onDelete(DialogBox dialog) {
        int x = table.getRowCount();
        int c = 0;
        for (int i = 0; i < x; i++) {
          int idx = i - c;
          if (BeeUtils.unbox(((CheckBox) table.getWidget(idx, 0)).getValue())) {
            table.removeRow(idx);
            c++;
          }
        }
      }

      @Override
      public void onSuccess() {
        Map<String, String> props = Maps.newLinkedHashMap();

        for (int i = 0; i < table.getRowCount(); i++) {
          props.put(table.getCellFormatter().getElement(i, 1).getInnerText(),
              ((InputText) table.getWidget(i, 2)).getValue());
        }
        getActiveRow().setValue(index, Codec.beeSerialize(props));
      }
    }, null, null, EnumSet.of(Action.DELETE));
  }
}
