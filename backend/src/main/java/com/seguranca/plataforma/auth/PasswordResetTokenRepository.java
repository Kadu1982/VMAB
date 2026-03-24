package com.seguranca.plataforma.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUserIdAndTokenHashAndConsumedAtIsNullAndExpiresAtAfter(Long userId, String tokenHash, OffsetDateTime now);

    List<PasswordResetToken> findByUserIdAndConsumedAtIsNull(Long userId);

    long deleteByExpiresAtBefore(OffsetDateTime expiresAt);

    @Modifying
    @Query("update PasswordResetToken token set token.consumedAt = :now where token.userId = :userId and token.consumedAt is null")
    int consumeActiveTokensByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now);
}
