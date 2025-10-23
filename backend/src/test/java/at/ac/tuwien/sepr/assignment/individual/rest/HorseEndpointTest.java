package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;




@ActiveProfiles({"test", "datagen"})
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
class HorseEndpointTest {

  @Autowired private WebApplicationContext ctx;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JdbcTemplate jdbc;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
  }

  // --- US1: Liste ---
  @Test
  void gettingAllHorses() throws Exception {
    var body = mockMvc.perform(get("/horses").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> list = objectMapper.readerFor(HorseListDto.class)
            .<HorseListDto>readValues(body).readAll();

    assertThat(list)
            .hasSizeGreaterThanOrEqualTo(1)
            .extracting(HorseListDto::id, HorseListDto::name)
        .contains(tuple(-1L, "Wendy"));
  }

  // --- US1: Create 201 ---
  @Test
  void createHorse_returns201_andBody() throws Exception {
    long ownerId = insertOwner("Alex", "Miller");

    String body = """
      {
        "name": "Bucephalus",
        "description": "The legendary one",
        "dateOfBirth": "2000-01-01",
        "sex": "MALE",
        "ownerId": %d
      }
        """.formatted(ownerId);

    mockMvc.perform(post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("Bucephalus"))
            .andExpect(jsonPath("$.sex").value("MALE"))
            .andExpect(jsonPath("$.owner.id").value(ownerId)); // optionaler Extra-Check
  }



  // --- US1: Create invalid -> 422 ---
  @Test
  void createHorse_invalid_returns422_withErrors() throws Exception {
    var json = """
      {"name":"","description":"x","dateOfBirth":"2099-01-01","sex":null,"ownerId":null}
        """;

    var res = mockMvc.perform(post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

    var node = objectMapper.readTree(res.getResponse().getContentAsByteArray());
    assertThat(node.get("message").asText()).isEqualTo("Invalid horse");
    assertThat(node.get("errors").toString())
            .contains("name must not be empty")
            .contains("sex must not be null")
            .contains("dateOfBirth must not be in the future");
  }

  // --- US2: Update Owner setzen ---
  @Test
  void update_setsOwner() throws Exception {
    long horseId = createHorse("Amy", "2015-05-10", "FEMALE", null);
    long ownerId = insertOwner("Ann", "Smith");

    var payload = """
      {"name":"Amy","dateOfBirth":"2015-05-10","sex":"FEMALE","ownerId":%d}
        """.formatted(ownerId);

    mockMvc.perform(put("/horses/{id}", horseId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.owner.id").value(ownerId));
  }

  // --- US2: Update Owner entfernen ---
  @Test
  void update_removesOwner_whenNull() throws Exception {
    long ownerId = insertOwner("Ben", "Fox");
    long horseId = createHorse("Bob", "2014-03-01", "MALE", ownerId);

    var payload = """
      {"name":"Bob","dateOfBirth":"2014-03-01","sex":"MALE","ownerId":null}
        """;

    mockMvc.perform(put("/horses/{id}", horseId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.owner").doesNotExist());
  }

  // --- US2: Delete ---
  @Test
  void delete_removesHorse_and_then_404_on_get() throws Exception {
    long horseId = createHorse("Cara", "2016-09-09", "FEMALE", null);

    mockMvc.perform(delete("/horses/{id}", horseId))
            .andExpect(status().isNoContent());

    mockMvc.perform(get("/horses/{id}", horseId))
            .andExpect(status().isNotFound());
  }


  // Helpers
  private long createHorse(String name, String dob, String sex, Long ownerId) throws Exception {
    var json = """
      {"name":"%s","dateOfBirth":"%s","sex":"%s","ownerId":%s}
        """.formatted(name, dob, sex, ownerId == null ? "null" : ownerId.toString());

    var res = mockMvc.perform(post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();

    var node = objectMapper.readTree(res.getResponse().getContentAsByteArray());
    return node.get("id").asLong();
  }

  private long insertOwner(String first, String last) {
    jdbc.update("INSERT INTO owner(first_name,last_name) VALUES(?,?)", first, last);
    Number n = jdbc.queryForObject("SELECT MAX(id) FROM owner", Number.class);
    return n.longValue();
  }


  @Test
  void post_withValidParents_returns201AndParents() throws Exception {
    long mother = insertHorse("M", "2010-01-01", "FEMALE");
    long father = insertHorse("F", "2010-01-02", "MALE");

    String json = """
    {"name":"Candy","description":"Foal","dateOfBirth":"2016-05-03","sex":"FEMALE",
     "ownerId":null,"motherId":%d,"fatherId":%d}
        """.formatted(mother, father);

    mockMvc.perform(post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.motherId").value(mother))
            .andExpect(jsonPath("$.fatherId").value(father));
  }

  @Test
  void post_sameSexParents_returns422() throws Exception {
    long p1 = insertHorse("A", "2010-01-01", "MALE");
    long p2 = insertHorse("B", "2011-01-01", "MALE");

    String json = """
    {"name":"X","dateOfBirth":"2016-01-01","sex":"FEMALE",
     "motherId":%d,"fatherId":%d}
        """.formatted(p1, p2);

    mockMvc.perform(post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void post_unknownParent_returns404() throws Exception {
    String json = """
    {"name":"X","dateOfBirth":"2016-01-01","sex":"FEMALE","motherId":999999}
        """;

    mockMvc.perform(post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isNotFound());
  }

  private long insertHorse(String name, String dob, String sex) throws Exception {
    String json = """
    {"name":"%s","dateOfBirth":"%s","sex":"%s"}
        """.formatted(name, dob, sex);

    var result = mockMvc.perform(post("/horses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isCreated())
            .andReturn();

    String response = result.getResponse().getContentAsString();
    Number id = JsonPath.parse(response).read("$.id", Number.class);
    return id.longValue();

  }



}
