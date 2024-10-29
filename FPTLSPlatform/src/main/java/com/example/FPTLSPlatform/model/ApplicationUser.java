package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
@Getter
@Setter
@Entity
@Table(name = "application_user")
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_user_id")
    private Long applicationUserId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "application_type_id", nullable = false)
    private ApplicationType applicationType;
    @ManyToOne
    @JoinColumn(name = "user_name", nullable = true)
    private User user;
    @ManyToOne
    @JoinColumn(name = "teacher", nullable = true)
    private Teacher teacher;

    public Double getAmountFromDescription() {
        if (description == null || !description.contains("Amount:")) {
            return null;
        }

        try {
            // Sử dụng regex để tìm số tiền sau "Amount:"
            Pattern pattern = Pattern.compile("Amount:\\s*(\\d+(\\.\\d{1,2})?)");
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Không thể đọc số tiền từ description.");
        }

        return null;
    }
}
