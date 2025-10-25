package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataAccessException;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import java.util.List;
import java.util.Map;
import java.util.Objects;




/**
 * Implementation of {@link HorseService}.
 */
@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;


  @Autowired
  public HorseServiceImpl(HorseDao dao,
                          HorseMapper mapper,
                          HorseValidator validator,
                          OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    var horses = dao.getAll();
    var ownerIds = horses.stream()
        .map(Horse::ownerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  @Override
  public HorseDetailDto create(HorseCreateDto dto) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("create({})", dto);
    validator.validateForCreate(dto);

    // Owner prüfen (du hast hier bisher ConflictException verwendet – ok.
    // Wenn du TS27 maximal genau auslegen willst, kannst du stattdessen NotFoundException werfen.)
    if (dto.ownerId() != null) {
      try {
        ownerService.getById(dto.ownerId());
      } catch (NotFoundException e) {
        // Entweder so lassen …
        throw new ConflictException(
                "Owner does not exist",
                List.of("ownerId %d does not exist".formatted(dto.ownerId()))
        );
        // … oder (empfohlen gemäß TS27) direkt weiterreichen:
        // throw e;
      }
    }

    // Eltern laden (falls angegeben) → NotFoundException, falls ID nicht existiert
    Horse mother = (dto.motherId() == null) ? null : dao.getById(dto.motherId());
    Horse father = (dto.fatherId() == null) ? null : dao.getById(dto.fatherId());

    // Eltern-Regeln prüfen (unterschiedliches Geschlecht, keine Selbstreferenz, Alter)
    validator.validateParents(dto.dateOfBirth(), null, mother, father);

    var entity = new Horse(
            null,
            dto.name(),
            dto.description(),
            dto.dateOfBirth(),
            dto.sex(),
            dto.ownerId(),
            null,
            null,
            dto.motherId(),    // NEW
            dto.fatherId()     // NEW
    );

    var saved = dao.insert(entity);
    return mapper.entityToDetailDto(saved, ownerMapForSingleId(saved.ownerId()));
  }





  /*
  @Override
  public void storeHorseImage(long horseId, MultipartFile file)
          throws NotFoundException, ValidationException {

    LOG.trace("storeHorseImage({}, file size={})", horseId, file == null ? null : file.getSize());

    // 1) Existenz prüfen
    dao.getById(horseId); // wirft 404, wenn nicht da

    // 2) Validieren (einfach & ausreichend für US1)
    if (file == null || file.isEmpty()) {
      throw new ValidationException("Invalid image", List.of("image file must not be empty"));
    }
    var contentType = file.getContentType();
    if (contentType == null || !(contentType.startsWith("image/"))) {
      throw new ValidationException("Invalid image", List.of("unsupported content type: " + contentType));
    }
    // Optionale Größenbeschränkung (z.B. 5 MB)
    if (file.getSize() > 5 * 1024 * 1024) {
      throw new ValidationException("Invalid image", List.of("image too large"));
    }

    // 3) Zielverzeichnis aus config (application.yml) lesen
    var uploadDir = Paths.get(storageProperties.getHorseImageDir()); // siehe unten
    try {
      Files.createDirectories(uploadDir);
      var filename = horseId + "-" + UUID.randomUUID();
      var target = uploadDir.resolve(filename);
      try (var in = file.getInputStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
      }
      // 4) DB-Felder aktualisieren (Pfad + Content-Type)
      dao.updateImage(horseId, target.toString(), contentType);
    } catch (IOException e) {
      throw new FatalException("Storing image failed", e);
    }
  }

*/

  @Override
  public void delete(long id) throws NotFoundException, ConflictException {
    LOG.trace("delete({})", id);
    try {
      dao.delete(id);
    } catch (DataAccessException e) {
      // wegen FK-Verletzung
      throw new ConflictException("Horse cannot be deleted", List.of("There are referencing entities for horse " + id));
    }
  }



  @Override
  public HorseDetailDto update(HorseUpdateDto dto)
          throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", dto);
    validator.validateForUpdate(dto);

    // Zielpferd existiert?
    var existing = dao.getById(dto.id()); // wirft NotFoundException → 404

    // Owner prüfen (optional – analog zu create)
    if (dto.ownerId() != null) {
      try {
        ownerService.getById(dto.ownerId());
      } catch (NotFoundException e) {
        // entweder Conflict wie bisher …
        throw new ConflictException(
                "Owner does not exist",
                List.of("ownerId %d does not exist".formatted(dto.ownerId()))
        );
        // … oder TS27-konform einfach weiterreichen:
        // throw e;
      }
    }

    // Eltern laden (falls gesetzt)
    Horse mother = (dto.motherId() == null) ? null : dao.getById(dto.motherId());
    Horse father = (dto.fatherId() == null) ? null : dao.getById(dto.fatherId());

    // Eltern-Regeln prüfen
    validator.validateParents(dto.dateOfBirth(), dto.id(), mother, father);

    var updatedHorse = dao.update(dto); // schreibt mother_id/father_id mit
    return mapper.entityToDetailDto(
            updatedHorse,
            ownerMapForSingleId(updatedHorse.ownerId())
    );
  }






  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(
        horse,
        ownerMapForSingleId(horse.ownerId()));
  }


  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      if (ownerId == null) {
        return Collections.emptyMap(); // ← statt null zurückgeben
      }
      return Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId), e);
    }
  }

  @Override
  public List<HorseListDto> search(HorseSearchDto searchCriteria) {
    LOG.trace("search({})", searchCriteria);

    // 1) DAO-Abfrage
    List<Horse> horses = dao.search(searchCriteria);

    // 2) Owner der Treffer vorab in einem Rutsch laden (Performance, TS25/30)
    var ownerIds = horses.stream()
            .map(Horse::ownerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      // dürfte nicht passieren, falls DB konsistent ist; Fatal -> 500
      throw new FatalException("Horse refers to non-existing owner", e);
    }

    // 3) Mapping → ListDto inkl. Ownername
    return horses.stream()
            .map(h -> mapper.entityToListDto(h, ownerMap))
            .toList();
  }




}
