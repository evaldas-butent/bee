package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.ImportType;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class ImportOptionsGrid extends AbstractGridInterceptor implements ClickHandler {

  private static final String STYLE_UPDATE_RATES_PREFIX = "bee-co-updateRates-";

  private final Image loading = new Image(Global.getImages().loading());
  private final Button importButton = new Button(Localized.getConstants().trImport(), this);

  @Override
  public GridInterceptor getInstance() {
    return new ImportOptionsGrid();
  }

  @Override
  public void onShow(GridPresenter presenter) {
    setImporting(false);
  }

  @Override
  public void onClick(ClickEvent event) {
    IsRow row = getGridView().getActiveRow();

    if (row != null) {
      Integer typeIndex = row.getInteger(getGridView().getDataIndex(COL_IMPORT_TYPE));
      ImportType type = EnumUtils.getEnumByIndex(ImportType.class, typeIndex);

      if (type != null) {
        ParameterList args = TransportHandler.createArgs(SVC_DO_IMPORT);
        args.addDataItem(COL_IMPORT_OPTION, row.getId());
        args.addDataItem(COL_IMPORT_TYPE, typeIndex);

        if (event.isShiftKeyDown()) {
          args.addDataItem("test", 1);
        }
        switch (type) {
          case COSTS:
            importCosts(args);
            break;

          case TRACKING:
            importTracking(args);
            break;
        }
      }
    }
  }

  private void importCosts(final ParameterList args) {
    upload(new Callback<String>() {
      @Override
      public void onSuccess(String fileName) {
        setImporting(true);
        args.addDataItem(VAR_IMPORT_FILE, fileName);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            setImporting(false);
            Assert.notNull(response);

            if (response.hasErrors()) {
              response.notify(getGridView());
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
          }
        });
      }
    });
  }

  private void importTracking(final ParameterList args) {
    Flow panel = new Flow(STYLE_UPDATE_RATES_PREFIX + "panel");

    Label lowLabel = new Label(Localized.getConstants().dateFromShort());
    lowLabel.addStyleName(STYLE_UPDATE_RATES_PREFIX + "lowLabel");
    panel.add(lowLabel);

    final InputDate lowInput = new InputDate();
    lowInput.addStyleName(STYLE_UPDATE_RATES_PREFIX + "lowInput");
    lowInput.setDate(TimeUtils.today());
    lowInput.setNullable(false);
    panel.add(lowInput);

    CustomDiv rangeSeparator = new CustomDiv(STYLE_UPDATE_RATES_PREFIX + "rangeSeparator");
    panel.add(rangeSeparator);

    Label highLabel = new Label(Localized.getConstants().dateToShort());
    highLabel.addStyleName(STYLE_UPDATE_RATES_PREFIX + "highLabel");
    panel.add(highLabel);

    final InputDate highInput = new InputDate();
    highInput.addStyleName(STYLE_UPDATE_RATES_PREFIX + "highInput");
    highInput.setDate(TimeUtils.today(1));
    highInput.setNullable(false);
    panel.add(highInput);

    CustomDiv actionSeparator = new CustomDiv(STYLE_UPDATE_RATES_PREFIX + "actionSeparator");
    panel.add(actionSeparator);

    Button submit = new Button(Localized.getConstants().trImport());
    submit.addStyleName(STYLE_UPDATE_RATES_PREFIX + "submit");
    panel.add(submit);

    final DialogBox dialog = DialogBox.create(null, STYLE_UPDATE_RATES_PREFIX + "dialog");
    dialog.setWidget(panel);

    dialog.setAnimationEnabled(true);
    dialog.setHideOnEscape(true);

    dialog.center();

    submit.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        JustDate lowDate = lowInput.getDate();
        if (lowDate == null) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().valueRequired());
          lowInput.setFocus(true);
          return;
        }

        JustDate hightDate = highInput.getDate();
        if (hightDate == null) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().valueRequired());
          highInput.setFocus(true);
          return;
        }

        if (TimeUtils.isMeq(lowDate, hightDate)) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().invalidRange(),
              BeeUtils.joinWords(lowDate, hightDate));
          return;
        }
        setImporting(true);
        dialog.close();

        args.addDataItem(VAR_DATE_LOW, lowDate.getDays());
        args.addDataItem(VAR_DATE_HIGH, hightDate.getDays());

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            setImporting(false);
            Assert.notNull(response);

            if (response.hasErrors()) {
              response.notify(getGridView());
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
          }
        });
      }
    });
  }

  private void setImporting(boolean importing) {
    HeaderView hdr = getGridPresenter().getHeader();
    hdr.clearCommandPanel();

    if (importing) {
      hdr.addCommandItem(loading);
    } else {
      hdr.addCommandItem(importButton);
    }
  }

  private static void upload(final Callback<String> fileCallback) {
    final Popup popup = new Popup(OutsideClick.CLOSE);

    final InputFile widget = new InputFile(false);
    widget.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        popup.close();
        List<NewFileInfo> files = FileUtils.getNewFileInfos(widget.getFiles());

        for (final NewFileInfo fi : files) {
          FileUtils.uploadTempFile(fi, fileCallback);
        }
      }
    });
    popup.setWidget(widget);
    popup.center();
  }
}
