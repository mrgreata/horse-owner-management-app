package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;
import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OwnerJdbcDao implements OwnerDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "owner";
  private static final String SQL_SELECT_BY_ID =
          "SELECT * FROM " + TABLE_NAME + " WHERE id = :id";
  private static final String SQL_SELECT_ALL =
          "SELECT * FROM " + TABLE_NAME + " WHERE id IN (:ids)";
  private static final String SQL_SELECT_SEARCH =
          "SELECT * FROM " + TABLE_NAME
                  + " WHERE UPPER(first_name || ' ' || last_name) LIKE UPPER('%%' || COALESCE(:name, '') || '%%')";
  private static final String SQL_SELECT_SEARCH_LIMIT_CLAUSE = " LIMIT :limit";

  private final JdbcClient jdbcClient;
  private final JdbcTemplate jdbcTemplate;

  public OwnerJdbcDao(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
    this.jdbcClient = jdbcClient;
    this.jdbcTemplate = jdbcTemplate;
  }


  @Override
  public Owner getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Owner> owners = jdbcClient
            .sql(SQL_SELECT_BY_ID)
            .param("id", id)
            .query(this::mapRow)
            .list();

    if (owners.isEmpty()) {
      throw new NotFoundException("Owner with ID %d not found".formatted(id));
    }
    if (owners.size() > 1) {
      // Sollte nie passieren: Datenproblem oder fehlerhaftes SQL
      throw new FatalException("Found more than one owner with ID %d".formatted(id));
    }
    return owners.getFirst();
  }

  @Override
  public Collection<Owner> getAllById(Collection<Long> ids) {
    LOG.trace("getAllById({})", ids);
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return jdbcClient
            .sql(SQL_SELECT_ALL)
            .param("ids", ids)
            .query(this::mapRow)
            .list();
  }


  @Override
  public Collection<Owner> search(OwnerSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    String query = SQL_SELECT_SEARCH;

    Map<String, Object> params = new HashMap<>();
    params.put("name", searchParameters.name());

    Integer maxAmount = searchParameters.maxAmount();
    if (maxAmount != null) {
      query += SQL_SELECT_SEARCH_LIMIT_CLAUSE;
      params.put("limit", maxAmount);
    }

    return jdbcClient
            .sql(query)
            .params(params)
            .query(this::mapRow)
            .list();
  }

  @Override
  public Owner create(Owner owner) {
    LOG.debug("DAO create owner: {}", owner);
    final String sql = "INSERT INTO owner (first_name, last_name, email) VALUES (?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, owner.firstName());
      ps.setString(2, owner.lastName());
      if (owner.email() == null || owner.email().isBlank()) {
        ps.setNull(3, Types.VARCHAR);
      } else {
        ps.setString(3, owner.email().trim());
      }
      return ps;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new FatalException("Failed to retrieve generated id for owner");
    }

    // neuen Record mit generierter ID zurückgeben
    return new Owner(key.longValue(), owner.firstName(), owner.lastName(), owner.email());
  }



  private Owner mapRow(ResultSet rs, int i) throws SQLException {
    return new Owner(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email")
    );
  }


}
