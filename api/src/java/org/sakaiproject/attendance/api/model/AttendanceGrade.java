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
 * The AttendanceGrade earned for the all AttendanceItems
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]au)
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "ATTENDANCE_GRADE_T")
@Data
@ToString(exclude = {"attendanceSite"})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceGrade implements Serializable, PersistableEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "A_GRADE_ID", length = 19)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "attendance_grade_id_sequence")
    @SequenceGenerator(name = "attendance_grade_id_sequence", sequenceName = "ATTENDANCE_GRADE_S")
    private Long id;

    @Column(name = "GRADE")
    private Double grade;

    @Column(name = "USER_ID", length = 99)
    private String userID;

    @Column(name = "OVERRIDE")
    private Boolean override;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "A_SITE_ID")
    private AttendanceSite attendanceSite;

    public AttendanceGrade(AttendanceSite aS, String userId) {
        this.attendanceSite = aS;
        this.userID = userId;
    }
}
