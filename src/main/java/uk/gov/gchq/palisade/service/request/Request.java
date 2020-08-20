/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.service.request;


import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.RequestId;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * This is the high level API for any request sent to a service.
 * This makes sure each request has a unique identifier.
 */
public abstract class Request {
    private RequestId id; //this is a unique ID for each individual request made between the micro-services
    private RequestId originalRequestId; //this Id is unique per data access request from a user

    public Request() {
        this.id = new RequestId().id(UUID.randomUUID().toString());
    }

    @Generated
    public Request originalRequestId(final RequestId originalRequestId) {
        this.setOriginalRequestId(originalRequestId);
        return this;
    }

    @Generated
    public RequestId getId() {
        return id;
    }


    @Generated
    public RequestId getOriginalRequestId() {
        return originalRequestId;
    }

    @Generated
    public void setOriginalRequestId(final RequestId originalRequestId) {
        requireNonNull(originalRequestId);
        this.originalRequestId = originalRequestId;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Request)) {
            return false;
        }
        Request request = (Request) o;
        return id.equals(request.id) &&
                originalRequestId.equals(request.originalRequestId);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id, originalRequestId);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", Request.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("originalRequestId=" + originalRequestId)
                .toString();
    }
}