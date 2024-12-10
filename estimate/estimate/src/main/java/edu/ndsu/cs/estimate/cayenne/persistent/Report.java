package edu.ndsu.cs.estimate.cayenne.persistent;

import edu.ndsu.cs.estimate.cayenne.persistent.auto._Report;

public class Report extends _Report {

    private static final long serialVersionUID = 1L;

    public Integer getPK() {
        if (getObjectId() != null && !getObjectId().isTemporary()) {
            return (Integer) getObjectId().getIdSnapshot().get(REPORT_PK_PK_COLUMN);
        }
        return null;
    }

    @Override
    public String toString() {
        return getCategory(); // Assuming category is a field in the Report class
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Report) {
            Report other = (Report) o;
            if (this.getPK() == null || other.getPK() == null) {
                return false;
            } else {
                return this.getPK().equals(other.getPK());
            }
        }
        return false;
    }
}
