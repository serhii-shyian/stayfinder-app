package com.example.stayfinder.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.user.UserRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
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
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

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
            Find user by existing username
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUsername_ExistingUsername_ReturnsUser() {
        // Given
        String username = "john.doe";

        // When
        Optional<User> optionalUser = userRepository.findByUsername(username);

        // Then
        assertTrue(optionalUser.isPresent());
        assertEquals(username, optionalUser.get().getUsername());
    }

    @Test
    @DisplayName("""
            Find user by non-existing username
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUsername_NonExistingUsername_ReturnsEmptyOptional() {
        // Given
        String username = "non-existing-user";

        // When
        Optional<User> optionalUser = userRepository.findByUsername(username);

        // Then
        assertFalse(optionalUser.isPresent());
    }
}
