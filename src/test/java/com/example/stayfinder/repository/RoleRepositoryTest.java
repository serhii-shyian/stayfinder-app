package com.example.stayfinder.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.stayfinder.model.Role;
import com.example.stayfinder.repository.role.RoleRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/delete-all-data-before-tests.sql"));
        }
    }

    @Test
    @DisplayName("""
            Find roles by set of role names
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByNameContaining_ExistingRoleNames_ReturnsRoles() {
        // Given
        Set<Role.RoleName> rolesSet = new HashSet<>();
        rolesSet.add(Role.RoleName.ADMIN);
        rolesSet.add(Role.RoleName.USER);

        // When
        Set<Role> roles = roleRepository.findAllByNameContaining(rolesSet);

        // Then
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(
                role -> role.getName().equals(Role.RoleName.ADMIN)));
        assertTrue(roles.stream().anyMatch(
                role -> role.getName().equals(Role.RoleName.USER)));
    }

    @Test
    @DisplayName("""
            Find roles by a single role name
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByNameContaining_SingleRoleName_ReturnsSingleRole() {
        // Given
        Set<Role.RoleName> rolesSet = new HashSet<>();
        rolesSet.add(Role.RoleName.ADMIN);

        // When
        Set<Role> roles = roleRepository.findAllByNameContaining(rolesSet);

        // Then
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.stream().anyMatch(
                role -> role.getName().equals(Role.RoleName.ADMIN)));
    }

    @Test
    @DisplayName("""
            Find roles with an empty set of role names
            """)
    void findAllByNameContaining_EmptyRoleNames_ReturnsEmpty() {
        // Given
        Set<Role.RoleName> rolesSet = new HashSet<>();

        // When
        Set<Role> roles = roleRepository.findAllByNameContaining(rolesSet);

        // Then
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("""
            Find roles by partial role name match
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByNameContaining_PartialRoleNameMatch_ReturnsMatchingRoles() {
        // Given
        Set<Role.RoleName> rolesSet = new HashSet<>();
        rolesSet.add(Role.RoleName.ADMIN);
        rolesSet.add(Role.RoleName.USER);

        // When
        Set<Role> roles = roleRepository.findAllByNameContaining(rolesSet);

        // Then
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertTrue(roles.stream().anyMatch(
                role -> role.getName().toString().contains("ADMIN")));
    }
}
