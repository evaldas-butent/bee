package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.COL_CONTACT;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

public class TradeActValidator {

    /**
     * Checks can assign rent project for this act
     * @param notify notification listener to notify error message
     * @param act act to test
     * @return true if is act valid and can asign act
     */
    public static boolean canAssignRentProject(NotificationListener notify, IsRow act, String viewName) {
        boolean result;
        boolean canNotify = notify != null;

        // 1. Act can not be member of rent project;
        result = !DataUtils.isId(getRentProject(act, viewName));

        if (!result && canNotify) {
            notify.notifyWarning("Aktas priklauso nuomos projektui ID:"
                    + getRentProject(act, viewName));
            return false;
        }

        // 2. Act must has kind
        // 2.1. Act can not be continuous act
        // 2.2. Act can not be rent project itself
        // 2.3. Kind of act must support return items
        result &= getKind(act, viewName) != null && !isContinuousAct(act, viewName)
                && !isRentProjectAct(act, viewName)  && getKind(act, viewName).enableReturn();

        if (!result && canNotify) {
            notify.notifyWarning("Aktas " + act.getId()
                    + (getKind(act, viewName) != null ? " yra " + getKind(act, viewName).getCaption() : ""));
            return false;
        }

        // 3. Act can not have reference to continuous act
        result &= !DataUtils.isId(getContinuousAct(act, viewName));
        if (!result && canNotify) {
            notify.notifyWarning("Aktas " + act.getId() + " susietas su tęstiniu "
                    + getContinuousAct(act, viewName));
            return false;
        }

        return result;
    }

    /**
     * Checks Series and Company match between acts
     * @param notify notification listener to notify error message
     * @param act1 first act to compare
     * @param act2 second act to compare
     * @return true if act series and company data equals
     */
    public static boolean isSeriesAndCompanyMatch(NotificationListener notify, IsRow act1, IsRow act2, String viewName) {

        if (!Objects.equals(getSeries(act1, viewName), getSeries(act2,viewName))
                || !Objects.equals(getCompany(act1, viewName), getCompany(act2, viewName))) {
            notify.notifyWarning(Localized.dictionary().taIsDifferent());
            return false;
        }

        return true;
    }

    public static boolean validateTradeActForm(NotificationListener notify, GridView itemsGrid, IsRow row, String viewName) {
        return  checkContactField(notify, row, viewName)
                && checkDateWithRentProject(notify, row, viewName)
                && checkObject(notify, row, viewName)
                && checkRegistrationNumber(notify, row, viewName)
                && checkStatuses(notify, itemsGrid, row, viewName);
    }

    private static boolean checkContactField(NotificationListener notify, IsRow row, String viewName) {
        boolean valid = true;
        boolean canNotify = notify != null;
        if (DataUtils.isId(getCompany(row, viewName)) && !isReturnAct(row, viewName)) {
            valid = BeeUtils.unbox(getContactPhysical(row, viewName)) || DataUtils.isId(getContact(row, viewName));
        }

        if (!valid && canNotify) {
            notify.notifySevere(Localized.dictionary().contact() + " "
                    + Localized.dictionary().valueRequired());
        }

        return valid;
    }

    private static boolean checkDateWithRentProject(NotificationListener notify, IsRow row, String viewName) {
        boolean valid = !DataUtils.isId(getRentProject(row, viewName))
                || TimeUtils.isMeq(getDate(row, viewName), getRentProjectDate(row, viewName));
        boolean canNotify = notify != null;

        if (!valid && canNotify) {
            notify.notifySevere(Localized.dictionary().invalidDate(), Localized.dictionary().taDate(),
                    "Data privalo būti vėlesnė už nuomos aktą");
        }

        return valid;
    }

    private static boolean checkObject(NotificationListener notify, IsRow row, String viewName) {
        /* All objects is valid except*/
        boolean valid = !DataUtils.isId(getRentProject(row, viewName));
        boolean canNotify = notify != null;

        if (valid) {
            return true;
        }

        /* There is not match with Rent act project object*/
        valid = Objects.equals(getObject(row,viewName), getRentProjectObject(row, viewName));

        if (!valid && canNotify) {
            notify.notifySevere(Localized.dictionary().object(), "Nesutampa su nuomos aktu",
                    BeeUtils.parenthesize(getRentProjectObjectName(row, viewName)));
        }

        return valid;
    }

    private static boolean checkRegistrationNumber(NotificationListener notify, IsRow row, String viewName) {
        boolean valid = !isReturnAct(row, viewName) || !BeeUtils.isEmpty(getRegistrationNo(row, viewName));
        boolean canNotify = notify != null;
        if (!valid && canNotify) {
            notify.notifySevere(Localized.dictionary().taRegistrationNo() + " "
                    + Localized.dictionary().valueRequired());
        }

        return valid;
    }

