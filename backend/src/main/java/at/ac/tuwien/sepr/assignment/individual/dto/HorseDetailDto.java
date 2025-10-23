package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Represents a Data Transfer Object (DTO) for detailed horse information.
 * This record provides all necessary details about a horse.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HorseDetailDto(
        Long id,
        String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,
        Sex sex,
        OwnerDto owner,
        Long motherId,
        Long fatherId
) {}
