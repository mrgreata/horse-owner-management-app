package at.ac.tuwien.sepr.assignment.individual.service.impl;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import java.time.LocalDate;


/**
 * Validator for horse-related operations, ensuring that all horse data meets the required constraints.
 */
@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  /**
   * Validates a horse before updating, ensuring all fields meet constraints and checking for conflicts.
   *
   * @param horse the {@link HorseUpdateDto} to validate
   * @throws ValidationException if validation fails
   * @throws ConflictException   if conflicts with existing data are detected
   */
  public void validateForUpdate(HorseUpdateDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

  }

  /**
   * Validates a new horse before creation according to US1/TS15.
   *
   * @param dto the data to validate
   * @throws ValidationException if any field is invalid; the exception contains all error messages
   */
  public void validateForCreate(HorseCreateDto dto) throws ValidationException {
    var errors = new ArrayList<String>();

    if (dto == null) {
      errors.add("body must not be null");
    } else {
      if (dto.name() == null || dto.name().isBlank()) {
        errors.add("name must not be empty");
      }
      if (dto.sex() == null) {
        errors.add("sex must not be null");
      }
      if (dto.dateOfBirth() == null) {
        errors.add("dateOfBirth must not be null");
      } else if (dto.dateOfBirth().isAfter(LocalDate.now())) {
        errors.add("dateOfBirth must not be in the future");
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationException("Invalid horse", errors);
    }
  }

}
