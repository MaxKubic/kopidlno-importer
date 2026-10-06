package cz.smartform.importer.repository;

import cz.smartform.importer.entity.Obec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObecRepository extends JpaRepository<Obec, Integer> {}