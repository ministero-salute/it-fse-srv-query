/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.validators;

import it.finanze.sanita.fse2.ms.srvquery.validators.impl.NoFutureDateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoFutureDateValidator.class)
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoFutureDate {
    String message() default "The date is set in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
