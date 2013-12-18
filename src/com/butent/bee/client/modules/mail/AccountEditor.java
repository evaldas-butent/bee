package com.butent.bee.client.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
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
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;
import java.util.Set;

public class AccountEditor extends AbstractFormInterceptor implements SelectorEvent.Handler {

  private static int addPropertyRow(final HtmlTable table, String property, String value) {
    int row = table.getRowCount();

    if (row == 0) {
      table.setText(row, 0, Localized.getConstants().property());
      table.getCellFormatter().setHorizontalAlignment(row, 0, TextAlign.CENTER);
      table.setText(row, 1, Localized.getConstants().value());
      table.getCellFormatter().setHorizontalAlignment(row, 1, TextAlign.CENTER);
      row++;
    }
    InputText input = new InputText();
    table.setWidget(row, 0, input);

    if (!BeeUtils.isEmpty(property)) {
      input.setValue(property);
    }
    input = new InputText();
    table.setWidget(row, 1, input);

    if (!BeeUtils.isEmpty(value)) {
      input.setValue(value);
    }
    final FaLabel delete = new FaLabel(FontAwesome.TRASH_O);
    delete.setTitle(Localized.getConstants().delete());
    delete.getElement().getStyle().setCursor(Cursor.POINTER);

    delete.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        for (int i = 1; i < table.getRowCount(); i++) {
          if (Objects.equal(delete, table.getWidget(i, 2))) {
            table.removeRow(i);
            break;
          }
        }
        if (table.getRowCount() == 1) {
          table.clear();
        }
      }
    });
    table.setWidget(row, 2, delete);
    return row;
  }

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
            editPropertyMap(name, BeeUtils.same(name, COL_TRANSPORT_PROPERTIES)
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

  private void editPropertyMap(String name, final String protocol) {
    final int index = getDataIndex(name);

    if (index == BeeConst.UNDEF) {
      return;
    }
    Map<String, String> properties = Codec.beeDeserializeMap(getActiveRow().getString(index));
    final HtmlTable table = new HtmlTable();

    for (String property : properties.keySet()) {
      addPropertyRow(table, property, properties.get(property));
    }
    FlowPanel widget = new FlowPanel();
    widget.add(table);

    FaLabel add = new FaLabel(FontAwesome.PLUS);
    add.setTitle(Localized.getConstants().actionAdd());
    add.getElement().getStyle().setCursor(Cursor.POINTER);
    StyleUtils.setTextAlign(add.getElement(), TextAlign.CENTER);

    add.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int row = addPropertyRow(table, null, null);
        UiHelper.focus(table.getWidget(row, 0));
      }
    });
    widget.add(add);

    Global.inputWidget(BeeUtils.joinWords(Localized.getConstants().properties(),
        BeeUtils.parenthesize(protocol)), widget, new InputCallback() {
      @Override
      public String getErrorMessage() {
        String error = super.getErrorMessage();

        if (BeeUtils.isEmpty(error)) {
          Set<String> values = Sets.newHashSet();

          for (int i = 1; i < table.getRowCount(); i++) {
            InputText input = (InputText) table.getWidget(i, 0);

            if (BeeUtils.isEmpty(input.getValue())) {
              error = Localized.getConstants().valueRequired();
            } else if (values.contains(BeeUtils.normalize(input.getValue()))) {
              error = Localized.getMessages()
                  .valueExists(Localized.getConstants().property(), input.getValue());
            } else {
              values.add(BeeUtils.normalize(input.getValue()));
              continue;
            }
            UiHelper.focus(input);
            break;
          }
        }
        return error;
      }

      @Override
      public void onSuccess() {
        Map<String, String> props = Maps.newLinkedHashMap();

        for (int i = 1; i < table.getRowCount(); i++) {
          props.put(((InputText) table.getWidget(i, 0)).getValue(),
              ((InputText) table.getWidget(i, 1)).getValue());
        }
        getActiveRow().setValue(index, Codec.beeSerialize(props));
      }
    });
  }
}
