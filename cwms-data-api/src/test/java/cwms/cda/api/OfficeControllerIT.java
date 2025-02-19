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

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import cwms.cda.formatters.Formats;
import io.javalin.core.util.Header;
import io.restassured.filter.log.LogDetail;
import javax.servlet.http.HttpServletResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("integration")
public class OfficeControllerIT extends DataApiTestIT {

    public static final String OFFICE = "SPK";

    @Test
    void test_get_one()  {

        given()
                .log().ifValidationFails(LogDetail.ALL,true)
                .accept(Formats.JSONV2)
            .when()
                .redirects().follow(true)
                .redirects().max(3)
                .get("/offices/" + OFFICE)
            .then()
                .assertThat()
                .log().ifValidationFails(LogDetail.ALL,true)
                .statusCode(is(HttpServletResponse.SC_OK))
                .header(Header.ETAG, not(isEmptyOrNullString()))
                .headers(Header.CACHE_CONTROL.toLowerCase(), containsString("max-age="))
                .body("long-name", containsString("Sacramento"));
    }

    @Test
    void test_get_one_xmlv2()  {

        given()
                .log().ifValidationFails(LogDetail.ALL,true)
                .accept(Formats.XMLV2)
            .when()
                .redirects().follow(true)
                .redirects().max(3)
                .get("/offices/" + OFFICE)
            .then()
                .assertThat()
                .log().ifValidationFails(LogDetail.ALL,true)
                .statusCode(is(HttpServletResponse.SC_OK))
                .header(Header.ETAG, not(isEmptyOrNullString()))
                .headers(Header.CACHE_CONTROL.toLowerCase(), containsString("max-age="))
                .body("office.long-name", containsString("Sacramento"));
    }

    @Test
    void test_get_all()  {
        given()
                .log().ifValidationFails(LogDetail.ALL,true)
                .accept(Formats.JSONV2)
            .when()
                .redirects().follow(true)
                .redirects().max(3)
                .get("/offices/")
            .then()
                .assertThat()
                .log().ifValidationFails(LogDetail.ALL,true)
                .statusCode(is(HttpServletResponse.SC_OK))
                .header(Header.ETAG, not(isEmptyOrNullString()))
                .headers(Header.CACHE_CONTROL.toLowerCase(), containsString("max-age="))
                .rootPath("find {it.name == '%s'}", withArgs("CPC"))
                .body("long-name", CoreMatchers.equalTo("Central Processing Center"));
    }

    @EnumSource(AliasTest.class)
    @ParameterizedTest(name = "{index} {0}")
    void test_get_all_aliases(AliasTest test)
    {
        given()
                .log().ifValidationFails(LogDetail.ALL,true)
                .accept(test._acceptFormat)
            .when()
                .redirects().follow(true)
                .redirects().max(3)
                .get("/offices/")
            .then()
                .assertThat()
                .log().ifValidationFails(LogDetail.ALL,true)
                .statusCode(is(test._expectedGetAllStatusCode))
                .contentType(is(test._expectedFormat));
    }

    @EnumSource(AliasTest.class)
    @ParameterizedTest(name = "{index} {0}")
    void test_get_one_aliases(AliasTest test)
    {
        given()
                .log().ifValidationFails(LogDetail.ALL,true)
                .accept(test._acceptFormat)
            .when()
                .redirects().follow(true)
                .redirects().max(3)
                .get("/offices/" + OFFICE)
            .then()
                .assertThat()
                .log().ifValidationFails(LogDetail.ALL,true)
                .statusCode(is(test._expectedGetAllStatusCode))
                .contentType(is(test._expectedFormat));
    }

    enum AliasTest
    {
        DEFAULT(Formats.DEFAULT, Formats.JSONV2),
        JSON(Formats.JSON, Formats.JSONV2),
        XML(Formats.XML, Formats.XMLV2),
        XMLv1(Formats.XMLV1, Formats.XMLV1),
        /* expected format is the error response format */
        BAD("text/plain", Formats.JSON, HttpServletResponse.SC_NOT_ACCEPTABLE)
        ;

        private final String _expectedFormat;
        private final String _acceptFormat;
        private final int _expectedGetAllStatusCode;

        AliasTest(String acceptFormat, String expectedFormat)
        {
            this(acceptFormat, expectedFormat, HttpServletResponse.SC_OK);
        }
        AliasTest(String acceptFormat, String expectedFormat, int expectedGetAllStatusCode)
        {
            _acceptFormat = acceptFormat;
            _expectedFormat = expectedFormat;
            _expectedGetAllStatusCode = expectedGetAllStatusCode;
        }
    }
}
