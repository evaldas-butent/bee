package com.butent.bee.client.modules.crm;

import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

class TaskSelectorHandler implements SelectorEvent.Handler {
  
  TaskSelectorHandler() {
    super();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), VIEW_TASK_TEMPLATES)) {
      handleTemplate(event);
    }
  }

  private void handleTemplate(SelectorEvent event) {
    DataSelector selector = event.getSelector();

    if (event.isClosed()) {
      selector.clearDisplay();
    }
    if (!event.isChanged()) {
      return;
    }
    
    IsRow templateRow = event.getRelatedRow();
    if (templateRow == null) {
      selector.clearDisplay();
      return;
    }

    FormView form = UiHelper.getForm(event.getSelector());
    if (form == null) {
      return;
    }

    IsRow taskRow = form.getActiveRow();
    if (taskRow == null) {
      return;
    }
    
    List<BeeColumn> templateColumns = Data.getColumns(VIEW_TASK_TEMPLATES);
    if (BeeUtils.isEmpty(templateColumns)) {
      return;
    }
    
    Set<String> updatedColumns = Sets.newHashSet();
    
    for (int i = 0; i < templateColumns.size(); i++) {
      String colName = templateColumns.get(i).getId();
      String value = templateRow.getString(i);
      
      if (BeeUtils.same(colName, COL_NAME)) {
        selector.setDisplayValue(BeeUtils.trim(value));
      } else if (!BeeUtils.isEmpty(value)) {
        int index = Data.getColumnIndex(VIEW_TASKS, colName);
        if (index >= 0 && taskRow.isNull(index)) {
          taskRow.setValue(index, value);
          if (templateColumns.get(i).isWritable()) {
            updatedColumns.add(colName);
          }
        }
      }
    }
    
    for (String colName : updatedColumns) {
      form.refreshBySource(colName);
    }
  }
}
