package com.spring.jpa.postgresql.controller;


import com.spring.jpa.postgresql.model.Item;
import com.spring.jpa.postgresql.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class ItemController {

	@Autowired
	ItemRepository itemRepository;

	@GetMapping("/items")
	public ResponseEntity<List<Item>> getItems(@RequestParam(required = false) String title) {
		try {
			List<Item> itemList = new ArrayList<>();
			itemRepository.findAll().forEach(itemList::add);

			if (itemList.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(itemList, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//	@GetMapping("/Items/{id}")
//	public ResponseEntity<Item> getItemById(@PathVariable("id") long id) {
//		Optional<Item> ItemData = itemRepository.findById(id);
//
//		if (ItemData.isPresent()) {
//			return new ResponseEntity<>(ItemData.get(), HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
//	}

	@PostMapping("/items")
	public ResponseEntity<Item> createItem(@RequestBody Item item) {
		try {
			Item item1 = new Item(item.getItemName());
			Item _Item = itemRepository.save(item1);
			return new ResponseEntity<>(_Item, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//
//	@PutMapping("/Items/{id}")
//	public ResponseEntity<Item> updateItem(@PathVariable("id") long id, @RequestBody Item Item) {
//		Optional<Item> ItemData = itemRepository.findById(id);
//
//		if (ItemData.isPresent()) {
//			Item _Item = ItemData.get();
//			_Item.setTitle(Item.getTitle());
//			_Item.setDescription(Item.getDescription());
//			_Item.setPublished(Item.isPublished());
//			return new ResponseEntity<>(itemRepository.save(_Item), HttpStatus.OK);
//		} else {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
//	}
//
//	@DeleteMapping("/Items/{id}")
//	public ResponseEntity<HttpStatus> deleteItem(@PathVariable("id") long id) {
//		try {
//			itemRepository.deleteById(id);
//			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//		} catch (Exception e) {
//			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//
//	@DeleteMapping("/Items")
//	public ResponseEntity<HttpStatus> deleteAllItems() {
//		try {
//			itemRepository.deleteAll();
//			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//		} catch (Exception e) {
//			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//
//	}
//
//	@GetMapping("/Items/published")
//	public ResponseEntity<List<Item>> findByPublished() {
//		try {
//			List<Item> Items = itemRepository.findByPublished(true);
//
//			if (Items.isEmpty()) {
//				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//			}
//			return new ResponseEntity<>(Items, HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

}
