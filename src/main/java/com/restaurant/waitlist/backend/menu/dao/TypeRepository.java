package com.restaurant.waitlist.backend.menu.dao;

import com.restaurant.waitlist.backend.menu.model.Type;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {

    @Query("SELECT t FROM Type t WHERE t.status = 'ACTIVE'")
    List<Type> findAll();

    List<Type> findByNameInAndStatus(List<String> names, Status status);

    @Modifying
    @Query("UPDATE Type t SET t.status = 'INACTIVE' WHERE t.id = :id")
    void softDeleteById(Long id);
}
