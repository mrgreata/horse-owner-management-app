package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
//import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import java.util.ArrayList;



/**
 * JDBC implementation of {@link HorseDao} for interacting with the database.
 */
@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME;

  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE ID = :id";

  private static final String SQL_UPDATE = """
    UPDATE horse
       SET name = :name,
           description = :description,
           date_of_birth = :date_of_birth,
           sex = :sex,
           owner_id = :owner_id,
           mother_id = :mother_id,
           father_id = :father_id
     WHERE id = :id
      """;


  private static final String SQL_INSERT = """
  INSERT INTO horse
    (name, description, date_of_birth, sex, owner_id, image_path, image_content_type, mother_id, father_id)
  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;



  private final JdbcClient jdbcClient;
  private final JdbcTemplate jdbcTemplate;


  public HorseJdbcDao(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
    this.jdbcClient = jdbcClient;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Horse insert(Horse horse) {
    LOG.trace("insert({})", horse);
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(con -> {
      PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, horse.name());
      ps.setString(2, horse.description());
      ps.setDate(3, Date.valueOf(horse.dateOfBirth()));
      ps.setString(4, horse.sex().name());
      if (horse.ownerId() == null) {
        ps.setNull(5, java.sql.Types.BIGINT);
      } else {
        ps.setLong(5, horse.ownerId());
      }
      // Bild (optional)
      if (horse.imagePath() == null) {
        ps.setNull(6, java.sql.Types.VARCHAR);
      } else {
        ps.setString(6, horse.imagePath());
      }
      if (horse.imageContentType() == null) {
        ps.setNull(7, java.sql.Types.VARCHAR);
      } else {
        ps.setString(7, horse.imageContentType());
      }

      // US4
      if (horse.motherId() == null) {
        ps.setNull(8, java.sql.Types.BIGINT);
      } else {
        ps.setLong(8, horse.motherId());
      }
      if (horse.fatherId() == null) {
        ps.setNull(9, java.sql.Types.BIGINT);
      } else {
        ps.setLong(9, horse.fatherId());
      }

      return ps;
    }, keyHolder);

    long id = keyHolder.getKey().longValue();
    return new Horse(
            id,
            horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            horse.ownerId(),
            horse.imagePath(),
            horse.imageContentType(),
            horse.motherId(),
            horse.fatherId()
    );
  }


  /*@Autowired
  public HorseJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  } */

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcClient
        .sql(SQL_SELECT_ALL)
        .query(this::mapRow)
        .list();
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses = jdbcClient
        .sql(SQL_SELECT_BY_ID)
        .param("id", id)
        .query(this::mapRow)
        .list();

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.getFirst();
  }


  @Override
  public Horse update(HorseUpdateDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcClient.sql(SQL_UPDATE)
            .param("id", horse.id())
            .param("name", horse.name())
            .param("description", horse.description())
            .param("date_of_birth", horse.dateOfBirth())
            .param("sex", horse.sex().toString())
            .param("owner_id", horse.ownerId())
            .param("mother_id", horse.motherId())
            .param("father_id", horse.fatherId())
            .update();

    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }

    // existierende Bild-Felder beibehalten
    var current = getById(horse.id());
    return new Horse(
            horse.id(),
            horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex(),
            horse.ownerId(),
            current.imagePath(),
            current.imageContentType(),
            horse.motherId(),
            horse.fatherId()
    );
  }



  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse(
            result.getLong("id"),
            result.getString("name"),
            result.getString("description"),
            result.getDate("date_of_birth").toLocalDate(),
            Sex.valueOf(result.getString("sex")),
            result.getObject("owner_id", Long.class),
            result.getString("image_path"),
            result.getString("image_content_type"),
            result.getObject("mother_id", Long.class),
            result.getObject("father_id", Long.class)
    );
  }


  @Override
  public void updateImage(long id, String imagePath, String contentType) {
    jdbcClient.sql("""
      UPDATE horse
         SET image_path = :path,
             image_content_type = :ctype
       WHERE id = :id
      """)
            .param("path", imagePath)
            .param("ctype", contentType)
            .param("id", id)
            .update();
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);
    int affected = jdbcClient.sql("DELETE FROM horse WHERE id = :id")
            .param("id", id)
            .update();

    LOG.debug("delete({}) affectedRows={}", id, affected);
    if (affected == 0) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
  }

  @Override
  public List<Horse> search(HorseSearchDto searchCriteria) {
    LOG.trace("search({})", searchCriteria);

    StringBuilder sql = new StringBuilder("""
      SELECT h.id,
             h.name,
             h.description,
             h.date_of_birth,
             h.sex,
             h.owner_id,
             h.image_path,
             h.image_content_type,
             h.mother_id,
             h.father_id
        FROM horse h
        LEFT JOIN owner o ON h.owner_id = o.id
       WHERE 1=1
        """);

    List<Object> params = new ArrayList<>();

    if (searchCriteria.name() != null && !searchCriteria.name().isBlank()) {
      sql.append(" AND LOWER(h.name) LIKE LOWER(?)");
      params.add("%" + searchCriteria.name().trim() + "%");
    }
    if (searchCriteria.description() != null && !searchCriteria.description().isBlank()) {
      sql.append(" AND LOWER(h.description) LIKE LOWER(?)");
      params.add("%" + searchCriteria.description().trim() + "%");
    }
    if (searchCriteria.bornBefore() != null) {
      sql.append(" AND h.date_of_birth < ?");
      params.add(Date.valueOf(searchCriteria.bornBefore()));
    }
    if (searchCriteria.sex() != null) {
      sql.append(" AND h.sex = ?");
      params.add(searchCriteria.sex().toString());
    }
    if (searchCriteria.ownerName() != null && !searchCriteria.ownerName().isBlank()) {
      sql.append(" AND LOWER(COALESCE(o.first_name,'') || ' ' || COALESCE(o.last_name,'')) LIKE LOWER(?)");
      params.add("%" + searchCriteria.ownerName().trim() + "%");
    }

    sql.append(" ORDER BY h.name ASC");

    if (searchCriteria.limit() != null && searchCriteria.limit() > 0) {
      sql.append(" LIMIT ?");
      params.add(searchCriteria.limit());
    }

    return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapRow(rs, rowNum), params.toArray());

  }





}
