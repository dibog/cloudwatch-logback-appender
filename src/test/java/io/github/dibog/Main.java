package io.github.dibog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.SQLException;

public class Main {

    private static final int QUEUE_LEN = 100;

    public static Exception makeNestedException() {

        SQLException e = new SQLException("Some message");
        SQLException e1 = new SQLException("Sub exception #1");
        SQLException e2 = new SQLException("Sub exception #2");
        SQLException e3 = new SQLException("Sub sub exception");

        e2.addSuppressed(e3);
        e.addSuppressed(e1);
        e.addSuppressed(e2);

        return new RuntimeException("Main exception", e);
    }

    public static void main(String[] args) throws InterruptedException {
        testLog();
        //testMDC();
    }

    public static void testLog() throws InterruptedException {
        Logger logger = LoggerFactory.getLogger(Main.class);

        for(int i=0; i<2*QUEUE_LEN; ++i) {
            logger.info("Message {}", i);
//            Thread.sleep(10_000L );
        }

        Thread.sleep(20_000L);

        System.out.println("### Done");
    }

    public static void testMDC() throws InterruptedException {
        MDC.put("customer_name", "Jon Snow");
        Logger logger = LoggerFactory.getLogger(Main.class);
        // HOW TO TEST: Set breakpoint at the end of io.github.dibog.LoggingEventToStringImpl.map,
        // check that the values written include the MDC key
        for (int i=0; i<200; ++i) {
            logger.info("This message should be logged together with the given MDC");
        }
        Thread.sleep(20_000L);

        System.out.println("### Done");
    }
}
