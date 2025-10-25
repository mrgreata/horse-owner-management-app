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

import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;




@ActiveProfiles("test")
@SpringBootTest
class HorseServiceTest {

  @Autowired
  HorseService horseService;

  @Autowired
  JdbcTemplate jdbc; // <-- hinzufügen

  @Test
  void getAllReturnsAllStoredHorses() {

    jdbc.update("INSERT INTO horse(name, date_of_birth, sex) VALUES (?,?,?)",
            "Any", Date.valueOf("2010-01-01"), "FEMALE");


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
            Sex.FEMALE,
            null,   // ownerId
            null,   // motherId
            null    // fatherId
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
            null,
            null,  // ownerId
            null,  // motherId
            null   // fatherId
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


  //US4
  @Test
  void create_sameSexParents_throwsValidation() {
    long p1 = insertHorse("A", "2010-01-01", "MALE");
    long p2 = insertHorse("B", "2011-01-01", "MALE");

    var dto = new HorseCreateDto(
            "X", null,
            LocalDate.parse("2016-01-01"),
            Sex.FEMALE,
            null,    // ownerId
            p1,      // motherId (absichtlich MALE -> Regel wird im Validator geprüft)
            p2       // fatherId
    );

    assertThatThrownBy(() -> horseService.create(dto))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Parents must be of opposite sex");
  }

  @Test
  void update_selfParent_throwsValidation() throws Exception {
    long child = insertHorse("Kid", "2016-01-01", "MALE");

    var dto = new HorseUpdateDto(
            child, "Kid", null,
            LocalDate.parse("2016-01-01"),
            Sex.MALE,
            null,       // ownerId
            child,      // motherId -> self-parent
            null        // fatherId
    );

    assertThatThrownBy(() -> horseService.update(dto))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("cannot be its own mother");
  }

  @Test
  void create_parentNotOlder_throwsValidation() {
    long youngMother = insertHorse("M", "2020-01-01", "FEMALE");

    var dto = new HorseCreateDto(
            "Kid", null,
            LocalDate.parse("2020-01-01"),
            Sex.MALE,
            null,           // ownerId
            youngMother,    // motherId (gleich alt wie Kind)
            null
    );

    assertThatThrownBy(() -> horseService.create(dto))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Mother must be older than child");
  }

  @Test
  void create_unknownParent_throwsNotFound() {
    var dto = new HorseCreateDto(
            "Kid", null,
            LocalDate.parse("2016-01-01"),
            Sex.MALE,
            null,         // ownerId
            999_999L,     // motherId unbekannt
            null
    );

    assertThatThrownBy(() -> horseService.create(dto))
            .isInstanceOf(NotFoundException.class);
  }

  // ---------- Helpers ----------

  private long insertHorse(String name, String dob, String sex) {
    jdbc.update("INSERT INTO horse(name, date_of_birth, sex) VALUES (?,?,?)",
            name, Date.valueOf(dob), sex);
    return jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);
  }

  // ----- US6 ------

  @BeforeEach
  void clean() {
    jdbc.update("DELETE FROM horse");
    jdbc.update("DELETE FROM owner");
  }
  @Test
  void search_ownerNameFilters_andOwnerIsMappedInListDto() {
    // Arrange
    jdbc.update("INSERT INTO owner (id, first_name, last_name) VALUES (200, 'Anna','Smith')");
    jdbc.update("INSERT INTO owner (id, first_name, last_name) VALUES (201, 'Bob','Jones')");
    jdbc.update("""
    INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id)
    VALUES (10, 'Comet', 'sprinter', '2018-01-01', 'MALE', 200)
        """);
    jdbc.update("""
    INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id)
    VALUES (11, 'Aurora', 'calm mare', '2010-07-07', 'FEMALE', 201)
        """);

    // Act
    List<HorseListDto> result = horseService.search(
            new HorseSearchDto(null, null, null, null, "Anna", null)
    );

    // Assert
    assertThat(result).hasSize(1);
    HorseListDto dto = result.getFirst();
    assertThat(dto.name()).isEqualTo("Comet");
    assertThat(dto.owner()).isNotNull();
    assertThat(dto.owner().firstName()).isEqualTo("Anna");
    assertThat(dto.owner().lastName()).isEqualTo("Smith");
  }

  @Test
  void search_combinedFilters_work() {
    // Arrange
    jdbc.update("INSERT INTO owner (id, first_name, last_name) VALUES (210, 'X','Y')");
    jdbc.update("""
    INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id)
    VALUES (20, 'Stardust', 'calm mare', '2010-07-07', 'FEMALE', 210)
        """);
    jdbc.update("""
    INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id)
    VALUES (21, 'Starwind', 'fast', '2015-05-10', 'MALE', 210)
        """);

    // Act
    var criteria = new HorseSearchDto("star", "calm", LocalDate.parse("2012-01-01"), Sex.FEMALE, "X", null);
    var result = horseService.search(criteria);

    // Assert
    assertThat(result).extracting(HorseListDto::name).containsExactly("Stardust");
  }





}
