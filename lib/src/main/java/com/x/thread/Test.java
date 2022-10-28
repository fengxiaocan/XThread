package com.x.thread;

import com.x.thread.execute.ExecutorProvide;
import com.x.thread.function.Observer;
import com.x.thread.function.RxFuture;
import com.x.thread.function.Worker;
import com.x.thread.thread.BinaryQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) {
//        final AtomicInteger atomicInteger = new AtomicInteger(1);
//        final BinaryQueue<Info> queue = new BinaryQueue<>();
//
//        final Random random = new Random();
//
//        final AtomicInteger ok = new AtomicInteger(0);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 100; i++) {
//                    queue.offer(new Info(atomicInteger.getAndIncrement()));
//                    int i1 = random.nextInt(50) + 10;
//                    try {
//                        Thread.sleep(i1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                ok.incrementAndGet();
//            }
//        }).start();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 100; i++) {
//                    queue.offer(new Info(atomicInteger.getAndIncrement()));
//                    int i1 = random.nextInt(50) + 10;
//                    try {
//                        Thread.sleep(i1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                ok.incrementAndGet();
//            }
//        }).start();
//
//        new Thread(() -> {
//            try {
//                Thread.sleep(500);
//                System.err.println("插入0");
//                queue.offer(new Info(0), true);
//
//                Thread.sleep(1500);
//                System.err.println("插入-1");
//                queue.offer(new Info(-1), true);
//
//                Thread.sleep(500);
//                System.err.println("插入-2");
//                queue.offer(new Info(-2), true);
//
//
//                Thread.sleep(100);
//                System.err.println("插入-9");
//                queue.offer(new Info(-9), true);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();

//        while (true){
//            if (ok.get()>=2){
//                break;
//            }
//        }
        int add = 0;
//        Object[] array = queue.toArray();
//        for (Object o : array) {
//            System.err.println("info:" + ((Info) o).i);
//            add++;
//        }

//        Iterator<Info> iterator = queue.iterator();
//        while (iterator.hasNext()){
//            int i = iterator.next().i;
//            iterator.remove();
//            System.err.println("info:" + i);
//            add++;
//        }
//
//        ArrayList<Info> list = new ArrayList<>();
//        int size = queue.drainTo(list);
//        System.err.println("size:" + size);
//        for (Info o : list) {
//            System.err.println("当前为:" + o.i);
//            add++;
//        }
//        Info info = null;
//        while (true) {
//            try {
//                if ((info = queue.poll(5, TimeUnit.SECONDS)) == null) break;
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            add++;
//            if (info != null) {
//                System.err.println("当前为:" + info.i);
//            }
////            try {
////                Thread.sleep(500);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 1000, 2, TimeUnit.SECONDS, new PriorityBlockingQueue<>());
        executor.allowCoreThreadTimeOut(true);

//        ExecutorProvide provide = RxThread.executor();
        for (int i = 0; i < 50; i++) {
            final int index = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.err.println("run=" + index + " name:" + Thread.currentThread().getName());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
//        System.err.println("开始");
//        RxThreadPool.work().setKeepAliveTime(1000);
//        RxFuture<Long> future = RxThread.range(1L,100L).subscribe(new Observer() {
//            @Override
//            public void onCancel() {
//                System.err.println("onCancel=" + Thread.currentThread().getName());
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.err.println("onError=" + e.getMessage());
//            }
//
//            @Override
//            public void onComplete() {
//                System.err.println("onComplete=" + Thread.currentThread().getName());
//
//            }
//        }).execute(new Worker<Long>() {
//            @Override
//            public void onExecutor(Long data) throws InterruptedException {
////                perfect(data);
//                Thread.sleep(500);
//                System.err.println(data+" name:"+Thread.currentThread().getName());
//            }
//        });
//        future.await(5000, new RxFuture.Callback<Long>() {
//            @Override
//            public void onFinish(Long time) {
//                System.err.println("onFinish=" + Thread.currentThread().getName() + " 耗时:" + time);
//            }
//        });
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        future.cancel(true);
        System.err.println("总共输出:" + add);
    }

    private static List<Info> getArrays(int count) {
        List<Info> infos = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            infos.add(new Info(i));
        }
        return infos;
    }

    //求因数优化代码
    public static int factor(long num) {
        int count = 0;
        for (int i = 1; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                count++;
                //num%i==0，那么num/i也应该是num的因数，例如8和2 2是因数，4也是的
                if (i != num / i) {    //避免重复计数
                    count++;
                }
            }
        }
        return count;
    }

    //求完全数优化代码
    public static void perfect(final long num) {
        int count = 1;
//        Set<Long> set = new HashSet<>();
        double sqrt = Math.sqrt(num);
        for (long i = 2; i <= sqrt; i++) {
            if (num % i == 0) {
//                System.err.println(String.format("num:%d 因数 %d", num,i));
                count += i;
//                set.add(i);
                //num%i==0，那么num/i也应该是num的因数，例如8和2 2是因数，4也是的
                if (i != num / i) {    //避免重复计数
                    count += (num / i);
//                    set.add((num / i));
                }
            }
        }
        boolean perfect = count == num;
        if (perfect) {
            System.err.println(num + "是一个完全数:" + Thread.currentThread().getName());
        }
    }

    public static class Info {
        public final int i;

        public Info(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return String.valueOf(i);
        }
    }
}
