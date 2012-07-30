package com.butent.bee.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;

import java.util.EnumSet;

public class RowPresenter extends AbstractPresenter implements Printable {
  
  public static final String STYLE_CONTAINER = "bee-RowContainer";
  public static final String STYLE_HEADER = "bee-RowHeader";
  public static final String STYLE_CAPTION = "bee-RowCaption";

  private final Complex container;
  private final HeaderView header;
  
  private HandlesActions actionDelegate = null;
  
  public RowPresenter(FormView formView) {
    this.header = createHeader(formView.getCaption());
    this.container = createContainer(header, formView);

    header.setViewPresenter(this);
    formView.setViewPresenter(this);
  }
  
  @Override
  public Element getPrintElement() {
    return getWidget().getElement();
  }

  @Override
  public Widget getWidget() {
    return container;
  }

  @Override
  public void handleAction(Action action) {
    if (getActionDelegate() != null) {
      getActionDelegate().handleAction(action);
    }
  }
  
  @Override
  public boolean onPrint(Element source, Element target) {
    boolean ok;

    if (container.getId().equals(source.getId())) {
      StyleUtils.setSize(target, source.getClientWidth(), source.getClientHeight());
      ok = true;
    } else if (header.asWidget().getElement().isOrHasChild(source)) {
      ok = header.onPrint(source, target);
    } else {
      ok = true;
    }

    return ok;
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
