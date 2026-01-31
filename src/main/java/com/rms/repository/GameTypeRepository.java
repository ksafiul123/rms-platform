package com.rms.repository;

import com.rms.entity.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameTypeRepository extends JpaRepository<GameType, Long> {

    List<GameType> findByIsActiveTrueOrderByName();

    Optional<GameType> findByCode(String code);

    List<GameType> findByGameMode(GameType.GameMode gameMode);
}
