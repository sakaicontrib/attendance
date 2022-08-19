package org.sakaiproject.attendance.api.model.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.attendance.api.model.Status;

@AllArgsConstructor
public class StatusCount {
    @Getter @Setter private Status status;
    @Getter @Setter private Long total;
}
