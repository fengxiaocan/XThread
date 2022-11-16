package com.x.thread.execute;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class CacheExecutors {
    public static final String NEW_THREAD = "newThread";
    public static final String IO = "IO";
    public static final String SINGLE = "Single";
    public static final String Work = "Work";

    static Map<String, RxExecutors> cache;

    public static ExecutorService newThread(RxExecutors.Mode mode, int threadCount) {
        return mode.create(NEW_THREAD, threadCount).executor();
    }

    public static RxExecutors IO(RxExecutors.Mode mode) {
        return RxExecutors.getExecutor(IO, 2, mode);
    }

    public static RxExecutors IO(RxExecutors.Mode mode, int threadCount) {
        return RxExecutors.getExecutor(IO, threadCount, mode);
    }

    public static RxExecutors Single(RxExecutors.Mode mode) {
        return RxExecutors.getExecutor(SINGLE, 1, mode);
    }

    public static RxExecutors Work(RxExecutors.Mode mode) {
        return RxExecutors.getExecutor(Work, 5, mode);
    }

    public static RxExecutors Work(RxExecutors.Mode mode, int threadCount) {
        return RxExecutors.getExecutor(Work, threadCount, mode);
    }

    static RxExecutors getPool(String key) {
        synchronized (Object.class) {
            if (cache == null) {
                cache = new HashMap<>();
                return null;
            }
            return cache.get(key);
        }
    }

    public static void clearIO(RxExecutors.Mode mode) {
        clear(IO, mode);
    }

    public static void clearIO() {
        clear(IO, RxExecutors.Mode.Deque);
        clear(IO, RxExecutors.Mode.Priority);
        clear(IO, RxExecutors.Mode.Queue);
    }

    public static void clearSingle(RxExecutors.Mode mode) {
        clear(SINGLE, mode);
    }

    public static void clearSingle() {
        clear(SINGLE, RxExecutors.Mode.Deque);
        clear(SINGLE, RxExecutors.Mode.Priority);
        clear(SINGLE, RxExecutors.Mode.Queue);
    }

    public static void clearWore(RxExecutors.Mode mode) {
        clear(Work, mode);
    }

    public static void clearWore() {
        clear(Work, RxExecutors.Mode.Deque);
        clear(Work, RxExecutors.Mode.Priority);
        clear(Work, RxExecutors.Mode.Queue);
    }

    public static void clear(String name, RxExecutors.Mode mode) {
        synchronized (Object.class) {
            if (cache != null) {
                cache.remove(mode.prefixName(name));
                if (cache.size() == 0) {
                    cache = null;
                }
            }
        }
    }

    public static void recycler(String name, RxExecutors.Mode mode) {
        synchronized (Object.class) {
            if (cache != null) {
                RxExecutors pool = cache.remove(mode.prefixName(name));
                if (pool != null) {
                    pool.recycler();
                }
                if (cache.size() == 0) {
                    cache = null;
                }
            }
        }
    }

    public static void clearAll() {
        synchronized (Object.class) {
            if (cache != null) {
                cache.clear();
            }
            cache = null;
        }
    }

}
