package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;




/**
 * REST controller for managing owner-related operations.
 * Provides endpoints for searching and creating owners.
 */
@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/owners";

  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }

  /**
   * Searches for owners based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the owner search
   * @return a stream of {@link OwnerDto} matching the search criteria
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<OwnerDto> search(@ModelAttribute OwnerSearchDto searchParameters) {
    LOG.info("GET {} query parameters: {}", BASE_PATH, searchParameters);
    return service.search(searchParameters).toList();
  }


  /**
   * Creates a new owner.
   *
   * @param dto data for the owner to create
   * @return 201 Created with Location header and created owner body
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OwnerDto> createOwner(@RequestBody OwnerCreateDto dto)
          throws ValidationException {
    LOG.info("POST {} body={}", BASE_PATH, dto);
    var created = service.create(dto);
    var location = URI.create(BASE_PATH + "/" + created.id());
    return ResponseEntity.created(location).body(created);
  }


}
