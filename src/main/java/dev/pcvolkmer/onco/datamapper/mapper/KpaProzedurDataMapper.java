package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.OncoProcedure;
import dev.pcvolkmer.mv64e.mtb.PeriodDate;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ProzedurCatalogue;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_uf_prozedur'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaProzedurDataMapper extends AbstractKpaTherapieverlaufDataMapper<OncoProcedure> {

    public KpaProzedurDataMapper(final ProzedurCatalogue catalogue) {
        super(catalogue);
    }

    /**
     * Loads and maps Prozedur related by database id
     *
     * @param id The database id of the procedure data set
     * @return The loaded MtbDiagnosis file
     */
    @Override
    public OncoProcedure getById(final int id) {
        var data = catalogue.getById(id);
        return this.map(data);
    }

    @Override
    protected OncoProcedure map(final ResultSet resultSet) {
        var diseases = catalogue.getDiseases(resultSet.getProcedureId());

        if (diseases.size() != 1) {
            throw new IllegalStateException(String.format("No unique disease for procedure %s", resultSet.getProcedureId()));
        }

        var builder = OncoProcedure.builder();
        builder
                .id(resultSet.getString("id"))
                .patient(Reference.builder().id(resultSet.getString("patient_id")).build())
                .basedOn(Reference.builder().id(diseases.get(0).getDiseaseId().toString()).build())
                .recordedOn(resultSet.getDate("erfassungsdatum"))
                .therapyLine(resultSet.getLong("therapielinie"))
                .intent(getMtbTherapyIntentCoding(resultSet.getString("intention")))
                .status(getTherapyStatusCoding(resultSet.getString("status")))
                .statusReason(getMtbTherapyStatusReasonCoding(resultSet.getString("statusgrund")))
                .period(PeriodDate.builder().start(resultSet.getDate("beginn")).end(resultSet.getDate("ende")).build())
                .code(getOncoProcedureCoding(resultSet.getString("typ")))
        ;
        return builder.build();
    }

}
