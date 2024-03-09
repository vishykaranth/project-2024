package com.bezkoder.spring.files.csv.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.bezkoder.spring.files.csv.helper.CSVItemHelper;
import com.bezkoder.spring.files.csv.model.Item;
import com.bezkoder.spring.files.csv.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bezkoder.spring.files.csv.helper.CSVHelper;
import com.bezkoder.spring.files.csv.model.Tutorial;
import com.bezkoder.spring.files.csv.repository.TutorialRepository;

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
