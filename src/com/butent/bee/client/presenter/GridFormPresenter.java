package com.butent.bee.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;

import java.util.Set;

public class GridFormPresenter extends AbstractPresenter implements HasGridView, Printable,
    ParentRowCreator {

  private static final BeeLogger logger = LogUtils.getLogger(GridFormPresenter.class);

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

  @Override
  public boolean createParentRow(NotificationListener notificationListener,
      Callback<IsRow> callback) {
    return gridView.createParentRow(notificationListener, callback);
  }

  @Override
  public String getCaption() {
    return header.getCaption();
  }

  public FormView getForm() {
    for (Widget child : container) {
      if (child instanceof FormView) {
        return (FormView) child;
      }
    }
    return null;
  }

  public GridInterceptor getGridInterceptor() {
    return gridView.getGridInterceptor();
  }

  @Override
  public GridView getGridView() {
    return gridView;
  }

  @Override
  public HeaderView getHeader() {
    return header;
  }

  @Override
  public View getMainView() {
    return getForm();
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
    if (action == null) {
      return;
    }

    switch (action) {
      case CLOSE:
        getForm().onClose(new CloseCallback() {
          @Override
          public void onClose() {
            gridView.formCancel();
          }

          @Override
          public void onSave() {
            save();
          }
        });
        break;

      case EDIT:
        gridView.getForm(true).setEnabled(true);
        hideAction(action);
        if (editSave) {
          showAction(Action.SAVE);
        }
        break;

      case SAVE:
        save();
        break;

      case PRINT:
        Printer.print(this);
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

  public boolean isActionEnabled(Action action) {
    return header.isActionEnabled(action);
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
    HeaderView formHeader = GWT.create(HeaderImpl.class);
    formHeader.asWidget().addStyleName(STYLE_FORM_HEADER);
    formHeader.asWidget().addStyleName(getFormStyle(STYLE_FORM_HEADER, edit));

    formHeader.create(caption, false, false, null, actions, null);
    formHeader.addCaptionStyle(STYLE_FORM_CAPTION);
    formHeader.addCaptionStyle(getFormStyle(STYLE_FORM_CAPTION, edit));

    return formHeader;
  }

  private void save() {
    final FormView form = getForm();
    if (!form.validate(form, true)) {
      return;
    }

    if (gridView.isAdding() && gridView.likeAMotherlessChild()) {
      FormView parentForm = UiHelper.getForm(gridView.asWidget());
      if (parentForm == null) {
        logger.severe(getCaption(), "parent form not found");
        return;
      }

      if (parentForm.getViewPresenter() instanceof ParentRowCreator) {
        ((ParentRowCreator) parentForm.getViewPresenter()).createParentRow(form,
            new Callback<IsRow>() {
              @Override
              public void onFailure(String... reason) {
                form.notifySevere(reason);
              }

              @Override
              public void onSuccess(IsRow result) {
                if (gridView.likeAMotherlessChild()) {
                  logger.severe(getCaption(), "parent row not created");
                } else {
                  gridView.formConfirm();
                }
              }
            });

      } else {
        logger.severe(getCaption(), "cannot create parent row");
      }

    } else {
      gridView.formConfirm();
    }
  }
}
