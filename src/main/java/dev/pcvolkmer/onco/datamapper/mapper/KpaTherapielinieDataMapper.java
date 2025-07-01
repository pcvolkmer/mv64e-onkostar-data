package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.MtbSystemicTherapy;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.TherapielinieCatalogue;

import java.util.List;

import static dev.pcvolkmer.onco.datamapper.mapper.MapperUtils.getPatientReference;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_therapielinie'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaTherapielinieDataMapper extends AbstractKpaTherapieverlaufDataMapper<MtbSystemicTherapy> {

    public KpaTherapielinieDataMapper(final TherapielinieCatalogue catalogue, final PropertyCatalogue propertyCatalogue) {
        super(catalogue, propertyCatalogue);
    }

    /**
     * Loads and maps Prozedur related by database id
     *
     * @param id The database id of the procedure data set
     * @return The loaded MtbDiagnosis file
     */
    @Override
    public MtbSystemicTherapy getById(final int id) {
        var data = catalogue.getById(id);
        return this.map(data);
    }

    @Override
    protected MtbSystemicTherapy map(final ResultSet resultSet) {
        var diseases = catalogue.getDiseases(resultSet.getId());

        if (diseases.size() != 1) {
            throw new IllegalStateException(String.format("No unique disease for procedure %s", resultSet.getId()));
        }

        var builder = MtbSystemicTherapy.builder();
        builder
                .id(resultSet.getString("id"))
                .patient(getPatientReference(resultSet.getString("patient_id")))
                .basedOn(
                        Reference.builder()
                                .id(resultSet.getString("ref_einzelempfehlung"))
                                .build()
                )
                .reason(
                        Reference.builder()
                                .id(diseases.get(0).getString("id"))
                                .type("MTBDiagnosis")
                                .build()
                )
                .therapyLine(resultSet.getLong("nummer"))
                .recordedOn(resultSet.getDate("erfassungsdatum"))
                .intent(
                        getMtbTherapyIntentCoding(
                                resultSet.getString("intention"),
                                resultSet.getInteger("intention_propcat_version")
                        )
                )
                .status(
                        getTherapyStatusCoding(
                                resultSet.getString("status"),
                                resultSet.getInteger("status_propcat_version")
                        )
                )
                .statusReason(
                        getMtbTherapyStatusReasonCoding(
                                resultSet.getString("statusgrund"),
                                resultSet.getInteger("statusgrund_propcat_version")
                        )
                )
                .period(
                        PeriodDate.builder()
                                .start(resultSet.getDate("beginn"))
                                .end(resultSet.getDate("ende"))
                                .build()
                )
                .medication(JsonToMedicationMapper.map(resultSet.getString("wirkstoffcodes")))
        ;

        if (resultSet.getString("stellung_propcat_version") != null) {
            builder.category(
                    getMtbSystemicTherapyCategoryCoding(
                            resultSet.getString("stellung"),
                            resultSet.getInteger("stellung_propcat_version")
                    )
            );
        }

        if (resultSet.getString("dosisdichte_propcat_version") != null) {
            builder.dosage(
                    getMtbSystemicTherapyDosageDensityCoding(
                            resultSet.getString("dosisdichte"),
                            resultSet.getInteger("dosisdichte_propcat_version")
                    )
            );
        }

        if (resultSet.getString("umsetzung_propcat_version") != null) {
            builder.recommendationFulfillmentStatus(
                    getMtbSystemicTherapyRecommendationFulfillmentStatusCoding(
                            resultSet.getString("umsetzung"),
                            resultSet.getInteger("umsetzung_propcat_version")
                    )
            );
        }

        if (resultSet.getString("anmerkungen") != null) {
            builder.notes(List.of(resultSet.getString("anmerkungen")));
        }

        return builder.build();
    }


}
