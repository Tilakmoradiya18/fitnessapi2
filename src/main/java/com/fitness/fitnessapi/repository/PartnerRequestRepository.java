package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.PartnerRequest;
import com.fitness.fitnessapi.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface PartnerRequestRepository extends JpaRepository<PartnerRequest, Long> {
    Optional<PartnerRequest> findBySenderIdAndSlotIdAndStatus(Long senderId, Long slotId, RequestStatus status);
    List<PartnerRequest> findByReceiverIdAndStatusIn(Long receiverId, List<RequestStatus> statuses);
    List<PartnerRequest> findBySenderId(Long senderId);
    List<PartnerRequest> findByStatus(RequestStatus status);

    @Query("SELECT r FROM PartnerRequest r WHERE r.sender.id = :senderId AND r.slot.date = :date AND r.status = :status")
    List<PartnerRequest> findBySenderIdAndSlotDateAndStatus(Long senderId, LocalDate date, RequestStatus status);

}
