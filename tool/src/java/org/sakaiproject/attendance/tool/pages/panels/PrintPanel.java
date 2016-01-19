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

package org.sakaiproject.attendance.tool.pages.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.sakaiproject.attendance.model.AttendanceEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class PrintPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private IModel<AttendanceEvent> eventModel;

    private AttendanceEvent selectedEvent;

    private RadioGroup<String> printFormatGroup;

    public PrintPanel(String id, IModel<AttendanceEvent> event) {
        super(id, event);
        this.eventModel = event;

        add(createPrintForm());
    }

    private Form<?> createPrintForm() {

        final Form<?> printForm = new Form<Void>("print-form"){

            @Override
            protected void onSubmit() {

                final boolean isSignIn = printFormatGroup.getModelObject().equals("signin");
                String filename = eventModel.getObject().getName().trim().replaceAll("\\s+", "") + (isSignIn?"-signin.pdf":"-attendance.pdf");

                    AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException {
                            if(isSignIn){
                                pdfExporter.createSignInPdf(eventModel.getObject(), outputStream);
                            } else {
                                pdfExporter.createAttendanceSheetPdf(eventModel.getObject(), outputStream);
                            }
                        }
                    };

                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, filename);
                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
            }
        };

        printForm.add(new Label("event-name", eventModel.getObject().getName()));

        DropDownChoice<String> groupIdChoice = new DropDownChoice<String>("group-id-choice", new Model<String>(), Arrays.asList(new String[]{sakaiProxy.getCurrentSiteId()}));
        printForm.add(groupIdChoice);

        Radio<String> signIn = new Radio<String>("sign-in-radio", new Model<String>("signin"));
        Radio<String> attendance = new Radio<String>("attendance-radio", new Model<String>("attendance"));

        printFormatGroup = new RadioGroup<String>("format-group", new Model<String>());

        printFormatGroup.add(signIn);
        printFormatGroup.add(attendance);

        printForm.add(printFormatGroup);

        SubmitLink print = new SubmitLink("print");

        printForm.add(print);

        return printForm;
    }
}
