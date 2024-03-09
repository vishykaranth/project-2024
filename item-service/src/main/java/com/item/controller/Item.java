package com.item.controller;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "items")
public class Item {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long id;

    @Column(name = "item_name")
    private String itemName;

//    public Item(String itemName){
//        this.itemName = itemName;
//    }
}
