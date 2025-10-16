package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Integration test for {@link HorseService}.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
public class HorseServiceTest extends TestBase {

  @Autowired
  HorseService horseService;

  /**
   * Tests whether retrieving all stored horses returns the expected number and specific entries.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {
    List<HorseListDto> horses = horseService.allHorses()
        .toList();

    assertThat(horses)
        .hasSizeGreaterThanOrEqualTo(1) // TODO: Adapt to expected number of test data entries
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.FEMALE));
  }

  @Test
  public void create_valid_returnsDetailDtoWithId() throws Exception {
    var create = new HorseCreateDto(
            "Bella",
            "Test",
            LocalDate.of(2020, 5, 5),
            Sex.FEMALE,
            null
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
  public void create_invalid_throwsValidationException() {
    var create = new HorseCreateDto(
            "",        // invalid name
            "x",
            LocalDate.of(2099, 1, 1), // future
            null,      // invalid sex
            null
    );

    assertThatThrownBy(() -> horseService.create(create))
            .isInstanceOf(ValidationException.class)
            .hasMessageStartingWith("Invalid horse");
  }
}
