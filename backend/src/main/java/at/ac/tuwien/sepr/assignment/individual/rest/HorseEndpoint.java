package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateRestDto;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;


@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class HorseEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/horses";

  private final HorseService service;

  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  @GetMapping
  public List<HorseListDto> searchHorses(HorseSearchDto searchParameters) {
    LOG.info("GET {}", BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);
    // TODO (US6): searchParameters an Service weiterreichen
    return service.allHorses().toList();
  }

  @GetMapping("{id}")
  public HorseDetailDto getById(@PathVariable("id") long id) throws NotFoundException {
    LOG.info("GET {}/{}", BASE_PATH, id);
    return service.getById(id);
  }

  @PutMapping(path = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public HorseDetailDto update(@PathVariable("id") long id,
                               @RequestBody HorseUpdateRestDto toUpdate)
          throws NotFoundException, ValidationException, ConflictException {
    LOG.info("PUT {}/{}", BASE_PATH, id);
    LOG.debug("Body of request:\n{}", toUpdate);
    return service.update(toUpdate.toUpdateDtoWithId(id));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public HorseDetailDto create(@RequestBody HorseCreateDto toCreate)
          throws ValidationException, ConflictException, NotFoundException {
    LOG.info("POST {}", BASE_PATH);
    LOG.debug("Body of request:\n{}", toCreate);
    return service.create(toCreate);
  }


  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") long id)
          throws NotFoundException, ConflictException {
    LOG.info("DELETE {}/{}", BASE_PATH, id);
    service.delete(id);
  }
}
