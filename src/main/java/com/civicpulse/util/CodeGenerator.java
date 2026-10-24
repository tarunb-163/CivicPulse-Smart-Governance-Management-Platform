package com.civicpulse.util;

import com.civicpulse.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private final ComplaintRepository complaintRepository;
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    private int currentYear = 0;

    public synchronized String generateComplaintCode() {
        int year = ZonedDateTime.now(ZoneId.of("UTC")).getYear();
        if (year != currentYear) {
            currentYear = year;
            ZonedDateTime startOfYear = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime endOfYear = ZonedDateTime.of(year + 1, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            long count = complaintRepository.countComplaintsInYear(startOfYear.toInstant(), endOfYear.toInstant());
            sequenceCounter.set(count);
        }

        long nextVal = sequenceCounter.incrementAndGet();
        return String.format("CP-%04d-%06d", year, nextVal);
    }

    public static String generate(int year, long seq) {
        return String.format("CP-%04d-%06d", year, seq);
    }
}
