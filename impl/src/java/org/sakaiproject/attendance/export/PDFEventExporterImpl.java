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

package org.sakaiproject.attendance.export;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.Setter;

import org.apache.log4j.Logger;

import org.sakaiproject.attendance.export.util.SortNameUserComparator;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.user.api.User;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PDFEventExporterImpl implements PDFEventExporter {

    private static final Logger log = Logger.getLogger(PDFEventExporterImpl.class);

    private static final Font h1 = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
    private static final Font h3 = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
    private static final Font body = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

    private static final Font tableHeader = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

    private AttendanceEvent event;
    private Document document;

    public void createSignInPdf(AttendanceEvent event, OutputStream outputStream) {

        this.event = event;
        this.document = new Document();

        buildDocumentShell(outputStream, true);
    }

    public void createAttendanceSheetPdf(AttendanceEvent event, OutputStream outputStream) {

        this.event = event;
        this.document = new Document();

        buildDocumentShell(outputStream, false);
    }

    private void buildDocumentShell(OutputStream outputStream, boolean isSignInSheet) {
        String eventName = event.getName();
        Date eventDate = event.getStartDateTime();

        // TODO: This will be the roster title/name once SAKAI-2370 is resolved
        String siteTitle = sakaiProxy.getSiteTitle(sakaiProxy.getCurrentSiteId());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, YYYY h:mm a");

        try {
            PdfWriter.getInstance(document, outputStream);

            document.open();

            String pageTitle = isSignInSheet?"Sign-In Sheet":"Attendance Sheet";

            Paragraph title = new Paragraph(pageTitle + " - " + siteTitle, h1);


            document.add(title);

            String eventDateString = eventDate==null?"":" (" + dateFormat.format(eventDate) + ")";

            Paragraph eventHeader = new Paragraph(eventName + eventDateString, h3);
            eventHeader.setSpacingBefore(14);
            document.add(eventHeader);

            if(isSignInSheet) {
                document.add(signInSheetTable());
            } else {
                document.add(attendanceSheetTable());
            }

            document.close(); // no need to close PDFwriter?

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private PdfPTable signInSheetTable() {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(12);

        PdfPCell nameHeader = new PdfPCell(new Paragraph("Student Name", tableHeader));
        nameHeader.setPadding(10);

        PdfPCell signatureHeader = new PdfPCell(new Paragraph("Signature", tableHeader));
        signatureHeader.setPadding(10);

        table.addCell(nameHeader);
        table.addCell(signatureHeader);

        List<User> userList = sakaiProxy.getCurrentSiteMembership();
        Collections.sort(userList, new SortNameUserComparator());

        for(User user : userList) {

            PdfPCell userCell = new PdfPCell(new Paragraph(user.getSortName(), body));
            userCell.setPadding(10);

            PdfPCell blankCell = new PdfPCell(new Paragraph());
            blankCell.setPadding(10);

            table.addCell(userCell);
            table.addCell(blankCell);
        }

        return table;

    }

    private PdfPTable attendanceSheetTable() {

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setSpacingBefore(12);

        PdfPCell nameHeader = new PdfPCell(new Paragraph("Student Name", tableHeader));
        nameHeader.setPadding(10);
        nameHeader.setColspan(5);

        // TODO: Status headers are hard coded now but can be changed once SAKAI-2236 is resolved

        PdfPCell presentHeader = new PdfPCell(new Paragraph("Pres", tableHeader));
        presentHeader.setPadding(10);

        PdfPCell unexcusedHeader = new PdfPCell(new Paragraph("Abse", tableHeader));
        unexcusedHeader.setPadding(10);

        PdfPCell excusedHeader = new PdfPCell(new Paragraph("Excu", tableHeader));
        excusedHeader.setPadding(10);

        PdfPCell lateHeader = new PdfPCell(new Paragraph("Late", tableHeader));
        lateHeader.setPadding(10);

        PdfPCell leftEarlyHeader = new PdfPCell(new Paragraph("Left", tableHeader));
        leftEarlyHeader.setPadding(10);

        table.addCell(nameHeader);
        table.addCell(presentHeader);
        table.addCell(unexcusedHeader);
        table.addCell(excusedHeader);
        table.addCell(lateHeader);
        table.addCell(leftEarlyHeader);

        List<User> userList = sakaiProxy.getCurrentSiteMembership();
        Collections.sort(userList, new SortNameUserComparator());

        for(User user : userList) {

            PdfPCell userCell = new PdfPCell(new Paragraph(user.getSortName() + " (" + user.getDisplayId() + ")", body));
            userCell.setPadding(10);
            userCell.setColspan(5);

            table.addCell(userCell);

            for(int i=0; i < 5; i++) {
                // Add blank cell
                table.addCell(new PdfPCell(new Paragraph()));
            }

        }

        return table;
    }

    /**
     * init - perform any actions required here for when this bean starts up
     */
    public void init() {
        log.info("init");
    }

    @Setter
    private SakaiProxy sakaiProxy;

    @Setter
    private AttendanceLogic attendanceLogic;

}
