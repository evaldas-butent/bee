package com.butent.bee.client.modules.transport;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;

import java.util.HashMap;
import java.util.Map;

public class CustomShipmentRequestForm {
  public static String createFileName(String report, String orderNo) {
    Map<String, String> translations = new HashMap<>();
    translations.put("en", "Contract");
    translations.put("lt", "Kontraktas");
    translations.put("ru", "Контракт");

    return translations.getOrDefault(Localized.extractLanguage(report), translations.get("en"))
        + "_" + orderNo + ".pdf";
  }

  public static String createEmailSubject(FileInfo fileInfo) {
    return fileInfo.getCaption().replace(".pdf", "");
  }
}
