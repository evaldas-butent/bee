package com.butent.bee.client.output;

import static com.butent.bee.shared.Service.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
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
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
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

  public static void getPdfReport(String report, Consumer<FileInfo> reportConsumer,
      Map<String, String> parameters, BeeRowSet... data) {

    makeRequest(report, "pdf", Assert.notNull(reportConsumer), parameters, data);
  }

  public static void preview(FileInfo repInfo) {
    preview(repInfo, null);
  }

  public static void preview(FileInfo repInfo, Consumer<FileInfo> callback) {
    String url = FileUtils.getUrl(repInfo.getId(), BeeUtils.notEmpty(repInfo.getCaption(),
        repInfo.getName()));

    Frame frame = new Frame(url);

    StyleUtils.setWidth(frame, BeeKeeper.getScreen().getWidth() * 0.7, CssUnit.PX);
    StyleUtils.setHeight(frame, BeeKeeper.getScreen().getHeight() * 0.9, CssUnit.PX);

    if (callback != null) {
      Global.inputWidget(Localized.dictionary().preview(), frame, () -> callback.accept(repInfo));
    } else {
      Global.showModalWidget(Localized.dictionary().preview(), frame);
    }
  }

  public static void showReport(String report, Consumer<FileInfo> callback,
      Map<String, String> parameters, BeeRowSet... data) {

    getPdfReport(report, repInfo -> preview(repInfo, callback), parameters, data);
  }

  private static void makeRequest(String report, String format, Consumer<FileInfo> responseConsumer,
      Map<String, String> parameters, BeeRowSet... data) {

    ParameterList args = new ParameterList(GET_REPORT);
    args.addDataItem(VAR_REPORT, Assert.notEmpty(report));
    args.addNotEmptyData(VAR_REPORT_FORMAT, format);
    args.addDataItem(VAR_REPORT_PARAMETERS, Codec.beeSerialize(parameters));

    if (!ArrayUtils.isEmpty(data)) {
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
