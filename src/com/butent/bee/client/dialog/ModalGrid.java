package com.butent.bee.client.dialog;

import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.CssUnit;

public class ModalGrid extends ModalView {

  public static PresenterCallback opener(int width, int height) {
    return opener(new Dimensions(width, height));
  }

  public static PresenterCallback opener(double width, CssUnit widthUnit,
      double height, CssUnit heightUnit) {
    return opener(new Dimensions(width, widthUnit, height, heightUnit));
  }

  public static PresenterCallback opener(final HasDimensions dimensions) {
    return new PresenterCallback() {
      @Override
      public void onCreate(Presenter presenter) {
        ModalGrid modalGrid = new ModalGrid(presenter, dimensions);

        modalGrid.setAnimationEnabled(true);
        modalGrid.setHideOnEscape(true);

        modalGrid.cascade();
      }
    };
  }

  public ModalGrid(Presenter presenter, HasDimensions dimensions) {
    super(presenter, BeeConst.CSS_CLASS_PREFIX + "ModalGrid", dimensions);
  }

  @Override
  public String getIdPrefix() {
    return "modal-grid";
  }
}
