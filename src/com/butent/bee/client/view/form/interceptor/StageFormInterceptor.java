package com.butent.bee.client.view.form.interceptor;

import com.google.gwt.user.client.ui.HasWidgets;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.modules.cars.Stage;
import com.butent.bee.client.modules.cars.StageUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;

import java.util.List;
import java.util.Objects;

public interface StageFormInterceptor {

  FormView getFormView();

  HasWidgets getStageContainer();

  List<Stage> getStages();

  default void refreshStages() {
    List<Stage> stages = getStages();

    if (Objects.isNull(stages)) {
      StageUtils.initStages(getFormView().getViewName(), newStages -> {
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
        .filter(stage -> stage.hasTrigger(trigger)).findFirst()
        .ifPresent(this::updateStage);
  }

  default void updateStage(Stage stage) {
    FormView form = getFormView();

    int stageIdx = form.getDataIndex(COL_STAGE);
    int nameIdx = form.getDataIndex(COL_STAGE_NAME);

    IsRow oldRow = form.getOldRow();
    IsRow row = form.getActiveRow();

    row.setValue(stageIdx, stage.getId());
    row.setValue(nameIdx, stage.getName());

    if (DataUtils.isNewRow(row)) {
      form.refresh();
    } else {
      form.saveChanges(new RowUpdateCallback(form.getViewName()) {
        @Override
        public void onCancel() {
          row.setValue(stageIdx, oldRow.getLong(stageIdx));
          row.setValue(nameIdx, oldRow.getString(nameIdx));
          super.onCancel();
        }

        @Override
        public void onFailure(String... reason) {
          onCancel();
          super.onFailure(reason);
        }
      });
    }
  }
}
