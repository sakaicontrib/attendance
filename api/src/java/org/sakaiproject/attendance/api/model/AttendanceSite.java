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

package org.sakaiproject.attendance.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.sakaiproject.attendance.api.util.AttendanceConstants;
import org.sakaiproject.springframework.data.PersistableEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * An AttendanceSite represents all the Attendance related data for a specific Sakai Site.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "ATTENDANCE_SITE_T")
@Data
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSite implements Serializable, PersistableEntity<Long> {
    private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "A_SITE_ID", length = 19)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "attendance_site_id_sequence")
	@SequenceGenerator(name = "attendance_site_id_sequence", sequenceName = "ATTENDANCE_SITE_S")
    private Long id;

	@Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteID;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "DEFAULT_STATUS", nullable = false, length = 20)
    private Status defaultStatus = Status.UNKNOWN;

	@Column(name = "MAXIMUM_GRADE")
    private Double maximumGrade;

	@Column(name = "IS_GRADE_SHOWN")
    private Boolean isGradeShown = Boolean.FALSE;

	@Column(name = "SEND_TO_GRADEBOOK")
    private Boolean sendToGradebook = Boolean.FALSE;

	@Column(name = "AUTO_GRADING")
    private Boolean useAutoGrading = Boolean.FALSE;

	@Column(name = "GRADE_BY_SUBTRACTION")
    private Boolean autoGradeBySubtraction = Boolean.TRUE;

	@Column(name = "GRADEBOOK_ITEM_NAME")
    private String gradebookItemName;

	@Column(name = "SHOW_COMMENTS")
	private Boolean showCommentsToStudents = Boolean.FALSE;

	@OneToMany(mappedBy = "attendanceSite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AttendanceStatus> attendanceStatuses = new HashSet<>(0);

    public AttendanceSite(String siteID) {
        this.siteID = siteID;
        this.gradebookItemName = AttendanceConstants.GRADEBOOK_ITEM_NAME;
    }
}
