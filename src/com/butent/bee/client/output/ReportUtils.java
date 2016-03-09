package com.butent.bee.client.output;

import static com.butent.bee.shared.Service.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.widget.Frame;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;

public final class ReportUtils {

  public static void getPdf(String html, Consumer<FileInfo> responseConsumer) {
    ParameterList args = new ParameterList(CREATE_PDF);
    args.addDataItem(VAR_REPORT_DATA, Assert.notEmpty(html));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        responseConsumer.accept(FileInfo.restore(response.getResponseAsString()));
      }
    });
  }

  public static void getPdfReport(String report, Map<String, String> parameters, BeeRowSet data,
      Consumer<FileInfo> reportConsumer) {

    Assert.notNull(reportConsumer);
    makeRequest(report, "pdf", parameters, data, (repInfo) -> reportConsumer.accept(repInfo));
  }

  public static void preview(FileInfo repInfo) {
    preview(repInfo, null);
  }

  public static void preview(FileInfo repInfo, InputCallback callback) {
    String url = FileUtils.getUrl(repInfo.getId(), repInfo.getName());

    Frame frame = new Frame(url);

    StyleUtils.setWidth(frame, BeeKeeper.getScreen().getWidth() * 0.7, CssUnit.PX);
    StyleUtils.setHeight(frame, BeeKeeper.getScreen().getHeight() * 0.9, CssUnit.PX);

    if (callback != null) {
      Global.inputWidget(Localized.getConstants().preview(), frame, callback);
    } else {
      Global.showModalWidget(Localized.getConstants().preview(), frame);
    }
  }

  public static void showReport(String report, Map<String, String> parameters, BeeRowSet data) {
    makeRequest(report, "pdf", parameters, data, (repInfo) -> preview(repInfo));
  }

  private static void makeRequest(String report, String format,
      Map<String, String> parameters, BeeRowSet data, Consumer<FileInfo> responseConsumer) {

    ParameterList args = new ParameterList(GET_REPORT);
    args.addDataItem(VAR_REPORT, Assert.notEmpty(report));
    args.addNotEmptyData(VAR_REPORT_FORMAT, format);
    args.addDataItem(VAR_REPORT_PARAMETERS, Codec.beeSerialize(parameters));

    if (data != null) {
      args.addDataItem(VAR_REPORT_DATA, Codec.beeSerialize(data));
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (!response.hasErrors()) {
          responseConsumer.accept(FileInfo.restore(response.getResponseAsString()));
        }
      }
    });
  }

  private ReportUtils() {
  }
}
