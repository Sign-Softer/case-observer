package ro.signsofter.caseobserver.service;

import lombok.Data;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.HearingDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.PartyDto;

import java.util.ArrayList;
import java.util.List;

public interface CaseChangeDetectorService {
    
    CaseChanges detectChanges(CourtCase existingCase, CaseDetailsDto newData);
    
    @Data
    class CaseChanges {
        private boolean hasChanges = false;
        private boolean statusChanged = false;
        private boolean proceduralStageChanged = false;
        private boolean hearingsChanged = false;
        private boolean partiesChanged = false;
        private boolean categoryChanged = false;
        private boolean subjectChanged = false;
        private boolean departmentChanged = false;
        
        private String oldStatus;
        private String newStatus;
        private String oldProceduralStage;
        private String newProceduralStage;
        private String oldCategory;
        private String newCategory;
        private String oldSubject;
        private String newSubject;
        private String oldDepartment;
        private String newDepartment;
        
        private List<HearingChange> hearingChanges = new ArrayList<>();
        private List<PartyChange> partyChanges = new ArrayList<>();
        
        public boolean hasAnyChanges() {
            return statusChanged || proceduralStageChanged || hearingsChanged || 
                   partiesChanged || categoryChanged || subjectChanged || departmentChanged;
        }
        
        @Data
        public static class HearingChange {
            private String type; // ADDED, UPDATED, REMOVED
            private HearingDto hearing;
            private String description;
        }
        
        @Data
        public static class PartyChange {
            private String type; // ADDED, UPDATED, REMOVED
            private PartyDto party;
            private String description;
        }
    }
}
