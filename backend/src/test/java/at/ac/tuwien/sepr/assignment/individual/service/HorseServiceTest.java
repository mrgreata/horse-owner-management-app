package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles({"test", "datagen"})
@SpringBootTest
class HorseServiceTest {

  @Autowired
  HorseService horseService;

  @Autowired
  JdbcTemplate jdbc; // <-- hinzufügen

  @Test
  void getAllReturnsAllStoredHorses() {
    var horses = horseService.allHorses().toList();

    assertThat(horses).isNotEmpty();
    assertThat(horses.stream().anyMatch(h -> h.sex() != null && h.sex().name().equals("FEMALE")))
            .isTrue();
  }

  @Test
  void create_valid_returnsDetailDtoWithId() throws Exception {
    var create = new HorseCreateDto(
            "Bella", "Test",
            LocalDate.of(2020, 5, 5),
            Sex.FEMALE, null
    );

    HorseDetailDto result = horseService.create(create);

    assertAll(
            () -> assertThat(result.id()).isNotNull(),
            () -> assertThat(result.name()).isEqualTo("Bella"),
            () -> assertThat(result.description()).isEqualTo("Test"),
            () -> assertThat(result.dateOfBirth()).isEqualTo(LocalDate.of(2020, 5, 5)),
            () -> assertThat(result.sex()).isEqualTo(Sex.FEMALE),
            () -> assertThat(result.owner()).isNull()
    );
  }

  @Test
  void create_invalid_throwsValidationException() {
    var create = new HorseCreateDto(
            "", "x", LocalDate.of(2099, 1, 1),
            null, null
    );

    assertThatThrownBy(() -> horseService.create(create))
            .isInstanceOf(ValidationException.class)
            .hasMessageStartingWith("Invalid horse");
  }

  @Test
  void delete_existingHorse_ok() {
    // Arrange: Pferd direkt anlegen (ohne Multi-Statement)
    jdbc.update("INSERT INTO horse(name, date_of_birth, sex) VALUES (?,?,?)",
            "ToDelete", Date.valueOf("2012-02-02"), "MALE");
    Long id = jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);

    // Act + Assert: darf keine Exception werfen
    assertThatCode(() -> horseService.delete(id)).doesNotThrowAnyException();

    // Verify: wirklich weg
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM horse WHERE id=?", Integer.class, id);
    assertThat(count).isZero();
  }

  @Test
  void delete_unknownHorse_throwsNotFound() {
    assertThatThrownBy(() -> horseService.delete(987654321L))
            .isInstanceOf(NotFoundException.class);
  }
}
