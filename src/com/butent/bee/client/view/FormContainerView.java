package com.butent.bee.client.view;

import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.BeeColumn;

import java.util.List;

public interface FormContainerView extends View {

  void bind();

  void create(FormDescription formDescription, List<BeeColumn> dataColumns, int rowCount);

  FormView getContent();
}
