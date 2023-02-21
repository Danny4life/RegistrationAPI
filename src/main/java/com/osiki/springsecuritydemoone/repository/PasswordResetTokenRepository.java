package com.osiki.springsecuritydemoone.repository;

import com.osiki.springsecuritydemoone.entity.PasswordResetToken;
import com.osiki.springsecuritydemoone.model.PasswordModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long > {
    PasswordResetToken findByToken(String token);
}
