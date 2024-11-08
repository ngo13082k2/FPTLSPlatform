package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.SlotDTO;
import com.example.FPTLSPlatform.service.ISlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {

    private final ISlotService slotService;

    @Autowired
    public SlotController(ISlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<SlotDTO> createSlot(@RequestBody SlotDTO slotDTO) {
        SlotDTO createdSlot = slotService.createSlot(slotDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSlot);
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<SlotDTO> updateSlot(@PathVariable Long slotId, @RequestBody SlotDTO updatedSlotDTO) {
        SlotDTO slot = slotService.updateSlot(slotId, updatedSlotDTO);
        return ResponseEntity.ok(slot);
    }

    @GetMapping
    public ResponseEntity<List<SlotDTO>> getAllSlots() {
        List<SlotDTO> slots = slotService.getAllSlots();
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<SlotDTO> getSlotById(@PathVariable Long slotId) {
        SlotDTO slot = slotService.getSlotById(slotId);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        slotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}
