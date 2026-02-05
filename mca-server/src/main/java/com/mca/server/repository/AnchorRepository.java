package com.mca.server.repository;

import com.mca.server.entity.Anchor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnchorRepository extends JpaRepository<Anchor, String> {

    List<Anchor> findByUserId(String userId);

    Optional<Anchor> findByUserIdAndTiktokId(String userId, String tiktokId);
}
