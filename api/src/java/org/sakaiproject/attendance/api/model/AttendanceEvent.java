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
import org.hibernate.annotations.Type;
import org.sakaiproject.springframework.data.PersistableEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an AttendanceEvent, such as a class meeting or seminar
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu])
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "ATTENDANCE_EVENT_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent implements Serializable, PersistableEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "A_EVENT_ID", length = 19)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "attendance_event_id_sequence")
    @SequenceGenerator(name = "attendance_event_id_sequence", sequenceName = "ATTENDANCE_EVENT_S")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "START_DATE_TIME")
    private Instant startDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "A_SITE_ID")
    private AttendanceSite attendanceSite;

    @OneToMany(mappedBy = "attendanceEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AttendanceRecord> records = new HashSet<>(0);

    // Copy constructor
    public AttendanceEvent(AttendanceEvent attendanceEvent) {
        this.name = attendanceEvent.name;
        this.startDateTime = attendanceEvent.startDateTime;
        this.attendanceSite = attendanceEvent.attendanceSite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AttendanceEvent that = (AttendanceEvent) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "startDateTime = " + startDateTime + ")";
    }
}
