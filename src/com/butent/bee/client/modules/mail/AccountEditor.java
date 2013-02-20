package com.butent.bee.client.modules.mail;

import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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

    } else if (widget instanceof HasClickHandlers
        && BeeUtils.inListSame(name, "StorePassword", "TransportPassword")) {

      ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Global.inputString("Naujas slaptažodis", null, new StringCallback(false) {
            @Override
            public void onSuccess(String value) {
              getFormView().getActiveRow().setValue(getFormView().getDataIndex(name),
                  BeeUtils.isEmpty(value) ? null : Codec.encodeBase64(value));
            }
          }, null, BeeConst.UNDEF, BeeConst.DOUBLE_UNDEF, null, BeeConst.UNDEF,
              Global.CONSTANTS.ok(), Global.CONSTANTS.cancel(), new WidgetInitializer() {
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
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return this;
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
}
