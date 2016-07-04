package com.butent.bee.shared.modules.discussions;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiscussionsUtils {

  private static final long MEGABYTE_IN_BYTES = 1024 * 1024;

  public static List<Long> getDiscussionMembers(IsRow row, List<BeeColumn> columns) {
    List<Long> users = new ArrayList<>();

    Long owner = row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns));
    if (owner != null) {
      users.add(owner);
    }

    List<Long> members = DataUtils.parseIdList(row.getProperty(PROP_MEMBERS));

    for (Long member : members) {
      if (!users.contains(member)) {
        users.add(member);
      }
    }

    return users;
  }

  public static Map<String, String> getDiscussionsParameters(IsRow formRow) {
    if (formRow == null) {
      return new HashMap<>();
    }

    if (BeeUtils.isEmpty(formRow.getProperty(PROP_PARAMETERS))) {
      return new HashMap<>();
    }

    return Codec.deserializeMap(formRow.getProperty(PROP_PARAMETERS));
  }

  public static int getMarkCount(long markId, Long commentId, SimpleRowSet marksStats) {
    int result = 0;

    if (marksStats == null) {
      return result;
    }

    for (String[] row : marksStats.getRows()) {
      if ((markId == BeeUtils
          .unbox(BeeUtils.toLongOrNull(row[marksStats.getColumnIndex(COL_MARK)])))
          && (BeeUtils.unbox(commentId) == BeeUtils.unbox(BeeUtils.toLongOrNull(row[marksStats
              .getColumnIndex(COL_COMMENT)])))) {
        result++;
      }
    }

    return result;
  }

  public static SimpleRowSet getMarkData(IsRow formRow) {
    if (formRow == null) {
      return null;
    }

    if (BeeUtils.isEmpty(formRow.getProperty(PROP_MARK_DATA))) {
      return null;
    }

    return SimpleRowSet.restore(formRow.getProperty(PROP_MARK_DATA));
  }

  public static List<String> getMarkStats(Long commentId, SimpleRowSet marksStats) {
    List<String> result = new ArrayList<>();

    if (marksStats == null) {
      return result;
    }

    for (String[] row : marksStats.getRows()) {
      if (BeeUtils.unbox(commentId) == BeeUtils.unbox(BeeUtils.toLongOrNull(row[marksStats
          .getColumnIndex(COL_COMMENT)]))) {
        String text = BeeUtils.joinWords(
            row[marksStats.getColumnIndex(ClassifierConstants.COL_FIRST_NAME)],
            row[marksStats.getColumnIndex(ClassifierConstants.COL_LAST_NAME)]);
        text += BeeConst.STRING_COMMA + BeeConst.STRING_SPACE
            + Localized.maybeTranslate(row[marksStats.getColumnIndex(COL_MARK_NAME)]);

        result.add(text);
      }
    }

    return result;
  }

  public static BeeRowSet getMarkTypes(IsRow formRow) {
    if (formRow == null) {
      return null;
    }

    if (BeeUtils.isEmpty(formRow.getProperty(PROP_MARK_TYPES))) {
      return null;
    }

    return BeeRowSet.restore(formRow.getProperty(PROP_MARK_TYPES));
  }

  public static boolean isFileSizeLimitExceeded(long uploadFileSize, Long checkParam) {
    if (checkParam == null) {
      return false;
    }
    if (checkParam <= 0) {
      return false;
    }

    return uploadFileSize > (checkParam * MEGABYTE_IN_BYTES);
  }

  public static boolean isForbiddenExtention(String fileExtention, String fileExtentionList) {
    if (BeeUtils.isEmpty(fileExtention) || BeeUtils.isEmpty(fileExtentionList)) {
      return false;
    }

    String[] extentions = BeeUtils.split(fileExtentionList, BeeConst.CHAR_SPACE);
    return BeeUtils.inListSame(fileExtention, null, null, extentions);
  }

  public static boolean isMarked(long markId, long userId, Long commentId,
      SimpleRowSet marksStats) {
    boolean result = false;

    if (marksStats == null) {
      return result;
    }

    for (String[] row : marksStats.getRows()) {
      result =
          result
              || (markId == BeeUtils.unbox(BeeUtils.toLongOrNull(row[marksStats
                  .getColumnIndex(COL_MARK)]))
                  && (userId == BeeUtils.unbox(BeeUtils.toLongOrNull(row[marksStats
                      .getColumnIndex(AdministrationConstants.COL_USER)])))
                  && (BeeUtils.unbox(commentId) == BeeUtils.unbox(BeeUtils
                  .toLongOrNull(row[marksStats.getColumnIndex(COL_COMMENT)]))));
    }

    return result;
  }

  public static boolean hasOneMark(Long userId, Long commentId, SimpleRowSet marksStats) {
    boolean result = false;

    if (marksStats == null) {
      return result;
    }

    for (String[] row : marksStats.getRows()) {
      result =
          result
              || ((BeeUtils.unbox(userId) == BeeUtils.unbox(BeeUtils.toLongOrNull(row[marksStats
                  .getColumnIndex(AdministrationConstants.COL_USER)])))
              && (BeeUtils.unbox(commentId) == BeeUtils.unbox(BeeUtils.toLongOrNull(
                  row[marksStats
                      .getColumnIndex(COL_COMMENT)]))));
    }

    return result;
  }

  public static boolean sameMembers(IsRow oldRow, IsRow newRow) {
    if (oldRow == null || newRow == null) {
      return false;
    } else {
      return DataUtils
          .sameIdSet(oldRow.getProperty(PROP_MEMBERS), newRow.getProperty(PROP_MEMBERS));
    }
  }

  private DiscussionsUtils() {
  }
}
