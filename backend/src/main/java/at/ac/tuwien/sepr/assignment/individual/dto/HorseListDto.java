package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;



/**
 * Represents a Data Transfer Object (DTO) for loading a list of horses.
 * This record encapsulates essential horse attributes required for listing.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HorseListDto(
        Long id,
        String name,
        String description,
        LocalDate dateOfBirth,
        Sex sex,
        @JsonInclude(JsonInclude.Include.NON_NULL)   // <— auch hier
        OwnerDto owner
) {}
