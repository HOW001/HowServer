/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server.netserver.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;


/**
 * Same as a java.util.concurrent.ThreadPoolExecutor but implements a much more efficient
 * {@link #getSubmittedCount()} method, to be used to properly handle the work queue.
 * If a RejectedExecutionHandler is not specified a default one will be configured
 * and that one will always throw a RejectedExecutionException
 * 作为一个java . util . concurrent相同， 但实现ThreadPoolExecutor更高效的方法,用于妥善处理工作队列。
 * 如果一个RejectedExecutionHandler没有指定一个默认的配置,一个总是会抛出一个RejectedExecutionException
 * @author fhanik
 *
 */
public class ThreadPoolExec extends ThreadPoolExecutor {

    private static final Logger log = Logger.getLogger(ThreadPoolExec.class);;//LogFactory.getLog();

    /**
     * The number of tasks submitted but not yet finished. This includes tasks
     * in the queue and tasks that have been handed to a worker thread but the
     * latter did not start executing the task yet.
     * This number is always greater or equal to {@link #getActiveCount()}.
     * 任务的数量提交但尚未完成。
     * 这包括任务队列和任务,已经交给一个工作者线程,但后者并没有开始执行任务不过该数字总是大于或等于 getActiveCount()。
     */
    private final AtomicInteger submittedCount = new AtomicInteger(0);
    private final AtomicLong lastContextStoppedTime = new AtomicLong(0L);

    /**
     * Most recent time in ms when a thread decided to kill itself to avoid
     * potential memory leaks. Useful to throttle the rate of renewals of
     * threads.
     * 当一个线程决定杀死自己,以避免潜在的内存泄漏。有用的速度节流阀的线程续签。
     */
    private final AtomicLong lastTimeThreadKilledItself = new AtomicLong(0L);

    /**
     * Delay in ms between 2 threads being renewed. If negative, do not renew threads.
     * 
     */
    private long threadRenewalDelay = 1000L;

    public ThreadPoolExec(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public ThreadPoolExec(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public ThreadPoolExec(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectHandler());
    }

    public ThreadPoolExec(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new RejectHandler());
    }

    public long getThreadRenewalDelay() {
        return threadRenewalDelay;
    }

    public void setThreadRenewalDelay(long threadRenewalDelay) {
        this.threadRenewalDelay = threadRenewalDelay;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        submittedCount.decrementAndGet();

        if (t == null) {
            stopCurrentThreadIfNeeded();
        }
    }

    /**
     * If the current thread was started before the last time when a context was
     * stopped, an exception is thrown so that the current thread is stopped.
     * 当一个上下文被停止时当前线程已开始执行,就抛出一个异常,当前线程停止
     */
    protected void stopCurrentThreadIfNeeded() {
        if (currentThreadShouldBeStopped()) {
            long lastTime = lastTimeThreadKilledItself.longValue();
            if (lastTime + threadRenewalDelay < System.currentTimeMillis()) {
                if (lastTimeThreadKilledItself.compareAndSet(lastTime,
                        System.currentTimeMillis() + 1)) {
                    // OK, it's really time to dispose of this thread
                	//好的,现在正式来处理这个线程

                    final String msg="ThreadPool.stopCurrentThreadIfNeeded()";
                    Thread.currentThread().setUncaughtExceptionHandler(
                            new UncaughtExceptionHandler() {
                                @Override
                                public void uncaughtException(Thread t,
                                        Throwable e) {
                                    // yes, swallow the exception
                                    log.error(msg);
                                }
                            });
                    throw new RuntimeException(msg);
                }
            }
        }
    }

    protected boolean currentThreadShouldBeStopped() {
        if (threadRenewalDelay >= 0
            && Thread.currentThread() instanceof TaskThread) {
            TaskThread currentTaskThread = (TaskThread) Thread.currentThread();
            if (currentTaskThread.getCreationTime() <
                    this.lastContextStoppedTime.longValue()) {
                return true;
            }
        }
        return false;
    }

