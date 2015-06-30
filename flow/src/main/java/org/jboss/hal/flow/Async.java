/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.flow;

import com.google.gwt.core.client.Scheduler;

/**
 * Flow control functions for GWT.
 * Integrates with the default GWT scheduling mechanism.
 *
 * @author Heiko Braun
 */
public class Async<C> {

    private final Progress progress;

    public Async() {
        this(Progress.NOOP);
    }

    public Async(final Progress progress) {
        this.progress = progress;
    }

    /**
     * Run an array of functions in series, each one running once the previous function has completed.
     * If any functions in the series pass an error to its callback,
     * no more functions are run and outcome for the series is immediately called with the value of the error.
     */
    public void series(final C context, final Outcome<C> outcome, final Function<C>... functions) {
        _series(context, outcome, functions);
    }

    /**
     * Runs an array of functions in series, working on a shared context.
     * However, if any of the functions pass an error to the callback,
     * the next function is not executed and the outcome is immediately called with the error.
     */
    @SafeVarargs
    public final void waterfall(final C context, final Outcome<C> outcome, final Function<C>... functions) {
        _series(context, outcome, functions);
    }

    @SafeVarargs
    private final void _series(final C context, final Outcome<C> outcome, final Function<C>... functions) {
        final SequentialControl ctrl = new SequentialControl(context, functions);

        // reset progress
        progress.reset(functions.length);

        // select first function and start
        ctrl.proceed();
        Scheduler.get().scheduleIncremental(() -> {
            if (ctrl.isDrained()) {
                // schedule deferred so that 'return false' executes first!
                Scheduler.get().scheduleDeferred(() -> {
                    progress.finish();
                    outcome.onSuccess(context);
                });
                return false;
            } else if (ctrl.isAborted()) {
                // schedule deferred so that 'return false' executes first!
                Scheduler.get().scheduleDeferred(() -> {
                    progress.finish();
                    outcome.onFailure(context);
                });
                return false;
            } else {
                ctrl.nextUnlessPending();
                return true;
            }
        });
    }

    /**
     * Run an array of functions in parallel, without waiting until the previous function has completed.
     * If any of the functions pass an error to its callback, the outcome is immediately called with the value of the
     * error.
     */
    public void parallel(final C context, final Outcome<C> outcome, final Function<C>... functions) {
        final CountingControl ctrl = new CountingControl(context, functions);
        progress.reset(functions.length);

        Scheduler.get().scheduleIncremental(() -> {
            if (ctrl.isAborted() || ctrl.allFinished()) {
                // schedule deferred so that 'return false' executes first!
                Scheduler.get().scheduleDeferred(() -> {
                    if (ctrl.isAborted()) {
                        progress.finish();
                        outcome.onFailure(context);
                    } else {
                        progress.finish();
                        outcome.onSuccess(context);
                    }

                });
                return false;
            } else {
                // one after the other until all are active
                ctrl.next();
                return true;
            }
        });
    }

    /**
     * Repeatedly call function, while condition is met. Calls the callback when stopped, or an error occurs.
     */
    public void whilst(C context, Precondition condition, final Outcome<C> outcome, final Function<C> function) {
        whilst(context, condition, outcome, function, -1);
    }

    /**
     * Same as {@link #whilst(Object, Precondition, Outcome, Function)} but waits {@code period} millis between calls to
     * {@code function}.
     *
     * @param period any value below 100 is ignored!
     */
    public void whilst(C context, Precondition condition, final Outcome<C> outcome, final Function<C> function, int period) {
        final GuardedControl ctrl = new GuardedControl(context, condition);
        progress.reset();

        Scheduler.RepeatingCommand repeatingCommand = () -> {
            if (!ctrl.shouldProceed()) {
                // schedule deferred so that 'return false' executes first!
                Scheduler.get().scheduleDeferred(() -> {
                    if (ctrl.isAborted()) {
                        progress.finish();
                        outcome.onFailure(context);
                    } else {
                        progress.finish();
                        outcome.onSuccess(context);
                    }
                });
                return false;
            } else {
                function.execute(ctrl);
                progress.tick();
                return true;
            }
        };

        if (period > 100) {
            Scheduler.get().scheduleFixedPeriod(repeatingCommand, period);
        } else {
            Scheduler.get().scheduleIncremental(repeatingCommand);
        }
    }

    private class SequentialControl implements Control<C> {

        private final C context;
        private final Function<C>[] functions;
        private Function<C> next;
        private int index;
        private boolean drained;
        private boolean aborted;
        private boolean pending;

        @SafeVarargs
        SequentialControl(final C context, final Function<C>... functions) {
            this.context = context;
            this.functions = functions;
        }

        @Override
        public C getContext() {
            return context;
        }

        @Override
        public void proceed() {
            if (index > 0) {
                // start ticking *after* the first function has finished
                progress.tick();
            }
            if (index >= functions.length) {
                next = null;
                drained = true;
            } else {
                next = functions[index];
                index++;
            }
            this.pending = false;
        }

        @Override
        public void abort() {
            this.aborted = true;
            this.pending = false;
        }

        public boolean isAborted() {
            return aborted;
        }

        public boolean isDrained() {
            return drained;
        }

        public void nextUnlessPending() {
            if (!pending) {
                pending = true;
                next.execute(this);
            }
        }
    }

    private class CountingControl implements Control<C> {

        private final C context;
        private final Function<C>[] functions;
        protected boolean aborted;
        private int index;
        private int finished;

        @SafeVarargs
        CountingControl(final C context, final Function<C>... functions) {
            this.context = context;
            this.functions = functions;
        }

        @Override
        public C getContext() {
            return context;
        }

        public void next() {
            if (index < functions.length) {
                functions[index].execute(this);
                index++;
            }
        }

        @Override
        public void proceed() {
            if (index > 0) {
                // start ticking *after* the first function has finished
                progress.tick();
            }
            increment();
        }

        private void increment() {
            ++finished;
        }

        @Override
        public void abort() {
            increment();
            aborted = true;
        }

        public boolean isAborted() {
            return aborted;
        }

        public boolean allFinished() {
            return finished >= functions.length;
        }
    }

    private class GuardedControl implements Control<C> {

        private final C context;
        private final Precondition condition;
        private boolean aborted;

        GuardedControl(final C context, final Precondition condition) {
            this.context = context;
            this.condition = condition;
        }

        @Override
        public void proceed() {
            // ignore
        }

        public boolean shouldProceed() {
            return condition.isMet() && !aborted;
        }

        @Override
        public void abort() {
            this.aborted = true;
        }

        public boolean isAborted() {
            return aborted;
        }

        @Override
        public C getContext() {
            return context;
        }
    }
}
