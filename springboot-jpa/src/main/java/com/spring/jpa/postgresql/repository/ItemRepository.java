package com.spring.jpa.postgresql.repository;

import com.spring.jpa.postgresql.model.Item;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, Long> {
}
