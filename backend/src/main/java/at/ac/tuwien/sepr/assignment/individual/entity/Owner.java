package at.ac.tuwien.sepr.assignment.individual.entity;

/** Immutable Owner-Entity. */
public record Owner(
        Long id,
        String firstName,
        String lastName,
        String email
) {}
