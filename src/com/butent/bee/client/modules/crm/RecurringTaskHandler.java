package com.butent.bee.client.modules.crm;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;

class RecurringTaskHandler extends AbstractFormInterceptor {

  private FileCollector fileCollector;
  
  RecurringTaskHandler() {
  }
  
  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof FileCollector) {
      this.fileCollector = (FileCollector) widget;
      this.fileCollector.bindDnd(getFormView());
    }
  }
  
  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    if (getFileCollector() != null && !getFileCollector().isEmpty()) {
      FileUtils.commitFiles(getFileCollector().getFiles(), VIEW_RT_FILES,
          COL_RTF_RECURRING_TASK, result.getId(), COL_RTF_FILE, COL_RTF_CAPTION);
    }

    super.afterInsertRow(result, forced);
  }
  
  @Override
  public FormInterceptor getInstance() {
    return new RecurringTaskHandler();
  }
  
  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    if (getFileCollector() != null) {
      getFileCollector().clear();
    }

    super.onStartNewRow(form, oldRow, newRow);
  }

  private FileCollector getFileCollector() {
    return fileCollector;
  }
}
