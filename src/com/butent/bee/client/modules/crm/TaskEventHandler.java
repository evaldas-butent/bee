package com.butent.bee.client.modules.crm;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class TaskEventHandler {

  public TaskEventHandler(final TaskEvent ev, final FormView form) {
    if (BeeUtils.isEmpty(form.getRowData().getId())) {
      return;
    }
    switch (ev) {
      case COMMENTED:
        doComment(form);
        break;

      case EXTENDED:
        doExtend(form);
        break;

      default:
        Global.showError("Event usnsupported:", ev);
        break;
    }
  }

  private ParameterList createParams(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CrmConstants.CRM_MODULE);
    args.addQueryItem(CrmConstants.CRM_METHOD, name);
    // args.addQueryItem(Service.RPC_VAR_CTP, ContentType.BINARY);
    return args;
  }

  private void doComment(final FormView form) {
    final DialogBox dialog = new DialogBox("Komentaras");
    Absolute panel = new Absolute(Position.RELATIVE);

    FlexTable container = new FlexTable();
    panel.add(container);

    container.setCellSpacing(5);

    int row = 0;
    container.setWidget(row, 0, new BeeLabel("Komentaras"));
    final InputArea commentWidget = new InputArea();
    commentWidget.setVisibleLines(5);
    commentWidget.setCharacterWidth(40);
    container.setWidget(row, 1, commentWidget);

    row++;
    BeeButton button = new BeeButton("Išsaugoti");
    container.setWidget(row, 0, button);
    FlexCellFormatter formater = container.getFlexCellFormatter();
    formater.setColSpan(row, 0, 2);
    formater.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String comment = commentWidget.getValue();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.COMMENTED.name());
        args.addDataItem(CrmConstants.VAR_TASK_ID, form.getRowData().getId());
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasErrors()) {
              Global.showError((Object[]) response.getErrors());
            } else {
              dialog.hide();
              form.updateRowData(form.getRowData());
            }
          }
        });
      }
    });
    dialog.setWidget(panel);
    dialog.center();
  }

  private void doExtend(final FormView form) {
    final DialogBox dialog = new DialogBox("Termino pratęsimas");
    Absolute panel = new Absolute(Position.RELATIVE);

    FlexTable container = new FlexTable();
    panel.add(container);

    container.setCellSpacing(5);

    int row = 0;
    container.setWidget(row, 0, new BeeLabel("Pabaigos data"));
    final InputDate termWidget = new InputDate(ValueType.DATETIME);
    container.setWidget(row, 1, termWidget);

    row++;
    container.setWidget(row, 0, new BeeLabel("Komentaras"));
    final InputArea commentWidget = new InputArea();
    commentWidget.setVisibleLines(5);
    commentWidget.setCharacterWidth(40);
    container.setWidget(row, 1, commentWidget);

    row++;
    BeeButton button = new BeeButton("Išsaugoti");
    container.setWidget(row, 0, button);
    FlexCellFormatter formater = container.getFlexCellFormatter();
    formater.setColSpan(row, 0, 2);
    formater.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Long term = BeeUtils.toLongOrNull(termWidget.getNormalizedValue());
        if (BeeUtils.isEmpty(term)) {
          Global.showError("Įveskite terminą");
          return;
        }
        IsRow data = form.getRowData();
        Long oldTerm = data.getLong(form.getDataIndex("FinishTime"));

        if (term < oldTerm) {
          Global.showError("Neteisingas terminas");
          return;
        }

        String comment = commentWidget.getValue();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.EXTENDED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.DATETIME, "FinishTime"));
        rs.setViewName("Tasks");
        rs.addRow(data.getId(), data.getVersion(),
            new String[] {data.getString(form.getDataIndex("FinishTime"))});

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_TERM, term);
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasErrors()) {
              Global.showError((Object[]) response.getErrors());
            } else if (response.hasResponse(BeeRow.class)) {
              dialog.hide();
              form.updateRowData(BeeRow.restore((String) response.getResponse()));
            } else {
              Global.showError("Unknown response");
            }
          }
        });
      }
    });
    dialog.setWidget(panel);
    dialog.center();
  }
}
