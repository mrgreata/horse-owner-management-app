package at.ac.tuwien.sepr.assignment.individual.entity;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * Persisted horse entity (maps 1:1 to table {@code horse}).
 *
 * @param id                database id (generated)
 * @param name              horse name (required)
 * @param description       optional description
 * @param dateOfBirth       date of birth (required)
 * @param sex               biological sex (required)
 * @param ownerId           optional owner FK
 * @param imagePath         optional file path of stored image (nullable)
 * @param imageContentType  optional media type of stored image (nullable)
 */
public record Horse(
        Long id,
        String name,
        String description,
        LocalDate dateOfBirth,
        Sex sex,
        Long ownerId,
        String imagePath,
        String imageContentType,
        Long motherId,   // NEW
        Long fatherId    // NEW
) { }
