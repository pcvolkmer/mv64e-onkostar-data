/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2025  Paul-Christian Volkmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.*;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.*;
import dev.pcvolkmer.onco.datamapper.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class to load and map Mtb files from database
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MtbDataMapper implements DataMapper<Mtb> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DataCatalogueFactory catalogueFactory;
    private final PropertyCatalogue propertyCatalogue;

    MtbDataMapper(final JdbcTemplate jdbcTemplate) {
        this.catalogueFactory = DataCatalogueFactory.initialize(jdbcTemplate);
        this.propertyCatalogue = PropertyCatalogue.initialize(jdbcTemplate);
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
        var kpaPatientDataMapper = new KpaPatientDataMapper(kpaCatalogue, propertyCatalogue);
        var diagnosisDataMapper = new KpaDiagnosisDataMapper(
                kpaCatalogue,
                catalogueFactory.catalogue(TumorausbreitungCatalogue.class),
                catalogueFactory.catalogue(TumorgradingCatalogue.class),
                catalogueFactory.catalogue(KeimbahndiagnoseCatalogue.class),
                propertyCatalogue
        );
        var mtbEpisodeDataMapper = new MtbEpisodeDataMapper(kpaCatalogue, propertyCatalogue);
        var prozedurMapper = new KpaProzedurDataMapper(
                catalogueFactory.catalogue(ProzedurCatalogue.class),
                propertyCatalogue
        );
        var therapielinieMapper = new KpaTherapielinieDataMapper(
                catalogueFactory.catalogue(TherapielinieCatalogue.class),
                propertyCatalogue
        );
        var ecogMapper = new KpaEcogDataMapper(
                catalogueFactory.catalogue(EcogCatalogue.class)
        );

        var einzelempfehlungCatalogue = catalogueFactory.catalogue(EinzelempfehlungCatalogue.class);
        var therapieplanCatalogue = catalogueFactory.catalogue(TherapieplanCatalogue.class);
        var therapieplanDataMapper = new TherapieplanDataMapper(
                therapieplanCatalogue,
                catalogueFactory.catalogue(RebiopsieCatalogue.class),
                catalogueFactory.catalogue(ReevaluationCatalogue.class),
                einzelempfehlungCatalogue,
                propertyCatalogue
        );

        var verwandteDataMapper = new KpaVerwandteDataMapper(catalogueFactory.catalogue(VerwandteCatalogue.class));

        var molekulargenetikCatalogue = catalogueFactory.catalogue(MolekulargenetikCatalogue.class);
        var molekulargenetikToSpecimenDataMapper = new MolekulargenetikToSpecimenDataMapper(
                molekulargenetikCatalogue,
                therapieplanCatalogue,
                catalogueFactory.catalogue(RebiopsieCatalogue.class),
                catalogueFactory.catalogue(ReevaluationCatalogue.class),
                einzelempfehlungCatalogue,
                catalogueFactory.catalogue(VorbefundeCatalogue.class),
                catalogueFactory.catalogue(HistologieCatalogue.class)
        );

        var kpaMolekulargenetikDataMapper = new KpaMolekulargenetikDataMapper(molekulargenetikCatalogue, catalogueFactory.catalogue(MolekulargenuntersuchungCatalogue.class), propertyCatalogue);

        var kpaVorbefundeDataMapper = new KpaVorbefundeDataMapper(
                catalogueFactory.catalogue(VorbefundeCatalogue.class),
                molekulargenetikCatalogue,
                propertyCatalogue
        );

        var kpaHistologieDataMapper = new KpaHistologieDataMapper(
                catalogueFactory.catalogue(HistologieCatalogue.class),
                molekulargenetikCatalogue,
                propertyCatalogue
        );

        var consentMvDataMapper = new ConsentMvDataMapper(
                catalogueFactory.catalogue(ConsentMvCatalogue.class),
                catalogueFactory.catalogue(ConsentMvVerlaufCatalogue.class)
        );

        var resultBuilder = Mtb.builder();

        try {
            var kpaPatient = kpaPatientDataMapper.getById(kpaId);
            var patient = patientDataMapper.getById(Integer.parseInt(kpaPatient.getId()));
            kpaPatient.setId(patient.getId());
            kpaPatient.setAddress(patient.getAddress());

            var diagnosis = diagnosisDataMapper.getById(kpaId);

            var specimens = molekulargenetikToSpecimenDataMapper.getAllByKpaId(
                    kpaId,
                    Reference.builder().id(diagnosis.getId()).type("MTBDiagnosis").build()
            );

            resultBuilder
                    .patient(kpaPatient)
                    .episodesOfCare(List.of(mtbEpisodeDataMapper.getById(kpaId)))
                    // DNPM Klinik/Anamnese
                    .diagnoses(List.of(diagnosis))
                    .guidelineProcedures(prozedurMapper.getByParentId(kpaId))
                    .guidelineTherapies(therapielinieMapper.getByParentId(kpaId))
                    .performanceStatus(ecogMapper.getByParentId(kpaId))
                    .familyMemberHistories(verwandteDataMapper.getByParentId(kpaId))
                    // Vorbefunde
                    .priorDiagnosticReports(kpaVorbefundeDataMapper.getByParentId(kpaId))
                    // Histologie-Berichte
                    .histologyReports(kpaHistologieDataMapper.getByParentId(kpaId))
                    // Tumorproben
                    .specimens(specimens)
                    // DNPM Therapieplan
                    .carePlans(
                            therapieplanCatalogue
                                    .getByKpaId(kpaId).stream()
                                    .map(therapieplanDataMapper::getById)
                                    // Remove references to alterations not present in specimens
                                    .peek(therapieplan -> therapieplan.setMedicationRecommendations(
                                                    therapieplan.getMedicationRecommendations().stream()
                                                            .peek(mtbMedicationRecommendation ->
                                                                    mtbMedicationRecommendation.setSupportingVariants(
                                                                            mtbMedicationRecommendation.getSupportingVariants().stream()
                                                                                    .filter(geneAlterationReference -> specimens.stream()
                                                                                            .map(TumorSpecimen::getId)
                                                                                            .collect(Collectors.toList()).contains(geneAlterationReference.getVariant().getId()))
                                                                                    .collect(Collectors.toList())
                                                                    )
                                                            ).collect(Collectors.toList())
                                            )
                                    )
                                    .collect(Collectors.toList())
                    )
                    // NGS Berichte
                    .ngsReports(
                            kpaMolekulargenetikDataMapper.getAllByKpaId(kpaId)
                    )
            ;

            // Consent - as far as present
            var consentId = kpaCatalogue.getById(kpaId).getString("consentmv64e");
            if (null != consentId) {
                resultBuilder.metadata(
                        MvhMetadata.builder()
                                .modelProjectConsent(
                                        consentMvDataMapper.getById(kpaCatalogue.getById(kpaId).getInteger("consentmv64e"))
                                )
                                .build()
                );
            }
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

    /**
     * Loads and maps a Mtb file using the patient id and tumor id
     *
     * @param patientId The patients id (not database id)
     * @param tumorId   The tumor identification
     * @return The loaded Mtb file
     */
    public Mtb getLatestByPatientIdAndTumorId(String patientId, int tumorId) {
        return this.getById(
                this.catalogueFactory.catalogue(KpaCatalogue.class)
                        .getLatestProcedureIdByPatientIdAndTumor(patientId, tumorId)
        );
    }
}
