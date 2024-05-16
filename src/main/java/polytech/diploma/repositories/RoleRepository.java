package polytech.diploma.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import polytech.diploma.models.user.Role;
import polytech.diploma.models.user.RoleType;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
   Role findByRoleType(RoleType roleType);
}
