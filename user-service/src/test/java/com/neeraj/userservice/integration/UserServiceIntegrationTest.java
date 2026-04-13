package com.neeraj.userservice.integration;


import com.neeraj.userservice.dto.UserDTO;
import com.neeraj.userservice.entity.User;
import com.neeraj.userservice.repository.UserRepository;
import com.neeraj.userservice.testsupport.MySqlTestContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class UserServiceIntegrationTest extends MySqlTestContainerBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateUser_viaRestApi_persistsAndReturnsUser() {
        UserDTO request = UserDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .address("123 Main St")
                .alertEnabled(true)
                .energyAlertThreshold(100.0)
                .build();

        ResponseEntity<UserDTO> response = restTemplate
                                            .postForEntity("/api/v1/user", request, UserDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assert response.getBody() != null;
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo(request.getFirstName());
        assertThat(response.getBody().getLastName()).isEqualTo(request.getLastName());
        assertThat(response.getBody().getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getBody().getAddress()).isEqualTo(request.getAddress());
        assertThat(response.getBody().getAlertEnabled()).isEqualTo(request.getAlertEnabled());
        assertThat(response.getBody().getEnergyAlertThreshold()).isEqualTo(request.getEnergyAlertThreshold());

        ResponseEntity<UserDTO> loadedDBUser = restTemplate
                                            .getForEntity("/api/v1/user/" + response.getBody().getId(), UserDTO.class);

        assertThat(loadedDBUser.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loadedDBUser.getBody()).isNotNull();
        assert loadedDBUser.getBody() != null;
        assertThat(loadedDBUser.getBody().getId()).isEqualTo(response.getBody().getId());
        assertThat(loadedDBUser.getBody().getFirstName()).isEqualTo(request.getFirstName());
        assertThat(loadedDBUser.getBody().getLastName()).isEqualTo(request.getLastName());
        assertThat(loadedDBUser.getBody().getEmail()).isEqualTo(request.getEmail());
        assertThat(loadedDBUser.getBody().getAddress()).isEqualTo(request.getAddress());
        assertThat(loadedDBUser.getBody().getAlertEnabled()).isEqualTo(request.getAlertEnabled());
        assertThat(loadedDBUser.getBody().getEnergyAlertThreshold()).isEqualTo(request.getEnergyAlertThreshold());
    }

    @Test
    void saveUser_viaRepository_roundTripsThroughMysql() {
        User saved = userRepository.save(User.builder()
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace.it@example.com")
                .address("2 Compiler Way")
                .alertEnabled(false)
                .energyAlertThreshold(900.0)
                .build());

        assertThat(saved.getId()).isNotNull();

        User fromDb = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getEmail()).isEqualTo("grace.it@example.com");
        assertThat(fromDb.getFirstName()).isEqualTo("Grace");
        assertThat(fromDb.getAlertEnabled()).isFalse();
        assertThat(fromDb.getEnergyAlertThreshold()).isEqualTo(900.0);
    }


    @Test
    void updateUser_viaRestApi_persistsChanges() {
        UserDTO createRequest = UserDTO.builder()
                .firstName("Alan")
                .lastName("Turing")
                .email("alan.update.it@example.com")
                .address("10 Bletchley Park")
                .alertEnabled(true)
                .energyAlertThreshold(500.0)
                .build();

        ResponseEntity<UserDTO> created =
                restTemplate.postForEntity("/api/v1/user", createRequest, UserDTO.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assert created.getBody() != null;
        Long id = created.getBody().getId();

        UserDTO updateRequest = UserDTO.builder()
                .id(id)
                .firstName("Alan Mathison")
                .lastName("Turing")
                .email("alan.update.it@example.com")
                .address("12 Wilmslow Rd")
                .alertEnabled(false)
                .energyAlertThreshold(750.0)
                .build();

        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/api/v1/user/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<UserDTO> loaded =
                restTemplate.getForEntity("/api/v1/user/" + id, UserDTO.class);
        assertThat(loaded.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loaded.getBody()).isNotNull();
        assert loaded.getBody() != null;
        assertThat(loaded.getBody().getFirstName()).isEqualTo("Alan Mathison");
        assertThat(loaded.getBody().getAddress()).isEqualTo("12 Wilmslow Rd");
        assertThat(loaded.getBody().getAlertEnabled()).isFalse();
        assertThat(loaded.getBody().getEnergyAlertThreshold()).isEqualTo(750.0);
    }

    @Test
    void deleteUser_viaRestApi_removesUser() {
        UserDTO createRequest = UserDTO.builder()
                .firstName("Edsger")
                .lastName("Dijkstra")
                .email("edsger.delete.it@example.com")
                .address("3 Structured Programming Ln")
                .alertEnabled(false)
                .energyAlertThreshold(300.0)
                .build();

        ResponseEntity<UserDTO> created =
                restTemplate.postForEntity("/api/v1/user", createRequest, UserDTO.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assert created.getBody() != null;
        Long id = created.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/user/" + id,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<UserDTO> afterDelete =
                restTemplate.getForEntity("/api/v1/user/" + id, UserDTO.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
