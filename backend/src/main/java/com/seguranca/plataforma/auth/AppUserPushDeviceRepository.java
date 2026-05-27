package com.seguranca.plataforma.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserPushDeviceRepository extends JpaRepository<AppUserPushDevice, Long> {
    List<AppUserPushDevice> findByUserIdInAndRevokedAtIsNullOrderByUpdatedAtDesc(List<Long> userIds);

    List<AppUserPushDevice> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<AppUserPushDevice> findByUserIdAndExpoPushToken(Long userId, String expoPushToken);

    Optional<AppUserPushDevice> findByExpoPushToken(String expoPushToken);
}


