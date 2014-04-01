package com.butent.bee.client.dialog;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.shared.css.CssUnit;

public class ModalGrid extends Popup {

  private static final String STYLE_NAME = "bee-ModalGrid";

  private static final double MAX_WIDTH_FACTOR = 0.9;
  private static final double MAX_HEIGHT_FACTOR = 0.9;

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
        ModalGrid modalGrid = new ModalGrid(presenter.getWidget().asWidget(), dimensions);

        modalGrid.setAnimationEnabled(true);
        modalGrid.setHideOnEscape(true);

        modalGrid.center();
      }
    };
  }

  public ModalGrid(Widget widget, HasDimensions dimensions) {
    super(OutsideClick.IGNORE, STYLE_NAME);

    widget.addStyleName(STYLE_NAME + "-content");
    setWidget(widget);

    if (dimensions != null) {
      setDimensions(dimensions, MAX_WIDTH_FACTOR, MAX_HEIGHT_FACTOR);
    }
    if (ViewHelper.hasHeader(widget)) {
      enableDragging();
    }
  }

  @Override
  public String getIdPrefix() {
    return "modal-grid";
  }

  @Override
  protected boolean isCaptionEvent(NativeEvent event) {
    return isViewHeaderEvent(event);
  }
}
