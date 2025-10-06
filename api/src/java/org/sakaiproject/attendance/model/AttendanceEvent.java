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

package org.sakaiproject.attendance.model;

import lombok.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Represents an AttendanceEvent, such as a class meeting or seminar
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu])
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude={"records","stats"})
@Entity(name = "AttendanceEvent")
@Table(name = "ATTENDANCE_EVENT_T")
public class AttendanceEvent implements Serializable {
	private static final 	long 		serialVersionUID = 1L;

    @Id
    @GenericGenerator(name = "ATTENDANCE_EVENT_GEN", strategy = "native",
            parameters = @Parameter(name = "sequence", value = "ATTENDANCE_EVENT_S"))
    @GeneratedValue(generator = "ATTENDANCE_EVENT_GEN")
    @Column(name = "A_EVENT_ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_DATE_TIME")
    private Date startDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_DATE_TIME")
    private Date endDateTime;

    @Column(name = "IS_REOCCURRING")
    private Boolean isReoccurring;

    @Column(name = "REOCCURRING_ID")
    private Long reoccurringID;

    @Column(name = "IS_REQUIRED")
    private Boolean isRequired;

    @Column(name = "RELEASED_TO")
    private String releasedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "A_SITE_ID", nullable = false)
    private AttendanceSite attendanceSite;

    @Column(name = "LOCATION")
    private String location;

    @OneToMany(mappedBy = "attendanceEvent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AttendanceRecord> records = new HashSet<AttendanceRecord>(0);

    @OneToOne(mappedBy = "attendanceEvent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AttendanceItemStats stats;

    @Column(name = "LAST_MODIFIED_BY", nullable = false, length = 99)
    private String lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED_DATE", nullable = false)
    private Date lastModifiedDate;

	// Copy constructor
	public AttendanceEvent(AttendanceEvent attendanceEvent){
		this.name 			= attendanceEvent.name;
		this.startDateTime 	= attendanceEvent.startDateTime;
		this.endDateTime 	= attendanceEvent.endDateTime;
		this.isReoccurring 	= attendanceEvent.isReoccurring;
		this.reoccurringID 	= attendanceEvent.reoccurringID;
		this.isRequired 	= attendanceEvent.isRequired;
		this.releasedTo 	= attendanceEvent.releasedTo;
		this.attendanceSite = attendanceEvent.attendanceSite;
		this.location 		= attendanceEvent.location;
	}

}
