package com.training.demo.repository;

import com.training.demo.entity.Role;
import com.training.demo.utils.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name
     * @param name name
     * @return Role
     */
    Optional<Role> findByName(RoleType name);

}
