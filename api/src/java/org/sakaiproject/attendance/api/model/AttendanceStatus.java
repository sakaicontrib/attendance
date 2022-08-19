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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.sakaiproject.springframework.data.PersistableEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * An AttendanceStatus is a wrapper around the Status enum type defining meta information on individual Statuses.
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "ATTENDANCE_STATUS_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatus implements Serializable, PersistableEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "A_STATUS_ID", length = 19)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "attendance_status_id_sequence")
    @SequenceGenerator(name = "attendance_status_id_sequence", sequenceName = "ATTENDANCE_STATUS_S")
    private Long id;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive = Boolean.TRUE;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private Status status;

    @Column(name = "SORT_ORDER")
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "A_SITE_ID")
    private AttendanceSite attendanceSite;

    // Create a copy constructor
    public AttendanceStatus(AttendanceStatus attendanceStatus) {
        this.isActive = attendanceStatus.getIsActive();
        this.status = attendanceStatus.getStatus();
        this.sortOrder = attendanceStatus.getSortOrder();
        this.attendanceSite = attendanceStatus.getAttendanceSite();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttendanceStatus that = (AttendanceStatus) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
