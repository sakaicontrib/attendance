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

        //@Override
        public void onSubmit2() {
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
                    //put Excel sheet's data in a temp file and prepare to iterate over the whole thing
                    File temp = upload.writeToTempFile();
                    FileInputStream fis = new FileInputStream(temp);
                    HSSFWorkbook workbook = new HSSFWorkbook(fis);
                    HSSFSheet sheet = workbook.getSheetAt(0);
                    Iterator rows = sheet.rowIterator();
                    if(!(rows.hasNext())) { //when the file is empty
                        hasRows = false;
                    }
                    if(hasRows){
                        rowCounter = 0;
                        //loop through all present rows; essentially iterating by student
                        for(int r =0; r <= studentCount; r++) {
                            if (rows.hasNext()) {
                                sheetLengthcounter = 0;
                                HSSFRow row = (HSSFRow) rows.next();
                                Iterator cells = row.cellIterator();    //iterate over the cells in the current row
                                List data = new ArrayList();    // container for the current row's data
                                List missingNames = new ArrayList();
                                while (cells.hasNext()) {   // create the arrayList of current row's data
                                    HSSFCell cell = (HSSFCell) cells.next();
                                    data.add(cell);
                                    hasCells = true;
                                    sheetLengthcounter++;
                                }
                                // check to see what kind of data is in the current row....
                                if (rowCounter == 0) {
                                    if ((data.get(0).toString().equals("StudentID")) && (data.get(1).toString().equals("Student Name")) && (data.get(2).toString().equals("Section"))) {
                                        unmodified = true;  // unmodified = true will skip the row. Do we really need to check three cells?
                                    }
                                    hasComments = (data.get(4).toString().contains("]Comments("));
                                } else {    // go here when we AREN'T in the header row            we're going to try and manage without HasCells and its operations at all. we're replacing it with iterator.hasNext.
                                    if (data.size() > 0) {  // see if current row has data
                                        if (data.get(0).toString().equals("")) {    //check to see if current non-header row has any data in its first cell
                                            hasCells = false;
                                        } else {
                                            hasCells = true;
                                        }
                                    } else {
                                        hasCells = false;
                                    }   //why did we need two checks to arrive here? why do we check the whole row AND its first cell? One of these should be enough.
                                }

                                if (hasComments) {
                                    eventCounter = ((sheetLengthcounter - 3) / 2);  //this is slightly misleading...it should really be rowLengthCounter because it tracks how many cells are in the row. this line removes comment cells from the count.
                                } else {
                                    eventCounter = (sheetLengthcounter - 3);    //not sure why we're subtracting 3 so much when we could just make eventCounter 0 to begin with
                                }
                                if (unmodified) {   //unmodified was initialized as False, and would be True by now only if this is the header row.    we'll have to stick this Header stuff in the method later
                                    if (rowCounter == 0) {  //...so why are we still singling out the header row at this point?
                                        hasCells = false;
                                        for (int q = 0; q < eventCounter; q++) {    //build the list/row of events. different if it has comments.
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
                                    if ((rowCounter > 0) && (hasCells)) {   //i don't know how this code ever gets hit because Unmodified is true ONLY for the header row
                                        hasCells = false;   //hasCells would be True until now unless it's the header row.
                                        String userName = String.valueOf(data.get(1));
                                        String userEID = String.valueOf(data.get(0));
                                        ////List<AttendanceRecord> attendanceRecordlist = attendanceLogic.getAttendanceRecordsForUser(userStatsList.get(rowCounter - 1).getUserID().toString());
                                        List<AttendanceEvent> siteEventList = new ArrayList<AttendanceEvent>(attendanceLogic.getAttendanceEventsForCurrentSite());
                                        User userGetter;

                                        for (int q = 0; q < eventCounter; q++) {    //building each student's row of data
                                            recordExists = false;
                                            eventExists = false;
                                            for (int i = 0; i < siteEventList.size(); i++) {    //verify the event against what's present in Attendance
                                                if (siteEventList.get(i).getId().equals(idTracker.get(q))) {
                                                    eventExists = true;
                                                }
                                            }

                                            if (eventExists) {  // do these things if event has been verified
                                                List<AttendanceRecord> records = new ArrayList<AttendanceRecord>((attendanceLogic.getAttendanceEvent(idTracker.get(q))).getRecords());
                                                for (int s = 0; s < records.size(); s++) {  //I believe this is making sure that there is a spot in Attendance for the data we're trying to give it.
                                                    userGetter = sakaiProxy.getUser(records.get(s).getUserID());
                                                    if (userEID.equals(userGetter.getEid())) {  //if current row's user id matches one in Attendance, the Record Exists and we can use the new data.
                                                        indexCounter = s; //set this so we know exactly where in Attendance the matching record is.
                                                        recordExists = true;
                                                    }
                                                }
                                                AttendanceRecord aR;
                                                if (recordExists) { //if the record exists, grab it from the exisitng Attendance data.
                                                    aR = attendanceLogic.getAttendanceRecord(records.get(indexCounter).getId());
                                                } else {    //if it doesn't exist, put this User ID in the list of missing ones.
                                                    missingNames.add(userEID);
                                                    userGetter = sakaiProxy.getUserByEID(userEID);
                                                    aR = new AttendanceRecord((attendanceLogic.getAttendanceEvent(idTracker.get(q))), userGetter.getId(), Status.UNKNOWN);  //it looks like we're creating a dummy entry for nonexistent records.
                                                    missingNames.clear();   //why is this happening? we haven't done a thing with the data from this array.
                                                }
                                                if (hasComments) {  //if there are comments in the Excel data, get them after doing math to account for the actual student-presence data
                                                    statusInput = String.valueOf(data.get(3 + (2 * q))).toUpperCase();
                                                    comment = String.valueOf(data.get(4 + (2 * q)));
                                                } else {    //if there are not comments in Excel, leave ones in Attendance that are already there. no Comment-math needed to grab the student-presence data.
                                                    statusInput = String.valueOf(data.get(3 + q)).toUpperCase();
                                                    comment = String.valueOf(aR.getComment());
                                                }
                                                oldComment = aR.getComment(); //gather the original comment from Attendance
                                                if (Objects.equals(oldComment, null)) { //set oldComment to blank if it is null, I guess?
                                                    oldComment = "";
                                                } else {    //this straight-up does not need to be here. we literally just set it two lines above, and change it only under certain conditions.
                                                    oldComment = aR.getComment();
                                                }
                                                if (comment.equals("null")) {   //if the comment literally says "null", we clear it.
                                                    comment = "";
                                                }

                                                aR.setComment(comment); // whatever the comment is after all that, put it in the Attendance record.
                                                String eventName = String.valueOf(eventNameList.get(q));
                                                String eventDate = String.valueOf(eventDateList.get(q));
                                                Status holder = aR.getStatus(); //the student's current data from Attendance for the given event
                                                if (statusInput.equals("P") || (statusInput.equals("PRESENT"))) {   //take data from Excel and put it in the current Attendance record. should this be a Switch statement instead?
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
                                                if (aR.getStatus().equals(holder) && (oldComment.equals(aR.getComment()))) {    //this appears to prevent the Else actions from happening to a row without any changes relative to Attendance. but haven't we already filtered for that? and why not just negate the If to avoid using an Else?
                                                } else {    //give all the new and old data to the importing thing
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
                                                if (oldComment.equals(aR.getComment())) {   //this is ridiculous. BOOLEAN CONDITIONS CAN BE NEGATED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                                } else {    //...but it looks like comment changes need to be flagged separately. I feel like this could have been done elsewhere.
                                                    commentsChanged = true;
                                                }
                                            } else {    //if the event didn't exist, it's a bad header.
                                                badHeader = true;
                                            }
                                        }
                                    } else {    // if !(rowCounter > 0) && !(hasCells), we skip everything and set HasCells to true. why?
                                        hasCells = true;
                                    }
                                }   // this ends If Unmodified.
                                rowCounter++;
                            }   //  this ends the part for rows.hasNext .
                        }   //ends the For loop based on number of students.
                    }   //ends the hasRows If.
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

        @Override
        public void onSubmit(){
            // Local variables for processing
            final List<AttendanceEvent> siteEventList = new ArrayList<AttendanceEvent>(attendanceLogic.getAttendanceEventsForCurrentSite());
            final List<Long> usableIDs = getActualEventIds(siteEventList);
            List<ImportConfirmList> ICList = new ArrayList<ImportConfirmList>();
            boolean unmodified = false;
            boolean checkHeader = true;
            boolean changes = true;
            boolean commentsChanged = false;
            // Start processing
            HSSFSheet sheet = getIterableExcelSheet();
            if(sheet != null){    //if we weren't able to get a usable sheet, it would be Null and we shouldn't process it.
                Iterator rows = sheet.rowIterator();
                if(rows.hasNext()){ //don't do anything with the Iterator unless it has something.
                    final List<Long> idTracker = processHeaderRow(rows);
                    List<Long> badIds = checkHeader(siteEventList, idTracker);
                    checkHeader = badIds.size()<1;  //if there are bad IDs, the header is bad [checkHeader = false]
                    while(rows.hasNext()){
                        HSSFRow currentRow = (HSSFRow) rows.next();
                        commentsChanged = processOneDataRow(currentRow, usableIDs, idTracker, ICList, commentsChanged);
                    }
                }else{
                    getSession().error(getString("attendance.export.import.error.empty_file"));
                    setResponsePage(new ExportPage());
                }
            }
            if(changes){
                if(!checkHeader){
                    getSession().error(getString("attendance.export.import.error.badHeaderError.submit"));
                }
                setResponsePage(new ImportConfirmation(ICList ,commentsChanged));
            } else if (unmodified) {
                if(!checkHeader){
                    getSession().error(getString("attendance.export.import.error.badHeaderError.nochange"));
                } else {
                    getSession().error(getString("attendance.export.import.save.noChange"));
                }
                setResponsePage(new ExportPage());
            } else {
                getSession().error(getString("attendance.export.import.save.fileError"));
                setResponsePage(new ExportPage());
            }

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

        private List<Long> processHeaderRow(Iterator rows){
            HSSFRow row = (HSSFRow) rows.next();
            Iterator cells = row.cellIterator();    //iterate over the cells in the current row
            List data = new ArrayList();    // container for the current row's data
            List<Long> idTracker = new ArrayList<Long>();
            String eventHeaderHolder;
            while (cells.hasNext()) {   // create the arrayList of current row's data
                HSSFCell cell = (HSSFCell) cells.next();
                if (cell.getStringCellValue().length() != 0){   //leaving blank cells out of the array.
                    data.add(cell);
                }
            }
            boolean hasComments = (data.get(4).toString().contains("]Comments("));
            for (int q = 0; (3+(2*q)) < data.size() && hasComments; q++){   //build the list/row of events with comments
                eventHeaderHolder = String.valueOf(data.get(3 + (2 * q)));
                int headerIndexStart = eventHeaderHolder.lastIndexOf("(");
                int headerIndexEnd = eventHeaderHolder.lastIndexOf(")");
                String idHolder = eventHeaderHolder.substring(headerIndexStart + 1, headerIndexEnd);
                idTracker.add(Long.parseLong(idHolder));
            }
            for (int q = 0; (3+q) < data.size() && !hasComments; q++) {    //build the list/row of events without comments
                eventHeaderHolder = String.valueOf(data.get(3 + q));
                int headerIndexStart = eventHeaderHolder.lastIndexOf("(");
                int headerIndexEnd = eventHeaderHolder.lastIndexOf(")");
                String idHolder = eventHeaderHolder.substring(headerIndexStart + 1, headerIndexEnd);
                idTracker.add(Long.parseLong(idHolder));
            }
            return idTracker;
        }

        private List<Long> checkHeader(List<AttendanceEvent> siteEventList, List<Long> idTracker){
            List<Long> siteEventListIds = new ArrayList<Long>();
            for(int i = 0; i<siteEventList.size(); i++){    //put the AttendanceEvent IDs in their own list
                siteEventListIds.add(siteEventList.get(i).getId());
            }
            List<Long> badIds = new ArrayList<Long>();
            for(int count = 0; count < idTracker.size(); count++){  //see if every ID in idTracker is in Attendance already
                if(!siteEventListIds.contains(idTracker.get(count))){   //if not, this header had bad IDs (events that Attendance doesn't have)
                    badIds.add(idTracker.get(count));   //add the bad ID into the array.
                    getSession().error("The spreadsheet's event with ID " + idTracker.get(count) + " is not an event listed for this class in Attendance.");
                }
            }
            return badIds;
        }

        private List<Long> getActualEventIds (List<AttendanceEvent> siteEventList){
            List<Long> usableIds = new ArrayList<Long>();
            for (int count = 0; count<siteEventList.size(); count++){
                usableIds.add(siteEventList.get(count).getId());
            }
            return usableIds;
        }

        private boolean processOneDataRow(HSSFRow row, List<Long> usableIds, List<Long> idTracker, List<ImportConfirmList> ICList, boolean commentsChanged){
            Iterator cells = row.cellIterator();    //iterate over the cells in the current Excel row
            AttendanceSite attendanceSite = attendanceLogic.getAttendanceSite(sakaiProxy.getCurrentSiteId());
            List data = new ArrayList();    // container for the current row's data
            while (cells.hasNext()) {   // create the arrayList of the current Excel row's data
                HSSFCell cell = (HSSFCell) cells.next();
                data.add(cell.toString());
            }
            String userEID = String.valueOf(data.get(0));
            User currentUser = sakaiProxy.getUserByEID(userEID);
            if(currentUser != null){ //if it passes this Boolean condition, that should mean that Attendance has a slot for this user.
                List<AttendanceRecord> oldUserData = attendanceLogic.getAttendanceRecordsForUser(currentUser.getId());   //the student's row of data from Attendance
                boolean hasComments = idTracker.size() < data.size()-3;  //we must have comments if there are more Excel columns than IDs.
                int idTrackerCount = 0; //separate counter for idTracker, which should not advance by 2 for comments like the Excel data might have to.
                for(int count = 0; idTrackerCount<idTracker.size(); count++){    //iterate over every cell in the Excel data
                    if(usableIds.contains(idTracker.get(idTrackerCount))){   //if the current column/cell's event ID is Usable [present already in Attendance], process the cell.
                        ImportConfirmList ICL = new ImportConfirmList();
                        if (data.get(count+3).equals("P") || (data.get(count+3).equals("PRESENT"))) {   //take data from Excel and put it in the current Attendance record. should this be a Switch statement instead?
                            ICL.setStatus(Status.PRESENT);
                        } else if (data.get(count+3).equals("A") || (data.get(count+3).equals("UNEXCUSED_ABSENCE")) || (data.get(count+3).equals("ABSENT")) || (data.get(count+3).equals("UNEXCUSED ABSENCE")) || (data.get(count+3).equals("UNEXCUSED"))) {
                            ICL.setStatus(Status.UNEXCUSED_ABSENCE);
                        } else if (data.get(count+3).equals("E") || (data.get(count+3).equals("EXCUSED_ABSENCE")) || (data.get(count+3).equals("EXCUSED ABSENCE")) || (data.get(count+3).equals("EXCUSED"))) {
                            ICL.setStatus(Status.EXCUSED_ABSENCE);
                        } else if (data.get(count+3).equals("L") || (data.get(count+3).equals("LATE"))) {
                            ICL.setStatus(Status.LATE);
                        } else if (data.get(count+3).equals("LE") || (data.get(count+3).equals("LEFT_EARLY")) || (data.get(count+3).equals("LEFT EARLY"))) {
                            ICL.setStatus(Status.LEFT_EARLY);
                        } else {
                            ICL.setStatus(Status.UNKNOWN);
                        }
                        Iterator<AttendanceRecord> traverseOldData = oldUserData.iterator();
                        AttendanceRecord checker;
                        while(traverseOldData.hasNext()){   //get event/record from old data for the eventID we're currently working with
                            checker = traverseOldData.next();
                            if(checker.getAttendanceEvent().getId().equals(idTracker.get(idTrackerCount))){
                                ICL.setAttendanceEvent(checker.getAttendanceEvent());
                                ICL.setAttendanceRecord(checker);
                                ICL.setEventName(checker.getAttendanceEvent().getName() + " ");
                                ICL.setOldComment(checker.getComment());
                                ICL.setOldStatus(checker.getStatus());
                                ICL.setAttendanceSite(attendanceSite);
                                if (hasComments){
                                    ICL.setComment(data.get(count+4).toString());   //having used Count+3 as the index for Status, we'd use Count+4 for Comments.
                                    if (ICL.getComment().length() < 1){
                                        ICL.setComment(null);   //we null the blank so that it will match old data without a comment
                                    }else{
                                        commentsChanged = true; //we won't consider the comment Changed unless it's not blank.
                                    }
                                    count++;    //If there are comments, we need to advance Count by one more to move ahead to the next Status data on the next round of the For loop.
                                }
                                ICL.setId(idTracker.get(idTrackerCount));    //Attendace event's ID
                                ICL.setUserID(currentUser.getId()); //we can't use the EID for this...it has to be the longer, hashy one
                                ICL.setEventDate(checker.getAttendanceEvent().getStartDateTime().toString());
                                if(!ICL.getStatus().equals(checker.getStatus()) || (hasComments && ICL.getComment()!=(checker.getComment()))){ //make sure the new cell doesn't just have the same status/comment as the old one before processing.
                                    ICList.add(ICL);
                                }
                                break;  //stop iterating over oldUserData once we've found the matching event ID and processed its data
                            }
                        }
                    }
                    idTrackerCount++;   //advance the idTracker counter, since it's not part of the For declaration.
                }
            }else{
                getSession().error("The student " + data.get(1) + " is not on this class's roster.");   //when there's a fake student in Excel that isn't in Attendance
            }
            return commentsChanged;
        }
    }
}

