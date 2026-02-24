package ru.samara.pet.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/diagnostics")
public class VirtualThreadDiagnosticController {

    @GetMapping("/thread-info")
    public Map<String, Object> getThreadInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        // Текущий поток
        Thread currentThread = Thread.currentThread();
        info.put("currentThreadName", currentThread.getName());
        info.put("isVirtual", currentThread.isVirtual());
        info.put("threadGroup", currentThread.getThreadGroup() != null ?
                currentThread.getThreadGroup().getName() : "none");

        // Информация о пуле
        info.put("virtualThreadsEnabled",
                System.getProperty("spring.threads.virtual.enabled"));

        // Проверка через Thread.Builder
        info.put("canCreateVirtualThread",
                testIfVirtualThreadsAvailable());

        // Статистика
        info.put("activeThreads", Thread.activeCount());
        info.put("virtualThreadCount", countVirtualThreads());

        return info;
    }

    private boolean testIfVirtualThreadsAvailable() {
        try {
            Thread.Builder builder = Thread.ofVirtual();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private long countVirtualThreads() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isVirtual)
                .count();
    }

    @GetMapping("/request-details")
    public Map<String, Object> getCurrentRequestDetails() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> details = new LinkedHashMap<>();

        Thread current = Thread.currentThread();


        details.put("handlingThread", current.getName());
        details.put("isVirtualThread", current.isVirtual());
        details.put("threadId", current.threadId());
        details.put("threadPriority", current.getPriority());
        details.put("threadState", current.getState().toString());



        return details;
    }
}