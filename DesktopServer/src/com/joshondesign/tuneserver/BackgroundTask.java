package com.joshondesign.tuneserver;

import java.util.concurrent.*;

public abstract class BackgroundTask<D,R> {
    private D data;

    protected BackgroundTask() {
    }

    public void setData(D data) {
        this.data = data;

    }
    protected void onStart(D data) {
//        u.p("starting. thread = " + Thread.currentThread() + " data = " + data);
    }

    abstract protected R onWork(D data);

    protected void updateGUI(D data, R result, double percentage) {
        //ProgressUpdate<D,R> e = new ProgressUpdate<D,R>(this,data,result,percentage);
        //EventBus.getSystem().publish(e);
    }

    protected void onEnd(R result) {
//        u.p("ending. thread = " + Thread.currentThread() + " result = " + result);
    }

    public final void start() throws InterruptedException {
        onStart(data);
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<R> result = pool.submit(new Callable<R>() {
            public R call() throws Exception {
                try {
                    final R results = onWork(data);
                    /*
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            onEnd(results);
                        }
                    });
                    */
                    onEnd(results);
                    return results;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
        });
        pool.awaitTermination(3, TimeUnit.SECONDS);
        u.p("terminated");
        pool.shutdown();
    }
}
