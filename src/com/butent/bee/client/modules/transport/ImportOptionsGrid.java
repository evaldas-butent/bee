package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.ImportType;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class ImportOptionsGrid extends AbstractGridInterceptor implements ClickHandler {

  private final Button importButton = new Button(Localized.getConstants().trImport(), this);

  @Override
  public GridInterceptor getInstance() {
    return new ImportOptionsGrid();
  }

  @Override
  public void onShow(GridPresenter presenter) {
    presenter.getHeader().clearCommandPanel();
    presenter.getHeader().addCommandItem(importButton);
  }

  @Override
  public void onClick(ClickEvent event) {
    IsRow row = getGridView().getActiveRow();

    if (row != null) {
      Integer typeIndex = row.getInteger(getGridView().getDataIndex(COL_IMPORT_TYPE));
      ImportType type = EnumUtils.getEnumByIndex(ImportType.class, typeIndex);

      if (type != null) {
        switch (type) {
          case COSTS:
            importCosts(row.getId(), typeIndex);
            break;

          case INVOICES:
            break;
        }
      }
    }
  }

  private void importCosts(final Long optionId, final Integer typeIndex) {
    upload(new Callback<Long>() {
      @Override
      public void onSuccess(Long fileId) {
        importButton.setVisible(false);
        ParameterList args = TransportHandler.createArgs(SVC_DO_IMPORT);
        args.addDataItem(COL_IMPORT_OPTION, optionId);
        args.addDataItem(COL_IMPORT_TYPE, typeIndex);
        args.addDataItem(VAR_IMPORT_FILE, fileId);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasErrors()) {
              Global.showError(Lists.newArrayList(response.getErrors()));
              return;
            }
            IsTable<?, ?> data;

            if (response.hasResponse(BeeRowSet.class)) {
              data = BeeRowSet.restore(response.getResponseAsString());
            } else {
              data = new ExtendedPropertiesData(PropertyUtils
                  .restoreExtended(response.getResponseAsString()), false);
            }
            Global.showModalGrid(null, data);

            importButton.setVisible(true);
          }
        });
      }
    });
  }

  private static void upload(final Callback<Long> fileCallback) {
    final Popup popup = new Popup(OutsideClick.CLOSE);

    final InputFile widget = new InputFile(false);
    widget.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        popup.close();
        List<NewFileInfo> files = FileUtils.getNewFileInfos(widget.getFiles());

        for (final NewFileInfo fi : files) {
          FileUtils.uploadFile(fi, fileCallback);
        }
      }
    });
    popup.setWidget(widget);
    popup.center();
  }
}
