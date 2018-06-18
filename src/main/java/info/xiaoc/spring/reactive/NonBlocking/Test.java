package info.xiaoc.spring.reactive.NonBlocking;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by ionst on 17/03/2017.
 */
public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        logger.info("---------- Running test for blocking ----------");
        Stopwatch stopwatch = Stopwatch.createStarted();
        CompletableFuture<Integer> result1 = runBlocking();
        try {
            logger.info("---------- Execution result of blocking: " + result1.get() + " ----------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        stopwatch.stop();
        long millis = stopwatch.elapsed(MILLISECONDS);
        logger.info("---------- Execution time of blocking: " + millis + " ----------");

        logger.info("---------- Running test for non-blocking ----------");
        Stopwatch stopwatch2 = Stopwatch.createStarted();
        CompletableFuture<Integer> result2 = runNonBlocking();
        try {
            logger.info("---------- Execution result of non-blocking: " + result2.get() + " ----------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        stopwatch2.stop();
        long millis2 = stopwatch2.elapsed(MILLISECONDS);
        logger.info("---------- Execution time of non-blocking: " + millis2 + " ----------");
        System.exit(0);
    }

    private static CompletableFuture<Integer> runBlocking() {
        Integer i1 = task1(1).join();
        CompletableFuture<Integer> future2 = task2(i1);
        CompletableFuture<Integer> future3 = task3(i1);
        Integer result = task4(future2.join() + future3.join()).join();
        return CompletableFuture.completedFuture(result);
    }

    private static CompletableFuture<Integer> runNonBlocking() {
        return task1(1)
                .thenCompose(
                    i1 -> (task2(i1).thenCombine(task3(i1), (i2,i3) -> i2+i3))
                )
                .thenCompose(i4 -> task4(i4));
    }

    // task definitions
    private static CompletableFuture<Integer> task1(int input) {
        return TimedEventSupport.delayedSuccess(1, input + 1);
    }

    private static CompletableFuture<Integer> task2(int input) {
        return TimedEventSupport.delayedSuccess(2, input + 2);
    }

    private static CompletableFuture<Integer> task3(int input) {
        return TimedEventSupport.delayedSuccess(3, input + 3);
    }

    private static CompletableFuture<Integer> task4(int input) {
        return TimedEventSupport.delayedSuccess(1, input + 4);
    }

}
