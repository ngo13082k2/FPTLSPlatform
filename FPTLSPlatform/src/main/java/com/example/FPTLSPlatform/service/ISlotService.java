package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.SlotDTO;

import java.util.List;

public interface ISlotService {
    SlotDTO createSlot(SlotDTO slotDTO);
    SlotDTO updateSlot(Long slotId, SlotDTO updatedSlotDTO);
    List<SlotDTO> getAllSlots();
    SlotDTO getSlotById(Long slotId);
    void deleteSlot(Long slotId);
}
