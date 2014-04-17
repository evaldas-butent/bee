package com.butent.bee.client.output;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.InputElement;

import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XWorkbook;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

public final class Exporter {

  private static final BeeLogger logger = LogUtils.getLogger(Exporter.class);

  private static final String EXPORT_URL = "export";

  public static void export(XSheet sheet, String fileName) {
    if (sheet == null || sheet.isEmpty()) {
      logger.severe(NameUtils.getClassName(Exporter.class), "sheet is empty");

    } else {
      XWorkbook wb = new XWorkbook();
      wb.add(sheet);
      export(wb, fileName);
    }
  }

  public static void export(XWorkbook wb, String fileName) {
    if (wb == null || wb.isEmpty()) {
      logger.severe(NameUtils.getClassName(Exporter.class), "workbook is empty");

    } else if (BeeUtils.isEmpty(fileName)) {
      logger.severe(NameUtils.getClassName(Exporter.class), "file name not specified");

    } else {
      final FormElement form = Document.get().createFormElement();

      form.setAcceptCharset(BeeConst.CHARSET_UTF8);
      form.setMethod(Keywords.METHOD_POST);
      form.setAction(GWT.getHostPageBaseURL() + EXPORT_URL);

      form.appendChild(createFormParameter(Service.RPC_VAR_SVC, Service.EXPORT_WORKBOOK));
      form.appendChild(createFormParameter(Service.VAR_FILE_NAME, fileName));
      form.appendChild(createFormParameter(Service.VAR_DATA, wb.serialize()));

      BodyPanel.conceal(form);
      form.submit();

      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          form.removeFromParent();
        }
      });
    }
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
