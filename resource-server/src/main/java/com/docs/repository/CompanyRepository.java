package com.docs.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CompanyRepository extends CrudRepository<com.docs.entity.Company, Integer> {
    List<com.docs.entity.Company> findAllByOrderByName();
    com.docs.entity.Company findByName(String name);
    com.docs.entity.Company findById(int id);
}
