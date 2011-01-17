package com.butent.bee.server.datasource.base;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataException;
import com.butent.bee.shared.data.Reasons;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.logging.Logger;

public class DataSourceParameters {

  private static Logger logger = Logger.getLogger(DataSourceParameters.class.getName());

  private static final String REQUEST_ID_PARAM_NAME = "reqId";
  private static final String SIGNATURE_PARAM_NAME = "sig";
  private static final String OUTPUT_TYPE_PARAM_NAME = "out";
  private static final String RESPONSE_HANDLER_PARAM_NAME = "responseHandler";
  private static final String REQUEST_OUTFILENAME_PARAM_NAME = "outFileName";

  private static final String DEFAULT_ERROR_MSG = "Internal error";

  public static DataSourceParameters getDefaultDataSourceParameters() {
    DataSourceParameters dsParams = null;
    try {
      dsParams = new DataSourceParameters(null);
    } catch (DataException e) {
      Assert.untouchable();
    }
    return dsParams;
  }
  private String tqxValue = null;
  private String requestId = null;
  private String signature = null;
  private OutputType outputType = OutputType.defaultValue();
  private String responseHandler = "google.visualization.Query.setResponse";

  private String outFileName = "data.csv";

  public DataSourceParameters(String tqxValue) throws DataException {
    if (BeeUtils.isEmpty(tqxValue)) {
      return;
    }

    this.tqxValue = tqxValue;

    String[] parts = tqxValue.split(";");

    for (String part : parts) {
      String[] nameValuePair = part.split(":");
      if (nameValuePair.length != 2) {
        LogUtils.severe(logger, "Invalid name-value pair: " + part);
        throw new DataException(Reasons.INVALID_REQUEST,
            DEFAULT_ERROR_MSG + "(malformed)");
      }

      String name = nameValuePair[0];
      String value = nameValuePair[1];

      if (name.equals(REQUEST_ID_PARAM_NAME)) {
        requestId = value;
      } else if (name.equals(SIGNATURE_PARAM_NAME)) {
        signature = value;
      } else if (name.equals(OUTPUT_TYPE_PARAM_NAME)) {
        outputType = OutputType.findByCode(value);
        if (outputType == null) {
          outputType = OutputType.defaultValue();
        }
      } else if (name.equals(RESPONSE_HANDLER_PARAM_NAME)) {
        responseHandler = value;
      } else if (name.equals(REQUEST_OUTFILENAME_PARAM_NAME)) {
        outFileName = value;

        if (!outFileName.contains(".")) {
          outFileName += ".csv";
        }
      }
    }
  }

  public String getOutFileName() {
    return outFileName;
  }

  public OutputType getOutputType() {
    return outputType;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getResponseHandler() {
    return responseHandler;
  }

  public String getSignature() {
    return signature;
  }

  public String getTqxValue() {
    return tqxValue;
  }

  public void setOutputType(OutputType outputType) {
    this.outputType = outputType;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }
}
