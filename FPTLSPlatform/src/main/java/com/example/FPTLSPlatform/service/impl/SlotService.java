package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.SlotDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Slot;
import com.example.FPTLSPlatform.repository.SlotRepository;
import com.example.FPTLSPlatform.service.ISlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotService implements ISlotService {

    @Autowired
    private SlotRepository slotRepository;

    public SlotDTO createSlot(SlotDTO slotDTO) {
        Slot slot = mapDTOToEntity(slotDTO);
        Slot savedSlot = slotRepository.save(slot);
        return mapEntityToDTO(savedSlot);
    }

    public SlotDTO updateSlot(Long slotId, SlotDTO updatedSlotDTO) {
        Slot existingSlot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + slotId));

        existingSlot.setPeriod(updatedSlotDTO.getPeriod());
        existingSlot.setStartTime(updatedSlotDTO.getStart());
        existingSlot.setEndTime(updatedSlotDTO.getEnd());

        Slot savedSlot = slotRepository.save(existingSlot);
        return mapEntityToDTO(savedSlot);
    }

    public List<SlotDTO> getAllSlots() {
        return slotRepository.findAll().stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public SlotDTO getSlotById(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + slotId));
        return mapEntityToDTO(slot);
    }

    public void deleteSlot(Long slotId) {
        Slot existingSlot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + slotId));

        slotRepository.delete(existingSlot);
    }

    private Slot mapDTOToEntity(SlotDTO slotDTO) {
        return Slot.builder()
                .slotId(slotDTO.getSlotId())
                .period(slotDTO.getPeriod())
                .startTime(slotDTO.getStart())
                .endTime(slotDTO.getEnd())
                .build();
    }

    private SlotDTO mapEntityToDTO(Slot slot) {
        return SlotDTO.builder()
                .slotId(slot.getSlotId())
                .period(slot.getPeriod())
                .start(slot.getStartTime())
                .end(slot.getEndTime())
                .build();
    }
    
}
