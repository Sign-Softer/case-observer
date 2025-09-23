package ro.signsofter.caseobserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.signsofter.caseobserver.controller.dto.HearingResponseDto;
import ro.signsofter.caseobserver.controller.mapper.CourtCaseMapper;
import ro.signsofter.caseobserver.entity.Hearing;
import ro.signsofter.caseobserver.repository.HearingRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hearings")
public class HearingController {

    @Autowired
    private HearingRepository hearingRepository;

    @GetMapping
    public List<HearingResponseDto> listAll() {
        return hearingRepository.findAll().stream().map(CourtCaseMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/by-case/{caseId}")
    public List<HearingResponseDto> listByCase(@PathVariable Long caseId) {
        return hearingRepository.findByCourtCaseId(caseId).stream().map(CourtCaseMapper::toDto).collect(Collectors.toList());
    }

    // mapping moved to CourtCaseMapper
}


