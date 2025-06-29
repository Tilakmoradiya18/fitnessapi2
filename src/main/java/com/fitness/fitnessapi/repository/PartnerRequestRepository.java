package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.PartnerRequest;
import com.fitness.fitnessapi.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerRequestRepository extends JpaRepository<PartnerRequest, Long> {
    Optional<PartnerRequest> findBySenderIdAndSlotIdAndStatus(Long senderId, Long slotId, RequestStatus status);
    List<PartnerRequest> findByReceiverIdAndStatusIn(Long receiverId, List<RequestStatus> statuses);
    List<PartnerRequest> findBySenderId(Long senderId);
}
