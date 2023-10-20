package com.bot.individualentrepreneurbot.dao;

import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompanyDaoHandler {
    private Company lastCompany;

    public Company getLastCompany() {
        return lastCompany;
    }

    public void setLastCompany(Company lastCompany) {
        this.lastCompany = lastCompany;
    }

    @Autowired
    private CompanyDao companyDao;

    public String returnAllRecords() throws NotFoundException {
        StringBuilder result = new StringBuilder();
        List<Company> companies = companyDao.findAllByOrderByName();
        if (companies.size() == 0) {
            throw new NotFoundException();
        }
        for (Company company : companies) {
            result.append(company.toString()).append("\n");
        }
        return result.toString();
    }

    public Company returnCompanyByName(String name) throws NotFoundException {
        Company company = companyDao.findByName(name);
        if (company == null) {
            throw new NotFoundException();
        }
        return company;
    }


    public Company returnCompanyById(int id) throws NotFoundException {
        Company company = companyDao.findById(id);
        if (company == null) {
            throw new NotFoundException();
        }
        return company;
    }

}
