/*
 * MIT License
 *
 * Copyright (c) 2023 Hydrologic Engineering Center
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cwms.cda.api;

import fixtures.TestAccounts.KeyUser;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import cwms.cda.data.dto.Location;
import cwms.cda.formatters.Formats;
import cwms.cda.formatters.json.JsonV1;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.servlet.http.HttpServletResponse;

import static cwms.cda.api.Controllers.CASCADE_DELETE;
import static cwms.cda.api.Controllers.FORMAT;
import static cwms.cda.api.Controllers.OFFICE;
import static cwms.cda.data.dao.JsonRatingUtilsTest.loadResourceAsString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Tag("integration")
public class LocationControllerTestIT extends DataApiTestIT {

    @Test
    void test_location_create_get_delete() throws Exception {
        String officeId = "SPK";
        String json = loadResourceAsString("cwms/cda/api/location_create_spk.json");
        Location location = new Location.Builder(Formats.parseContent(Formats.parseHeader(Formats.JSON, Location.class),
            json, Location.class))
                .withOfficeId(officeId)
                //withName(getClass().getSimpleName())
                .build();
        String serializedLocation = JsonV1.buildObjectMapper().writeValueAsString(location);

        KeyUser user = KeyUser.SPK_NORMAL;
        // create location
        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .accept(Formats.JSON)
            .contentType(Formats.JSON)
            .body(serializedLocation)
            .header("Authorization", user.toHeaderValue())
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .post("/locations")
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_OK));
        //Create associated time series so delete fails without cascade
        try {
            createTimeseries(officeId, location.getName() + ".Flow.Inst.~1Hour.0.cda-test");
        } catch (Exception ex) {
            // ignore
        }

        // get it back
        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .accept(Formats.JSON)
            .header("Authorization", user.toHeaderValue())
            .queryParam("office", officeId)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .get("/locations/" + location.getName())
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_OK));

        // delete without cascade should fail
        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .accept(Formats.JSON)
            .header("Authorization", user.toHeaderValue())
            .queryParam(OFFICE, officeId)
            .queryParam(CASCADE_DELETE, false)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .delete("/locations/" + location.getName())
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_CONFLICT));

        // delete with cascade should succeed
        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .accept(Formats.JSON)
            .header("Authorization", user.toHeaderValue())
            .queryParam(OFFICE, officeId)
            .queryParam(CASCADE_DELETE, true)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .delete("/locations/" + location.getName())
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_OK));

        // get it back
        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .accept(Formats.JSON)
            .header("Authorization", user.toHeaderValue())
            .queryParam("office", officeId)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .get("/locations/" + location.getName())
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_NOT_FOUND));
    }

    @Test
    void test_delete_location_that_does_not_exist() {
        final String officeId = "SPK";
        final String locationName = "I do not exit";
        final KeyUser user = KeyUser.SPK_NORMAL;

        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .accept(Formats.JSON)
            .header("Authorization", user.toHeaderValue())
            .queryParam(OFFICE, officeId)
            .queryParam(CASCADE_DELETE, true)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .delete("/locations/{loc}", locationName)
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_NOT_FOUND));
    }

    @ParameterizedTest
    @EnumSource(GetAllTest.class)
    void test_get_all_locations(GetAllTest test)
    {
        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .accept(test._accept)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .get("/locations/")
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_OK))
            .contentType(is(test._expectedContentType));
    }

    @ParameterizedTest
    @EnumSource(GetAllLegacyTest.class)
    void test_get_all_locations_legacy_types(GetAllLegacyTest test)
    {
        given()
            .log().ifValidationFails(LogDetail.ALL,true)
            .queryParam(FORMAT, test._accept)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .get("/locations/")
        .then()
            .log().ifValidationFails(LogDetail.ALL,true)
            .assertThat()
            .statusCode(is(HttpServletResponse.SC_OK))
            .contentType(is(test._expectedContentType));
    }

    enum GetAllLegacyTest
    {
        JSON(Formats.JSON_LEGACY, Formats.JSON),
        CSV(Formats.CSV_LEGACY, Formats.CSV),
        XML(Formats.XML_LEGACY, Formats.XML),
        TAB(Formats.TAB_LEGACY, Formats.TAB),
        GEOJSON(Formats.GEOJSON_LEGACY, Formats.GEOJSON),
        ;

        final String _accept;
        final String _expectedContentType;

        GetAllLegacyTest(String accept, String expectedContentType)
        {
            _accept = accept;
            _expectedContentType = expectedContentType;
        }
    }

    enum GetAllTest
    {
        DEFAULT(Formats.DEFAULT, Formats.JSONV2),
        JSON(Formats.JSON, Formats.JSONV2),
        JSONV1(Formats.JSONV1, Formats.JSONV1),
        JSONV2(Formats.JSONV2, Formats.JSONV2),
        GEOJSON(Formats.GEOJSON, Formats.GEOJSON),
        XML(Formats.XML, Formats.XMLV2),
        XMLV1(Formats.XMLV1, Formats.XMLV1),
        XMLV2(Formats.XMLV2, Formats.XMLV2),
        ;

        final String _accept;
        final String _expectedContentType;

        GetAllTest(String accept, String expectedContentType)
        {
            _accept = accept;
            _expectedContentType = expectedContentType;
        }
    }
}
