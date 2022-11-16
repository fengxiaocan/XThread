package java.util.concurrent;

public final class TPExecutorHelper {
    private TPExecutorHelper() {
        throw new UnsupportedOperationException();
    }

    public static void ensurePrestart(ThreadPoolExecutor executor){
        executor.ensurePrestart();
    }
    public static void reject(ThreadPoolExecutor executor, Runnable command){
        executor.reject(command);
    }
    public static boolean isStopped(ThreadPoolExecutor executor){
        return executor.isStopped();
    }
}
