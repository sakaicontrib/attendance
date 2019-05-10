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

import org.apache.commons.codec.binary.StringUtils;
import org.apache.wicket.model.StringResourceModel;
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
        public void onSubmit(){
            // Local variables for processing
            final List<AttendanceEvent> siteEventList = new ArrayList<AttendanceEvent>(attendanceLogic.getAttendanceEventsForCurrentSite());
            final List<Long> usableIDs = getActualEventIds(siteEventList);
            List<ImportConfirmList> ICList = new ArrayList<ImportConfirmList>();
            boolean checkHeader = true;
            boolean commentsChanged = false;
            List<String> errors = new ArrayList<String>();
            // Start processing
            HSSFSheet sheet = getIterableExcelSheet();
            if(sheet != null){    //if we weren't able to get a usable sheet, it would be Null and we shouldn't process it.
                Iterator rows = sheet.rowIterator();
                if(rows.hasNext()){ //don't do anything with the Iterator unless it has something.
                    final List<String> headerRow = processHeaderRow(rows);
                    List<Long> badIds = checkHeader(siteEventList, headerRow, errors);
                    checkHeader = badIds.size()<1;  //if there are bad IDs, the header is bad [checkHeader = false]
                    while(rows.hasNext()){
                        HSSFRow currentRow = (HSSFRow) rows.next();
                        commentsChanged = processOneDataRow(currentRow, usableIDs, headerRow, ICList, commentsChanged, errors);
                    }
                }else{
                    getSession().error(getString("attendance.export.import.error.empty_file"));
                    setResponsePage(new ExportPage());
                }
            }
            if(!checkHeader){
                getSession().error(getString("attendance.export.import.error.badHeaderError.submit"));
            }
            addErrorsToSession(errors);
            setResponsePage(new ImportConfirmation(ICList ,commentsChanged));
        }

        private HSSFSheet getIterableExcelSheet(){
            final FileUpload upload = this.fileUploadField.getFileUpload();
            String extension = upload.getClientFileName().substring(upload.getClientFileName().lastIndexOf(".")+1);
            if (upload == null) {
                getSession().error(getString("attendance.export.import.error.null_file"));
                setResponsePage(new Overview());
            } else if (upload.getSize() == 0) {
                getSession().error(getString("attendance.export.import.error.empty_file"));
                setResponsePage(new ExportPage());
            }else if (!(ExportPage.ExportFormat.XLS.toString().toLowerCase()).equals(extension)) {
                getSession().error(getString("attendance.export.import.error.bad_file_format"));
                setResponsePage(new ExportPage());
            }
            HSSFSheet sheet = null;
            try{
                File temp = upload.writeToTempFile();
                FileInputStream fis = new FileInputStream(temp);
                HSSFWorkbook workbook = new HSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
            }catch (final IOException e){
                getSession().error(getString("attendance.export.import.save.fileError"));
                setResponsePage(new ExportPage());
            }
            return sheet;
        }

        private List<String> processHeaderRow(Iterator rows){
            HSSFRow row = (HSSFRow) rows.next();
            Iterator cells = row.cellIterator();    //iterate over the cells in the current row
            List data = new ArrayList();    // container for the current row's data
            while (cells.hasNext()) {   // create the arrayList of current row's data
                HSSFCell cell = (HSSFCell) cells.next();
                data.add(cell.toString());
            }
            return data;
        }

        private List<Long> checkHeader(List<AttendanceEvent> siteEventList, List<String> headerRow, List<String> errors){
            List<Long> siteEventListIds = getActualEventIds(siteEventList);
            List<Long> badIds = new ArrayList<Long>();
            for(int count = 3; count < headerRow.size() && headerRow.get(count).length()>0; count++){  //see if every ID in idTracker is in Attendance already
                Long getIdResult = getIdFromString(headerRow.get(count), errors);
                if(!siteEventListIds.contains(getIdResult)){   //if not, this header had bad IDs (events that Attendance doesn't have)
                    badIds.add(getIdResult);   //add the bad ID into the array.
                    errors.add(getString("attendance.import.bad.event"));
                }
                if(headerRow.get(count).contains("]Comments(") && headerRow.size()%2<1){    //if there are comments in the file, there must be an odd number of columns.
                    errors.add(getString("attendance.import.missing.column"));
                }
            }
            return badIds;
        }

        private Long getIdFromString(String cellData, List<String> errors){
            int headerIndexStart = cellData.lastIndexOf("(");
            int headerIndexEnd = cellData.lastIndexOf(")");
            String idHolder = "0";
            try{
                idHolder = cellData.substring(headerIndexStart + 1, headerIndexEnd);
            }catch(StringIndexOutOfBoundsException e){
                errors.add(getString("attendance.import.bad.column"));
            }
            return Long.parseLong(idHolder);
        }

        private List<Long> getActualEventIds (List<AttendanceEvent> siteEventList){
            List<Long> usableIds = new ArrayList<Long>();
            for (int count = 0; count<siteEventList.size(); count++){
                usableIds.add(siteEventList.get(count).getId());
            }
            return usableIds;
        }

        private void addErrorsToSession(List<String> errors){
            for(int count = 0; count<4 && count<errors.size() && errors.size()>0; count++){
                getSession().error(errors.get(count));
            }
            if(errors.size()>4){
                String howManyErrors = "" + (errors.size()-4);
                getSession().error(new StringResourceModel("attendance.import.more.errors", null, new String[]{howManyErrors}).getString());
            }
        }

        private boolean processOneDataRow(HSSFRow row, List<Long> usableIds, List<String> headerRow, List<ImportConfirmList> ICList, boolean commentsChanged, List<String> errors) {
            Iterator cells = row.cellIterator();    //iterate over the cells in the current Excel row
            AttendanceSite attendanceSite = attendanceLogic.getAttendanceSite(sakaiProxy.getCurrentSiteId());
            List data = new ArrayList();    // container for the current row's data
            while (cells.hasNext()) {   // create the arrayList of the current Excel row's data
                HSSFCell cell = (HSSFCell) cells.next();
                data.add(cell.toString());
            }
            if(data.size() != headerRow.size()){
                errors.add(getString("attendance.import.header.length"));
            }
            String userEID = "";
            try{
                userEID = String.valueOf(data.get(0));
            }catch(IndexOutOfBoundsException e){
                userEID = "0";
            }
            User currentUser = sakaiProxy.getUserByEID(userEID);
            if (currentUser != null) { //if it passes this Boolean condition, that should mean that Attendance has a slot for this user.
                List<AttendanceRecord> oldUserData = attendanceLogic.getAttendanceRecordsForUser(currentUser.getId());   //the student's row of data from Attendance
                for (int count = 3; count<data.size() && count<headerRow.size(); count++) {    //iterate over current student row, starting at 3 to account for id/name/section
                    Long currentID = getIdFromString(headerRow.get(count), errors); //get the eventID of the current cell
                    if (usableIds.contains(currentID)) { //if it's one of the real event IDs, and also not a comment column...
                        AttendanceRecord newData = attendanceLogic.getAttendanceRecord(currentID);
                        ImportConfirmList ICL = new ImportConfirmList();
                        if (data.get(count).equals("P") || (data.get(count).equals("PRESENT"))) {   //take data from Excel and put it in the current Attendance record. should this be a Switch statement instead?
                            newData.setStatus(Status.PRESENT);
                        } else if (data.get(count).equals("A") || (data.get(count).equals("UNEXCUSED_ABSENCE")) || (data.get(count).equals("ABSENT")) || (data.get(count).equals("UNEXCUSED ABSENCE")) || (data.get(count).equals("UNEXCUSED"))) {
                            newData.setStatus(Status.UNEXCUSED_ABSENCE);
                        } else if (data.get(count).equals("E") || (data.get(count).equals("EXCUSED_ABSENCE")) || (data.get(count).equals("EXCUSED ABSENCE")) || (data.get(count).equals("EXCUSED"))) {
                            newData.setStatus(Status.EXCUSED_ABSENCE);
                        } else if (data.get(count).equals("L") || (data.get(count).equals("LATE"))) {
                            newData.setStatus(Status.LATE);
                        } else if (data.get(count).equals("LE") || (data.get(count).equals("LEFT_EARLY")) || (data.get(count).equals("LEFT EARLY"))) {
                            newData.setStatus(Status.LEFT_EARLY);
                        } else {
                            newData.setStatus(Status.UNKNOWN);
                        }
                        Iterator<AttendanceRecord> traverseOldData = oldUserData.iterator();
                        AttendanceRecord checker;
                        while (traverseOldData.hasNext()) {   //get event/record from old data for the eventID we're currently working with
                            checker = traverseOldData.next();
                            if (checker.getAttendanceEvent().getId().equals(currentID)) {   //once we've found the right event ID, put all the data for the current record into the ICL.
                                newData.setUserID(currentUser.getId());
                                newData.setAttendanceEvent(checker.getAttendanceEvent());
                                newData.setId(checker.getId());
                                newData.setComment(checker.getComment());
                                if(!headerRow.get(count).contains("]Comments(") && count+1<headerRow.size()){
                                    if(headerRow.get(count+1).contains("]Comments(")){
                                        try{
                                            newData.setComment(data.get(count+1).toString());
                                            commentsChanged = true;
                                        }catch(IndexOutOfBoundsException e){
                                            errors.add(new StringResourceModel("attendance.import.column.format", null, new String[]{currentID.toString()}).getString());
                                        }
                                        count++;    //increment Count again to move on to the next ID when grabbing the comment.
                                    }
                                }
                                ICL.setAttendanceEvent(checker.getAttendanceEvent()); //start putting everything in the ICL.
                                ICL.setEventName(checker.getAttendanceEvent().getName() + " ");
                                ICL.setOldComment(checker.getComment());
                                ICL.setOldStatus(checker.getStatus());
                                ICL.setAttendanceSite(attendanceSite);
                                ICL.setId(checker.getId());    //Attendace event's ID
                                ICL.setUserID(currentUser.getId()); //we can't use the EID for this...it has to be the longer, hashy one
                                ICL.setEventDate(checker.getAttendanceEvent().getStartDateTime().toString());   //much of the data, like Event Date, will not change, so it can be grabbed from Checker.
                                if(newData.getComment()!=null && newData.getComment().length() > 0){    //add the newData comment to the ICL if it's not empty.
                                    ICL.setComment(newData.getComment());
                                }else{  //if it IS empty, just throw in the old comment.
                                    ICL.setComment(checker.getComment());
                                }
                                ICL.setAttendanceRecord(newData);   //the other ones that really need to be from the New Data are these last two.
                                ICL.setStatus(newData.getStatus());
                                if (!ICL.getStatus().equals(checker.getStatus()) || !StringUtils.equals(ICL.getComment(), checker.getComment())) { //make sure the new cell doesn't just have the same status/comment as the old one before processing.
                                    ICList.add(ICL);
                                }
                                break;  //stop iterating over oldUserData once we've found the matching event ID and processed its data
                            }
                        }
                    }else{
                        errors.add(new StringResourceModel("attendance.import.bad.event", null, new String[]{currentID.toString()}).getString());
                    }
                }
            }else if (data.size()>2){
                if(data.get(1).toString().length() > 0){
                    errors.add(new StringResourceModel("attendance.import.fake.student", null, new String[]{data.get(1).toString()}).getString());   //when there's a fake student in Excel that isn't in Attendance
                }else{
                    errors.add(getString("attendance.import.blank.row"));   //when there's a blank row in Excel that has no data
                }
            }else{
                errors.add(getString("attendance.import.blank.row"));   //when there's a fake/extra row in Excel that has no data
            }
            return commentsChanged;
        }
    }
}