package org.infoUFPS.Model;

public enum Status {

    UNDER_REVIEW {
        public boolean to(Status nextStatus) {return nextStatus == PUBLISHED || nextStatus == ARCHIVED;}
    },
    PUBLISHED {
        public boolean to(Status nextStatus) {return nextStatus == ARCHIVED;}
    },
    ARCHIVED {
        public boolean to(Status nextStatus) {return false; }
    };

}
