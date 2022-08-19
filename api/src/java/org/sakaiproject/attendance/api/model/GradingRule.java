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
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "ATTENDANCE_RULE_T")
@Getter
@Setter
@NoArgsConstructor
public class GradingRule implements Serializable, PersistableEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GRADING_RULE_ID", length = 19)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "grading_rule_id_sequence")
    @SequenceGenerator(name = "grading_rule_id_sequence", sequenceName = "ATTENDANCE_GRADING_RULE_S")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "A_SITE_ID")
    private AttendanceSite attendanceSite;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private Status status;

    @Column(name = "START_RANGE", nullable = false)
    private Integer startRange = 0;

    @Column(name = "END_RANGE")
    private Integer endRange;

    @Column(name = "POINTS", nullable = false)
    private Double points = 0D;

    public GradingRule(AttendanceSite attendanceSite) {
        this.attendanceSite = attendanceSite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GradingRule that = (GradingRule) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
