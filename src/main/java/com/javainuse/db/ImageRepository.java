package com.javainuse.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javainuse.model.ImageModel;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageModel, Long> {
	Optional<ImageModel> findByName(String name);
}
