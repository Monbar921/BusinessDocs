package com.bot.individualentrepreneurbot.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CompanyDao extends CrudRepository<Company, Integer> {
    List<Company> findAllByOrderByName();
    Company findByName(String name);
}
