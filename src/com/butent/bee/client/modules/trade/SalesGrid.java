package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.COL_NOTES;
import static com.butent.bee.shared.modules.mail.MailConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.PRM_INVOICE_MAIL_SIGNATURE;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.richtext.RichTextEditor;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SalesGrid extends InvoicesGrid {

  private final CustomAction mailAction = new CustomAction(FontAwesome.ENVELOPE_O, click -> send());

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    mailAction.setTitle(Localized.dictionary().trWriteEmail());
    presenter.getHeader().addCommandItem(mailAction);

    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return new SalesGrid();
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    return super.isRowEditable(row)
        && SalesInvoiceForm.isEditable(row.isNull(getDataIndex(TradeConstants.COL_TRADE_EXPORTED)));
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
    Queries.getDistinctLongs(getViewName(), TradeConstants.COL_TRADE_CUSTOMER, Filter.idIn(ids),
        customers -> {
          if (customers.size() == 1) {
            sendToSingle(view, ids, BeeUtils.peek(customers));
          } else {
            sendtoAll(view, ids);
          }
        });
  }

  private void sendtoAll(GridView view, Set<Long> ids) {
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
            args.addDataItem(MailConstants.COL_CONTENT, BeeUtils.nvl(rte.getValue(), ""));

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

  private void sendToSingle(GridView view, Set<Long> ids, Long companyId) {
    mailAction.running();
    ParameterList args = TradeKeeper.createArgs(TradeConstants.SVC_SEND_INVOICES);
    args.addDataItem(TradeConstants.VAR_ID_LIST, DataUtils.buildIdList(ids));

    BeeKeeper.getRpc().makePostRequest(args, response -> {
      mailAction.idle();
      response.notify(view);

      if (response.hasErrors()) {
        return;
      }
      Map<Long, FileInfo> invoices = new HashMap<>();
      Codec.deserializeHashMap(response.getResponseAsString()).forEach((invoice, file) ->
          invoices.put(BeeUtils.toLong(invoice), FileInfo.restore(file)));

      TradeActUtils.getInvoiceEmails(companyId, emails -> NewMailMessage.create(emails, null, null,
          "Sąskaitos", null, invoices.values(), null, false, (messageId, saveMode) -> {

            BeeRowSet files = new BeeRowSet(VIEW_SALE_FILES, Data.getColumns(VIEW_SALE_FILES,
                Arrays.asList(COL_SALE, AdministrationConstants.COL_FILE, COL_NOTES,
                    MailConstants.COL_MESSAGE)));

            invoices.forEach((invoiceId, fileInfo) -> {
              BeeRow row = files.addEmptyRow();
              row.setValue(files.getColumnIndex(COL_SALE), invoiceId);
              row.setValue(files.getColumnIndex(AdministrationConstants.COL_FILE),
                  fileInfo.getId());
              row.setValue(files.getColumnIndex(COL_NOTES),
                  Format.renderDateTime(TimeUtils.nowMinutes()));
              row.setValue(files.getColumnIndex(MailConstants.COL_MESSAGE), messageId);
            });
            Queries.insertRows(files, result -> {
              Queries.update(view.getViewName(), Filter.idIn(invoices.keySet()), "IsSentToEmail",
                  BeeConst.STRING_TRUE, res -> Data.resetLocal(view.getViewName()));
            });
          }, Global.getParameterText(PRM_INVOICE_MAIL_SIGNATURE)));
    });
  }
}
