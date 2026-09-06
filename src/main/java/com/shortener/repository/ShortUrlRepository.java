package com.shortener.repository;

import com.shortener.model.db.ShortUrl;
import com.shortener.model.db.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    boolean existsByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCode(String shortCode);

    List<ShortUrl> findByUserIdOrderByCreatedTimestampDesc(Long userId);

    Optional<ShortUrl> findByIdAndUserId(Long id, Long userId);
}
