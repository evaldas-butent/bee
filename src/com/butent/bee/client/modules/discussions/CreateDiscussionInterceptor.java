package com.butent.bee.client.modules.discussions;

import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;

class CreateDiscussionInterceptor extends AbstractFormInterceptor {

  CreateDiscussionInterceptor() {
    super();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
  }

  @Override
  public FormInterceptor getInstance() {
    return new CreateDiscussionInterceptor();
  }

}
