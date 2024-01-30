package com.epam.repository;

import com.epam.entity.Mp3File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Mp3File, Integer> {
}
