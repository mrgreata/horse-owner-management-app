package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.TestBase;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Horse REST API endpoint.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest extends TestBase {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Sets up the MockMvc instance before each test.
   */
  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  /**
   * Tests retrieving all horses from the endpoint.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void gettingAllHorses() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    assertThat(horseResult)
        .isNotNull()
        .hasSizeGreaterThanOrEqualTo(1)
        .extracting(HorseListDto::id, HorseListDto::name)
        .contains(tuple(-1L, "Wendy"));

  }

  /**
   * Tests that accessing a nonexistent URL returns a 404 status.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/asdf123")
        ).andExpect(status().isNotFound());
  }

  @Test
  public void createHorse_returns201_andBody() throws Exception {
    var json = """
    {
      "name":"Bella",
      "description":"Test",
      "dateOfBirth":"2020-05-05",
      "sex":"FEMALE",
      "ownerId": null
    }
        """;

    var mvcResult = mockMvc.perform(
                    MockMvcRequestBuilders.post("/horses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();

    var body = mvcResult.getResponse().getContentAsByteArray();
    var dto = objectMapper.readValue(body, at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto.class);

    assertAll(
            () -> assertThat(dto.id()).isNotNull(),
            () -> assertThat(dto.name()).isEqualTo("Bella"),
            () -> assertThat(dto.description()).isEqualTo("Test"),
            () -> assertThat(dto.dateOfBirth().toString()).isEqualTo("2020-05-05"),
            () -> assertThat(dto.sex().name()).isEqualTo("FEMALE"),
            () -> assertThat(dto.owner()).isNull()
    );
  }

  @Test
  public void createHorse_invalid_returns422_withErrors() throws Exception {
    var json = """
    {
      "name":"",
      "description":"x",
      "dateOfBirth":"2099-01-01",
      "sex":null,
      "ownerId": null
    }
        """;

    var mvcResult = mockMvc.perform(
                    MockMvcRequestBuilders.post("/horses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

    var node = objectMapper.readTree(mvcResult.getResponse().getContentAsByteArray());
    assertAll(
            () -> assertThat(node.get("message").asText()).isEqualTo("Invalid horse"),
            () -> assertThat(node.get("errors").toString())
                    .contains("name must not be empty")
                    .contains("sex must not be null")
                    .contains("dateOfBirth must not be in the future")
    );
  }

}
