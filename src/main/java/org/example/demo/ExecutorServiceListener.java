package org.example.demo;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceListener implements ServletContextListener {
    private ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent e) {
        executor = Executors.newFixedThreadPool(8);
        e.getServletContext().setAttribute("exectutor", executor);
        System.out.println("Executor service started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        ExecutorService executor = (ExecutorService) e.getServletContext().getAttribute("executor");
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            System.out.println("Executor service shutdown.");
        }
    }
}
