package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting {@link Owner} entities
 * to and from DTOs ({@link OwnerDto}, {@link OwnerCreateDto}).
 */
@Component
public class OwnerMapper {

  private static final Logger LOG =
          LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Converts an {@link Owner} entity to a corresponding {@link OwnerDto}.
   *
   * @param owner the {@link Owner} entity to convert
   * @return the corresponding {@link OwnerDto}, or {@code null} if input is {@code null}
   */
  public OwnerDto entityToDto(Owner owner) {
    LOG.trace("entityToDto({})", owner);
    if (owner == null) {
      return null;
    }
    return new OwnerDto(owner.id(), owner.firstName(), owner.lastName(), owner.email());

  }

  /**
   * Converts a {@link OwnerCreateDto} into a new {@link Owner} entity.
   *
   * @param dto the DTO containing the creation data
   * @return the new {@link Owner} entity, or {@code null} if dto is {@code null}
   */
  public Owner fromCreateDto(OwnerCreateDto dto) {
    LOG.trace("fromCreateDto({})", dto);
    if (dto == null) {
      return null;
    }
    String normalizedEmail =
            (dto.email() == null || dto.email().isBlank()) ? null : dto.email().trim();

    return new Owner(
            null,
            dto.firstName(),
            dto.lastName(),
            normalizedEmail
    );
  }

}
