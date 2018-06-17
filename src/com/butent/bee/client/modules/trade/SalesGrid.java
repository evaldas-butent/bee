package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.mail.MailConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.PRM_INVOICE_MAIL_SIGNATURE;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.richtext.RichTextEditor;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SalesGrid extends InvoicesGrid {

  private final CustomAction mailAction = new CustomAction(FontAwesome.ENVELOPE_O, click -> send());

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    mailAction.setTitle(Localized.dictionary().trWriteEmail());
    presenter.getHeader().addCommandItem(mailAction);
  }

  @Override
  public GridInterceptor getInstance() {

    return new SalesGrid();
  }

  public void send() {
    GridView view = getGridView();
    final Set<Long> ids = new HashSet<>();

    for (RowInfo row : view.getSelectedRows(GridView.SelectedRows.ALL)) {
      ids.add(row.getId());
    }
    if (ids.isEmpty()) {
      view.notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }
    Queries.getRowSet(TBL_SIGNATURES, Collections.singletonList(COL_SIGNATURE_CONTENT),
        Filter.and(Filter.equals(COL_USER, BeeKeeper.getUser().getUserId()),
            Filter.equals(COL_SIGNATURE_NAME, Global.getParameterText(PRM_INVOICE_MAIL_SIGNATURE))),
        result -> {
          RichTextEditor rte = new RichTextEditor(true);
          rte.setValue(result.getNumberOfRows() > 0 ? result.getString(0, 0) : "");
          StyleUtils.setSize(rte, BeeUtils.toInt(BeeKeeper.getScreen().getWidth() * 0.5),
              BeeUtils.toInt(BeeKeeper.getScreen().getHeight() * 0.5));

          Global.inputWidget("Siunčiamų sąskaitų laiško turinys", rte, () -> {
            mailAction.running();
            ParameterList args = TradeKeeper.createArgs(TradeConstants.SVC_SEND_INVOICES);
            args.addDataItem(TradeConstants.VAR_ID_LIST, DataUtils.buildIdList(ids));
            args.addNotEmptyData(MailConstants.COL_CONTENT, rte.getValue());

            BeeKeeper.getRpc().makePostRequest(args, response -> {
              mailAction.idle();
              response.notify(view);

              if (!response.hasErrors()) {
                Data.resetLocal(view.getViewName());
              }
            });
          });
        });
  }
}
