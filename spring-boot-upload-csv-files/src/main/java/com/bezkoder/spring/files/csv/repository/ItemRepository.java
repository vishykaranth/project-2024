package com.bezkoder.spring.files.csv.repository;

import com.bezkoder.spring.files.csv.model.Item;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, Long> {
}
