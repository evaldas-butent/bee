package com.butent.bee.server.concurrency;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;

import com.butent.bee.server.Config;
import com.butent.bee.server.Invocation;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.ParameterEvent;
import com.butent.bee.server.modules.ParameterEventHandler;
import com.butent.bee.server.websocket.Endpoint;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.websocket.messages.LogMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class ConcurrencyBean {

  public interface HasTimerService extends TimedObject {
    TimerService getTimerService();
  }

  public interface AsynchronousRunnable extends Runnable {

    default String getId() {
      return null;
    }

    default long getTimeout() {
      return TimeUtils.MILLIS_PER_HOUR;
    }

    default void onError() {
    }
  }

  private static final class Worker extends FutureTask<Void> {

    private long start;
    private final AsynchronousRunnable runnable;

    private Worker(AsynchronousRunnable runnable) {
      super(runnable, null);
      this.runnable = runnable;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      return Objects.equals(getId(), ((Worker) o).getId());
    }

    public String getId() {
      String id = runnable.getId();
      return BeeUtils.isEmpty(id) ? runnable.toString() : id;
    }

    @Override
    public int hashCode() {
      return getId().hashCode();
    }

    public void onError() {
      runnable.onError();
    }

    @Override
    public void run() {
      start = System.currentTimeMillis();
      logger.info("Started:", this);
      super.run();
    }

    @Override
    public String toString() {
      return BeeUtils.joinWords(getId(), Integer.toHexString(hashCode()));
    }

    public boolean zombie() {
      if (BeeUtils.isLess(System.currentTimeMillis() - start, runnable.getTimeout())) {
        logger.info("Running:", this, TimeUtils.elapsedSeconds(start));
        return false;
      }
      if (!cancel(true)) {
        finish();
      }
      return true;
    }

    @Override
    protected void done() {
      boolean ok = true;

      try {
        get();
      } catch (Throwable e) {
        if (!(e instanceof CancellationException)) {
          logger.error(e, this);
        }
        ok = false;
      }
      if (ok) {
        logger.info("Ended:", this, TimeUtils.elapsedSeconds(start));
      } else {
        if (start > 0) {
          logger.info("Canceled:", this, TimeUtils.elapsedSeconds(start));
        } else {
          logger.info("Rejected:", this);
        }
        runnable.onError();
      }
      finish();
    }

    private void finish() {
      ConcurrencyBean bean = Invocation.locateRemoteBean(ConcurrencyBean.class);

      if (Objects.nonNull(bean)) {
        bean.finish(this);
      }
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(ConcurrencyBean.class);

  private final Map<String, Worker> asyncThreads = new ConcurrentHashMap<>();
  private final Queue<Worker> waitingThreads = new ConcurrentLinkedQueue<>();

  private Multimap<String, Class<? extends HasTimerService>> calendarRegistry;
  private Multimap<String, Class<? extends HasTimerService>> intervalRegistry;

  private final ReentrantLock lock = new ReentrantLock();

  @EJB
  ParamHolderBean prm;
  @EJB
  UserServiceBean usr;
  @Resource
  ManagedExecutorService executor;
  @Resource
  UserTransaction utx;

  @Lock(LockType.READ)
  public void asynchronousCall(AsynchronousRunnable runnable) {
    execute(new Worker(Assert.notNull(runnable)));
  }

  public <T extends HasTimerService> void createCalendarTimer(Class<T> handler, String parameter) {
    if (calendarRegistry == null) {
      calendarRegistry = HashMultimap.create();

      prm.registerParameterEventHandler(new ParameterEventHandler() {
        @Subscribe
        public void createTimers(ParameterEvent event) {
          String param = event.getParameter();

          if (calendarRegistry.containsKey(param)) {
            ConcurrencyBean concurrency = Invocation.locateRemoteBean(ConcurrencyBean.class);

            if (concurrency != null) {
              for (Class<? extends HasTimerService> clazz : calendarRegistry.get(param)) {
                concurrency.createCalendarTimer(clazz, param);
              }
            }
          }
        }
      });
    }
    if (!calendarRegistry.containsEntry(parameter, handler)) {
      calendarRegistry.put(parameter, handler);
    }
    TimerService timerService = removeTimer(handler, parameter);
    String hours = prm.getText(parameter);

    if (!BeeUtils.isEmpty(hours)) {
      try {
        Timer timer = timerService.createCalendarTimer(new ScheduleExpression().hour(hours),
            new TimerConfig(parameter, false));

        logger.info("Created", NameUtils.getClassName(handler), parameter, "timer on hours [",
            hours, "] starting at", timer.getNextTimeout());
      } catch (IllegalArgumentException ex) {
        if (DataUtils.isId(usr.getCurrentUserId())) {
          Endpoint.sendToUser(usr.getCurrentUserId(), LogMessage.error(ex));
        }
        logger.error(ex);
      }
    }
  }

  public <T extends HasTimerService> void createIntervalTimer(Class<T> handler, String parameter) {
    if (intervalRegistry == null) {
      intervalRegistry = HashMultimap.create();

      prm.registerParameterEventHandler(new ParameterEventHandler() {
        @Subscribe
        public void createTimers(ParameterEvent event) {
          String param = event.getParameter();

          if (intervalRegistry.containsKey(param)) {
            ConcurrencyBean concurrency = Invocation.locateRemoteBean(ConcurrencyBean.class);

            if (concurrency != null) {
              for (Class<? extends HasTimerService> clazz : intervalRegistry.get(param)) {
                concurrency.createIntervalTimer(clazz, param);
              }
            }
          }
        }
      });
    }
    if (!intervalRegistry.containsEntry(parameter, handler)) {
      intervalRegistry.put(parameter, handler);
    }
    TimerService timerService = removeTimer(handler, parameter);
    Integer minutes = prm.getInteger(parameter);

    if (BeeUtils.isPositive(minutes)) {
      try {
        Timer timer = timerService.createIntervalTimer(minutes * TimeUtils.MILLIS_PER_MINUTE,
            minutes * TimeUtils.MILLIS_PER_MINUTE, new TimerConfig(parameter, false));

        logger.info("Created", NameUtils.getClassName(handler), parameter, "timer every", minutes,
            "minutes starting at", timer.getNextTimeout());
      } catch (IllegalArgumentException ex) {
        if (DataUtils.isId(usr.getCurrentUserId())) {
          Endpoint.sendToUser(usr.getCurrentUserId(), LogMessage.error(ex));
        }
        logger.error(ex);
      }
    }
  }

  @Lock(LockType.READ)
  public void finish(Worker worker) {
    asyncThreads.remove(worker.getId());
    Worker candidate = waitingThreads.poll();

    if (Objects.nonNull(candidate)) {
      logger.info("Polling:", candidate);
      execute(candidate);
    }
  }

  @Lock(LockType.READ)
  public boolean isParameterTimer(Timer timer, Object parameter) {
    if (!Config.isInitialized()) {
      return false;
    }
    Assert.noNulls(timer, parameter);
    return Objects.equals(timer.getInfo(), parameter);
  }

  @Lock(LockType.READ)
  public void synchronizedCall(Runnable runnable) {
    Assert.notNull(runnable);
    lock.lock();

    try {
      utx.begin();
      runnable.run();
      utx.commit();
    } catch (Throwable ex) {
      logger.error(ex);

      try {
        utx.rollback();
      } catch (SystemException ex2) {
        logger.error(ex2);
      }
    } finally {
      lock.unlock();
    }
  }

  private void execute(Worker worker) {
    String id = worker.getId();
    Worker running = asyncThreads.get(id);

    if (Objects.nonNull(running) && !running.zombie()) {
      worker.onError();
      return;
    }
    if (asyncThreads.size() < maxActiveThreads()) {
      asyncThreads.put(id, worker);

      try {
        executor.execute(worker);

      } catch (Throwable e) {
        logger.error(e);

        if (!worker.cancel(true)) {
          finish(worker);
        }
      }
    } else if (!waitingThreads.contains(worker)) {
      logger.info("Queuing:", worker);
      waitingThreads.offer(worker);
    } else {
      logger.info("Waiting:", worker);
      worker.onError();
    }
  }

  private static int maxActiveThreads() {
    Integer maxThreads = BeeUtils.toInt(Config.getProperty("MaxActiveThreads"));

    if (BeeUtils.betweenInclusive(maxThreads, 1, 1000)) {
      return maxThreads;
    }
    return 25;
  }

  private <T extends HasTimerService> TimerService removeTimer(Class<T> handler,
      String parameter) {
    T bean = Assert.notNull(Invocation.locateRemoteBean(handler));
    TimerService timerService = Assert.notNull(bean.getTimerService());

    for (Timer timer : timerService.getTimers()) {
      if (isParameterTimer(timer, parameter)) {
        timer.cancel();
        logger.info("Removed", NameUtils.getClassName(handler), parameter, "timer");
        break;
      }
    }
    return timerService;
  }
}
