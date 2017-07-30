package com.butent.bee.client.modules.documents;

import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;

public class DocumentTemplateForm extends DocumentDataForm {

  private final Button newDocumentButton = new Button(Localized.dictionary().documentNew(),
      event -> RowFactory.createRow(TBL_DOCUMENTS, Opener.MODAL,
          row -> DocumentsHandler.copyDocumentData(getLongValue(COL_DOCUMENT_DATA),
              dataId -> Queries.update(TBL_DOCUMENTS, Filter.compareId(row.getId()),
                  COL_DOCUMENT_DATA, new LongValue(dataId), res -> {
                    Long oldId = Data.getLong(TBL_DOCUMENTS, row, COL_DOCUMENT_DATA);

                    if (DataUtils.isId(oldId)) {
                      Queries.deleteRow(VIEW_DOCUMENT_DATA, oldId);
                    }
                    RowEditor.open(TBL_DOCUMENTS, row.getId());
                  }))));

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    getHeaderView().clearCommandPanel();

    if (!DataUtils.isNewRow(row)) {
      getHeaderView().addCommandItem(newDocumentButton);
    }
    super.afterRefresh(form, row);
  }
}
