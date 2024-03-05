package cwms.cda.data.dto.binarytimeseries;

import static cwms.cda.data.dto.TimeSeries.ZONED_DATE_TIME_FORMAT;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import cwms.cda.api.enums.VersionType;
import cwms.cda.api.errors.FieldException;
import cwms.cda.data.dto.CwmsDTO;
import cwms.cda.formatters.xml.adapters.ZonedDateTimeAdapter;
import hec.data.timeSeriesText.DateDateKey;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(builder = BinaryTimeSeries.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public class BinaryTimeSeries extends CwmsDTO {
    private final String name;
    private final Long intervalOffset;
    private final String timeZone;

    @Schema(description = "The version type for the binary time series being queried. Can be in the form of MAX_AGGREGATE, SINGLE_VERSION, or UNVERSIONED. " +
            "MAX_AGGREGATE will get the latest version date value for each value in the date range. SINGLE_VERSION must be called with a valid " +
            "version date and will return the values for the version date provided. UNVERSIONED return values from an unversioned time series. " +
            "Note that SINGLE_VERSION requires a valid version date while MAX_AGGREGATE and UNVERSIONED each require a null version date.")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    VersionType dateVersionType;

    @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FORMAT)
    @Schema(description = "The version date of the time series trace")
    ZonedDateTime versionDate;

    private final NavigableMap<DateDateKey, BinaryTimeSeriesRow> entries;

    private BinaryTimeSeries(Builder builder) {
        super(builder.officeId);
        name = builder.name;
        intervalOffset = builder.intervalOffset;
        timeZone = builder.timeZone;

        dateVersionType = builder.dateVersionType;
        versionDate = builder.versionDate;


        if (builder.entriesMap != null) {
            entries = new TreeMap<>(builder.entriesMap);
        } else {
            entries = null;
        }

    }

    public String getName() {
        return name;
    }

    public Long getIntervalOffset() {
        return intervalOffset;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public VersionType getDateVersionType() {
        return dateVersionType;
    }

    public ZonedDateTime getVersionDate() {
        return versionDate;
    }

    @Nullable
    public Collection<BinaryTimeSeriesRow> getBinaryValues() {
        if (entries == null) {
            return null;
        }
        return Collections.unmodifiableCollection(entries.values());
    }

    @Override
    public void validate() throws FieldException {

    }


    @JsonPOJOBuilder
    @JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
    public static class Builder {
        private String name;

        private String officeId;

        private Long intervalOffset;
        private String timeZone;
        public VersionType dateVersionType;
        public ZonedDateTime versionDate;

        NavigableMap<DateDateKey, BinaryTimeSeriesRow> entriesMap = null;

        public Builder() {
        }

        public Builder withName(String tsid) {
            this.name = tsid;
            return this;
        }

        public Builder withOfficeId(String officeId) {
            this.officeId = officeId;
            return this;
        }

        public Builder withIntervalOffset(Long intervalOffset) {
            this.intervalOffset = intervalOffset;
            return this;
        }

        public Builder withTimeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public Builder withDateVersionType(VersionType dateVersionType) {
            this.dateVersionType = dateVersionType;
            return this;
        }

        public Builder withVersionDate(ZonedDateTime versionDate) {
            this.versionDate = versionDate;
            return this;
        }


        public Builder withBinaryValues(Collection<BinaryTimeSeriesRow> rows) {
            if (rows == null) {
                entriesMap = null;

            } else {
                if (entriesMap == null) {
                    entriesMap = new TreeMap<>(new DateDateComparator());
                } else {
                    entriesMap.clear();
                }

                for (BinaryTimeSeriesRow row : rows) {
                    withBinaryValue(row);
                }
            }
            return this;
        }

        public Builder withBinaryValue(BinaryTimeSeriesRow row) {
            if (row != null) {
                if (entriesMap == null) {
                    entriesMap = new TreeMap<>(new DateDateComparator());
                }
                entriesMap.put(new DateDateKey(Date.from(row.getDateTime()), Date.from(row.getDataEntryDate())), row);
            }
            return this;
        }

        public BinaryTimeSeries build() {

            return new BinaryTimeSeries(this);
        }
    }

}
