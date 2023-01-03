/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class KafkaCreateDTO {

    private String resource;
    
}
