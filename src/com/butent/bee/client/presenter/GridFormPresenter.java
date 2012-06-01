package com.butent.bee.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.ui.Action;

import java.util.Set;

public class GridFormPresenter extends AbstractPresenter implements HasGridView {
  
  public static final String STYLE_FORM_CONTAINER = "bee-GridFormContainer";
  public static final String STYLE_FORM_HEADER = "bee-GridFormHeader";
  public static final String STYLE_FORM_CAPTION = "bee-GridFormCaption";

  private static final String SUFFIX_EDIT = "-edit";
  private static final String SUFFIX_NEW_ROW = "-newRow";

  public static String getFormStyle(String base, boolean edit) {
    return base + (edit ? SUFFIX_EDIT : SUFFIX_NEW_ROW);
  }
  
  private final GridView gridView;
  
  private final HeaderView header;
  private final Complex container;
  
  private final boolean editSave;

  public GridFormPresenter(GridView gridView, FormView formView, String caption,
      Set<Action> actions, boolean edit, boolean editSave) {
    this.gridView = gridView;

    this.header = createHeader(caption, actions, edit);
    this.container = createContainer(this.header, formView, edit);

    this.header.setViewPresenter(this);
    
    this.editSave = editSave;
  }
  
  public FormView getForm() {
    for (Widget child : container) {
      if (child instanceof FormView) {
        return (FormView) child;
      }
    }
    return null;
  }
  
  public GridCallback getGridCallback() {
    return gridView.getGridCallback();
  }

  public GridView getGridView() {
    return gridView;
  }
  
  @Override
  public Widget getWidget() {
    return container;
  }

  public void handleAction(Action action) {
    if (action == null) {
      return;
    }
    
    switch (action) {
      case CLOSE:
        gridView.formCancel();
        break;

      case EDIT:
        gridView.getForm(true).setEnabled(true);
        hideAction(action);
        if (editSave) {
          showAction(Action.SAVE);
        }
        break;

      case SAVE:
        if (getForm().validate()) {
          gridView.formConfirm();
        }
        break;

      default:  
    }
  }
  
  public boolean hasAction(Action action) {
    return header.hasAction(action);
  }
  
  public void hideAction(Action action) {
    header.showAction(action, false);
  }
  
  public void setCaption(String caption) {
    header.setCaption(caption);
  }

  public void setMessage(String message) {
    header.setMessage(message);
  }
  
  public void showAction(Action action) {
    header.showAction(action, true);
  }

  public void showAction(Action action, boolean visible) {
    header.showAction(action, visible);
  }
  
  public void updateStyle(boolean edit) {
    container.removeStyleName(getFormStyle(STYLE_FORM_CONTAINER, !edit));
    container.addStyleName(getFormStyle(STYLE_FORM_CONTAINER, edit));

    header.removeCaptionStyle(getFormStyle(STYLE_FORM_CAPTION, !edit));
    header.addCaptionStyle(getFormStyle(STYLE_FORM_CAPTION, edit));
  }
  
  private Complex createContainer(HeaderView headerView, FormView formView, boolean edit) {
    Complex formContainer = new Complex();
    formContainer.addStyleName(STYLE_FORM_CONTAINER);
    formContainer.addStyleName(getFormStyle(STYLE_FORM_CONTAINER, edit));

    formContainer.addTopHeightFillHorizontal(headerView.asWidget(), 0, headerView.getHeight());
    formContainer.addTopBottomFillHorizontal(formView.asWidget(), headerView.getHeight(), 0);
    
    return formContainer;
  }
  
  private HeaderView createHeader(String caption, Set<Action> actions, boolean edit) {
    HeaderView formHeader = GWT.create(HeaderImpl.class);//new HeaderImpl();
    formHeader.asWidget().addStyleName(STYLE_FORM_HEADER);
    formHeader.asWidget().addStyleName(getFormStyle(STYLE_FORM_HEADER, edit));

    formHeader.create(caption, false, false, null, actions, null);
    formHeader.addCaptionStyle(STYLE_FORM_CAPTION);
    formHeader.addCaptionStyle(getFormStyle(STYLE_FORM_CAPTION, edit));
    
    return formHeader; 
  }
}
