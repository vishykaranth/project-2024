package com.spring.files.csv.repository;

import com.spring.files.csv.model.Item;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, Long> {
}
