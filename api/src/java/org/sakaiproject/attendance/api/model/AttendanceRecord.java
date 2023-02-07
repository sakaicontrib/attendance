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
import org.sakaiproject.springframework.data.PersistableEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * An AttendanceRecord for a specific user for a specific AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "ATTENDANCE_RECORD_T",
        indexes = {
                @Index(name = "ATTEN_EVENT_STATUS_I", columnList = "A_EVENT_ID, STATUS"),
                @Index(name = "ATTEN_EVENT_USER_STATUS_I", columnList = "A_EVENT_ID, USER_ID, STATUS")
        })
@Data
@ToString(exclude = {"attendanceEvent"})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord implements Serializable, PersistableEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "A_RECORD_ID", length = 19)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "attendance_record_id_sequence")
    @SequenceGenerator(name = "attendance_record_id_sequence", sequenceName = "ATTENDANCE_RECORD_S")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "A_EVENT_ID")
    private AttendanceEvent attendanceEvent;

    @Column(name = "USER_ID", length = 99)
    private String userID;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private Status status = Status.UNKNOWN;

    @Lob
    @Column(name = "RECORD_COMMENT", length = 4000)
    private String comment;

    public AttendanceRecord(AttendanceEvent e, String uId, Status s) {
        this.attendanceEvent = e;
        this.userID = uId;
        this.status = s;
    }
}
