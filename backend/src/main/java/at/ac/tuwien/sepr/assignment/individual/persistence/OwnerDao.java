package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.util.Collection;

/**
 * Data Access Object for owners.
 * Provides CRUD functionality for managing owners in the persistent data store.
 */
public interface OwnerDao {

  /**
   * Fetch an owner from the persistent data store by its ID.
   *
   * @param id the ID of the owner to get
   * @return the owner with the ID {@code id}
   * @throws NotFoundException if no owner with the given ID exists
   */
  Owner getById(long id) throws NotFoundException;

  /**
   * Fetch a set of owners by their IDs from the persistent data store.
   * Best effort: if an owner cannot be found, it is simply not included in the result.
   *
   * @param ids the IDs of the owners to fetch
   * @return the collection of all found owners (missing IDs are omitted)
   */
  Collection<Owner> getAllById(Collection<Long> ids);

  /**
   * Search for owners matching the criteria in {@code searchParameters}.
   * An owner is considered matched if its name contains {@code searchParameters.name} as a substring.
   * The returned collection never contains more than {@code searchParameters.maxAmount} elements,
   * even if there would be more matches in the persistent data store.
   *
   * @param searchParameters the parameters to match
   * @return a collection containing owners matching the criteria
   */
  Collection<Owner> search(OwnerSearchDto searchParameters);

  /**
   * Create a new owner.
   *
   * @param owner the owner to persist (without id)
   * @return the persisted owner with generated id
   */
  Owner create(Owner owner);
}
