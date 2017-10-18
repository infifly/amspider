package infifly.amazonapi.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

 
public class Threadpool {

    private int threadNum;

    private AtomicInteger threadRun = new AtomicInteger();

    private ReentrantLock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    public Threadpool(int threadNum) {
        this.threadNum = threadNum;
        this.executorService = Executors.newFixedThreadPool(threadNum);
    }


    public int getCurrentThread() {
        return threadRun.get();
    }

    public int getThreadNum() {
        return threadNum;
    }

    private ExecutorService executorService;

    public void execute(final Runnable runnable) {


        if (threadRun.get() >= threadNum) {
            try {
            	lock.lock();
                while (threadRun.get() >= threadNum) {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                    }
                }
            } finally {
            	lock.unlock();
            }
        }
        threadRun.incrementAndGet();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    try {
                    	lock.lock();
                        threadRun.decrementAndGet();
                        condition.signal();
                    } finally {
                    	lock.unlock();
                    }
                }
            }
        });
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public void shutdown() {
        executorService.shutdown();
    }


}
 