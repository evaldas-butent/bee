package com.butent.bee.client.modules.commons;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;
import java.util.Map.Entry;

public final class CommonsUtils {

  private static final String STYLE_COMPANY = "bee-co-companyInfo";
  private static final String STYLE_COMPANY_ITEM = STYLE_COMPANY + "-item";
  private static final String STYLE_COMPANY_LABEL = STYLE_COMPANY + "-label";

  public static void getCompanyInfo(Long companyId, final Widget target) {
    Assert.notNull(target);

    if (!DataUtils.isId(companyId)) {
      return;
    }
    ParameterList args = CommonsKeeper.createArgs(SVC_COMPANY_INFO);
    args.addDataItem(COL_COMPANY, companyId);

    String locale = DomUtils.getDataProperty(target.getElement(), "locale");

    if (!BeeUtils.isEmpty(locale)) {
      args.addDataItem("locale", locale);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        Map<String, Pair<String, String>> info = Maps.newHashMap();

        for (Entry<String, String> entry : Codec.beeDeserializeMap(response.getResponseAsString())
            .entrySet()) {
          info.put(entry.getKey(), Pair.restore(entry.getValue()));
        }
        Flow flow = new Flow();
        flow.setStyleName(STYLE_COMPANY);

        for (String col : new String[] {COL_NAME, COL_COMPANY_CODE, COL_COMPANY_VAT_CODE,
            COL_ADDRESS, COL_PHONE, COL_EMAIL_ADDRESS}) {

          Flow record = new Flow();
          record.setStyleName(STYLE_COMPANY + "-" + col.toLowerCase());

          if (BeeUtils.same(col, COL_ADDRESS)) {
            String value = BeeUtils.joinItems(info.get(COL_ADDRESS).getB(),
                info.get(COL_CITY).getB(), info.get(COL_COUNTRY).getB(),
                info.get(COL_POST_INDEX).getB());

            if (!BeeUtils.isEmpty(value)) {
              Widget widget = new Label(info.get(col).getA());
              widget.setStyleName(STYLE_COMPANY_LABEL);
              record.add(widget);
              widget = new Label(value);
              widget.setStyleName(STYLE_COMPANY_ITEM);
              record.add(widget);
            }
          } else if (BeeUtils.same(col, COL_PHONE)) {
            Flow phone = new Flow();

            for (String fld : new String[] {COL_PHONE, COL_MOBILE, COL_FAX}) {
              Pair<String, String> pair = info.get(fld);

              if (!BeeUtils.isEmpty(pair.getB())) {
                Widget label = new Label(pair.getA());
                label.setStyleName(STYLE_COMPANY_LABEL);
                phone.add(label);

                Widget item = new Label(pair.getB());
                item.setStyleName(STYLE_COMPANY_ITEM);
                phone.add(item);
              }
            }
            if (!phone.isEmpty()) {
              record.add(phone);
            }
          } else {
            Pair<String, String> pair = info.get(col);

            if (!BeeUtils.isEmpty(pair.getB())) {
              Widget widget = new Label(pair.getA());
              widget.setStyleName(STYLE_COMPANY_LABEL);
              record.add(widget);
              widget = new Label(pair.getB());
              widget.setStyleName(STYLE_COMPANY_ITEM);
              record.add(widget);
            }
          }
          if (!record.isEmpty()) {
            flow.add(record);
          }
        }
        target.getElement().setInnerHTML(flow.getElement().getString());
      }
    });
  }

  private CommonsUtils() {
  }
}
