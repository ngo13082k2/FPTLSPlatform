package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.DateSlotDTO;
import com.example.FPTLSPlatform.dto.DocumentDTO;
import com.example.FPTLSPlatform.dto.StudentDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.model.enums.OrderStatus;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.util.OAuth2Util;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.service.IClassService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Random;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClassService implements IClassService {

    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CloudinaryService cloudinaryService;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OrderRepository orderRepository;
    private final ViolationRepository violationRepository;
    private final ClassDateSlotRepository classDateSlotRepository;
    private final DocumentRepository documentRepository;
    private final FeedbackService feedbackService;

    @Autowired
    public ClassService(ClassRepository classRepository,
                        CourseRepository courseRepository,
                        TeacherRepository teacherRepository,
                        OrderDetailRepository orderDetailRepository,
                        CloudinaryService cloudinaryService,
                        SlotRepository slotRepository,
                        UserRepository userRepository, WalletRepository walletRepository, OrderRepository orderRepository, ViolationRepository violationRepository, ClassDateSlotRepository classDateSlotRepository, DocumentRepository documentRepository,
                        FeedbackService feedbackService) {
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cloudinaryService = cloudinaryService;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.orderRepository = orderRepository;
        this.violationRepository = violationRepository;
        this.classDateSlotRepository = classDateSlotRepository;
        this.documentRepository = documentRepository;
        this.feedbackService = feedbackService;
    }

    private static final Logger log = LoggerFactory.getLogger(ClassService.class);

    public ClassDTO createClass(ClassDTO classDTO, MultipartFile image) throws IOException {
        if (classDTO.getName() == null || classDTO.getDescription() == null ||
                classDTO.getMaxStudents() == null || classDTO.getPrice() == null || classDTO.getCourseCode() == null) {
            throw new RuntimeException("All fields must be provided and cannot be null");
        }

        if (classDTO.getCode() == null || classDTO.getCode().isEmpty()) {
            classDTO.setCode(generateUniqueCode());
        }

        String creatorName = getCurrentUsername();
        String user = String.valueOf(userRepository.findByUserName(creatorName)
                .orElseThrow(() -> new RuntimeException("User not found")));

        classDTO.setCreateDate(LocalDateTime.now());
        classDTO.setStatus(ClassStatus.PENDING);

        Optional<Course> courseOpt = courseRepository.findByCourseCode(classDTO.getCourseCode());
        if (courseOpt.isEmpty()) {
            throw new RuntimeException("Course with code " + classDTO.getCourseCode() + " not found");
        }
        Course course = courseOpt.get();

        // Kiểm tra số lượng slot của lớp
        int requiredSlots = course.getDuration(); // Số lượng slot dựa trên thời lượng của khóa học
        if (classDTO.getDateSlots() == null || classDTO.getDateSlots().size() != requiredSlots) {
            throw new RuntimeException("Class must have exactly " + requiredSlots + " slots to match the course duration.");
        }
        boolean hasDocument = documentRepository.existsByCourse_CourseCode(classDTO.getCourseCode());
        if (!hasDocument) {
            throw new RuntimeException("Cannot create class. No document associated with course code " + classDTO.getCourseCode());
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(image);
            classDTO.setImageUrl(imageUrl);
        }

        Class newClass = mapDTOToEntity(classDTO, course, user);
        Set<ClassDateSlot> dateSlots = mapDateSlots(classDTO, newClass);
        newClass.setDateSlots(dateSlots);

        Class savedClass = classRepository.save(newClass);
        return mapEntityToDTO(savedClass);
    }




    public String generateUniqueCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        while (classRepository.existsByCode(code.toString())) {
            code.setLength(0);
            for (int i = 0; i < 6; i++) {
                int index = random.nextInt(characters.length());
                code.append(characters.charAt(index));
            }
        }

        return code.toString();
    }


    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
    private Set<ClassDateSlot> mapDateSlots(ClassDTO classDTO, Class clazz) {
        Set<ClassDateSlot> dateSlots = new HashSet<>();
        for (DateSlotDTO dateSlotDTO : classDTO.getDateSlots()) {
            for (Long slotId : dateSlotDTO.getSlotIds()) {
                Slot slot = slotRepository.findById(slotId)
                        .orElseThrow(() -> new RuntimeException("Slot not found with ID: " + slotId));
                dateSlots.add(ClassDateSlot.builder()
                        .clazz(clazz)
                        .date(dateSlotDTO.getDate())
                        .slot(slot)
                        .build());
            }
        }
        return dateSlots;
    }


    public List<ClassDTO> getClassByMajor() {
        String username = getCurrentUsername();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Long> categoryIds = user.getMajor().stream()
                .map(Category::getCategoryId)
                .collect(Collectors.toSet());

        // Tìm danh sách các Class thuộc các Category này thông qua Course
        List<Class> classes = classRepository.findByCoursesCategoriesCategoryIdIn(categoryIds);

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    private String createGoogleMeetLink(String className, LocalDateTime startDateTime, LocalDateTime endDateTime) throws IOException, GeneralSecurityException {
        Calendar service = OAuth2Util.getCalendarService(); // Sử dụng OAuth2Util để khởi tạo Calendar service

        Event event = new Event()
                .setSummary(className)
                .setDescription("Online class: " + className);

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDateTime.toInstant(ZoneOffset.UTC).toString()))
                .setTimeZone("UTC");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(endDateTime.toInstant(ZoneOffset.UTC).toString()))
                .setTimeZone("UTC");
        event.setEnd(end);

        ConferenceData conferenceData = new ConferenceData()
                .setCreateRequest(new CreateConferenceRequest()
                        .setRequestId(UUID.randomUUID().toString())
                        .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet")));

        event.setConferenceData(conferenceData);

        // Insert event into Google Calendar and generate Google Meet link
        Event createdEvent = service.events().insert("primary", event)
                .setConferenceDataVersion(1)
                .execute();

        return createdEvent.getHangoutLink();
    }

    @Override
    public ClassDTO confirmClassCompletion(Long classId, String teacherUsername) throws Exception {
        Class scheduledClass = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (!scheduledClass.getTeacher().getTeacherName().equals(teacherUsername)) {
            throw new Exception("You are not the teacher of this class.");
        }


        scheduledClass.setStatus(ClassStatus.COMPLETED);
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(classId, Pageable.unpaged());
        for (OrderDetail orderDetail : orderDetails) {
            Order order = orderDetail.getOrder();
            order.setStatus(OrderStatus.COMPLETED);
            orderDetailRepository.save(orderDetail);
        }
        classRepository.save(scheduledClass);

        return mapEntityToDTO(scheduledClass);
    }

    public ClassDTO updateClass(Long classId, ClassDTO classDTO, MultipartFile image) throws IOException {
        Class existingClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class with id " + classId + " not found"));

        if (classDTO.getName() != null) existingClass.setName(classDTO.getName());
        if (classDTO.getDescription() != null) existingClass.setDescription(classDTO.getDescription());
        if (classDTO.getMaxStudents() != null) existingClass.setMaxStudents(classDTO.getMaxStudents());
        if (classDTO.getLocation() != null) existingClass.setLocation(classDTO.getLocation());
        if (classDTO.getPrice() != null) existingClass.setPrice(classDTO.getPrice());

        String imageUrl;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(image);
            existingClass.setImage(imageUrl);
        }

        Class updatedClass = classRepository.save(existingClass);
        return mapEntityToDTO(updatedClass);
    }

    public List<ClassDTO> getClassesByCourse(String courseCode) {
        List<Class> classes = classRepository.findByCoursesCourseCode(courseCode);

        if (classes.isEmpty()) {
            throw new RuntimeException("No classes found for course code: " + courseCode);
        }

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public ClassDTO getClassById(Long classId) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class with id " + classId + " not found"));

        return mapEntityToDTO(clazz);
    }

    public List<ClassDTO> getAllClasses() {
        List<Class> classes = classRepository.findAll();
        if (classes.isEmpty()) {
            throw new IllegalStateException("No classes found in the system.");
        }
        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public List<ClassDTO> getClassesByTeacherName(String teacherName) {
        teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new IllegalArgumentException("Teacher with name '" + teacherName + "' not found."));

        List<Class> classes = classRepository.findByTeacherTeacherName(teacherName);
        if (classes.isEmpty()) {
            throw new IllegalStateException("No classes found for teacher '" + teacherName + "'.");
        }

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public Page<StudentDTO> getAllStudentsInClass(Long classId, Pageable pageable) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(classId, pageable);
        return orderDetails.map(orderDetail -> {
            User student = orderDetail.getOrder().getUser();
            return new StudentDTO(student.getUserName(), student.getPhoneNumber(), student.getEmail(), student.getEmail(), student.getAddress());
        });
    }

    public List<ClassDTO> getAllClassesByCurrentTeacher() {
        String teacherName = getCurrentUsername();

        Teacher teacher = teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<Class> classes = classRepository.findByTeacher(teacher);

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public List<ClassDTO> getClassesByStatusCompleted() {
        List<Class> completedClasses = classRepository.findByStatus(ClassStatus.COMPLETED);

        if (completedClasses.isEmpty()) {
            throw new IllegalStateException("No classes found with status COMPLETED.");
        }

        return completedClasses.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    private Class mapDTOToEntity(ClassDTO classDTO, Course course, String user) {
        Class clazz = Class.builder()
                .name(classDTO.getName())
                .code(classDTO.getCode())
                .description(classDTO.getDescription())
                .status(classDTO.getStatus())
                .location(classDTO.getLocation())
                .maxStudents(classDTO.getMaxStudents())
                .price(classDTO.getPrice())
                .creator(user)
                .createDate(classDTO.getCreateDate())
                .image(classDTO.getImageUrl())
                .courses(course)
                .build();

        // Ánh xạ dateSlots từ DTO sang thực thể ClassDateSlot
        if (classDTO.getDateSlots() != null) {
            Set<ClassDateSlot> dateSlots = classDTO.getDateSlots().stream().flatMap(dateSlotDTO -> {
                LocalDate date = dateSlotDTO.getDate();
                return dateSlotDTO.getSlotIds().stream().map(slotId -> {
                    Slot slot = slotRepository.findById(slotId)
                            .orElseThrow(() -> new RuntimeException("Slot not found with ID: " + slotId));
                    return ClassDateSlot.builder()
                            .clazz(clazz)
                            .date(date)
                            .slot(slot)
                            .build();
                });
            }).collect(Collectors.toSet());
            clazz.setDateSlots(dateSlots);
        }

        return clazz;
    }



    private List<DocumentDTO> getDocumentsByCourseCode(String courseCode) {
        return documentRepository.findByCourse_CourseCode(courseCode).stream()
                .map(doc -> DocumentDTO.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .content(doc.getContent())
                        .filePath(doc.getFilePath())
                        .courseCode(doc.getCourse().getCourseCode())
                        .build())
                .collect(Collectors.toList());
    }

    ClassDTO mapEntityToDTO(Class clazz) {
        List<DocumentDTO> documents = getDocumentsByCourseCode(clazz.getCourses().getCourseCode());

        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(clazz.getClassId(), Pageable.unpaged());
        List<StudentDTO> studentDTOList = orderDetails.getContent().stream()
                .map(orderDetail -> {
                    Order order = orderDetail.getOrder();
                    User user = order.getUser();
                    if (!order.getStatus().equals(OrderStatus.CANCELLED)) {
                        return StudentDTO.builder()
                                .userName(user.getUserName())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .phoneNumber(user.getPhoneNumber())
                                .address(user.getAddress())
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<DateSlotDTO> dateSlotDTOs = clazz.getDateSlots().stream()
                .collect(Collectors.groupingBy(ClassDateSlot::getDate)) // Gom nhóm theo ngày
                .entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Long> slotIds = entry.getValue().stream()
                            .map(classDateSlot -> classDateSlot.getSlot().getSlotId())
                            .collect(Collectors.toList());
                    return DateSlotDTO.builder()
                            .date(date)
                            .slotIds(slotIds)
                            .build();
                })
                .toList();

        Teacher teacher = clazz.getTeacher();
        String teacherName = teacher != null ? teacher.getTeacherName() : null;
        String fullName = teacher != null ? teacher.getFullName() : null;
        String avatarImage = teacher != null ? teacher.getAvatarImage() : null;
        Double teacherFeedback = null;
        if (teacherName != null) {
            teacherFeedback = feedbackService.getAverageFeedbackForTeacher(teacherName);
        }
        if (teacherFeedback == null) {
            teacherFeedback = 0.0;
        }
        return ClassDTO.builder()
                .classId(clazz.getClassId())
                .name(clazz.getName())
                .code(clazz.getCode())
                .description(clazz.getDescription())
                .status(clazz.getStatus())
                .location(clazz.getLocation())
                .maxStudents(clazz.getMaxStudents())
                .createDate(clazz.getCreateDate())
                .price(clazz.getPrice())
                .teacherName(teacherName)
                .fullName(fullName)
                .courseCode(clazz.getCourses().getCourseCode())
                .imageUrl(clazz.getImage())
                .creator(clazz.getCreator())
                .dateSlots(dateSlotDTOs)
                .students(studentDTOList)
                .imageTeacher(avatarImage)
                .documents(documents)
                .teacherFeedback(teacherFeedback)
                .build();
    }



    public long getTotalClasses() {
        return classRepository.count();
    }

    public Map<YearMonth, Long> getClassesByStatusAndMonth(ClassStatus status, Integer year) {
        List<Class> classes = classRepository.findByStatus(status);

        return classes.stream()
                .filter(clazz -> clazz.getCreateDate() != null) // Lọc các bản ghi có create_date
                .filter(clazz -> year == null || clazz.getCreateDate().getYear() == year) // Lọc theo năm nếu có
                .collect(Collectors.groupingBy(
                        clazz -> YearMonth.from(clazz.getCreateDate()), // Nhóm theo tháng và năm
                        Collectors.counting()
                ));
    }

    public Map<String, Map<YearMonth, Long>> getClassesGroupedByStatusAndMonths(Integer year) {
        // Lấy danh sách lớp với ba trạng thái khác nhau
        Map<String, Map<YearMonth, Long>> result = new HashMap<>();

        result.put("ACTIVE", getClassesByStatusAndMonth(ClassStatus.ACTIVE, year));
        result.put("ONGOING", getClassesByStatusAndMonth(ClassStatus.ONGOING, year));
        result.put("COMPLETED", getClassesByStatusAndMonth(ClassStatus.COMPLETED, year));
        result.put("CANCELED", getClassesByStatusAndMonth(ClassStatus.CANCELED, year));
        result.put("PENDING", getClassesByStatusAndMonth(ClassStatus.PENDING, year));

        return result;
    }


    public List<ClassDTO> getClassesByStatusAndMonthDetailed(ClassStatus status, int year, Integer month) {
        List<Class> classes = classRepository.findByStatus(status);

        // Lọc các lớp học theo năm và tháng (nếu có)
        return classes.stream()
                .filter(clazz -> clazz.getCreateDate() != null)
                .filter(clazz -> clazz.getCreateDate().getYear() == year)
                .filter(clazz -> month == null || clazz.getCreateDate().getMonthValue() == month) // Lọc theo tháng nếu có
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String cancelClass(Long classId) {
        Class classToCancel = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId));

        if (classToCancel.getStatus() == ClassStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a class that is already completed.");
        } else if (classToCancel.getStatus() == ClassStatus.CANCELED) {
            throw new RuntimeException("This class has already been canceled.");
        }

        String currentUsername = getCurrentUsername();
        Teacher teacher = teacherRepository.findByTeacherName(currentUsername)
                .orElseThrow(() -> new RuntimeException("Teacher not found with username: " + currentUsername));

        Violation violation = violationRepository.findByTeacher(teacher);
        if (violation == null) {
            // Nếu chưa có vi phạm nào, tạo mới một vi phạm
            violation = new Violation();
            violation.setTeacher(teacher);
            violation.setViolationCount(1);  // Tăng số lần vi phạm
            violation.setPenaltyPercentage(0.1);  // Tỉ lệ trừ (ví dụ là 10%)
            violation.setLastViolationDate(LocalDateTime.now());
            violation.setDescription("Teacher cancelled a class.");
            violationRepository.save(violation);
        } else {
            violation.setViolationCount(violation.getViolationCount() + 1);
            violation.setLastViolationDate(LocalDateTime.now());
            violationRepository.save(violation);
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(classId);

        for (OrderDetail orderDetail : orderDetails) {
            Order order = orderDetail.getOrder();
            User student = order.getUser();

            Wallet studentWallet = student.getWallet();
            if (studentWallet == null) {
                throw new RuntimeException("Student does not have a wallet for refund.");
            }
            studentWallet.setBalance(studentWallet.getBalance() + orderDetail.getPrice());
            walletRepository.save(studentWallet);

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }

        classToCancel.setStatus(ClassStatus.CANCELED);
        classRepository.save(classToCancel);

        return "Class with ID " + classId + " has been successfully canceled, and refunds have been processed.";
    }
    public void assignTeacherToClass(Long classId) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (clazz.getTeacher() != null) {
            throw new RuntimeException("This class already has a teacher.");
        }

        String currentTeacherName = getCurrentUsername();
        Teacher teacher = teacherRepository.findByTeacherName(currentTeacherName)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        boolean hasConflict = clazz.getDateSlots().stream().anyMatch(dateSlot ->
                classDateSlotRepository.existsByClazz_Teacher_TeacherNameAndDateAndSlot_SlotIdAndClazz_StatusNot(
                        teacher.getTeacherName(),
                        dateSlot.getDate(),
                        dateSlot.getSlot().getSlotId(),
                        String.valueOf(ClassStatus.CANCELED)
                )
        );

        if (hasConflict) {
            throw new RuntimeException("Teacher already has a class scheduled on the same date and slot.");
        }

        clazz.setTeacher(teacher);
        classRepository.save(clazz);
    }
    public List<ClassDTO> getClassesByTeacherName() {
        String teacherName = getCurrentUsername();

        List<Class> classes = classRepository.findByTeacher_TeacherName(teacherName);

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    public ClassDTO updateClassLocation(Long classId, String location) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        clazz.setLocation(location);
        classRepository.save(clazz);

        return mapEntityToDTO(clazz);
    }
    public List<ClassDTO> getAllClassesWithoutTeacher(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Teacher username cannot be null or empty");
        }

        List<Class> classesWithoutTeacher = classRepository.findByTeacherIsNull();

        // Duyệt qua từng lớp học
        return classesWithoutTeacher.stream()
                .filter(clazz -> !hasScheduleConflict(clazz, username)) // Kiểm tra xung đột lịch
                .map(this::mapEntityToDTO) // Chuyển đổi thành DTO
                .collect(Collectors.toList());
    }

    private boolean hasScheduleConflict(Class clazz, String teacherUsername) {
        if (teacherUsername == null || teacherUsername.isEmpty()) {
            throw new IllegalArgumentException("Teacher username cannot be null or empty");
        }

        // Lấy các lớp học của giáo viên
        List<Class> teacherClasses = classRepository.findByTeacher_TeacherName(teacherUsername);

        // Kiểm tra xem lớp học có xung đột lịch hay không
        for (Class teacherClass : teacherClasses) {
            for (ClassDateSlot dateSlot : clazz.getDateSlots()) {
                for (ClassDateSlot teacherDateSlot : teacherClass.getDateSlots()) {
                    // Kiểm tra nếu có sự trùng lặp về ngày và slot
                    if (dateSlot.getDate().equals(teacherDateSlot.getDate()) &&
                            dateSlot.getSlot().getSlotId().equals(teacherDateSlot.getSlot().getSlotId())) {
                        return true; // Xung đột lịch
                    }
                }
            }
        }
        return false; // Không có xung đột lịch
    }

    public List<ClassDTO> getAllClassesWithTeacher() {
        return classRepository.findByTeacherIsNotNull().stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    public List<ClassDTO> getAllClassesWithTeacherByMajor() {
        String username = getCurrentUsername();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Long> categoryIds = user.getMajor().stream()
                .map(Category::getCategoryId)
                .collect(Collectors.toSet());

        List<Class> classes = classRepository.findByCoursesCategoriesCategoryIdInAndTeacherIsNotNull(categoryIds);

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }







}