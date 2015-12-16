/*
 *  Copyright (c) 2015, The Apereo Foundation
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

import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

import java.util.Date;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class SequentialDateTimeFieldValidator extends AbstractFormValidator {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** form components to be checked. */
    private final DateTimeField[] components;

    /**
     * Construct.
     *
     * @param formComponent1
     *            a form component
     * @param formComponent2
     *            a form component
     */
    public SequentialDateTimeFieldValidator(DateTimeField formComponent1,
                                   DateTimeField formComponent2) {
        if (formComponent1 == null) {
            throw new IllegalArgumentException(
                    "argument formComponent1 cannot be null");
        }
        if (formComponent2 == null) {
            throw new IllegalArgumentException(
                    "argument formComponent2 cannot be null");
        }
        components = new DateTimeField[] { formComponent1, formComponent2 };
    }

    /**
     * @see wicket.markup.html.form.validation.IFormValidator#getDependentFormComponents()
     */
    public FormComponent[] getDependentFormComponents() {
        return components;
    }

    /**
     * @see wicket.markup.html.form.validation.IFormValidator#validate(wicket.markup.html.form.Form)
     */
    public void validate(Form form) {
        // we have a choice to validate the type converted values or the raw
        // input values, we validate the raw input
        final DateTimeField formComponent1 = components[0];
        final DateTimeField formComponent2 = components[1];

        Date startDate = formComponent1.getConvertedInput();
        Date endDate = formComponent2.getConvertedInput();

       if(startDate.getTime() >= endDate.getTime()) {
            formComponent2.error(formComponent2.getString("error.startDateAfterEndDate"));
        }
    }
}
