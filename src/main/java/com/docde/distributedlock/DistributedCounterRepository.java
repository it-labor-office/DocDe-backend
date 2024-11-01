package com.docde.distributedlock;


import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributedCounterRepository extends JpaRepository<DistributedCounter, Long> {
}