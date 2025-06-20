package dev.pcvolkmer.onco.datamapper.datacatalogues;

import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.exceptions.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Load raw result sets from database table 'patient'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class PatientCatalogue implements DataCatalogue {

    private final JdbcTemplate jdbcTemplate;

    public PatientCatalogue(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static PatientCatalogue create(JdbcTemplate jdbcTemplate) {
        return new PatientCatalogue(jdbcTemplate);
    }

    /**
     * Get patient result set by procedure id
     *
     * @param id The procedure id
     * @return The procedure id
     */
    @Override
    public ResultSet getById(int id) {

        var result = this.jdbcTemplate.queryForList(
                "SELECT * FROM patient WHERE id = ?",
                id);

        if (result.isEmpty()) {
            throw new DataAccessException("No patient record found for id: " + id);
        } else if (result.size() > 1) {
            throw new DataAccessException("Multiple patient records found for id: " + id);
        }

        return ResultSet.from(result.get(0));
    }

}
