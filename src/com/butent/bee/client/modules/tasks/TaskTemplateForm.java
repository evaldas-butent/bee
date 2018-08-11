package com.butent.bee.client.modules.tasks;

import static com.butent.bee.shared.modules.tasks.TaskConstants.COL_END_RESULT;

import com.butent.bee.client.composite.Relations;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.tasks.TaskUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class TaskTemplateForm extends AbstractFormInterceptor {

  private Relations relations;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_END_RESULT) && widget instanceof Button) {
      ((Button) widget).addClickHandler(clickEvent -> {
        if (relations != null) {
          TaskUtils.renderEndResult(relations.getWidgetMap(true), getFormView(), true, null);
        }
      });
    } else if (BeeUtils.same(name,
        AdministrationConstants.TBL_RELATIONS) && widget instanceof Relations) {
      relations = (Relations) widget;
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TaskTemplateForm();
  }
}
