package com.example.FPTLSPlatform.dto;

import com.example.FPTLSPlatform.model.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalOrderDTO {
    private Long totalPrice;
    private Long amount;
}
