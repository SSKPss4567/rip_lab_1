package com.moviecatalog.repository;

import com.moviecatalog.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {

    Optional<Director> findByFirstNameAndLastName(String firstName, String lastName);
}

