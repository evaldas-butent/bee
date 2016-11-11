package com.butent.bee.client.modules.cars;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.COL_PHOTO;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class PhotoHandler extends AbstractFormInterceptor implements ClickHandler {

  private Image image;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (Objects.equals(name, COL_PHOTO) && widget instanceof Image) {
      image = (Image) widget;
      image.addClickHandler(this);
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (image != null) {
      String url;
      Long fileId = DataUtils.getLong(form.getDataColumns(), row, COL_PHOTO);

      if (DataUtils.isId(fileId)) {
        url = FileUtils.getUrl(fileId);
      } else {
        url = "images/logo.png";
      }
      image.setUrl(url);
    }
    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new PhotoHandler();
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    InputFile inputFile = new InputFile(false);

    DialogBox dialog = Global.inputWidget(Localized.dictionary().photo(), inputFile, () ->
        onSetPhoto(BeeUtils.peek(FileUtils.getNewFileInfos(inputFile.getFiles()))));

    inputFile.addChangeHandler(changeEvent -> {
      dialog.close();
      onSetPhoto(BeeUtils.peek(FileUtils.getNewFileInfos(inputFile.getFiles())));
    });
  }

  private void onSetPhoto(NewFileInfo fileInfo) {
    Long id = getActiveRowId();
    int idx = getFormView().getDataIndex(COL_PHOTO);

    if (fileInfo != null) {
      FileUtils.uploadFile(fileInfo, fileId -> {
        if (Objects.equals(id, getActiveRowId())) {
          getActiveRow().setValue(idx, fileId);
          getFormView().refresh();
        }
      });
    } else {
      getActiveRow().clearCell(idx);
      getFormView().refresh();
    }
  }
}
