package at.ac.tuwien.sepr.assignment.individual.service;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.util.stream.Stream;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import java.util.List;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;




/**
 * Service for working with horses.
 */
public interface HorseService {

  /**
   * Search horses by optional, combinable criteria.
   * Empty or null criteria fields are ignored; empty criteria returns all horses.
   * Supported filters:
   * <ul>
   *   <li>name: substring match (case-insensitive)</li>
   *   <li>description: substring match (case-insensitive)</li>
   *   <li>bornBefore: horse date_of_birth &lt; given date</li>
   *   <li>sex: exact match</li>
   *   <li>ownerName: substring match over "firstName lastName" (case-insensitive)</li>
   * </ul>
   *
   * @param searchCriteria criteria to apply
   * @return matching horses
   */
  List<HorseListDto> search(HorseSearchDto searchCriteria);


  /**
   * Lists all horses stored in the system.
   *
   * @return list of all stored horses
   */
  Stream<HorseListDto> allHorses();

  HorseDetailDto create(HorseCreateDto dto)
          throws ValidationException, ConflictException, NotFoundException;



  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse} in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException    if the horse with given ID does not exist
   * @throws ValidationException  if the update data is invalid (e.g., no name, invalid date)
   * @throws ConflictException    if the update conflicts with existing system state (e.g., owner does not exist)
   */
  HorseDetailDto update(HorseUpdateDto horse) throws NotFoundException, ValidationException, ConflictException;


  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  void delete(long id) throws NotFoundException, ConflictException;

}
