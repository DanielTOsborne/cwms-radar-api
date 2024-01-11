package cwms.cda.formatters.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cwms.cda.data.dto.Catalog;
import cwms.cda.data.dto.Clob;
import cwms.cda.data.dto.Clobs;
import cwms.cda.data.dto.CwmsDTOBase;
import cwms.cda.data.dto.Location;
import cwms.cda.data.dto.LocationCategory;
import cwms.cda.data.dto.LocationGroup;
import cwms.cda.data.dto.Office;
import cwms.cda.data.dto.RecentValue;
import cwms.cda.data.dto.TimeSeriesCategory;
import cwms.cda.data.dto.TimeSeriesGroup;
import cwms.cda.formatters.Formats;
import cwms.cda.formatters.FormattingException;
import cwms.cda.formatters.OfficeFormatV1;
import cwms.cda.formatters.OutputFormatter;
import io.javalin.http.BadRequestResponse;
import org.jetbrains.annotations.NotNull;
import service.annotations.FormatService;

@FormatService(contentType = Formats.JSON,
			   dataTypes = {
				   Location.class,
				   Catalog.class,
				   LocationGroup.class,
				   LocationCategory.class,
				   TimeSeriesCategory.class, TimeSeriesGroup.class,
				   TimeSeriesGroup.class,
				   RecentValue.class
				})
/**
 * A Formatter for the calls that returned JSON generated by CWMS itself inside of Oracle.
 */
public class JsonV1 implements OutputFormatter {

	private final ObjectMapper om;

	public JsonV1() {
		this(new ObjectMapper());
	}

	public JsonV1(ObjectMapper om) {
		this.om = om.copy();
		this.om.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		this.om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		this.om.registerModule(new JavaTimeModule());
	}

	@NotNull
	public static ObjectMapper buildObjectMapper() {
		return buildObjectMapper(new ObjectMapper());
	}

	@NotNull
	public static ObjectMapper buildObjectMapper(ObjectMapper om) {
		ObjectMapper retVal = om.copy();

		retVal.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		retVal.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		retVal.registerModule(new JavaTimeModule());
		return retVal;
	}

	@Override
	public String getContentType() {
		return Formats.JSON;
	}

	@Override
	public String format(CwmsDTOBase dto) {
		Object fmtv1 = buildFormatting(dto);
		try {
			return om.writeValueAsString(fmtv1);
		} catch(JsonProcessingException e) {
			throw new FormattingException("Could not format:" + dto, e);
		}
	}

	@Override
	public String format(List<? extends CwmsDTOBase> daoList) {
		Object wrapped = buildFormatting(daoList);
		try {
			return om.writeValueAsString(wrapped);
		} catch(JsonProcessingException e) {
			throw new FormattingException("Could not format list:" + daoList, e);
		}
	}

	private Object buildFormatting(CwmsDTOBase dao) {
		Object retVal = null;

		if (dao instanceof Office) {
			List<Office> offices = Arrays.asList((Office) dao);
			retVal = new OfficeFormatV1(offices);
		} else if (dataTypesContains(dao.getClass())) {
			// Any types that have to be handle as special cases
			// should be in else if branches before this
			// If the class is in the annotation assume we can just return it.
			retVal = dao;
		}

		if (retVal == null) {
			String klassName = "unknown";
			if (dao != null) { 
				klassName = dao.getClass().getName();
			}
			throw new BadRequestResponse(
					String.format("Format %s not implemented for data of class:%s", getContentType(), klassName));
		}
		return retVal;
	}

	private boolean dataTypesContains(Class<?> klass) {
		List<Class<?>> dataTypes = getDataTypes();
		return dataTypes.contains(klass);
	}

	private List<Class<?>> getDataTypes() {
		List<Class<?>> retVal = Collections.emptyList();
		FormatService fs = JsonV1.class.getDeclaredAnnotation(FormatService.class);
		if (fs != null) {
			Class<?>[] classes = fs.dataTypes();
			if (classes != null && classes.length > 0) {
				retVal = Arrays.asList(classes);
			}
		}
		return retVal;
	}

	private Object buildFormatting(List<? extends CwmsDTOBase> daoList) {
		Object retVal = null;

		if (daoList != null && !daoList.isEmpty()) {
			CwmsDTOBase firstObj = daoList.get(0);
			if (firstObj instanceof Office)	{
				List<Office> officesList = daoList.stream().map(Office.class::cast).collect(Collectors.toList());
				retVal = new OfficeFormatV1(officesList);
			} else if (dataTypesContains(firstObj.getClass())) {
				// If dataType annotated with the class we can return an array of them.
				// If a class needs to be handled differently an else_if branch can be added above
				// here and a wrapper obj used to format the return value however is desired.
				retVal = daoList;
			}

			if (retVal == null)	{
				String klassName = "unknown";
				if (firstObj != null) {
					klassName = firstObj.getClass().getName();
				}
				throw new BadRequestResponse(String.format("Format %s not implemented for data of class:%s", getContentType(), klassName));
			}
		}
		return retVal;
	}

}
