package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.model.ApplicationType;
import com.example.FPTLSPlatform.model.ApplicationUser;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.ApplicationTypeRepository;
import com.example.FPTLSPlatform.repository.ApplicationUserRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.service.IApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserService implements IApplicationUserService {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ApplicationTypeRepository applicationTypeRepository;

    @Autowired
    private UserRepository userRepository;

    public void processWithdrawalRequest(WithdrawalRequestDTO withdrawalRequestDto) {
        ApplicationType applicationType = applicationTypeRepository.findById(withdrawalRequestDto.getApplicationTypeId())
                .orElseThrow(() -> new RuntimeException("Application type not found"));

        User user = getLoggedInUser();

        ApplicationUser applicationUser = mapWithdrawalDtoToEntity(withdrawalRequestDto, applicationType, user);

        applicationUserRepository.save(applicationUser);
    }

    public void processOtherRequest(OtherApplicationDTO otherRequestDto) {
        ApplicationType applicationType = applicationTypeRepository.findById(otherRequestDto.getApplicationTypeId())
                .orElseThrow(() -> new RuntimeException("Application type not found"));

        User user = getLoggedInUser();

        ApplicationUser applicationUser = mapOtherDtoToEntity(otherRequestDto, applicationType, user);

        applicationUserRepository.save(applicationUser);
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ApplicationUser mapWithdrawalDtoToEntity(WithdrawalRequestDTO dto, ApplicationType applicationType, User user) {
        return ApplicationUser.builder()
                .name(dto.getAccountHolderName())
                .title("Withdrawal Request")
                .description("Account number: " + dto.getAccountNumber() + ", Bank: " + dto.getBank())
                .status("pending")
                .applicationType(applicationType)
                .user(user)
                .build();
    }

    private ApplicationUser mapOtherDtoToEntity(OtherApplicationDTO dto, ApplicationType applicationType, User user) {
        return ApplicationUser.builder()
                .name(dto.getStudentName())
                .title("Other Request")
                .description("Student Roll No: " + dto.getStudentRollNo() + ", Reason: " + dto.getReason())
                .status("pending")
                .applicationType(applicationType)
                .user(user)
                .build();
    }
}
