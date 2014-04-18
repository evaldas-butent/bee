package com.butent.bee.client.output;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Timer;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XWorkbook;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

public final class Exporter {

  private static final BeeLogger logger = LogUtils.getLogger(Exporter.class);

  private static final String EXPORT_URL = "export";

  private static final int SUBMIT_TIMEOUT = 60_000;

  public static void export(XSheet sheet, String fileName) {
    if (sheet == null || sheet.isEmpty()) {
      logger.severe(NameUtils.getClassName(Exporter.class), "sheet is empty");

    } else {
      XWorkbook wb = new XWorkbook();
      wb.add(sheet);
      export(wb, fileName);
    }
  }

  public static void export(XWorkbook wb, final String fileName) {
    if (wb == null || wb.isEmpty()) {
      logger.severe(NameUtils.getClassName(Exporter.class), "workbook is empty");

    } else if (BeeUtils.isEmpty(fileName)) {
      logger.severe(NameUtils.getClassName(Exporter.class), "file name not specified");

    } else {
      String frameName = NameUtils.createUniqueName("frame");

      final IFrameElement frame = Document.get().createIFrameElement();

      frame.setName(frameName);
      frame.setSrc(Keywords.URL_ABOUT_BLANK);

      BodyPanel.conceal(frame);

      final FormElement form = Document.get().createFormElement();

      form.setAcceptCharset(BeeConst.CHARSET_UTF8);
      form.setMethod(Keywords.METHOD_POST);
      form.setAction(GWT.getHostPageBaseURL() + EXPORT_URL);
      form.setTarget(frameName);

      form.appendChild(createFormParameter(Service.RPC_VAR_SVC, Service.EXPORT_WORKBOOK));
      form.appendChild(createFormParameter(Service.VAR_FILE_NAME, fileName));
      form.appendChild(createFormParameter(Service.VAR_DATA, wb.serialize()));

      BodyPanel.conceal(form);
      form.submit();

      Timer timer = new Timer() {
        @Override
        public void run() {
          form.removeFromParent();
          frame.removeFromParent();
          logger.debug("export", fileName, "cleared");
        }
      };

      timer.schedule(SUBMIT_TIMEOUT);
    }
  }

  public static void confirmExport(final XSheet sheet, String defFileName) {
    StringCallback callback = new StringCallback() {
      @Override
      public void onSuccess(String value) {
        export(sheet, BeeUtils.trim(value));
      }
    };
    
    int width = BeeUtils.resize(BeeUtils.trim(defFileName).length(), 20, 100, 300, 600);

    Global.inputString(Localized.getConstants().exportToMsExcel(),
        Localized.getConstants().fileName(), callback, defFileName, 200, null,
        width, CssUnit.PX, BeeConst.UNDEF,
        Action.EXPORT.getCaption(), Action.CANCEL.getCaption(), null);
  }

  private static InputElement createFormParameter(String name, String value) {
    InputElement inputElement = Document.get().createHiddenInputElement();

    inputElement.setName(name);
    inputElement.setValue(value);

    return inputElement;
  }

  private Exporter() {
  }
}
