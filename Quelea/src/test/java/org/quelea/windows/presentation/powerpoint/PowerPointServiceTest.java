package org.quelea.windows.presentation.powerpoint;

import java.io.File;
import javafx.geometry.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The service against a fake PowerPoint: commands run in order off the
 * calling thread, states reach the listeners, loop mode wraps around, a
 * missing permission reaches the error handler, and slide changes made in
 * PowerPoint itself are picked up by polling.
 */
@DisplayName("PowerPointService")
public class PowerPointServiceTest {

    /** A pretend PowerPoint with a 3 slide show. */
    static class FakePowerPoint implements PowerPointBackend {

        volatile int slide;
        volatile boolean black;
        volatile boolean open;
        volatile boolean denyAutomation;
        final List<String> calls = new CopyOnWriteArrayList<>();
        final int total = 3;

        private SlideState now() {
            return open ? new SlideState(slide, total, black ? SlideState.Mode.BLACK : SlideState.Mode.RUNNING)
                    : new SlideState(0, 0, SlideState.Mode.NOSHOW);
        }

        private void check(String call) throws PowerPointException {
            calls.add(call);
            if (denyAutomation) {
                throw PowerPointException.fromAppleScript(-1743, "Not authorised");
            }
        }

        @Override
        public SlideState open(File file, Rectangle2D screen) throws PowerPointException {
            check("open");
            open = true;
            slide = 1;
            return now();
        }

        @Override
        public SlideState close() throws PowerPointException {
            check("close");
            open = false;
            return now();
        }

        @Override
        public SlideState next() throws PowerPointException {
            check("next");
            slide = Math.min(total, slide + 1);
            return now();
        }

        @Override
        public SlideState previous() throws PowerPointException {
            check("previous");
            slide = Math.max(1, slide - 1);
            return now();
        }

        @Override
        public SlideState gotoSlide(int n) throws PowerPointException {
            check("goto" + n);
            slide = n;
            return now();
        }

        @Override
        public SlideState setBlack(boolean b) throws PowerPointException {
            check("black" + b);
            black = b;
            return now();
        }

        @Override
        public SlideState state() throws PowerPointException {
            check("state");
            return now();
        }

        @Override
        public void focus() throws PowerPointException {
            check("focus");
        }

        @Override
        public boolean supportsPolling() {
            return true;
        }

        @Override
        public void shutdown() {
            calls.add("shutdown");
        }
    }

    private final FakePowerPoint fake = new FakePowerPoint();
    private final List<SlideState> states = new CopyOnWriteArrayList<>();
    private final List<PowerPointException> errors = new CopyOnWriteArrayList<>();
    private PowerPointService service;

    private PowerPointService service() {
        service = new PowerPointService(fake, Runnable::run);
        service.addStateListener(states::add);
        service.setErrorHandler(errors::add);
        return service;
    }

    @AfterEach
    public void tearDown() {
        if (service != null) {
            service.shutdown();
        }
    }

    private static <T> T get(java.util.concurrent.Future<T> f) throws Exception {
        return f.get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Commands answer with the new state and tell the listeners")
    public void commands() throws Exception {
        PowerPointService s = service();
        assertEquals(1, get(s.open(new File("x.pptx"))).getSlide());
        assertEquals(2, get(s.next()).getSlide());
        assertEquals(3, get(s.gotoSlide(3)).getSlide());
        assertEquals(2, get(s.previous()).getSlide());
        assertTrue(get(s.setBlack(true)).isBlack());
        assertEquals(new SlideState(2, 3, SlideState.Mode.BLACK), s.getLastState());
        assertTrue(states.size() >= 5);
        assertEquals(new SlideState(2, 3, SlideState.Mode.BLACK), states.get(states.size() - 1));
    }

    @Test
    @DisplayName("Loop mode goes back to the first slide after the last")
    public void loopWraps() throws Exception {
        PowerPointService s = service();
        get(s.open(new File("x.pptx")));
        assertEquals(Boolean.FALSE, get(s.setLoop(true)));
        assertEquals(2, get(s.nextWrapping()).getSlide());
        assertEquals(3, get(s.nextWrapping()).getSlide());
        assertEquals(1, get(s.nextWrapping()).getSlide());
    }

    @Test
    @DisplayName("A missing permission reaches the error handler and fails the command")
    public void permission() throws Exception {
        PowerPointService s = service();
        fake.denyAutomation = true;
        ExecutionException ex = assertThrows(ExecutionException.class, () -> get(s.next()));
        PowerPointException cause = PowerPointService.unwrap(ex.getCause());
        assertSame(PowerPointException.Kind.NOT_AUTHORIZED, cause.getKind());
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Slide changes made in PowerPoint itself are picked up")
    public void polling() throws Exception {
        PowerPointService s = service();
        get(s.open(new File("x.pptx")));
        states.clear();
        fake.slide = 3; // The presenter's clicker.
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline && states.stream().noneMatch(st -> st.getSlide() == 3)) {
            Thread.sleep(50);
        }
        assertTrue(states.stream().anyMatch(st -> st.getSlide() == 3), "poll should report slide 3");
        get(s.close());
        int polls = (int) fake.calls.stream().filter("state"::equals).count();
        Thread.sleep(1200);
        assertEquals(polls, (int) fake.calls.stream().filter("state"::equals).count(), "no polling once closed");
    }

    @Test
    @DisplayName("Shutting down closes the presentation and the backend")
    public void shutdown() throws Exception {
        PowerPointService s = service();
        get(s.open(new File("x.pptx")));
        s.shutdown();
        service = null;
        assertTrue(fake.calls.contains("close"));
        assertTrue(fake.calls.contains("shutdown"));
    }
}
