package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.MtbStudyEnrollmentRecommendation;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.EinzelempfehlungCatalogue;

import java.util.List;
import java.util.stream.Collectors;

import static dev.pcvolkmer.onco.datamapper.mapper.MapperUtils.getPatientReference;

/**
 * Mapper class to load and map diagnosis data from database table 'dk_dnpm_einzelempfehlung'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class EinzelempfehlungStudieDataMapper extends AbstractEinzelempfehlungDataMapper<MtbStudyEnrollmentRecommendation> {

    public EinzelempfehlungStudieDataMapper(EinzelempfehlungCatalogue einzelempfehlungCatalogue) {
        super(einzelempfehlungCatalogue);
    }

    @Override
    protected MtbStudyEnrollmentRecommendation map(ResultSet resultSet) {
        return MtbStudyEnrollmentRecommendation.builder()
                .id(resultSet.getString("id"))
                .patient(getPatientReference(resultSet.getString("patient_id")))
                // TODO Fix id?
                .reason(Reference.builder().id(resultSet.getString("id")).build())
                .issuedOn(resultSet.getDate("datum"))
                .priority(
                        getRecommendationPriorityCoding(
                                resultSet.getString("evidenzlevel"),
                                resultSet.getInteger("evidenzlevel_propcat_version")
                        )
                )
                .medication(JsonToMedicationMapper.map(resultSet.getString("wirkstoffe_json")))
                .levelOfEvidence(getLevelOfEvidence(resultSet))
                .study(JsonToStudyMapper.map(resultSet.getString("studien_alle_json")))
                .build();
    }

    @Override
    public MtbStudyEnrollmentRecommendation getById(int id) {
        return this.map(this.catalogue.getById(id));
    }

    @Override
    public List<MtbStudyEnrollmentRecommendation> getByParentId(final int parentId) {
        return catalogue.getAllByParentId(parentId)
                .stream()
                // Filter Wirkstoffempfehlung (Systemische Therapie)
                .filter(it -> "studie".equals(it.getString("empfehlungskategorie")))
                .map(this::map)
                .collect(Collectors.toList());
    }

}
