package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.Mtb;
import dev.pcvolkmer.onco.datamapper.datacatalogues.DataCatalogueFactory;
import dev.pcvolkmer.onco.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.PatientCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ProzedurCatalogue;
import dev.pcvolkmer.onco.datamapper.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * Mapper class to load and map Mtb files from database
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MtbDataMapper implements DataMapper<Mtb> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DataCatalogueFactory catalogueFactory;

    MtbDataMapper(final JdbcTemplate jdbcTemplate) {
        this.catalogueFactory = DataCatalogueFactory.initialize(jdbcTemplate);
    }

    /**
     * Create instance of the mapper class
     *
     * @param dataSource The datasource to be used
     * @return The initialized mapper
     */
    public static MtbDataMapper create(final DataSource dataSource) {
        return new MtbDataMapper(new JdbcTemplate(dataSource));
    }

    /**
     * Create instance of the mapper class
     *
     * @param jdbcTemplate The Spring JdbcTemplate to be used
     * @return The initialized mapper
     */
    public static MtbDataMapper create(final JdbcTemplate jdbcTemplate) {
        return new MtbDataMapper(jdbcTemplate);
    }

    /**
     * Loads and maps a Mtb file using the root procedures database id
     *
     * @param kpaId The database id of the root procedure data set
     * @return The loaded Mtb file
     */
    @Override
    public Mtb getById(int kpaId) {
        var kpaCatalogue = catalogueFactory.catalogue(KpaCatalogue.class);
        var patientDataMapper = new PatientDataMapper(catalogueFactory.catalogue(PatientCatalogue.class));
        var kpaPatientDataMapper = new KpaPatientDataMapper(kpaCatalogue);
        var diagnosisDataMapper = new KpaDiagnosisDataMapper(kpaCatalogue);
        var prozedurMapper = new KpaProzedurDataMapper(catalogueFactory.catalogue(ProzedurCatalogue.class));

        var resultBuilder = Mtb.builder();

        try {
            var kpaPatient = kpaPatientDataMapper.getById(kpaId);
            var patient = patientDataMapper.getById(Integer.parseInt(kpaPatient.getId()));
            kpaPatient.setAddress(patient.getAddress());

            resultBuilder
                    .patient(kpaPatient)
                    .diagnoses(List.of(diagnosisDataMapper.getById(kpaId)))
                    .guidelineProcedures(prozedurMapper.getByParentId(kpaId))
            ;
        } catch (DataAccessException e) {
            logger.error("Error while getting Mtb.", e);
        }

        return resultBuilder.build();
    }

    /**
     * Loads and maps a Mtb file using the case id
     *
     * @param caseId The case id
     * @return The loaded Mtb file
     */
    public Mtb getByCaseId(String caseId) {
        return this.getById(
                this.catalogueFactory.catalogue(KpaCatalogue.class).getProcedureIdByCaseId(caseId)
        );
    }
}
