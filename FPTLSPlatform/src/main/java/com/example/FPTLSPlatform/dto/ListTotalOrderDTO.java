package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListTotalOrderDTO {
    private TotalOrderDTO totalOrderDTO;
    private List<OrderDetailDTO> orderDetails;
}
