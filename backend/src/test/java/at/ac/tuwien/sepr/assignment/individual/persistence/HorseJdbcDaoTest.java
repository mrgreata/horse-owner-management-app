package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ActiveProfiles("test")
@SpringBootTest
class HorseJdbcDaoTest {

  @Autowired private HorseDao dao;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void clean() {
    jdbc.update("DELETE FROM horse");
    jdbc.update("DELETE FROM owner");
  }

  private long insertOwner(String first, String last) {
    jdbc.update("INSERT INTO owner(first_name, last_name) VALUES (?, ?)", first, last);
    return jdbc.queryForObject("SELECT MAX(id) FROM owner", Long.class);
  }

  @Test
  void insert_and_getById() throws Exception {
    var saved = dao.insert(new Horse(
            null, "Bella", "desc",
            LocalDate.of(2019, 3, 10), Sex.FEMALE,
            null, // ownerId
            null, // imagePath
            null, // imageContentType
            null, // motherId
            null  // fatherId
    ));

    var loaded = dao.getById(saved.id());
    assertEquals(saved.id(), loaded.id());
    assertEquals("Bella", loaded.name());
    assertThat(loaded.ownerId()).isNull();

  }

  @Test
  void update_setOwner_thenRemoveOwner() throws Exception {
    var saved = dao.insert(new Horse(
            null, "Amy", null,
            LocalDate.parse("2015-05-10"), Sex.FEMALE,
            null, // ownerId
            null, // imagePath
            null, // imageContentType
            null, // motherId
            null  // fatherId
    ));
    long ownerId = insertOwner("Ann", "Smith");

    var withOwner = dao.update(new HorseUpdateDto(
            saved.id(), "Amy", null, LocalDate.parse("2015-05-10"), Sex.FEMALE, ownerId, null, null));
    assertEquals(ownerId, withOwner.ownerId());

    var removedOwner = dao.update(new HorseUpdateDto(
            saved.id(), "Amy", null, LocalDate.parse("2015-05-10"), Sex.FEMALE, null, null, null));
    assertThat(removedOwner.ownerId()).isNull();
  }

  @Test
  void delete_then_getById_throwsNotFound() throws Exception {
    var saved = dao.insert(new Horse(
            null, "Cara", null,
            LocalDate.parse("2016-09-09"), Sex.FEMALE,
            null, // ownerId
            null, // imagePath
            null, // imageContentType
            null, // motherId
            null  // fatherId
    ));

    dao.delete(saved.id());
    assertThrows(NotFoundException.class, () -> dao.getById(saved.id()));
  }

  // imports: org.junit.jupiter.api.*, org.assertj.core.api.Assertions.*, org.springframework.beans.factory.annotation.Autowired;
  // ggf. @SpringBootTest oder @JdbcTest + @Import(HorseJdbcDao.class)

  @Test
  void delete_existingHorse_removesRow_andChildrenAreDetached() throws Exception {
    // Arrange: Parent + Child anlegen (zweischrittig, ohne SCOPE_IDENTITY)
    jdbc.update("INSERT INTO horse(name, date_of_birth, sex) VALUES (?,?,?)",
            "Parent", java.sql.Date.valueOf("2000-01-01"), "MALE");
    Long parentId = jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);

    jdbc.update("INSERT INTO horse(name, date_of_birth, sex, father_id) VALUES (?,?,?,?)",
            "Child", java.sql.Date.valueOf("2020-01-01"), "FEMALE", parentId);
    Long childId = jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);

    // Act
    dao.delete(parentId);

    // Assert: Parent weg
    Integer parentCount = jdbc.queryForObject("SELECT COUNT(*) FROM horse WHERE id=?", Integer.class, parentId);
    assertThat(parentCount).isZero();

    // Assert: Child hat FK auf NULL (wegen ON DELETE SET NULL)
    Long fk = jdbc.queryForObject("SELECT father_id FROM horse WHERE id=?", Long.class, childId);
    assertThat(fk).isNull();
  }

  @Test
  void delete_unknownId_throwsNotFound() {
    assertThatThrownBy(() -> dao.delete(999_999L))   // ✅ braucht den AssertJ-Static-Import
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("999999");
  }

  @Test
  void insert_withParents_persistsAndReadsParents() throws NotFoundException {
    // arrange: mother, father, child
    jdbc.update("INSERT INTO horse(name,date_of_birth,sex) VALUES(?,?,?)",
            "Mare", java.sql.Date.valueOf("2010-01-01"), "FEMALE");
    Long motherId = jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);

    jdbc.update("INSERT INTO horse(name,date_of_birth,sex) VALUES(?,?,?)",
            "Stallion", java.sql.Date.valueOf("2010-01-02"), "MALE");
    Long fatherId = jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);

    var child = new Horse(null, "Foal", null, LocalDate.parse("2016-05-03"), Sex.FEMALE,
            null, null, null, motherId, fatherId);
    var inserted = dao.insert(child);

    var reloaded = dao.getById(inserted.id()); // wirft NotFoundException
    assertThat(reloaded.motherId()).isEqualTo(motherId);
    assertThat(reloaded.fatherId()).isEqualTo(fatherId);
  }


  @Test
  void delete_parent_setsChildFkNull() throws Exception {
    jdbc.update("INSERT INTO horse(name,date_of_birth,sex) VALUES(?,?,?)",
            "M", java.sql.Date.valueOf("2010-01-01"), "FEMALE");
    Long motherId = jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);

    jdbc.update("INSERT INTO horse(name,date_of_birth,sex,mother_id) VALUES(?,?,?,?)",
            "C", java.sql.Date.valueOf("2016-01-01"), "FEMALE", motherId);
    Long childId = jdbc.queryForObject("SELECT MAX(id) FROM horse", Long.class);

    dao.delete(motherId); // ON DELETE SET NULL sollte greifen

    Long fk = jdbc.queryForObject("SELECT mother_id FROM horse WHERE id=?", Long.class, childId);
    assertThat(fk).isNull();
  }



}
