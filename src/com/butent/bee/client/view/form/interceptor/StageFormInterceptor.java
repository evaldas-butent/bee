package com.butent.bee.client.view.form.interceptor;

import com.google.gwt.user.client.ui.HasWidgets;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.modules.cars.Stage;
import com.butent.bee.client.modules.cars.StageUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;

import java.util.List;
import java.util.Objects;

public interface StageFormInterceptor extends FormInterceptor {

  @Override
  default void beforeRefresh(FormView form, IsRow row) {
    refreshStages();
  }

  HasWidgets getStageContainer();

  List<Stage> getStages();

  default void refreshStages() {
    List<Stage> stages = getStages();

    if (Objects.isNull(stages)) {
      StageUtils.initStages(getViewName(), newStages -> {
        setStages(newStages);
        getFormView().refresh();
      });
      return;
    }
    HasWidgets container = getStageContainer();

    if (Objects.isNull(container)) {
      return;
    }
    container.clear();

    StageUtils.filterStages(stages, getFormView().getOldRow())
        .forEach(stage -> container.add(new Button(stage.getName(), () -> updateStage(stage))));
  }

  void setStages(List<Stage> stages);

  default void triggerStage(String trigger) {
    StageUtils.filterStages(getStages(), getFormView().getOldRow()).stream()
        .filter(stage -> stage.hasTrigger(trigger)).findFirst().ifPresent(this::updateStage);
  }

  default void updateStage(Stage stage) {
    FormView form = getFormView();
    IsRow row = form.getActiveRow();

    row.setValue(form.getDataIndex(COL_STAGE), stage.getId());
    row.setValue(form.getDataIndex(COL_STAGE_NAME), stage.getName());

    if (DataUtils.isNewRow(row)) {
      getFormView().refresh();
    } else {
      BeeRowSet rs = DataUtils.getUpdated(getViewName(), form.getDataColumns(), form.getOldRow(),
          row, form.getChildrenForUpdate());

      if (!DataUtils.isEmpty(rs)) {
        Queries.updateRow(rs, new RowUpdateCallback(form.getViewName()));
      }
    }
  }
}
