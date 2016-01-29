/*
 *  Copyright (c) 2016, The Apereo Foundation
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.pages.panels.util;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.sakaiproject.attendance.api.AttendanceGradebookProvider;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class GradebookItemNameValidator implements IValidator<String> {
    @SpringBean(name="org.sakaiproject.attendance.api.AttendanceGradebookProvider")
    private AttendanceGradebookProvider attendanceGradebookProvider;

    private String siteID;

    public GradebookItemNameValidator(String siteID) {
        this.siteID = siteID;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        Injector.get().inject(this);
        final String name = validatable.getValue();

        if(attendanceGradebookProvider.isGradebookAssignmentDefined(siteID, name)) {
            error(validatable, "gradebook.name.defined");
        }
    }

    private void error(IValidatable<String> validatable, String errorKey) {
        ValidationError error = new ValidationError();
        error.addKey(getClass().getSimpleName() + "." + errorKey);
        validatable.error(error);
    }
}
