package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailDTO {
    private Long orderDetailId;
    private Long orderId;
    private ClassDTO classDTO;
    private Long price;
}
