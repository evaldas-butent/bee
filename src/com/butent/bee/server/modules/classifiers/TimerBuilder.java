package com.butent.bee.server.modules.classifiers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.butent.bee.server.concurrency.*;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.*;

/**
 * General class for registering timers in modules.
 * Stores, updates, removes and cancels timers which are saved in registry.
 * For timers creation  - method buildTimers must be called.
 */
public abstract class TimerBuilder implements ConcurrencyBean.HasTimerService {

    private static BeeLogger logger = LogUtils.getLogger(TimerBuilder.class);

    private final Map<String, Multimap<String, Timer>> registry = new HashMap<>();

    @Override
    public void ejbTimeout(Timer timer) {
        if (!isRegistered(timer)) {
            logger.warning("Timer", timer.getInfo(), "not registered");
            return;
        }

        String timerInfo = (String) timer.getInfo();
        logger.info("Starting timer", timerInfo);
        onTimeout(timerInfo);
        try {
          logger.info("End timer", timerInfo, "Next timeout", timer.getNextTimeout());
        } catch (NoMoreTimeoutsException noMoreTimeoutsExceptions) {
          logger.info("End timer", timerInfo);
          for (String timerIdentifier : registry.keySet()) {
            if (BeeUtils.isPrefix(timerInfo, timerIdentifier)) {
              registry.get(timerIdentifier).removeAll(timerInfo);
              break;
            }
          }
        }
    }

    /**
     * Initiates creation of timers.
     */
    public void buildTimers(String ... timerIdentifiers) {
        if (ArrayUtils.isEmpty(timerIdentifiers)) {
            return;
        }

        for (String id : timerIdentifiers) {
            createOrUpdateTimers(id, null, null);
        }
    }

    /**
     * Executes timer methods on timer timeout.
     * @param timerInfo identifier name of timer
     */
    public abstract void onTimeout(String timerInfo);

    /**
     * General method for creating and registering timers.
     * Method can be executed at module initiation or specific data changes conditions.
     * @param viewName name of view which data have been changed.
     * @param relationId changed data id.
     */
    @Lock(LockType.WRITE)
    protected void createOrUpdateTimers(String timerIdentifier, String viewName, Long relationId) {
        Collection<Timer> timers = new ArrayList<>();
        IsCondition wh = null;

        if (viewName == null && registry.containsKey(timerIdentifier)) {
                timers = new ArrayList<>(registry.get(timerIdentifier).values());
                registry.get(timerIdentifier).clear();

        } else if (viewName != null) {
            Pair<IsCondition,  List<String>> whAndTimersId =
                    getConditionAndTimerIdForUpdate(timerIdentifier, viewName, relationId);

            if (whAndTimersId != null) {
                wh = whAndTimersId.getA();
                if (registry.get(timerIdentifier) != null) {
                  for (String timerId : whAndTimersId.getB()) {
                    timers.addAll(registry.get(timerIdentifier).removeAll(timerId));
                  }
                }
            }
        }

        if (!BeeUtils.isEmpty(timers)) {

            for (Timer timer : timers) {
                try {
                    logger.info("Canceled timer:", timer.getInfo());
                    timer.cancel();
                } catch (NoSuchObjectLocalException e) {
                    logger.warning(e);
                }
            }
        }

        List<Timer> createdTimers = createTimers(timerIdentifier, wh);

        for (Timer createdTimer : createdTimers) {
            String timerInfo = (String) createdTimer.getInfo();
            if (!registry.containsKey(timerIdentifier)) {
                registry.put(timerIdentifier, HashMultimap.create());
            }
            registry.get(timerIdentifier).put(timerInfo, createdTimer);
            logger.info("Created timer:", timerInfo, "on", createdTimer.getNextTimeout());
        }

    }

    /**
     * Creates list of timers according to timer identifier(type) and query filter conditions.
     *
     * @param wh condition filtering data for creation of timers.
     * @return created timers list, which will be added to timers registry.
     */
    protected abstract List<Timer> createTimers(String timerIdentifier, IsCondition wh);

    /**
     * Method used in timer updates to sort out which timers will be deleted and updated.
     * @param timerIdentifier  constant identifies type of timer.
     * @param viewName name of view which data have been changed.
     * @param relationId changed data id.
     * @return removable timer id and filter to query updatable timers.
     */
    protected abstract Pair<IsCondition, List<String>> getConditionAndTimerIdForUpdate(
            String timerIdentifier, String viewName, Long relationId);

    private boolean isRegistered(Timer timer) {

        for (Multimap<String, Timer> reg : registry.values()) {
            if (reg != null && reg.containsKey(timer.getInfo())) {
                return true;
            }
        }
        return false;
    }
}
