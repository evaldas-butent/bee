package com.butent.bee.client.modules.cars;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class StageEditorForm extends AbstractFormInterceptor {

  private final String dataView;

  public StageEditorForm(String dataView) {
    this.dataView = Assert.notEmpty(dataView);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (Objects.equals(name, TBL_STAGES) && widget instanceof GridPanel) {
      ((GridPanel) widget).setGridInterceptor(new AbstractGridInterceptor() {
        @Override
        public boolean initDescription(GridDescription gridDescription) {
          gridDescription.setFilter(Filter.equals(COL_STAGE_VIEW, dataView));
          return super.initDescription(gridDescription);
        }

        @Override
        public GridInterceptor getInstance() {
          return null;
        }

        @Override
        public void onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {
          event.getColumns().add(DataUtils.getColumn(COL_STAGE_VIEW, gridView.getDataColumns()));
          event.getValues().add(dataView);
          super.onReadyForInsert(gridView, event);
        }
      });
    }
    if (Objects.equals(name, TBL_STAGE_CONDITIONS) && widget instanceof ChildGrid) {
      ((ChildGrid) widget).setGridInterceptor(new StageConditionsGrid());
    }
    if (Objects.equals(name, TBL_STAGE_ACTIONS) && widget instanceof ChildGrid) {
      ((ChildGrid) widget).setGridInterceptor(new StageActionsGrid());
    }
    if (Objects.equals(name, TBL_STAGE_TRIGGERS) && widget instanceof ChildGrid) {
      ((ChildGrid) widget).setGridInterceptor(new StageTriggersGrid());
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new StageEditorForm(dataView);
  }

  @Override
  public String getCaption() {
    DataInfo info = Data.getDataInfo(dataView);
    return BeeUtils.joinWords(Localized.dictionary().statuses() + ":",
        Localized.maybeTranslate(info.getCaption()),
        BeeUtils.parenthesize(ModuleAndSub.parse(info.getModule()).getModule().getCaption()));
  }
}
