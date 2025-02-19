/*
 * MIT License
 * Copyright (c) 2024 Hydrologic Engineering Center
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cwms.cda.data.dto.location.kind;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cwms.cda.api.errors.FieldException;
import cwms.cda.data.dto.CwmsDTOBase;
import cwms.cda.data.dto.CwmsDTOValidator;
import cwms.cda.data.dto.CwmsId;
import cwms.cda.formatters.Formats;
import cwms.cda.formatters.annotations.FormattableWith;
import cwms.cda.formatters.json.JsonV1;
import java.util.ArrayList;
import java.util.List;

@FormattableWith(contentType = Formats.JSONV1, formatter = JsonV1.class, aliases = {Formats.DEFAULT, Formats.JSON})
@JsonDeserialize(builder = VirtualOutlet.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonPropertyOrder({"projectId", "virtualOutletId", "virtualRecords"})
public final class VirtualOutlet extends CwmsDTOBase {
    @JsonProperty(required = true)
    private final CwmsId projectId;
    @JsonProperty(required = true)
    private final CwmsId virtualOutletId;
    private final List<VirtualOutletRecord> virtualRecords = new ArrayList<>();

    public VirtualOutlet(Builder builder) {
        this.projectId = builder.projectId;
        this.virtualOutletId = builder.virtualOutletId;
        if (builder.virtualRecords != null) {
            virtualRecords.addAll(builder.virtualRecords);
        }
    }

    public CwmsId getVirtualOutletId() {
        return virtualOutletId;
    }

    public List<VirtualOutletRecord> getVirtualRecords() {
        return virtualRecords;
    }

    public CwmsId getProjectId() {
        return projectId;
    }

    @Override
    protected void validateInternal(CwmsDTOValidator validator) {
        super.validateInternal(validator);
        validator.validateCollection(virtualRecords);
    }

    public static final class Builder {
        private CwmsId projectId;
        private CwmsId virtualOutletId;
        private List<VirtualOutletRecord> virtualRecords = new ArrayList<>();

        public VirtualOutlet build() {
            return new VirtualOutlet(this);
        }

        public Builder withVirtualOutletId(CwmsId virtualOutletId) {
            this.virtualOutletId = virtualOutletId;
            return this;
        }

        public Builder withVirtualRecords(List<VirtualOutletRecord> virtualRecords) {
            this.virtualRecords = virtualRecords;
            return this;
        }

        public Builder withProjectId(CwmsId projectId) {
            this.projectId = projectId;
            return this;
        }
    }
}
