package com.butent.bee.client.modules.crm;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.crm.Constants.TaskEvent;
import com.butent.bee.shared.utils.BeeUtils;

public class TaskEventHandler {

  public TaskEventHandler(TaskEvent ev, final FormView form) {
    switch (ev) {
      case EXTENDED:
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
          public void onClick(ClickEvent event) {
            Long term = BeeUtils.toLongOrNull(termWidget.getNormalizedValue());
            if (BeeUtils.isEmpty(term)) {
              Global.showError("Įveskite terminą");
              return;
            }
            if (term < form.getRowData().getLong(form.getDataIndex("FinishTime"))) {
              Global.showError("Neteisingas terminas");
              return;
            }

            String comment = commentWidget.getValue();
            if (BeeUtils.isEmpty(comment)) {
              Global.showError("Įveskite komentarą");
              return;
            }
            dialog.hide();
          }
        });
        dialog.setWidget(panel);
        dialog.center();
        break;

      default:
        Global.showError("Event usnsupported:", ev);
        break;
    }
  }
}
