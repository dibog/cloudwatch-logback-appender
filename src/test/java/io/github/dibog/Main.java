package io.github.dibog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class Main {
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
        Logger logger = LoggerFactory.getLogger(Main.class);

        for(int i=0; i<200; ++i) {
            logger.info("Message {}", i);
//            Thread.sleep(10_000L );
        }

        Thread.sleep(20_000L);

        System.out.println("### Done");
    }
}
