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
package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.enums.ErrorClassEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.LanguageEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class EnumsTest {

    @Test
    @DisplayName("testErrorClassEnum")
    void testResultLogEnum() {
        String type = ErrorClassEnum.GENERIC.getType();
        String title = ErrorClassEnum.GENERIC.getTitle();
        String detail = ErrorClassEnum.GENERIC.getDetail();
        String instance = ErrorClassEnum.GENERIC.getInstance();
        
        assertEquals(type, ErrorClassEnum.GENERIC.getType());
        assertEquals(title, ErrorClassEnum.GENERIC.getTitle());
        assertEquals(detail, ErrorClassEnum.GENERIC.getDetail());
        assertEquals(instance, ErrorClassEnum.GENERIC.getInstance());
    }
    
    @Test
    @DisplayName("testLanguageEnum")
    void testLanguageEnum() {
        String code = LanguageEnum.ITALIAN.getCode();
        String description = LanguageEnum.ITALIAN.getDescription();
        
        assertEquals(code, LanguageEnum.ITALIAN.getCode());
        assertEquals(description, LanguageEnum.ITALIAN.getDescription());
    }
    
    @Test
    @DisplayName("testSubscriptionEnum")
    void testSubscriptionEnum() {
        String criteria = SubscriptionEnum.CODESYSTEM_ACTIVE.getCriteria();
        String risorsa = SubscriptionEnum.CODESYSTEM_ACTIVE.getRisorsa();
        
        assertEquals(criteria, SubscriptionEnum.CODESYSTEM_ACTIVE.getCriteria());
        assertEquals(risorsa, SubscriptionEnum.CODESYSTEM_ACTIVE.getRisorsa());
    }
    
}
