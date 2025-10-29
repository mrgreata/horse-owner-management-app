package at.ac.tuwien.sepr.assignment.individual.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OwnerEndpointTest {

  @Autowired
    MockMvc mvc;

  @Autowired
    ObjectMapper om;

  @Test
  void postOwners_valid_returns201WithLocationAndBody() throws Exception {
    var dto = new OwnerCreateDto("Nina", "Kern", "nina.kern@test.tld");

    var res = mvc.perform(post("/owners")
                    .contentType(MediaType.APPLICATION_JSON) // <-- Request content type
                    .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(".*/owners/\\d+"))) // robuster
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // <-- Response assertion
            .andReturn();

    var body = res.getResponse().getContentAsString();
    var created = om.readTree(body);
    assertThat(created.get("id").asLong()).isPositive();
    assertThat(created.get("firstName").asText()).isEqualTo("Nina");
    assertThat(created.get("lastName").asText()).isEqualTo("Kern");
  }


  @Test
    void postOwners_missingLastName_returns422() throws Exception {
    var dto = new OwnerCreateDto("Paul", "", "paul@test.tld");
    mvc.perform(post("/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
  }



  @Test
    void getOwners_search_returns200() throws Exception {
    mvc.perform(get("/owners").queryParam("name", "a").queryParam("maxAmount", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void getOwners_search_respectsLimit() throws Exception {
    var res = mvc.perform(get("/owners")
                    .queryParam("name", "a")
                    .queryParam("maxAmount", "3"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

    var json = om.readTree(res.getResponse().getContentAsString());
    assertThat(json.isArray()).isTrue();
    assertThat(json.size()).isLessThanOrEqualTo(3);
  }

  @Test
  void postOwners_invalidEmail_returns422WithValidationBody() throws Exception {
    var dto = new OwnerCreateDto("Paul", "Green", "broken");
    var res = mvc.perform(post("/owners")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

    var body = om.readTree(res.getResponse().getContentAsString());
    assertThat(body.get("message").asText()).isNotBlank();
    assertThat(body.get("errors").isArray()).isTrue();
    assertThat(body.get("errors")).isNotNull();
  }

  @Test
  void postOwners_valid_locationIdMatchesBodyId() throws Exception {
    var dto = new OwnerCreateDto("Mira", "Blue", "mira.blue@test.tld");
    var mvcRes = mvc.perform(post("/owners")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn();

    var location = mvcRes.getResponse().getHeader("Location");
    assertThat(location).isNotBlank();

    var locationId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    var body = om.readTree(mvcRes.getResponse().getContentAsString());
    assertThat(body.get("id").asLong()).isEqualTo(locationId);
  }

  @Test
  void postOwners_emptyEmail_isAcceptedAndPersistsNull() throws Exception {
    var dto = new OwnerCreateDto("Eva", "Gray", "");
    var mvcRes = mvc.perform(post("/owners")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn();

    var body = om.readTree(mvcRes.getResponse().getContentAsString());
    // E-Mail darf null oder leer zurückkommen – hier prüfen wir auf null
    assertThat(body.get("email").isNull()).isTrue();
  }

  @Test
  void postOwners_malformedJson_returns400() throws Exception {
    var brokenJson = "{\"firstName\":\"Otto\",\"lastName\": \"Nowak\""; // fehlende schließende Klammer
    mvc.perform(post("/owners")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(brokenJson))
            .andExpect(status().isBadRequest());
  }

  @Test
  void getOwners_withoutParams_returns200JsonArray() throws Exception {
    mvc.perform(get("/owners"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }




}
