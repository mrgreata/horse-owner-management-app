package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles({"test", "datagen"})
@SpringBootTest
class HorseDaoTest {

  @Autowired
    HorseDao horseDao;

  @Test
    void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.getAll();
    assertThat(horses)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(Horse::id, Horse::name)
                .contains(tuple(-1L, "Wendy"));
  }
}
