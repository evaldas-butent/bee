package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

public class AssessmentForwarderForm extends PrintFormInterceptor {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, "Income") && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(new SelectorEvent.Handler() {
        @Override
        public void onDataSelector(SelectorEvent event) {
          if (event.isOpened()) {
            FormView form = ViewHelper.getForm(getGridView());

            if (form != null) {
              event.getSelector()
                  .setAdditionalFilter(Filter.equals(COL_CARGO, form.getLongValue(COL_CARGO)));
            }
          }
        }
      });
    }
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new AssessmentPrintForm();
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentForwarderForm();
  }

}
