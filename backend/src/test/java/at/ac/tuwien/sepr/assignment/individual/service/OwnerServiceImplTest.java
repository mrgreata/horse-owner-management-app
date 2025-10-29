package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.impl.OwnerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@ActiveProfiles("test")
class OwnerServiceImplTest {

  @Autowired
    OwnerServiceImpl service;

  @Test
  void create_valid_returnsDtoWithId() throws ValidationException {  // <— hinzufügen
    var dto = new OwnerCreateDto("Lisa", "Mayer", "lisa.mayer@test.tld");
    OwnerDto created = service.create(dto);
    assertThat(created.id()).isNotNull();
    assertThat(created.firstName()).isEqualTo("Lisa");
    assertThat(created.lastName()).isEqualTo("Mayer");
  }


  @Test
    void create_missingFirstName_throwsValidation() {
    var dto = new OwnerCreateDto("", "Mayer", "x@test.tld");
    assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Owner data invalid");
  }

  @Test
    void create_invalidEmail_throwsValidation() {
    var dto = new OwnerCreateDto("Max", "Muster", "not-an-email");
    assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);
  }

  @Test
    void search_delegatesAndMaps() {
    var res = service.search(new at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto("a", 5));
    assertThat(res).isNotNull();
    assertThat(res.limit(5).toList().size()).isLessThanOrEqualTo(5);
  }

  @Test
  void create_blankEmail_normalizedToNull() throws Exception {
    var dto = new OwnerCreateDto("Eva", "Gray", "   ");
    OwnerDto created = service.create(dto);
    assertThat(created.email()).isNull();
  }


  @Test
  void create_firstOrLastNameTooLong_throwsValidation() {
    String long256 = "x".repeat(256);
    assertThatThrownBy(() -> service.create(new OwnerCreateDto(long256, "Ok", null)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("firstName too long");
    assertThatThrownBy(() -> service.create(new OwnerCreateDto("Ok", long256, null)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("lastName too long");
  }
  @Test
  void getById_unknown_throwsNotFound() {
    assertThatThrownBy(() -> service.getById(9_999_999L))
            .isInstanceOf(at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException.class);
  }
  @Test
  void getAllById_missingOne_throwsNotFound() {
    try {
      var created = service.create(new OwnerCreateDto("Ann", "Smith", null));
      var ids = java.util.List.of(created.id(), 42_4242L);
      assertThatThrownBy(() -> service.getAllById(ids))
              .isInstanceOf(at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException.class);
    } catch (ValidationException e) {
      org.junit.jupiter.api.Assertions.fail("Setup should be valid but threw ValidationException", e);
    }
  }


}
