package com.butent.bee.client.modules.classifiers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class CompaniesGrid extends AbstractGridInterceptor {

  private static final String NAME_INPUT_MODE = "InputMode";

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action.equals(Action.EDIT_MODE)) {
      if (readBoolean(NAME_INPUT_MODE)) {
        BeeKeeper.getStorage().set(storageKey(NAME_INPUT_MODE), false);
      } else {
        BeeKeeper.getStorage().set(storageKey(NAME_INPUT_MODE), true);
      }
    }
    return true;
  }

  @Override
  public GridInterceptor getInstance() {
    return new CompaniesGrid();
  }

  @Override
  public FaLabel getGridMenuIcon() {

    if (readBoolean(NAME_INPUT_MODE)) {
      return new FaLabel(FontAwesome.TOGGLE_ON);
    } else {
      return new FaLabel(FontAwesome.TOGGLE_OFF);
    }
  }

  @Override
  public Label getGridMenuLabel() {

    if (readBoolean(NAME_INPUT_MODE)) {
      return new Label(Localized.getConstants().previewMode());
    } else {
      return new Label(Localized.getConstants().editMode());
    }
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (readBoolean(NAME_INPUT_MODE)) {
      event.consume();
      RowEditor.openForm(FORM_NEW_COMPANY, VIEW_COMPANIES, getActiveRow(), Opener.NEW_TAB, null);
    }
  }

  private static boolean readBoolean(String name) {
    String key = storageKey(name);
    return BeeKeeper.getStorage().hasItem(key);
  }

  private static String storageKey(String name) {
    Long userId = BeeKeeper.getUser().getUserId();
    return BeeUtils.join(BeeConst.STRING_MINUS, "Companies_EditForm", userId, name);
  }
}