    public int getSubmittedCount() {
        return submittedCount.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable command) {
        execute(command,0,TimeUnit.MILLISECONDS);
    }

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the <tt>Executor</tt> implementation.
     * If no threads are available, it will be added to the work queue.
     * If the work queue is full, the system will wait for the specified
     * time and it throw a RejectedExecutionException if the queue is still
     * full after that.
     * 执行给定的命令在未来某个时刻。命令可以执行在一个新线程,在一个池线程,或在调用线程,由执行程序实现。
     * 如果没有可用的线程,它将被添加到工作队列
     * 如果工作队列满了之后,系统将等待指定的时间和它抛出一个RejectedExecutionException
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     * accepted for execution - the queue is full
     * @throws NullPointerException if command or unit is null
     */
    public void execute(Runnable command, long timeout, TimeUnit unit) {
        submittedCount.incrementAndGet();
        try {
            super.execute(command);
        } catch (RejectedExecutionException rx) {
            if (super.getQueue() instanceof TaskQueue) {
                final TaskQueue queue = (TaskQueue)super.getQueue();
                try {
                    if (!queue.force(command, timeout, unit)) {
                        submittedCount.decrementAndGet();
                        throw new RejectedExecutionException("Queue capacity is full.");
                    }
                } catch (InterruptedException x) {
                    submittedCount.decrementAndGet();
                    Thread.interrupted();
                    throw new RejectedExecutionException(x);
                }
            } else {
                submittedCount.decrementAndGet();
                throw rx;
            }
        }catch(Exception e){
        	log.error("ThreadPoolExec线程执行异常:",e);
        }
    }

    public void contextStopping() {
        this.lastContextStoppedTime.set(System.currentTimeMillis());

        // save the current pool parameters to restore them later
        //保存当前池参数以后恢复它们
        int savedCorePoolSize = this.getCorePoolSize();
        TaskQueue taskQueue =
                getQueue() instanceof TaskQueue ? (TaskQueue) getQueue() : null;
        if (taskQueue != null) {
            // note by slaurent : quite oddly threadPoolExecutor.setCorePoolSize
            // checks that queue.remainingCapacity()==0. I did not understand
            // why, but to get the intended effect of waking up idle threads, I
            // temporarily fake this condition.
        	//注意,slaurent:相当奇怪的是threadPoolExecutor。 
        	//setCorePoolSize检查queue.remainingCapacity()= = 0。
        	//我不明白为什么,但得到的预期效果空闲线程醒来,我暂时假这个条件。
            taskQueue.setForcedRemainingCapacity(Integer.valueOf(0));
        }

        // setCorePoolSize(0) wakes idle threads
        //setCorePoolSize(0)空闲线程醒来
        this.setCorePoolSize(0);

        // wait a little so that idle threads wake and poll the queue again,
        // this time always with a timeout (queue.poll() instead of
        // queue.take())
        // even if we did not wait enough, TaskQueue.take() takes care of timing
        // out, so that we are sure that all threads of the pool are renewed in
        // a limited time, something like
        // (threadKeepAlive + longest request time)
        //等一等,空闲线程和轮询队列之后,这次又总是与一个超时(不是queue.poll()而是  queue.take()),
        //即使我们没有等待足够的,TaskQueue.take()负责超时,因此,我们确信所有的线程池是在有限的时间内再次,像( 线程活跃期 +最长的请求时间)
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            // yes, ignore
        	//忽略异常
        }

        if (taskQueue != null) {
            // ok, restore the state of the queue and pool
        	//好的,恢复状态的线程队列和线程池
            taskQueue.setForcedRemainingCapacity(null);
        }
        this.setCorePoolSize(savedCorePoolSize);
    }

    private static class RejectHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r,
                java.util.concurrent.ThreadPoolExecutor executor) {
            throw new RejectedExecutionException();
        }

    }


}
