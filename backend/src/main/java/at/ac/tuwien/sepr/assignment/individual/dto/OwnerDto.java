package at.ac.tuwien.sepr.assignment.individual.dto;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a Data Transfer Object (DTO) for owner details.
 * This record encapsulates the essential information about an owner.
 */
@JsonInclude(JsonInclude.Include.ALWAYS) // <— null-Felder mitschicken
public record OwnerDto(
    long id,
    String firstName,
    String lastName,
    String email
) {
}
