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

package cwms.radar.data.dao;

import cwms.radar.data.dto.Catalog;
import cwms.radar.data.dto.Location;
import org.geojson.FeatureCollection;

import java.io.IOException;

public interface LocationsDao {
    String getLocations(String names,String format, String units, String datum, String officeId);
    Location getLocation(String locationName, String unitSystem,
                         String officeId) throws IOException;
    void deleteLocation(String locationName, String officeId);
    void deleteLocation(String locationName, String officeId, boolean cascadeDelete);
    void storeLocation(Location location) throws IOException;
    void renameLocation(String oldLocationName, Location renamedLocation) throws IOException;
    FeatureCollection buildFeatureCollection(String names, String units, String officeId);
    Catalog getLocationCatalog(String cursor, int pageSize, String unitSystem, String office, 
                               String idLike, String categoryLike, String groupLike);
}
