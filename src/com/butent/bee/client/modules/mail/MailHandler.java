package com.butent.bee.client.modules.mail;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.utils.BeeUtils;

public class MailHandler extends AbstractFormCallback {

  private static final int INBOX = 0;
  private static final int SENT = 1;
  private static final int DRAFTS = 2;
  private static final int TRASH = 3;

  private int currentMode = INBOX;

  private TabBar displayMode;
  private GridPanel messages;

  @Override
  public void afterCreate(FormView form) {
    if (displayMode != null) {
      displayMode.selectTab(currentMode, false);
    }
  }

  @Override
  public void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback) {
    if (widget instanceof TabBar && BeeUtils.same(name, "DisplayMode")) {
      displayMode = (TabBar) widget;

      displayMode.addSelectionHandler(new SelectionHandler<Integer>() {
        @Override
        public void onSelection(SelectionEvent<Integer> ev) {
          Assert.notNull(messages);
          Filter flt = null;
          currentMode = ev.getSelectedItem();

          switch (currentMode) {
            case INBOX:
              flt = ComparisonFilter.isEqual("Recipient", new TextValue("test1@butent.lt"));
              break;

            case SENT:
              flt = ComparisonFilter.isEqual("Email", new TextValue("test1@butent.lt"));
              break;

            case DRAFTS:
              break;

            case TRASH:
              flt = Filter.isFalse();
              break;
          }
          messages.getPresenter().getDataProvider().setParentFilter("vvv", flt);
          messages.getPresenter().refresh(false);
        }
      });
    } else if (widget instanceof GridPanel && BeeUtils.same(name, "Messages")) {
      messages = (GridPanel) widget;
    }
  }

  @Override
  public FormCallback getInstance() {
    return new MailHandler();
  }
}
