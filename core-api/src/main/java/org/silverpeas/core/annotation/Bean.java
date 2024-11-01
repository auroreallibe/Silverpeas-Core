/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.annotation;

import org.silverpeas.kernel.annotation.Managed;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.*;

/**
 * This annotation is to tag an object as to be managed by the underlying IoC container with the
 * following default life-cycle: for each ask for such a bean, a new instance is created and that
 * object is bound to the life-cycle of the client object. Therefore, any instance of the bean
 * injected into an object that is being created by the container is bound to the lifecycle of the
 * newly created object. This annotation can be both to annotate business or technical objects. It
 * can be also used to define as a base type for a more meaningful IoC annotation.
 * <p>
 * The annotation is an abstraction above the IoC container used by Silverpeas so that it is can
 * possible to change the IoC container (Spring or CDI for example) by changing the wrapped
 * annotation to those specific at this IoC implementation without impacting the annotated IoC
 * managed beans.
 * </p>
 * @author mmoquillon
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Managed
@Dependent
@Stereotype
public @interface Bean {
}
