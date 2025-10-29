package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.impl.OwnerJdbcDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;


@SpringBootTest
@ActiveProfiles("test")
class OwnerJdbcDaoTest {

  @Autowired
  OwnerJdbcDao dao;

  @Test
  void create_persistsAndReturnsGeneratedId() throws NotFoundException {
    Owner toCreate = new Owner(null, "Jane", "Doe", "jane.doe@test.tld");
    Owner created = dao.create(toCreate);

    assertThat(created.id()).isNotNull();

    Owner byId = dao.getById(created.id());
    assertThat(byId.firstName()).isEqualTo("Jane");
    assertThat(byId.lastName()).isEqualTo("Doe");
    assertThat(byId.email()).isEqualTo("jane.doe@test.tld");
  }

  @Test
  void getById_unknown_throwsNotFound() {
    assertThatThrownBy(() -> dao.getById(9_999_999L))
            .isInstanceOf(NotFoundException.class);
  }

  @Test
  void search_byName_honorsLimit() {
    var resAll = dao.search(new OwnerSearchDto("a", 100));
    var resLimited = dao.search(new OwnerSearchDto("a", 2));
    assertThat(resAll.size()).isGreaterThanOrEqualTo(resLimited.size());
    assertThat(resLimited.size()).isLessThanOrEqualTo(2);
  }

  @Test
  void getAllById_bestEffort_returnsOnlyExisting() {
    var ids = List.of(-1L, -2L, 42_4242L);
    var owners = dao.getAllById(ids);
    assertThat(owners).extracting(Owner::id).doesNotContain(42_4242L);
  }

  @Test
  void search_name_isCaseInsensitive() {
    dao.create(new Owner(null, "Ann", "Smith", null));
    dao.create(new Owner(null, "bob", "FOX", null));
    var res = dao.search(new OwnerSearchDto("an", 10));
    assertThat(res).extracting(Owner::firstName).contains("Ann");
  }

  @Test
  void search_withoutParams_returnsSome() {
    dao.create(new Owner(null, "Jane", "Doe", null));
    dao.create(new Owner(null, "John", "Doe", null));
    var res = dao.search(new OwnerSearchDto(null, null));
    assertThat(res.size()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void create_emailBlank_isStoredNull_andTrimmed() throws Exception {
    var created1 = dao.create(new Owner(null, "Eva", "Gray", "  "));
    assertThat(created1.email()).isNull();

    var created2 = dao.create(new Owner(null, "Max", "Payne", "  max@test.tld  "));
    var re = dao.getById(created2.id());
    assertThat(re.email()).isEqualTo("max@test.tld");
  }

  @Test
  void getAllById_emptyInput_returnsEmpty() {
    assertThat(dao.getAllById(List.of())).isEmpty();
  }



}
