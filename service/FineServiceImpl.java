package com.library.service;

import com.library.entity.Fine;
import com.library.entity.User;
import com.library.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FineServiceImpl {

    private final FineRepository fineRepository;

    public Fine findById(Long id) {
        return fineRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Fine not found: " + id));
    }

    public Page<Fine> findAll(Pageable pageable) {
        return fineRepository.findAll(pageable);
    }

    public Page<Fine> findByUser(User user, Pageable pageable) {
        return fineRepository.findByUser(user, pageable);
    }

    public List<Fine> findUnpaidByUser(User user) {
        return fineRepository.findUnpaidByUser(user);
    }

    public BigDecimal getTotalUnpaid(User user) {
        BigDecimal total = fineRepository.getTotalUnpaidByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Fine payFine(Long fineId, BigDecimal amount) {
        Fine fine = findById(fineId);
        if (fine.getStatus() == Fine.Status.PAID || fine.getStatus() == Fine.Status.WAIVED) {
            throw new IllegalStateException("Fine already settled");
        }

        BigDecimal newPaid = fine.getPaidAmount().add(amount);
        if (newPaid.compareTo(fine.getAmount()) > 0) {
            newPaid = fine.getAmount();
        }
        fine.setPaidAmount(newPaid);

        if (newPaid.compareTo(fine.getAmount()) >= 0) {
            fine.setStatus(Fine.Status.PAID);
            fine.setPaidDate(LocalDate.now());
        } else {
            fine.setStatus(Fine.Status.PARTIAL);
        }

        return fineRepository.save(fine);
    }

    public Fine waiveFine(Long fineId) {
        Fine fine = findById(fineId);
        fine.setStatus(Fine.Status.WAIVED);
        fine.setPaidDate(LocalDate.now());
        return fineRepository.save(fine);
    }
}
