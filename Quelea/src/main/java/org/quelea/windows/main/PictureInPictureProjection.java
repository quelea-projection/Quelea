package org.quelea.windows.main;

import javafx.stage.Stage;

/** Temporarily hides the projection while remembering visibility requests. */
final class PictureInPictureProjection {
    private Stage projection;
    private boolean active;
    private boolean shouldShow;

    void begin(Stage projection) {
        if (active) {
            return;
        }
        this.projection = projection;
        active = true;
        shouldShow = projection != null && projection.isShowing();
        if (shouldShow) {
            projection.hide();
        }
    }

    /** @return true if the request was deferred until PiP closes. */
    boolean request(Stage target, boolean showing) {
        if (!active || target == null) {
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
        boolean show = restore && shouldShow;
        active = false;
        projection = null;
        shouldShow = false;
        if (show && previous != null && previous == currentProjection && !previous.isShowing()) {
            previous.show();
        }
    }
}
