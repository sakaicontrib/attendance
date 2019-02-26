/*
 *  Copyright (c) 2017, University of Dayton
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

package org.sakaiproject.attendance.tool.pages;

import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.*;
import org.sakaiproject.user.api.User;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.CellType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by james on 5/18/17.
 */
public class ExportPage extends BasePage{
    enum ExportFormat {
        XLS
    }
    private String holder = "";
    private int rowCounter = 0;
    private int userStatsCounter = 0;
    private int repeatPlaceHolder = 0;
    private static final long serialVersionUID = 1L;
    boolean includeComments = false;
    boolean blankSheet = false;
    public ExportPage() {
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        disableLink(exportLink);
        if(this.role != null && this.role.equals("Student")) {
            throw new RestartResponseException(StudentView.class);
        }

        Model<AttendanceSite> siteModel = new Model<>(attendanceLogic.getCurrentAttendanceSite());
        Form<AttendanceSite> exportForm = new Form<>("export-form", siteModel);
        add(exportForm);
        exportForm.add(new AjaxCheckBox("exportIncludeComments", Model.of(this.includeComments)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
                ExportPage.this.includeComments = !ExportPage.this.includeComments;
                setDefaultModelObject(ExportPage.this.includeComments);
            }
        });

        exportForm.add(new AjaxCheckBox("exportBlankSheet", Model.of(this.blankSheet)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
                ExportPage.this.blankSheet = !ExportPage.this.blankSheet;
                setDefaultModelObject(ExportPage.this.blankSheet);
            }
        });

        exportForm.add(new DownloadLink("submit-link", new LoadableDetachableModel<File>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected File load() {
                return buildExcelFile(blankSheet, includeComments);
            }
            public void onSubmit() {
                setResponsePage(new ExportPage());
            }
        }).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

        add(new UploadForm("form"));
    }

    private File buildExcelFile(boolean blankSheet, boolean commentsOnOff) {
        File tempFile;
        try {
            setResponsePage(new ExportPage());
            userStatsCounter = 0;
            tempFile = File.createTempFile(buildFileNamePrefix(), buildFileNameSuffix());
            final HSSFWorkbook wb = new HSSFWorkbook();
            int eventCount;
            int studentCount;
            int columnFinder = 0;
            boolean studentRecorded = false;
            boolean localUserExists = false;
            // Create new sheet
            HSSFSheet mainSheet = wb.createSheet("Export");
            // Create Excel header
            final List<String> header = new ArrayList<String>();
            final String selectedGroup = null;
            final String siteID = sakaiProxy.getCurrentSiteId();
            final List<AttendanceEvent> eventHolder = new ArrayList<AttendanceEvent>();
            AttendanceSite attendanceSite = attendanceLogic.getAttendanceSite(siteID);
            List<AttendanceEvent> attendanceEventlist = attendanceLogic.getAttendanceEventsForSite(attendanceSite);
            List<String> groupIds = sakaiProxy.getAvailableGroupsForCurrentSite();
            List<AttendanceUserGroupStats> finalUserStatsList = new ArrayList<AttendanceUserGroupStats>();
            List<AttendanceUserStats> fullUserList = attendanceLogic.getUserStatsForSite(attendanceLogic.getAttendanceSite(siteID), null);

            AttendanceUserGroupStats finalUserStatsListholder = new AttendanceUserGroupStats();

            for(int i = 0; i < groupIds.size(); i++){
                List<User> sectionUsers = sakaiProxy.getSectionMembership(siteID, groupIds.get(i));
                if(sectionUsers.size()>0){
                    for(int j =0; j < sectionUsers.size(); j++){
                        for(int k = 0; k < finalUserStatsList.size(); k++){
                            if(sectionUsers.get(j).getId().equals(finalUserStatsList.get(k).getUserID())){
                                studentRecorded = true;
                                repeatPlaceHolder = k;
                            }
                        }
                        if(studentRecorded){
                            finalUserStatsListholder = new AttendanceUserGroupStats();
                            finalUserStatsListholder.setUserID(finalUserStatsList.get(repeatPlaceHolder).getUserID());
                            finalUserStatsListholder.setAttendanceSite(finalUserStatsList.get(repeatPlaceHolder).getAttendanceSite());
                            finalUserStatsListholder.setGroupId(finalUserStatsList.get(repeatPlaceHolder).getGroupId() + ", " + sakaiProxy.getGroupTitle(siteID ,groupIds.get(i)));

                            finalUserStatsList.set(repeatPlaceHolder, finalUserStatsListholder);
                        }
                        else {
                            finalUserStatsListholder = new AttendanceUserGroupStats();
                            finalUserStatsListholder.setUserID(sectionUsers.get(j).getId());
                            finalUserStatsListholder.setAttendanceSite(attendanceLogic.getAttendanceSite(siteID));
                            finalUserStatsListholder.setGroupId(sakaiProxy.getGroupTitle(siteID ,groupIds.get(i)));
                            finalUserStatsList.add(userStatsCounter, finalUserStatsListholder);
                            userStatsCounter++;
                        }
                        studentRecorded = false;
                    }
                }
            }

            if(fullUserList.size() > finalUserStatsList.size()){
                for(int i = 0; i < fullUserList.size(); i++){
                    for(int j = 0; j < finalUserStatsList.size(); j++){
                        if(fullUserList.get(i).getUserID().equals(finalUserStatsList.get(j).getUserID())){
                            localUserExists = true;
                        }

                    }
                    if(localUserExists){}
                    else {
                        finalUserStatsListholder = new AttendanceUserGroupStats();
                        finalUserStatsListholder.setUserID(fullUserList.get(i).getUserID());
                        finalUserStatsListholder.setAttendanceSite(fullUserList.get(i).getAttendanceSite());
                        finalUserStatsListholder.setGroupId("");
                        finalUserStatsList.add(userStatsCounter, finalUserStatsListholder);
                        userStatsCounter++;
                    }
                    localUserExists = false;
                }
            }
            Collections.sort(finalUserStatsList, new Comparator<AttendanceUserGroupStats>() {
                @Override
                public int compare(AttendanceUserGroupStats attendanceUserGroupStats, AttendanceUserGroupStats t1) {
                    if((attendanceUserGroupStats.getUserID() == null) && (t1.getUserID() == null)) {
                        return 0;
                    } else if (attendanceUserGroupStats.getUserID() == null){
                        return -1;
                    } else if (t1.getUserID() == null){
                        return 1;
                    } else {
                        return sakaiProxy.getUserSortName(attendanceUserGroupStats.getUserID()).compareTo(sakaiProxy.getUserSortName(t1.getUserID()));
                    }
                }
            });
            Collections.reverse(attendanceEventlist);
            Collections.sort(attendanceEventlist, new Comparator<AttendanceEvent>() {
                @Override
                public int compare(AttendanceEvent attendanceEvent, AttendanceEvent t1) {
                    if((attendanceEvent.getStartDateTime() == null) && (t1.getStartDateTime() == null)) {
                        return 0;
                    } else if (attendanceEvent.getStartDateTime() == null){
                        return -1;
                    } else if (t1.getStartDateTime() == null){
                        return 1;
                    } else{
                        return attendanceEvent.getStartDateTime().compareTo(t1.getStartDateTime());
                    }
                }
            });
            eventCount = attendanceEventlist.size();
            studentCount = finalUserStatsList.size();
            header.add("StudentID");
            header.add("Student Name");
            header.add("Section");

            for(int y = 0; y < eventCount; y++){
                String holder2 = String.valueOf(attendanceEventlist.get(y).getStartDateTime());
                if (holder2.equals("null")){
                    header.add(attendanceEventlist.get(y).getName() + " [] " + "(" + String.valueOf(attendanceEventlist.get(y).getId())+ ")");
                    if(commentsOnOff){
                        header.add(attendanceEventlist.get(y).getName() + " [] Comments" + "(" + String.valueOf(attendanceEventlist.get(y).getId())+ ")");
                    }
                }
                else{
                    header.add(attendanceEventlist.get(y).getName() + "[" + String.valueOf(attendanceEventlist.get(y).getStartDateTime()) + "]" + "(" + String.valueOf(attendanceEventlist.get(y).getId())+ ")");
                    if(commentsOnOff) {
                        header.add(attendanceEventlist.get(y).getName() + "[" + String.valueOf(attendanceEventlist.get(y).getStartDateTime()) + "]Comments" + "(" + String.valueOf(attendanceEventlist.get(y).getId())+ ")");
                    }
                }
                eventHolder.add(attendanceLogic.getAttendanceEvent(attendanceEventlist.get(y).getId()));
            }
            HSSFFont boldFont = wb.createFont();
            boldFont.setBold(true);
            boldFont.setUnderline(HSSFFont.U_SINGLE);
            HSSFCellStyle boldStyle = wb.createCellStyle();
            boldStyle.setFont(boldFont);

            // Create the Header row
            HSSFRow headerRow = mainSheet.createRow(0);
            for (int i = 0; i < header.size(); i++) {
                HSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(header.get(i));
                cell.setCellType(CellType.STRING);
                cell.setCellStyle(boldStyle);
            }

            final int[] rowCount = {1};
            final int[] cellCount = {0};
            for(int x = 0; x < studentCount; x++) {
                rowCounter = 0;
                List<AttendanceRecord> attendanceRecordlist = attendanceLogic.getAttendanceRecordsForUser(finalUserStatsList.get(x).getUserID().toString());
                HSSFRow row = mainSheet.createRow(rowCount[0]);
                final User user = sakaiProxy.getUser(finalUserStatsList.get(x).getUserID());
                cellCount[0] = 0;

                if (true) {
                    HSSFCell cell = row.createCell(cellCount[0]);
                    cell.setCellValue(user.getEid());
                    cell.setCellType(CellType.STRING);
                    cellCount[0]++;
                }
                if (true) {
                    HSSFCell cell = row.createCell(cellCount[0]);
                    cell.setCellValue(user.getSortName());
                    cell.setCellType(CellType.STRING);
                    cellCount[0]++;
                }
                if (true) {
                    HSSFCell cell = row.createCell(cellCount[0]);
                    cell.setCellValue(finalUserStatsList.get(x).getGroupId());
                    cell.setCellType(CellType.STRING);
                    cellCount[0]++;
                }
                for(int y = 0; y < eventCount; y++){
                    if (true) {
                        for(int p = 0; p < eventCount; p++){
                            if(String.valueOf(eventHolder.get(y)).equals(String.valueOf(attendanceRecordlist.get(p).getAttendanceEvent()))){
                                columnFinder = p;
                            }
                        }
                        this.holder = String.valueOf(attendanceRecordlist.get(columnFinder).getStatus());
                        if(this.holder.equals("PRESENT")) {
                            this.holder = "P";
                        } else if (this.holder.equals("UNEXCUSED_ABSENCE")){
                            this.holder = "A";
                        } else if (this.holder.equals("EXCUSED_ABSENCE")){
                            this.holder = "E";
                        } else if (this.holder.equals("LATE")){
                            this.holder = "L";
                        } else if (this.holder.equals("LEFT_EARLY")){
                            this.holder = "LE";
                        } else {
                            this.holder = "";
                        }
                        HSSFCell cell = row.createCell(cellCount[0]);
                        if(blankSheet){
                            cell.setCellValue("");
                        }else{
                            cell.setCellValue(this.holder);
                        }
                        cell.setCellType(CellType.STRING);
                        cellCount[0]++;
                    }
                    if(commentsOnOff) {
                        if (true) {
                            this.holder = String.valueOf(attendanceRecordlist.get(columnFinder).getComment());
                            if (this.holder.equals("null")){
                                this.holder = "";
                            }
                            HSSFCell cell = row.createCell(cellCount[0]);
                            if(blankSheet){
                                cell.setCellValue("");
                            }else{
                                cell.setCellValue(this.holder);
                            }
                            cell.setCellType(CellType.STRING);
                            cellCount[0]++;
                        }
                    }
                }
                rowCount[0]++;
                this.rowCounter++;
            }
            FileOutputStream fos = new FileOutputStream(tempFile);
            wb.write(fos);

            fos.close();
            wb.close();

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }

    private String buildFileNamePrefix() {
        final String prefix = "attendence_Export-";
        return prefix;
    }

    private String buildFileNameSuffix() {
        return "." + ExportPage.ExportFormat.XLS.toString().toLowerCase();
    }

    private class UploadForm extends Form<Void> {

        FileUploadField fileUploadField;

        public UploadForm(final String id) {
            super(id);

            setMultiPart(true);
            setMaxSize(Bytes.megabytes(2));

            this.fileUploadField = new FileUploadField("upload");
            add(this.fileUploadField);

            add(new Button("continuebutton"));
            SubmitLink submit = new SubmitLink("submitLink");
            add(submit);
        }

        @Override
        public void onSubmit() {
            String statusInput;
            String eventHeaderHolder;
            String idHolder;
            String dateHolder;
            String comment;
            String oldComment;
            String extension = "";
            String eventDateHolder ="";
            int sheetLengthcounter;
            int indexCounter = 0;
            int headerIndexStart = 0;
            int headerIndexEnd = 0;
            int eventCounter = 3;
            boolean hasComments = false;
            boolean hasCells = false;
            boolean recordExists = false;
            boolean eventExists;
            boolean commentsChanged = false;
            boolean hasRows = true;
            boolean unmodified = false;
            boolean badHeader = false;
            User userHolder;
            final String selectedGroup = null;
            final List<Long> idTracker = new ArrayList<Long>();
            final List<String> eventNameList = new ArrayList<String>();
            final List<String> eventDateList = new ArrayList<String>();
            int a = 0;
            boolean changes = false;
            ImportConfirmList ICL = new ImportConfirmList();
            List<ImportConfirmList> ICList = new ArrayList<ImportConfirmList>();
            List<AttendanceUserStats> userStatsList = attendanceLogic.getUserStatsForCurrentSite(selectedGroup);
            Collections.sort(userStatsList, new Comparator<AttendanceUserStats>() {
                @Override
                public int compare(AttendanceUserStats attendanceUserStats, AttendanceUserStats t1) {
                    if((attendanceUserStats.getId() == null) && (t1.getId() == null)) {
                        return 0;
                    } else if (attendanceUserStats.getId() == null){
                        return -1;
                    } else if (t1.getId() == null){
                        return 1;
                    } else {
                        return attendanceUserStats.getId().intValue() - t1.getId().intValue();
                    }
                }
            });
            AttendanceSite attendanceSite = attendanceLogic.getAttendanceSite(sakaiProxy.getCurrentSiteId());
            List<AttendanceEvent> attendanceEventlist = attendanceLogic.getAttendanceEventsForSite(attendanceSite);
            List<AttendanceUserStats> userList = attendanceLogic.getUserStatsForCurrentSite(selectedGroup);
            int eventCount = attendanceEventlist.size();
            int studentCount = userList.size();
            final FileUpload upload = this.fileUploadField.getFileUpload();
            if (upload != null) {
                extension = upload.getClientFileName();
                if (extension.contains(".")) {
                    extension = extension.substring(extension.lastIndexOf(".") + 1, extension.length());
                }
            }
            if (upload == null) {
                getSession().error(getString("attendance.export.import.error.null_file"));
                setResponsePage(new Overview());
            } else if (upload.getSize() == 0) {
                getSession().error(getString("attendance.export.import.error.empty_file"));
                setResponsePage(new ExportPage());
            }else if (!(ExportPage.ExportFormat.XLS.toString().toLowerCase()).equals(extension)){
                getSession().error(getString("attendance.export.import.error.bad_file_format"));
                setResponsePage(new ExportPage());
            } else if (upload != null) {
                try{
                    File temp = upload.writeToTempFile();
                    FileInputStream fis = new FileInputStream(temp);
                    HSSFWorkbook workbook = new HSSFWorkbook(fis);
                    HSSFSheet sheet = workbook.getSheetAt(0);
                    Iterator rows = sheet.rowIterator();
                    if(!(rows.hasNext())) {
                        hasRows = false;
                    }
                    if(hasRows){
                        rowCounter = 0;
                        for(int r =0; r <= studentCount; r++) {
                            if (rows.hasNext()) {
                                sheetLengthcounter = 0;
                                HSSFRow row = (HSSFRow) rows.next();
                                Iterator cells = row.cellIterator();
                                List data = new ArrayList();
                                List missingNames = new ArrayList();
                                while (cells.hasNext()) {
                                    HSSFCell cell = (HSSFCell) cells.next();
                                    data.add(cell);
                                    hasCells = true;
                                    sheetLengthcounter++;
                                }

                                if (rowCounter == 0) {
                                    if ((data.get(0).toString().equals("StudentID")) && (data.get(1).toString().equals("Student Name")) && (data.get(2).toString().equals("Section"))) {
                                        unmodified = true;
                                    }
                                    hasComments = (data.get(4).toString().contains("]Comments("));
                                } else {
                                    if (data.size() > 0) {
                                        if (data.get(0).toString().equals("")) {
                                            hasCells = false;
                                        } else {
                                            hasCells = true;
                                        }
                                    } else {
                                        hasCells = false;
                                    }
                                }

                                if (hasComments) {
                                    eventCounter = ((sheetLengthcounter - 3) / 2);
                                } else {
                                    eventCounter = (sheetLengthcounter - 3);
                                }
                                if (unmodified) {
                                    if (rowCounter == 0) {
                                        hasCells = false;
                                        for (int q = 0; q < eventCounter; q++) {
                                            if (hasComments) {
                                                eventHeaderHolder = String.valueOf(data.get(3 + (2 * q)));
                                            } else {
                                                eventHeaderHolder = String.valueOf(data.get(3 + q));
                                            }
                                            headerIndexStart = eventHeaderHolder.lastIndexOf("(");
                                            headerIndexEnd = eventHeaderHolder.lastIndexOf(")");
                                            idHolder = eventHeaderHolder.substring(headerIndexStart + 1, headerIndexEnd);
                                            headerIndexStart = eventHeaderHolder.lastIndexOf("[");
                                            headerIndexEnd = eventHeaderHolder.lastIndexOf("]");
                                            dateHolder = eventHeaderHolder.substring(headerIndexStart + 1, headerIndexEnd);
                                            idTracker.add(Long.parseLong(idHolder));
                                            eventNameList.add(eventHeaderHolder.substring(0, headerIndexStart));
                                            if (headerIndexEnd == (headerIndexStart + 1)) {
                                                eventDateList.add("NODATE");
                                            } else {
                                                eventDateList.add(dateHolder);
                                            }


                                        }
                                    }
                                    if ((rowCounter > 0) && (hasCells)) {
                                        hasCells = false;
                                        String userName = String.valueOf(data.get(1));
                                        String userEID = String.valueOf(data.get(0));
                                        ////List<AttendanceRecord> attendanceRecordlist = attendanceLogic.getAttendanceRecordsForUser(userStatsList.get(rowCounter - 1).getUserID().toString());
                                        List<AttendanceEvent> siteEventList = new ArrayList<AttendanceEvent>(attendanceLogic.getAttendanceEventsForCurrentSite());
                                        User userGetter;

                                        for (int q = 0; q < eventCounter; q++) {
                                            recordExists = false;
                                            eventExists = false;
                                            for (int i = 0; i < siteEventList.size(); i++) {
                                                if (siteEventList.get(i).getId().equals(idTracker.get(q))) {
                                                    eventExists = true;
                                                }
                                            }

                                            if (eventExists) {
                                                List<AttendanceRecord> records = new ArrayList<AttendanceRecord>((attendanceLogic.getAttendanceEvent(idTracker.get(q))).getRecords());
                                                for (int s = 0; s < records.size(); s++) {
                                                    userGetter = sakaiProxy.getUser(records.get(s).getUserID());
                                                    if (userEID.equals(userGetter.getEid())) {
                                                        indexCounter = s;
                                                        recordExists = true;
                                                    }
                                                }
                                                AttendanceRecord aR;
                                                if (recordExists) {
                                                    aR = attendanceLogic.getAttendanceRecord(records.get(indexCounter).getId());
                                                } else {
                                                    missingNames.add(userEID);
                                                    userGetter = sakaiProxy.getUserByEID(userEID);
                                                    aR = new AttendanceRecord((attendanceLogic.getAttendanceEvent(idTracker.get(q))), userGetter.getId(), Status.UNKNOWN);
                                                    missingNames.clear();
                                                }
                                                if (hasComments) {
                                                    statusInput = String.valueOf(data.get(3 + (2 * q))).toUpperCase();
                                                    comment = String.valueOf(data.get(4 + (2 * q)));
                                                } else {
                                                    statusInput = String.valueOf(data.get(3 + q)).toUpperCase();
                                                    comment = String.valueOf(aR.getComment());
                                                }
                                                oldComment = aR.getComment();
                                                if (Objects.equals(oldComment, null)) {
                                                    oldComment = "";
                                                } else {
                                                    oldComment = aR.getComment();
                                                }
                                                if (comment.equals("null")) {
                                                    comment = "";
                                                }

                                                aR.setComment(comment);
                                                String eventName = String.valueOf(eventNameList.get(q));
                                                String eventDate = String.valueOf(eventDateList.get(q));
                                                Status holder = aR.getStatus();
                                                if (statusInput.equals("P") || (statusInput.equals("PRESENT"))) {
                                                    aR.setStatus(Status.PRESENT);
                                                } else if (statusInput.equals("A") || (statusInput.equals("UNEXCUSED_ABSENCE")) || (statusInput.equals("ABSENT")) || (statusInput.equals("UNEXCUSED ABSENCE")) || (statusInput.equals("UNEXCUSED"))) {
                                                    aR.setStatus(Status.UNEXCUSED_ABSENCE);
                                                } else if (statusInput.equals("E") || (statusInput.equals("EXCUSED_ABSENCE")) || (statusInput.equals("EXCUSED ABSENCE")) || (statusInput.equals("EXCUSED"))) {
                                                    aR.setStatus(Status.EXCUSED_ABSENCE);
                                                } else if (statusInput.equals("L") || (statusInput.equals("LATE"))) {
                                                    aR.setStatus(Status.LATE);
                                                } else if (statusInput.equals("LE") || (statusInput.equals("LEFT_EARLY")) || (statusInput.equals("LEFT EARLY"))) {
                                                    aR.setStatus(Status.LEFT_EARLY);
                                                } else {
                                                    aR.setStatus(Status.UNKNOWN);
                                                }
                                                if (aR.getStatus().equals(holder) && (oldComment.equals(aR.getComment()))) {
                                                } else {
                                                    ICL = new ImportConfirmList();
                                                    ICL.setAttendanceEvent(attendanceLogic.getAttendanceEvent(idTracker.get(q)));
                                                    ICL.setAttendanceRecord(aR);
                                                    ICL.setAttendanceSite(attendanceSite);
                                                    ICL.setComment(comment);
                                                    ICL.setId(aR.getId());
                                                    ICL.setUserID(aR.getUserID());
                                                    ICL.setOldComment(oldComment);
                                                    ICL.setStatus(aR.getStatus());
                                                    ICL.setOldStatus(holder);
                                                    ICL.setEventName(eventName);
                                                    ICL.setEventDate(eventDate);
                                                    ICList.add(a, ICL);
                                                    a++;
                                                    changes = true;
                                                }
                                                if (oldComment.equals(aR.getComment())) {
                                                } else {
                                                    commentsChanged = true;
                                                }
                                            } else {
                                                badHeader = true;
                                            }
                                        }
                                    } else {
                                        hasCells = true;
                                    }
                                }
                                rowCounter++;
                            }
                        }
                    }
                    fis.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                log.debug("file upload success");
                if(!(hasRows)){
                    getSession().error(getString("attendance.export.import.error.empty_file"));
                    setResponsePage(new ExportPage());
                } else if(changes){
                    if(badHeader){
                        getSession().error(getString("attendance.export.import.error.badHeaderError.submit"));
                    }
                    setResponsePage(new ImportConfirmation(ICList ,commentsChanged));
                } else if (unmodified) {

                    if(badHeader){
                        getSession().error(getString("attendance.export.import.error.badHeaderError.nochange"));
                    } else {
                        getSession().error(getString("attendance.export.import.save.noChange"));
                    }
                    setResponsePage(new ExportPage());
                } else {
                    getSession().error(getString("attendance.export.import.save.fileError"));
                    setResponsePage(new ExportPage());
                }
            } else{
                getSession().error("Unknown error");
                setResponsePage(new Overview());
            }

        }
    }
}

