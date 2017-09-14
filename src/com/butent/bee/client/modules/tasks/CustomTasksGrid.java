package com.butent.bee.client.modules.tasks;

import static com.butent.bee.shared.modules.tasks.TaskConstants.FORM_NEW_TASK_ORDER;
import static com.butent.bee.shared.modules.tasks.TaskConstants.VIEW_TASKS;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.Modality;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;

public final class CustomTasksGrid {

  private CustomTasksGrid() {
  }

  public static CustomAction getOrderAction() {
    CustomAction createOrder = new CustomAction(FontAwesome.INDENT,
        clickEvent -> {
          DataInfo dataInfo = Data.getDataInfo(VIEW_TASKS);
          BeeRow row = RowFactory.createEmptyRow(dataInfo, true);
          RowFactory.createRow(FORM_NEW_TASK_ORDER, Localized.dictionary().newOrder(),
              Data.getDataInfo(VIEW_TASKS), row, Modality.ENABLED, null);
        });

    createOrder.setTitle(Localized.dictionary().newOrder());

    return createOrder;
  }
}
