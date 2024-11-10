//package com.example.FPTLSPlatform.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@RestController
//public class ClassStatusController {
//
//    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
//
//    @GetMapping("/class-status/updates")
//    public SseEmitter streamClassStatusUpdates() {
//        SseEmitter emitter = new SseEmitter();
//        String sessionId = generateSessionId(); // Tạo ID duy nhất cho mỗi kết nối SSE
//        emitters.put(sessionId, emitter);
//
//        // Remove emitter when complete or timeout
//        emitter.onCompletion(() -> emitters.remove(sessionId));
//        emitter.onTimeout(() -> emitters.remove(sessionId));
//
//        return emitter;
//    }
//
//    // Method to send status updates to all connected clients
//    public void sendClassStatusUpdate(Long classId, String newStatus) {
//        emitters.forEach((sessionId, emitter) -> {
//            try {
//                emitter.send(SseEmitter.event()
//                        .name("classStatusUpdate")
//                        .data("Class " + classId + " has changed to status: " + newStatus));
//            } catch (IOException e) {
//                emitters.remove(sessionId); // Loại bỏ kết nối nếu có lỗi
//            }
//        });
//    }
//
//    private String generateSessionId() {
//        return String.valueOf(System.currentTimeMillis());
//    }
//}