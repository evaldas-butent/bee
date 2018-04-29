package com.butent.bee.client.modules.classifiers;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.PRM_URL;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomCompanyForm extends CompanyForm {
  CustomAction cloudAction = new CustomAction(FontAwesome.CLOUD_DOWNLOAD, e -> getCloudInfo());

  @Override
  public FormInterceptor getInstance() {
    return new CustomCompanyForm();
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    super.onStartNewRow(form, oldRow, newRow);

    String url = Global.getParameterText(PRM_URL);

    if (!BeeUtils.isEmpty(url)) {
      cloudAction.setTitle("Importuoti duomenis iš " + url);
      form.getViewPresenter().getHeader().addCommandItem(cloudAction);
    }
  }

  private void getCloudInfo() {
    ParameterList args = ClassifierKeeper.createArgs(SVC_COMPANY_INFO_FROM_CLOUD);

    for (String s : Arrays.asList(COL_COMPANY_CODE, COL_COMPANY_NAME)) {
      String value = getStringValue(s);

      if (!BeeUtils.isEmpty(value)) {
        args.addDataItem(s, value);
        break;
      }
    }
    if (!args.hasData()) {
      notifyRequired("Įmonės kodas arba pavadinimas");
      return;
    }
    makeRequest(args);
  }

  private void makeRequest(ParameterList args) {
    cloudAction.running();

    BeeKeeper.getRpc().makePostRequest(args, response -> {
      cloudAction.idle();

      if (response.hasErrors()) {
        response.notify(getFormView());
      } else {
        Map<String, String> map = Codec.deserializeLinkedHashMap(response.getResponseAsString());

        if (map.containsKey("code")) {
          map.forEach((s, s2) -> getFormView().updateCell(s, s2));
        } else {
          List<String> values = new ArrayList<>(map.keySet());

          Global.choice("Pasirinkite įmonę", null, values, value -> {
            ParameterList newArgs = ClassifierKeeper.createArgs(SVC_COMPANY_INFO_FROM_CLOUD);
            newArgs.addDataItem(COL_COMPANY_CODE, map.get(values.get(value)));

            makeRequest(newArgs);
          });
        }
      }
    });
  }
}