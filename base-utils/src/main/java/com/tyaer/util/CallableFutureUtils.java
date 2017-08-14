package com.tyaer.util;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * Created by Twin on 2017/4/12.
 */
public class CallableFutureUtils{
    Logger logger=Logger.getLogger(CallableFutureUtils.class);

    public void callableAndFuture(Method method) {

        ExecutorService exec = Executors.newSingleThreadExecutor();
        Callable<Integer> callable = () -> {
            System.out.println();
            System.out.println();
            return null;
        };
        Future<Integer> future = exec.submit(callable);
        try {
            Integer integer = future.get(2, TimeUnit.MINUTES);
//            if (integer == 0) {
//                results.add(host_name);
//                logger.info(results);
//            } else {
//                logger.info(host_name + " " + host_port + " " + integer);
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            logger.error("线程阻塞！");
        }finally {
            future.cancel(true);
            exec.shutdownNow();
        }
    }
}
