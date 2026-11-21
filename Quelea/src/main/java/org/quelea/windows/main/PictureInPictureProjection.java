package org.quelea.windows.main;

import javafx.stage.Stage;

/** Temporarily hides the projection while remembering visibility requests. */
final class PictureInPictureProjection {
    private Stage projection;
    private boolean active;
    private boolean hiding;
    private boolean shouldShow;

    void begin(Stage projection, boolean hide) {
        if (active) {
            return;
        }
        active = true;
        setHiding(hide, projection);
    }

    void setHiding(boolean hide, Stage currentProjection) {
        if (!active || hiding == hide) return;
        if (hide) {
            projection = currentProjection;
            shouldShow = projection != null && projection.isShowing();
            hiding = true;
            if (shouldShow) projection.hide();
        } else {
            Stage previous = projection;
            boolean show = shouldShow && previous != null && previous == currentProjection;
            hiding = false;
            projection = null;
            shouldShow = false;
            if (show && !previous.isShowing()) previous.show();
        }
    }

    /** @return true if the request was deferred until PiP closes. */
    boolean request(Stage target, boolean showing) {
        if (!active || !hiding || target == null) {
            return false;
        }
        if (target != projection) {
            projection = target;
            if (projection.isShowing()) {
                projection.hide();
            }
        }
        shouldShow = showing;
        return true;
    }

    void end(boolean restore, Stage currentProjection) {
        Stage previous = projection;
        boolean show = hiding && restore && shouldShow;
        active = false;
        hiding = false;
        projection = null;
        shouldShow = false;
        if (show && previous != null && previous == currentProjection && !previous.isShowing()) {
            previous.show();
        }
    }
}
