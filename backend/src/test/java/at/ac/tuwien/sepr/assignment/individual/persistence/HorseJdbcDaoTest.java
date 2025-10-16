package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
class HorseJdbcDaoTest {

  @Autowired
  private HorseDao horseDao;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanDb() {
    // Reihenfolge ist wichtig, falls später FKs dazukommen
    jdbcTemplate.update("DELETE FROM horse");
  }

  @Test
  void insertShouldReturnGeneratedId() {
    Horse horse = new Horse(
            null,
            "JUnity",
            "Test horse",
            LocalDate.of(2021, 5, 5),
            Sex.FEMALE,
            null,   // ownerId
            null,   // imagePath
            null    // imageContentType
    );

    Horse saved = horseDao.insert(horse);

    assertNotNull(saved.id(), "ID should be generated");
    assertEquals("JUnity", saved.name());
    assertEquals(Sex.FEMALE, saved.sex());
  }

  @Test
  void getByIdReturnsInsertedHorse() throws NotFoundException {
    Horse saved = horseDao.insert(new Horse(
            null,
            "Bella",
            "desc",
            LocalDate.of(2019, 3, 10),
            Sex.FEMALE,
            null,   // ownerId
            null,   // imagePath
            null    // imageContentType
    ));

    Horse loaded = horseDao.getById(saved.id());

    assertEquals(saved.id(), loaded.id());
    assertEquals("Bella", loaded.name());
  }

  @Test
  void getByIdThrowsWhenNotFound() {
    assertThrows(NotFoundException.class, () -> horseDao.getById(999_999L));
  }

  @Test
  void insert_persists_andReturnsEntityWithId() throws Exception {
    var entity = new Horse(
            null, "Bella", "Test", LocalDate.of(2020, 5, 5), Sex.FEMALE,
            null,  // ownerId
            null,  // imagePath
            null   // imageContentType
    );

    var saved = horseDao.insert(entity);
    assertAll(
            () -> assertThat(saved.id()).isNotNull(),
            () -> assertThat(saved.name()).isEqualTo("Bella")
    );

    var loaded = horseDao.getById(saved.id());
    assertAll(
            () -> assertThat(loaded.name()).isEqualTo("Bella"),
            () -> assertThat(loaded.description()).isEqualTo("Test"),
            () -> assertThat(loaded.dateOfBirth()).isEqualTo(LocalDate.of(2020, 5, 5)),
            () -> assertThat(loaded.sex()).isEqualTo(Sex.FEMALE),
            () -> assertThat(loaded.ownerId()).isNull()
    );
  }
}
