package com.butent.bee.client.view;

import com.butent.bee.client.output.Printable;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.ui.HasCaption;

import java.util.List;

/**
 * Requires implementing classes to have methods for creating forms and getting their content.
 */

public interface FormContainerView extends View, Printable, HasCaption {

  void bind();

  void create(FormDescription formDescription, List<BeeColumn> dataColumns, int rowCount,
      FormCallback callback);

  FormView getContent();
  
  HeaderView getHeader();  
}
