package com.butent.bee.client.modules.trade.acts;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.EnumUtils;

public class TradeActForm extends AbstractFormInterceptor {

  private static final String STYLE_PREFIX = TradeKeeper.STYLE_PREFIX + "form-";

  private static final String STYLE_CREATE = STYLE_PREFIX + "create";
  private static final String STYLE_EDIT = STYLE_PREFIX + "edit";

  private static final String STYLE_HAS_SERVICES = STYLE_PREFIX + "has-services";

  private TradeActKind lastKind;

  TradeActForm() {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    TradeActKind kind = getKind(row);
    String caption;

    if (DataUtils.isNewRow(row)) {
      form.removeStyleName(STYLE_EDIT);
      form.addStyleName(STYLE_CREATE);

      caption = Localized.getConstants().tradeActNew();
      if (kind != null) {
        caption += " - " + kind.getCaption();
      }

    } else {
      form.removeStyleName(STYLE_CREATE);
      form.addStyleName(STYLE_EDIT);

      caption = (kind == null) ? Localized.getConstants().tradeAct() : kind.getCaption();
    }

    if (lastKind != kind) {
      form.setStyleName(STYLE_HAS_SERVICES, kind != null && kind.enableServices());

      if (lastKind != null) {
        form.removeStyleName(STYLE_PREFIX + lastKind.getStyleSuffix());
      }
      if (kind != null) {
        form.addStyleName(STYLE_PREFIX + lastKind.getStyleSuffix());
      }

      lastKind = kind;
    }

    if (form.getViewPresenter() != null && form.getViewPresenter().getHeader() != null) {
      form.getViewPresenter().getHeader().setCaption(caption);
    }

    super.afterRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeActForm();
  }

  private TradeActKind getKind(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return EnumUtils.getEnumByIndex(TradeActKind.class,
          row.getInteger(getDataIndex(COL_TA_KIND)));
    }
  }
}
