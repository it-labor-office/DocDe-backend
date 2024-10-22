package com.docde.domain.hospital.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Hospital {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
