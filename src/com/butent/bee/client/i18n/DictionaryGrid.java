package com.butent.bee.client.i18n;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.administration.AdministrationKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.HashSet;
import java.util.Set;

public class DictionaryGrid extends AbstractGridInterceptor {

  private static final BeeLogger logger = LogUtils.getLogger(GridFactory.class);

  private final boolean authoritah;

  public DictionaryGrid() {
    this.authoritah = BeeKeeper.getUser().hasAuthoritah();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (hasAuthoritah() && presenter != null && presenter.getHeader() != null) {
      presenter.getHeader().clearCommandPanel();

      Button save = new Button("Save to Properties");
      save.setTitle(SVC_DICTIONARY_DATABASE_TO_PROPERTIES);

      save.addClickHandler(event -> Global.confirm("Save to Properties", Icon.ALARM,
          Lists.newArrayList("O RLY?"), () -> BeeKeeper.getRpc().makeRequest(
              AdministrationKeeper.createArgs(SVC_DICTIONARY_DATABASE_TO_PROPERTIES),
              new ResponseCallback() {
                @Override
                public void onResponse(ResponseObject response) {
                  if (!response.hasErrors()) {
                    logger.debug(response.getResponse());
                  }
                }
              })));

      presenter.getHeader().addCommandItem(save);
    }

    super.afterCreatePresenter(presenter);
  }

  @Override
  public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
      boolean rowMode) {

    if (column != null) {
      SupportedLocale locale = null;

      for (SupportedLocale supportedLocale : SupportedLocale.values()) {
        if (BeeUtils.same(supportedLocale.getDictionaryCustomColumnName(), column.getId())) {
          locale = supportedLocale;
          break;
        }
      }

      if (locale != null) {
        ParameterList params = BeeKeeper.getRpc().createParameters(Service.CUSTOMIZE_DICTIONARY);
        params.addQueryItem(VAR_LOCALE, locale.getLanguage());
        params.setSummary(column.getId());

        BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (response.hasResponse()) {
              Localized.setGlossary(Codec.deserializeHashMap(response.getResponseAsString()));
            }
          }
        });
      }
    }
  }

  @Override
  public Set<Action> getDisabledActions(Set<Action> defaultActions) {
    if (hasAuthoritah()) {
      return super.getDisabledActions(defaultActions);

    } else {
      Set<Action> actions = new HashSet<>();
      actions.add(Action.ADD);
      actions.add(Action.DELETE);

      if (!BeeUtils.isEmpty(defaultActions)) {
        actions.addAll(defaultActions);
      }

      return actions;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new DictionaryGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (!hasAuthoritah() && event != null && !event.isReadOnly()) {
      boolean ok = false;

      for (SupportedLocale supportedLocale : SupportedLocale.values()) {
        if (event.hasSource(supportedLocale.getDictionaryCustomColumnName())) {
          ok = true;
          break;
        }
      }

      if (!ok) {
        event.consume();
      }
    }
  }

  private boolean hasAuthoritah() {
    return authoritah;
  }
}
