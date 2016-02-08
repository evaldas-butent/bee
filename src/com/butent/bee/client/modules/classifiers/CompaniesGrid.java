package com.butent.bee.client.modules.classifiers;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class CompaniesGrid extends AbstractGridInterceptor {

  private static final String STYLE_PREFIX = "bee-Companies-";
  private static final String STYLE_INPUT_TOGGLE = STYLE_PREFIX + "input-toggle";
  private static final String STYLE_INPUT_PANEL = STYLE_PREFIX + "input-panel";
  private static final String STYLE_LABEL_ACTIVE = STYLE_PREFIX + "label-active";
  private static final String STYLE_LABEL_DISABLED = STYLE_PREFIX + "label-disabled";
  private static final String STYLE_LABEL_ENABLED = STYLE_PREFIX + "label-enabled";

  private static final String NAME_INPUT_MODE = "InputMode";

  Toggle editFormToggle;

  @Override
  public GridInterceptor getInstance() {
    return new CompaniesGrid();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {

    if (!canEditData()) {
      return;
    }

    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    Flow inputPanel = new Flow(STYLE_INPUT_PANEL);
    Label disabled = new Label(Localized.getConstants().disabledShort());
    disabled.addStyleName(STYLE_LABEL_DISABLED);

    disabled.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (editFormToggle.isChecked()) {
          activateInputMode(false);
        }
      }
    });

    Label enabled = new Label(Localized.getConstants().enabledShort());
    enabled.addStyleName(STYLE_LABEL_ENABLED);

    enabled.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!editFormToggle.isChecked()) {
          activateInputMode(true);
        }
      }
    });
    editFormToggle = new Toggle(FontAwesome.TOGGLE_OFF, FontAwesome.TOGGLE_ON,
        STYLE_INPUT_TOGGLE, readBoolean(NAME_INPUT_MODE));

    editFormToggle.setTitle(Localized.getConstants().editMode());

    editFormToggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        activateInputMode(editFormToggle.isChecked());
      }
    });

    if (editFormToggle.isChecked()) {
      StyleUtils.setStyleName(enabled.getElement(), STYLE_LABEL_ACTIVE, true);
    } else {
      StyleUtils.setStyleName(disabled.getElement(), STYLE_LABEL_ACTIVE, true);
    }
    inputPanel.add(new Label(Localized.getConstants().editing()));
    inputPanel.add(disabled);
    inputPanel.add(editFormToggle);
    inputPanel.add(enabled);

    header.addCommandItem(inputPanel);
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (canEditData()) {
      if (editFormToggle.isChecked()) {
        event.consume();
        RowEditor.openForm(FORM_NEW_COMPANY, VIEW_COMPANIES, getActiveRow(), Opener.NEW_TAB, null);
      }
    }
  }

  private void activateInputMode(boolean modeEnabled) {
    Element root = editFormToggle.getParent().getElement();

    Element el = Selectors.getElementByClassName(root, STYLE_LABEL_DISABLED);
    if (el != null) {

      StyleUtils.setStyleName(el, STYLE_LABEL_ACTIVE, !modeEnabled);
    }

    el = Selectors.getElementByClassName(root, STYLE_LABEL_ENABLED);
    if (el != null) {
      StyleUtils.setStyleName(el, STYLE_LABEL_ACTIVE, modeEnabled);
    }

    if (editFormToggle.isChecked() != modeEnabled) {
      editFormToggle.setChecked(modeEnabled);
    }

    BeeKeeper.getStorage().set(storageKey(NAME_INPUT_MODE), modeEnabled);
  }

  private static boolean readBoolean(String name) {
    String key = storageKey(name);
    if (BeeKeeper.getStorage().hasItem(key)) {
      return BeeKeeper.getStorage().getBoolean(key);
    } else {
      return false;
    }
  }

  private static String storageKey(String name) {
    Long userId = BeeKeeper.getUser().getUserId();
    return BeeUtils.join(BeeConst.STRING_MINUS, "Companies_EditForm", userId, name);
  }

  private boolean canEditData() {
    return BeeKeeper.getUser().canEditData(getViewName());
  }
}
