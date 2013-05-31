package com.butent.bee.client.modules.commons;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class CommonsUtils {

  private static final String STYLE_COMPANY = "companyInfo-";
  private static final String STYLE_COMPANY_ITEM = STYLE_COMPANY + "item";
  private static final String STYLE_COMPANY_LABEL = STYLE_COMPANY + "label";

  public static void getCompanyInfo(Long companyId, final HasWidgets target) {
    Assert.notNull(target);
    target.clear();

    if (!DataUtils.isId(companyId)) {
      return;
    }
    ParameterList args = CommonsKeeper.createArgs(SVC_COMPANY_INFO);
    args.addDataItem(COL_COMPANY, companyId);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        Flow flow = new Flow();
        SimpleRow row = SimpleRowSet.restore((String) response.getResponse()).getRow(0);

        Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put(COL_NAME, null);
        cols.put(COL_CODE, Localized.constants.companyCode());
        cols.put(COL_VAT_CODE, Localized.constants.companyVATCode());
        cols.put(COL_ADDRESS, Localized.constants.address());
        cols.put(COL_PHONE, null);
        cols.put(COL_EMAIL_ADDRESS, Localized.constants.email());

        for (String col : cols.keySet()) {
          Object value;

          if (BeeUtils.same(col, COL_ADDRESS)) {
            value = BeeUtils.joinItems(row.getValue(COL_ADDRESS), row.getValue(COL_CITY),
                row.getValue(COL_COUNTRY), row.getValue(COL_POST_INDEX));

          } else if (BeeUtils.same(col, COL_PHONE)) {
            Map<String, String> phones = Maps.newLinkedHashMap();
            phones.put(COL_PHONE, Localized.constants.phone());
            phones.put(COL_MOBILE, Localized.constants.mobile());
            phones.put(COL_FAX, Localized.constants.fax());

            value = new Flow();

            for (String phone : phones.keySet()) {
              String val = row.getValue(phone);

              if (!BeeUtils.isEmpty(val)) {
                if (!BeeUtils.isEmpty(phones.get(phone))) {
                  Widget label = new Label(phones.get(phone));
                  label.setStyleName(STYLE_COMPANY_LABEL);
                  ((Flow) value).add(label);
                }
                Widget item = new Label(val);
                item.setStyleName(STYLE_COMPANY_ITEM);
                ((Flow) value).add(item);
              }
            }
            if (((Flow) value).isEmpty()) {
              value = null;
            }
          } else {
            value = row.getValue(col);
          }
          if (value != null) {
            Flow record = new Flow();
            record.setStyleName(STYLE_COMPANY + col.toLowerCase());

            if (!BeeUtils.isEmpty(cols.get(col))) {
              Widget label = new Label(cols.get(col));
              label.setStyleName(STYLE_COMPANY_LABEL);
              record.add(label);
            }
            Widget item;

            if (value instanceof Widget) {
              item = (Widget) value;
            } else if (value instanceof String && !BeeUtils.isEmpty((String) value)) {
              item = new Label((String) value);
              item.setStyleName(STYLE_COMPANY_ITEM);
            } else {
              continue;
            }
            record.add(item);
            flow.add(record);
          }
        }
        target.add(flow);
      }
    });
  }
}
