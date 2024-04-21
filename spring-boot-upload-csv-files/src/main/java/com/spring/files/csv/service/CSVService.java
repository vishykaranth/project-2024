package com.spring.files.csv.service;

import java.io.IOException;
import java.util.List;

import com.spring.files.csv.helper.CSVItemHelper;
import com.spring.files.csv.model.Item;
import com.spring.files.csv.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CSVService {
  @Autowired
  ItemRepository repository;

  public void save(MultipartFile file) {
    try {
      List<Item> items = CSVItemHelper.csvToTutorials(file.getInputStream());
      System.out.println(items);
      repository.saveAll(items);
    } catch (IOException e) {
      throw new RuntimeException("fail to store csv data: " + e.getMessage());
    }
  }
//
//  public ByteArrayInputStream load() {
//    List<Tutorial> tutorials = repository.findAll();
//
//    ByteArrayInputStream in = CSVHelper.tutorialsToCSV(tutorials);
//    return in;
//  }

//  public List<Item> getAllTutorials() {
//    return repository.findAll();
//  }
}
