package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service for working with owners.
 */
public interface OwnerService {
  /**
   * Fetch an owner from the persistent data store by its ID.
   *
   * @param id the ID of the owner to get
   * @return the owner with the given ID
   * @throws NotFoundException if no owner with the given ID exists in the persistent data store
   */
  OwnerDto getById(long id) throws NotFoundException;

  /**
   * Fetch all owners referenced by the IDs in {@code ids}
   *
   * @param ids the IDs of the owners, that should be fetched
   * @return a map that contains the requested owners with their IDs as key
   * @throws NotFoundException if any of the requested owners is not found
   */
  Map<Long, OwnerDto> getAllById(Collection<Long> ids) throws NotFoundException;

  /**
   * Search for owners matching the criteria in {@code searchParameters}.
   *
   * <p>
   * An owner is considered matched, if its name contains {@code searchParameters.name} as a substring.
   * The returned stream of owners never contains more than {@code searchParameters.maxAmount} elements,
   * even if there would be more matches in the persistent data store.
   * </p>
   *
   * @param searchParameters object containing the search parameters to match
   * @return a stream containing owners matching the criteria in {@code searchParameters}
   */
  Stream<OwnerDto> search(OwnerSearchDto searchParameters);

  /**
   * Create a new owner.
   * Required: {@code firstName}, {@code lastName}. Optional: {@code email} (validated if present).
   *
   * @param toCreate data of the owner to create
   * @return the created owner including its generated {@code id}
   * @throws ValidationException if required fields are missing or invalid (e.g., malformed email)
   */
  OwnerDto create(OwnerCreateDto toCreate) throws ValidationException;


}
