package com.spring.files.csv.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.files.csv.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
}
