package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * Represents a Data Transfer Object (DTO) for creating a new horse entry.
 * This record encapsulates all necessary details for registering a horse.
 */
public record HorseCreateDto(
        String name,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,
        Sex sex,
        Long ownerId
) {}
