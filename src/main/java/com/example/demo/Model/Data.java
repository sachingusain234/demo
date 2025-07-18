package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
@lombok.Data
@Table(name = "student")
public class Data {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long marks;
    private String country;
    @Version
    private Long version;
}