    private static boolean checkStatuses(NotificationListener notify, GridView itemsGrid, IsRow row, String viewName) {
        TradeActKind kind = getKind(row, viewName);
        boolean canNotify = notify != null;
        boolean valid = kind != null;

        if (!valid && canNotify) {
            notify.notifySevere(Localized.dictionary().valueRequired(), Localized.dictionary().taKind());
        }

        // Check TA has return actions
        if (valid && !kind.enableReturn()) {
            return true;
        }

        // checks is TA statuses is not approved or completed
        if (valid && !isStatusPhaseApproved(row, viewName) && !isStatusPhaseCompleted(row, viewName)) {
            return true;
        }

        if (!valid) {
            return false;
        }

        // check is TA items all returned
        valid = isAllItemsReturned(row, viewName, itemsGrid);

        if (!valid && canNotify) {
            notify.notifySevere("Akto būsena", getStatusName(row, viewName), "negalima. Yra negrąžintų prekių");
        }

        return valid;
    }

    private static Long getContact(IsRow row, String viewName) {
        return Data.getLong(viewName,row, COL_CONTACT);
    }

    private static Long getContract(IsRow row, String viewName) {
        return Data.getLong(viewName,row, COL_TA_CONTRACT);
    }

    private static Long getCompany(IsRow row, String viewName) {
        return Data.getLong(viewName,row, COL_TA_COMPANY);
    }

    private static Boolean getContactPhysical(IsRow row, String viewName) {
        return Data.getBoolean(viewName,row, ALS_CONTACT_PHYSICAL);
    }

    private static Long getContinuousAct(IsRow row, String viewName) {
        return Data.getLong(viewName,row, COL_TA_CONTINUOUS);
    }

    private static DateTime getDate(IsRow row, String viewName) {
        return Data.getDateTime(viewName, row, COL_TA_DATE);
    }

    private static TradeActKind getKind(IsRow row, String viewName) {
        return TradeActKeeper.getKind(viewName, row);
    }

    public static Long getObject(IsRow row, String viewName) {
        return Data.getLong(viewName, row, COL_TA_OBJECT);
    }

    private static boolean getOperationReturnedItems(IsRow row, String viewName) {
        return BeeUtils.unbox(Data.getBoolean(viewName, row, ALS_OPERATION_RETURNED_ITEMS));
    }

    private static String getRegistrationNo(IsRow row, String viewName) {
        return Data.getString(viewName, row, COL_TA_REGISTRATION_NO);
    }

    private static Long getRentProject(IsRow row, String viewName) {
        return Data.getLong(viewName, row, COL_TA_RENT_PROJECT);
    }

    private static DateTime getRentProjectDate(IsRow row, String viewName) {
        return Data.getDateTime(viewName, row, ALS_RENT_PROJECT_DATE);
    }

    private static Long getRentProjectObject(IsRow row, String viewName) {
        return Data.getLong(viewName, row, ALS_RENT_PROJECT_OBJECT);
    }

    private static String getRentProjectObjectName(IsRow row, String viewName) {
        return Data.getString(viewName, row, ALS_RENT_PROJECT_OBJECT_NAME);
    }

    private static Long getSeries(IsRow row, String viewName) {
        return Data.getLong(viewName, row, COL_TA_SERIES);
    }

    private static String getStatusName(IsRow row, String viewName) {
        return Data.getString(viewName, row, COL_STATUS_NAME);
    }

    private static boolean isAllItemsReturned(IsRow row, String viewName, GridView itemsGrid) {
        boolean operationReturnedItems = getOperationReturnedItems(row, viewName);

        if (!operationReturnedItems) {
            return true;
        }

        if (itemsGrid == null || !VIEW_TRADE_ACT_ITEMS.equals(itemsGrid.getViewName()) || itemsGrid.isEmpty()) {
            return true;
        }

        int qtyIndex = itemsGrid.getDataIndex(COL_TRADE_ITEM_QUANTITY);
        double remained = BeeConst.DOUBLE_ZERO;
        QuantityReader quantityReader = new QuantityReader(qtyIndex);

        for (IsRow itemRow : itemsGrid.getRowData()) {
            Double amount = quantityReader.apply(itemRow);
            if (BeeUtils.isDouble(amount)) {
                remained += amount;
            }
        }

        double part = Math.pow(10, BeeUtils.unbox(itemsGrid.getDataInfo().getColumnScale(COL_TRADE_ITEM_QUANTITY)) + 1);
        double fraction = 1D /  part;

        return Math.abs(BeeConst.DOUBLE_ZERO - remained) < fraction;
    }

    private static boolean isContinuousAct(IsRow row, String viewName) {
        return TradeActKind.CONTINUOUS.equals(getKind(row, viewName));
    }

    private static boolean isRentProjectAct(IsRow row, String viewName) {
        return TradeActKind.RENT_PROJECT.equals(getKind(row, viewName));
    }

    private static boolean isReturnAct(IsRow row, String viewName) {
        return TradeActKind.RETURN.equals(getKind(row,viewName));
    }

    private static boolean isStatusPhaseApproved(IsRow row, String viewName) {
        return BeeUtils.unbox(Data.getBoolean(viewName, row, TradeDocumentPhase.APPROVED.getStatusColumnName()));
    }

    private static boolean isStatusPhaseCompleted(IsRow row, String viewName) {
        return BeeUtils.unbox(Data.getBoolean(viewName, row, TradeDocumentPhase.COMPLETED.getStatusColumnName()));
    }
}
