package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // <-- hinzugefügt

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForUpdate(HorseUpdateDto horse) throws ValidationException { // <-- ConflictException entfernt
    LOG.trace("validateForUpdate({})", horse);
    List<String> errors = new ArrayList<>();

    if (horse.id() == null) {
      errors.add("No ID given");
    }
    // gleiche Basisregeln wie bei Create (empfohlen)
    if (horse.name() == null || horse.name().isBlank()) {
      errors.add("name must not be empty");
    }
    if (horse.sex() == null) {
      errors.add("sex must not be null");
    }
    if (horse.dateOfBirth() == null || horse.dateOfBirth().isAfter(LocalDate.now())) {
      errors.add("dateOfBirth must not be in the future");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        errors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        errors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationException("Invalid horse", errors); // gleiche Summary wie bei Create
    }
  }

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

  public void validateParents(LocalDate childDob, Long childId, Horse mother, Horse father)
          throws ValidationException {
    List<String> errors = new ArrayList<>();

    if (mother != null && father != null && mother.sex() == father.sex()) {
      errors.add("Parents must be of opposite sex");
    }
    if (childId != null) {
      if (mother != null && Objects.equals(mother.id(), childId)) {
        errors.add("cannot be its own mother");
      }
      if (father != null && Objects.equals(father.id(), childId)) {
        errors.add("cannot be its own father");
      }
    }
    if (childDob != null) {
      if (mother != null && (mother.dateOfBirth() == null || !mother.dateOfBirth().isBefore(childDob))) {
        errors.add("Mother must be older than child");
      }
      if (father != null && (father.dateOfBirth() == null || !father.dateOfBirth().isBefore(childDob))) {
        errors.add("Father must be older than child");
      }
    }
    if (!errors.isEmpty()) {
      throw new ValidationException("Invalid horse", errors);
    }
  }
}
