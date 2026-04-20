package com.godare.app.service;

import com.godare.app.dto.RankingResponse;
import com.godare.app.entity.Range;
import com.godare.app.entity.User;
import com.godare.app.exception.BadRequestException;
import com.godare.app.repository.RangeRepository;
import com.godare.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;
    private final RangeRepository rangeRepository;

    public List<RankingResponse> getGlobalRanking(int limit) {

        List<User> users = userRepository
                .findAllByOrderByPointsDesc(PageRequest.of(0, limit))
                .getContent();

        AtomicInteger position = new AtomicInteger(1);

        return users.stream()
                .map(user -> {
                    Range range = rangeRepository.findByLevel(user.getRange())
                            .orElse(null);

                    return RankingResponse.builder()
                            .userId(user.getId())
                            .nick(user.getNick())
                            .userImageUrl(user.getUserImageUrl())
                            .points(user.getPoints())
                            .rangeLevel(user.getRange())
                            .rangeName(range != null ? range.getName() : null)
                            .rangeImageUrl(range != null ? range.getImageUrl() : null)
                            .position(position.getAndIncrement())
                            .build();
                })
                .toList();
    }

    public List<RankingResponse> getFollowingRanking(String userId, int limit) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        List<String> ids = new ArrayList<>(user.getFollowing());
        ids.add(userId);

        List<User> users = userRepository
                .findByIdInOrderByPointsDesc(ids, PageRequest.of(0, limit))
                .getContent();

        AtomicInteger position = new AtomicInteger(1);

        return users.stream()
                .map(u -> {
                    Range range = rangeRepository.findByLevel(u.getRange())
                            .orElse(null);

                    return RankingResponse.builder()
                            .userId(u.getId())
                            .nick(u.getNick())
                            .userImageUrl(u.getUserImageUrl())
                            .points(u.getPoints())
                            .rangeLevel(u.getRange())
                            .rangeName(range != null ? range.getName() : null)
                            .rangeImageUrl(range != null ? range.getImageUrl() : null)
                            .position(position.getAndIncrement())
                            .build();
                })
                .toList();
    }
}