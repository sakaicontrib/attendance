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

package org.sakaiproject.attendance.util;

/**
 * Class to hold constants (defaults, etc) for Attendance.
 *
 * Modeled after {@link org.sakaiproject.profile2.util.ProfileConstants}
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceConstants {
    /*
     * General
     */
    public static final String TOOL_NAME            = "Attendance";
    public static final String SAKAI_TOOL_NAME      = "sakai.attendance";
    public static final String GRADEBOOK_ITEM_NAME  = "Attendance";

    public static final int GRADING_METHOD_NONE = 0;
    public static final int GRADING_METHOD_SUBTRACT = 1;
    public static final int GRADING_METHOD_ADD = 2;
    public static final int GRADING_METHOD_MULTIPLY = 3;
}
