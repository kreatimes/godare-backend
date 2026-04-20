package com.godare.app.service;

import com.godare.app.entity.Range;
import com.godare.app.entity.User;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.RangeRepository;
import com.godare.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RangeService {

    private final RangeRepository rangeRepository;
    private final UserRepository userRepository;

    public List<Range> getAllRanges() {
        return rangeRepository.findAllByOrderByLevelAsc();
    }

    public Range getRangeByLevel(Integer level) {
        return rangeRepository.findByLevel(level)
                .orElseThrow(() -> new BadRequestException("Range not found"));
    }

    public Range getRangeByPoints(Integer points) {
        return rangeRepository
                .findByMinPointsLessThanEqualAndMaxPointsGreaterThanEqual(points, points)
                .orElseThrow(() -> new BadRequestException("No range configured for these points"));
    }

    public void updateUserRange(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Range range = getRangeByPoints(user.getPoints());

        user.setRange(range.getLevel());
        userRepository.save(user);
    }
}