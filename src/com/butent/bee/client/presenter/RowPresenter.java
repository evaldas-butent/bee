package com.butent.bee.client.presenter;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.ElementSize;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.ui.HasWidgetSupplier;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderSilverImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

public class RowPresenter extends AbstractPresenter implements Printable {
  
  private static class Container extends Complex implements HasCaption, HasWidgetSupplier {

    private final DataInfo dataInfo;
    private final long rowId;
    private final String initialCaption;
    
    private Container(DataInfo dataInfo, long rowId, String initialCaption) {
      super();
      this.dataInfo = dataInfo;
      this.rowId = rowId;
      this.initialCaption = initialCaption;
    }

    @Override
    public String getCaption() {
      String caption = DataUtils.getRowCaption(dataInfo, getForm().getActiveRow());
      if (BeeUtils.isEmpty(caption)) {
        return BeeUtils.notEmpty(initialCaption, getHeader().getCaption());
      } else {
        return caption;
      }
    }

    @Override
    public String getIdPrefix() {
      return "row-editor";
    }

    @Override
    public String getSupplierKey() {
      return RowEditor.getSupplierKey(dataInfo.getViewName(), rowId);
    }
    
    private FormView getForm() {
      for (Widget child : getChildren()) {
        if (child instanceof FormView) {
          return (FormView) child;
        }
      }
      return null;
    }

    private HeaderView getHeader() {
      for (Widget child : getChildren()) {
        if (child instanceof HeaderView) {
          return (HeaderView) child;
        }
      }
      return null;
    }
  }
  
  public static final String STYLE_CONTAINER = "bee-RowContainer";
  public static final String STYLE_HEADER = "bee-RowHeader";
  public static final String STYLE_CAPTION = "bee-RowCaption";
  
  private final Container container;
  
  private HandlesActions actionDelegate = null;
  
  public RowPresenter(FormView formView, DataInfo dataInfo, long rowId, String initialCaption,
      Set<Action> enabledActions, Set<Action> disabledActions) {

    HeaderView headerView = createHeader(formView.getCaption(), enabledActions, disabledActions);

    this.container = new Container(dataInfo, rowId, initialCaption);
    container.addStyleName(STYLE_CONTAINER);

    container.addTopHeightFillHorizontal(headerView.asWidget(), 0, headerView.getHeight());
    container.addTopBottomFillHorizontal(formView.asWidget(), headerView.getHeight(), 0);
    
    headerView.setViewPresenter(this);
    formView.setViewPresenter(this);
  }
  
  @Override
  public String getCaption() {
    return container.getCaption();
  }
  
  @Override
  public HeaderView getHeader() {
    return container.getHeader();
  }

  @Override
  public View getMainView() {
    return container.getForm();
  }
  
  @Override
  public Element getPrintElement() {
    return getWidget().asWidget().getElement();
  }

  @Override
  public IdentifiableWidget getWidget() {
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
      ElementSize.copyWithAdjustment(source, target, container.getForm().getPrintElement());
      ok = true;
    } else if (getHeader().asWidget().getElement().isOrHasChild(source)) {
      ok = container.getForm().printHeader() && getHeader().onPrint(source, target);
    } else {
      ok = true;
    }

    return ok;
  }
  
  public void setActionDelegate(HandlesActions actionDelegate) {
    this.actionDelegate = actionDelegate;
  }

  private HeaderView createHeader(String caption, Set<Action> enabledActions,
      Set<Action> disabledActions) {

    HeaderView formHeader = new HeaderSilverImpl();
    formHeader.asWidget().addStyleName(STYLE_HEADER);

    formHeader.create(caption, false, false, null, enabledActions, disabledActions);
    formHeader.addCaptionStyle(STYLE_CAPTION);
    
    return formHeader; 
  }

  private HandlesActions getActionDelegate() {
    return actionDelegate;
  }
}
