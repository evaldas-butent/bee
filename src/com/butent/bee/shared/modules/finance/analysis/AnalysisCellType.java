package com.butent.bee.shared.modules.finance.analysis;

import com.google.common.base.Splitter;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;

public class AnalysisCellType {

  private static final BeeLogger logger = LogUtils.getLogger(AnalysisCellType.class);

  private static final char VALUE_SEPARATOR = ',';
  private static final char SCALE_SEPARATOR = '.';

  private static final Splitter VALUE_SPLITTER =
      Splitter.on(VALUE_SEPARATOR).omitEmptyStrings().trimResults();

  public static List<AnalysisCellType> decode(String input) {
    List<AnalysisCellType> result = new ArrayList<>();

    if (!BeeUtils.isEmpty(input)) {
      String code;
      String decimals;

      for (String item : VALUE_SPLITTER.split(input)) {
        if (BeeUtils.contains(item, SCALE_SEPARATOR)) {
          code = BeeUtils.getPrefix(item, SCALE_SEPARATOR);
          decimals = BeeUtils.getSuffix(item, SCALE_SEPARATOR);
        } else {
          code = item;
          decimals = null;
        }

        AnalysisValueType avt = AnalysisValueType.parse(code);
        if (avt == null) {
          logger.warning(input, "cannot parse",
              NameUtils.getClassName(AnalysisValueType.class), code);

        } else if (BeeUtils.isDigit(decimals)) {
          result.add(new AnalysisCellType(avt, BeeUtils.toInt(decimals)));

        } else {
          result.add(new AnalysisCellType(avt));
        }
      }
    }

    return result;
  }

  public static String encode(List<AnalysisCellType> list) {
    if (BeeUtils.isEmpty(list)) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    for (AnalysisCellType item : list) {
      if (item != null) {
        if (sb.length() > 0) {
          sb.append(VALUE_SEPARATOR);
        }

        AnalysisValueType avt = item.getAnalysisValueType();
        sb.append(avt.getCode());

        if (avt.hasScale()) {
          int scale = item.getScale();
          if (!BeeConst.isUndef(scale) && scale != avt.getDefaultScale()) {
            sb.append(SCALE_SEPARATOR).append(scale);
          }
        }
      }
    }

    return (sb.length() > 0) ? sb.toString() : null;
  }

  public static boolean needsActual(List<AnalysisCellType> list) {
    if (!BeeUtils.isEmpty(list)) {
      for (AnalysisCellType item : list) {
        if (item != null && item.getAnalysisValueType().needsActual()) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean needsBudget(List<AnalysisCellType> list) {
    if (!BeeUtils.isEmpty(list)) {
      for (AnalysisCellType item : list) {
        if (item != null && item.getAnalysisValueType().needsBudget()) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean needsBudget(String input) {
    return needsBudget(decode(input));
  }

  public static List<AnalysisCellType> normalize(List<AnalysisCellType> list) {
    if (BeeUtils.isEmpty(list)) {
      List<AnalysisCellType> result = new ArrayList<>();
      result.add(new AnalysisCellType(AnalysisValueType.DEFAULT));
      return result;

    } else {
      return list;
    }
  }

  private final AnalysisValueType analysisValueType;
  private int scale;

  public AnalysisCellType(AnalysisValueType analysisValueType) {
    this(analysisValueType, BeeConst.UNDEF);
  }

  public AnalysisCellType(AnalysisValueType analysisValueType, int scale) {
    this.analysisValueType = analysisValueType;
    this.scale = scale;
  }

  public AnalysisValueType getAnalysisValueType() {
    return analysisValueType;
  }

  public int getScale() {
    return scale;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }
}
