package com.butent.bee.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;

import java.util.EnumSet;

public class NewRowPresenter extends AbstractPresenter {
  
  public static final String STYLE_CONTAINER = "bee-NewRowContainer";
  public static final String STYLE_HEADER = "bee-NewRowHeader";
  public static final String STYLE_CAPTION = "bee-NewRowCaption";

  private final Complex container;
  private HandlesActions actionDelegate = null;
  
  public NewRowPresenter(FormView formView, String caption) {
    HeaderView header = createHeader(caption);
    this.container = createContainer(header, formView);

    header.setViewPresenter(this);
    formView.setViewPresenter(this);
  }
  
  @Override
  public Widget getWidget() {
    return container;
  }

  public void handleAction(Action action) {
    if (getActionDelegate() != null) {
      getActionDelegate().handleAction(action);
    }
  }

  public void setActionDelegate(HandlesActions actionDelegate) {
    this.actionDelegate = actionDelegate;
  }
  
  private Complex createContainer(HeaderView headerView, FormView formView) {
    Complex formContainer = new Complex();
    formContainer.addStyleName(STYLE_CONTAINER);

    formContainer.addTopHeightFillHorizontal(headerView.asWidget(), 0, headerView.getHeight());
    formContainer.addTopBottomFillHorizontal(formView.asWidget(), headerView.getHeight(), 0);
    
    return formContainer;
  }
  
  private HeaderView createHeader(String caption) {
    HeaderView formHeader = GWT.create(HeaderImpl.class);
    formHeader.asWidget().addStyleName(STYLE_HEADER);

    formHeader.create(caption, false, false, null, EnumSet.of(Action.SAVE, Action.CLOSE), null);
    formHeader.addCaptionStyle(STYLE_CAPTION);
    
    return formHeader; 
  }

  private HandlesActions getActionDelegate() {
    return actionDelegate;
  }
}
