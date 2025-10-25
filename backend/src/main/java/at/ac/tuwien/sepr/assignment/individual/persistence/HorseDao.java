package at.ac.tuwien.sepr.assignment.individual.persistence;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import java.util.List;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;






/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {


  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();


  /**
   * Search horses by optional, combinable criteria.
   * <ul>
   *   <li>name: substring match (case-insensitive)</li>
   *   <li>description: substring match (case-insensitive)</li>
   *   <li>bornBefore: horse date_of_birth &lt; given date</li>
   *   <li>sex: exact match</li>
   *   <li>ownerName: substring match over owner full name (first + last)</li>
   * </ul>
   * Empty criteria return all horses.
   *
   * @param searchCriteria criteria to apply; any field may be null/blank
   * @return matching horses sorted as defined in the DAO impl
   */
  List<Horse> search(HorseSearchDto searchCriteria);


  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseUpdateDto horse) throws NotFoundException;


  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;



  /**
   * Insert a new horse into the persistent data store.
   *
   * @param horse the horse to be inserted
   * @return the inserted horse including it generated ID
   */
  Horse insert(Horse horse);

  void updateImage(long id, String imagePath, String contentType);

  void delete(long id) throws NotFoundException;

}
