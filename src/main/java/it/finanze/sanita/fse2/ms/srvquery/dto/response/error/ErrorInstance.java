/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response.error;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorInstance {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Validation {
                public static final String CONSTRAINT_FIELD = "/constraint/field";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Server {
                public static final String INTERNAL = "/internal";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Resource {
                public static final String CONFLICT = "/conflict";
                public static final String NOT_FOUND = "/not-found";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Fields {
                public static final String ID = "id";
                public static final String LAST_UPDATE = "lastUpdate";
        }

}
