package com.butent.bee.client.modules.transport;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;

public class OrderAssessmentForm extends AbstractFormInterceptor {

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (row == null) {
      return;
    }
    header.addCommandItem(new BeeButton("Naujas vertintojas", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      }
    }));
    header.addCommandItem(new BeeButton("Naujos pajamos", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      }
    }));
    header.addCommandItem(new BeeButton("Naujos sąnaudos", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      }
    }));
  }

  @Override
  public FormInterceptor getInstance() {
    return this;
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    showAssessmentInfo(row.getId(),
        row.getLong(DataUtils.getColumnIndex(COL_CARGO, form.getDataColumns())));
    return true;
  }

  private void showAssessmentInfo(long assessorId, long cargoId) {
    final Flow income;
    final Flow expenses;
    final Flow assessments;

    Widget widget = getFormView().getWidgetByName("IncomeInfo");

    if (widget instanceof Flow) {
      income = (Flow) widget;
      income.clear();
      income.add(new BeeLabel("---"));
    } else {
      income = null;
    }
    widget = getFormView().getWidgetByName("ExpenseInfo");

    if (widget instanceof Flow) {
      expenses = (Flow) widget;
      expenses.clear();
      expenses.add(new BeeLabel("---"));
    } else {
      expenses = null;
    }
    widget = getFormView().getWidgetByName("AssessmentInfo");

    if (widget instanceof Flow) {
      assessments = (Flow) widget;
      assessments.clear();
      assessments.add(new BeeLabel("---"));
    } else {
      assessments = null;
    }
    ParameterList args = TransportHandler.createArgs(SVC_GET_ASSESSMENT_INFO);
    args.addDataItem(COL_ASSESSOR, assessorId);
    args.addDataItem(COL_CARGO, cargoId);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getFormView());

        if (response.hasErrors()) {
          return;
        }
        String[] data = Codec.beeDeserializeCollection((String) response.getResponse());

        Map<String, SimpleRowSet> packet = Maps.newHashMapWithExpectedSize(data.length / 2);

        for (int i = 0; i < data.length; i += 2) {
          packet.put(data[i], SimpleRowSet.restore(data[i + 1]));
        }
        SimpleRowSet services = packet.get(TBL_CARGO_SERVICES);

        if (services.getNumberOfRows() >= 0) {
          final String style = "bee-tr-cargoServices-";

          Map<String, String> zz = Maps.newLinkedHashMap();
          zz.put(COL_SERVICE_DATE, "Data");
          zz.put("ServiceName", "Paslauga");
          zz.put(COL_SERVICE_AMOUNT, "Suma");
          zz.put("CurrencyName", "Val.");
          zz.put("CompanyName", "Įmonė");
          zz.put("Number", "Numeris");
          zz.put("Note", "Pastaba");

          HtmlTable incomeDisplay = new HtmlTable();
          incomeDisplay.addStyleName(style + "display");
          HtmlTable expensesDisplay = new HtmlTable();
          expensesDisplay.addStyleName(style + "display");
          int colIdx = 0;

          int amountIdx = 2;
          double incomeTotal = 0.0;
          double expensesTotal = 0.0;

          for (String col : zz.keySet()) {
            Widget cap = new CustomDiv(style + "caption");
            cap.getElement().setInnerText(BeeUtils.same(col, "CompanyName")
                ? "Mokėtojas" : zz.get(col));
            incomeDisplay.setWidget(0, colIdx, cap);

            cap = new CustomDiv(style + "caption");
            cap.getElement().setInnerText(BeeUtils.same(col, "CompanyName")
                ? "Tiekėjas" : zz.get(col));
            expensesDisplay.setWidget(0, colIdx, cap);

            int incomeIdx = 0;
            int expensesIdx = 0;

            for (int i = 0; i < services.getNumberOfRows(); i++) {
              boolean expense = BeeUtils.toBoolean(services.getValue(i, COL_SERVICE_EXPENSE));

              Widget cell = new CustomDiv(style + "cell");
              cell.getElement().setInnerText(BeeUtils.same(col, COL_SERVICE_DATE)
                  ? services.getDate(i, col).toString() : services.getValue(i, col));

              if (expense) {
                if (BeeUtils.same(col, COL_SERVICE_AMOUNT)) {
                  expensesTotal += BeeUtils.unbox(services.getDouble(i, "CargoCost"));
                }
                expensesDisplay.setWidget(++expensesIdx, colIdx, cell);
              } else {
                if (BeeUtils.same(col, COL_SERVICE_AMOUNT)) {
                  incomeTotal += BeeUtils.unbox(services.getDouble(i, "CargoCost"));
                }
                incomeDisplay.setWidget(++incomeIdx, colIdx, cell);
              }
            }
            colIdx++;
          }
          if (incomeTotal > 0) {
            income.clear();
            income.add(incomeDisplay);
            incomeDisplay.setHTML(incomeDisplay.getRowCount(), amountIdx,
                BeeUtils.toString(Math.round(incomeTotal * 100.0) / 100.0));
          }
          if (expensesTotal > 0) {
            expenses.clear();
            expenses.add(expensesDisplay);
            expensesDisplay.setHTML(expensesDisplay.getRowCount(), amountIdx,
                BeeUtils.toString(Math.round(expensesTotal * 100.0) / 100.0));
          }
        }
      }
    });
  }
}
