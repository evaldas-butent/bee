package com.butent.bee.client.modules.administration;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.PreElement;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.SVC_GET_CONFIG_DIFF;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.layout.HtmlPanel;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.Overflow;
import com.butent.bee.shared.i18n.Localized;

import java.util.HashMap;
import java.util.Map;

public class CustomConfigGrid extends AbstractGridInterceptor {

  @Override
  public void onEditStart(EditStartEvent event) {
    if (event.isReadOnly()) {
      event.consume();
      Map<String, String> data = new HashMap<>();

      for (int i = 0; i < getDataColumns().size(); i++) {
        data.put(getDataColumns().get(i).getId(), event.getRowValue().getString(i));
      }
      showDiff(data);
    } else {
      super.onEditStart(event);
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  public static void showDiff(Map<String, String> data) {
    ParameterList args = AdministrationKeeper.createArgs(SVC_GET_CONFIG_DIFF);

    for (Map.Entry<String, String> entry : data.entrySet()) {
      args.addNotEmptyData(entry.getKey(), entry.getValue());
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        PreElement pre = Document.get().createPreElement();
        pre.setInnerHTML(response.getResponseAsString());

        StyleUtils.setMaxHeight(pre, 700);
        StyleUtils.setMaxWidth(pre, 800);
        StyleUtils.setOverflow(pre, StyleUtils.ScrollBars.BOTH, Overflow.AUTO);

        Global.showModalWidget(Localized.dictionary().differences(),
            new HtmlPanel(pre.getString()));
      }
    });
  }
}
