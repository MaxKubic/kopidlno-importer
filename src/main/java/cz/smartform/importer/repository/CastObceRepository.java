package cz.smartform.importer.repository;

import cz.smartform.importer.entity.CastObce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastObceRepository extends JpaRepository<CastObce, Integer> {}