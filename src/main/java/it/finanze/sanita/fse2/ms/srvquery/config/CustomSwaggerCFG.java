/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.srvquery.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** 
 * Custom Swagger Config 
 *
 */
@Configuration
@Getter
public class CustomSwaggerCFG {

	/** 
	 * API ID 
	 */
    @Value("${docs.info.api-id}")
	private String apiId;

    /** 
     * API Summary
     */
	@Value("${docs.info.summary}")
	private String apiSummary;

	/** 
	 * Title 
	 */
    @Value("${docs.info.title}")
    private String title;

    /** 
     * Version 
     */
    @Value("${info.app.version}")
    private String version;

    /**
     * Description 
     */
    @Value("${docs.info.description}")
    private String description;

    /** 
     * Terms of service 
     */
    @Value("${docs.info.termsOfService}")
    private String termsOfService;

    /** 
     * Contact Name 
     */
    @Value("${docs.info.contact.name}")
    private String contactName;

    /** 
     * Contact Url 
     */
    @Value("${docs.info.contact.url}")
    private String contactUrl;

    /** 
     *  Contact Mail 
     */
    @Value("${docs.info.contact.mail}")
    private String contactMail;

    /** 
     * Port 
     */
    @Value("${server.port}")
    private Integer port;

    /** 
     * File Max Length 
     */
    @Value("${validation.file-max-size}")
    private Integer fileMaxLength;

} 


