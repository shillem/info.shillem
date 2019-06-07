package info.shillem.util.dots;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.dots.task.IServerTaskRunnable;
import com.ibm.dots.task.RunWhen;

public class TaskManager {

    public static class TaskRun {

        private final IServerTaskRunnable task;
        private final RunWhen runWhen;
        private final String[] args;
        private final IProgressMonitor progressMonitor;

        public TaskRun(IServerTaskRunnable task,
                RunWhen runWhen, String[] args, IProgressMonitor progressMonitor) {
            this.task = Objects.requireNonNull(
                    task, "Task when cannot be null");
            this.runWhen = Objects.requireNonNull(
                    runWhen, "Run when cannot be null");
            this.args = Objects.requireNonNull(
                    args, "Args when cannot be null");
            this.progressMonitor = Objects.requireNonNull(
                    progressMonitor, "Progress monitor cannot be null");
        }
        
        public String[] getArgs() {
            return args;
        }

        public IProgressMonitor getProgressMonitor() {
            return progressMonitor;
        }

        public RunWhen getRunWhen() {
            return runWhen;
        }
        
        public IServerTaskRunnable getTask() {
            return task;
        }

    }

    public static class TaskStatus {

        private final String id;
        private Instant current;
        private Instant lastStarted;
        private Instant lastStopped;

        public TaskStatus(String id) {
            this.id = Objects.requireNonNull(id, "Id cannot be null");
        }

        public String getId() {
            return id;
        }

        public Instant getLastStarted() {
            return lastStarted;
        }

        public Instant getLastStopped() {
            return lastStopped;
        }

        private boolean isRunning() {
            return current != null;
        }

        public synchronized boolean setAsRunning() {
            if (isRunning()) {
                return false;
            }

            lastStarted = Instant.now();
            lastStopped = null;

            return true;
        }

        public synchronized boolean setAsStopped() {
            if (!isRunning()) {
                return false;
            }

            lastStopped = Instant.now();

            return true;
        }

    }

    private final Map<String, TaskStatus> statuses;

    public TaskManager(Set<TaskStatus> statuses) {
        Objects.requireNonNull(statuses, "Statuses cannot be null");

        this.statuses = statuses
                .stream()
                .collect(Collectors.toMap(TaskStatus::getId, (t) -> t));
    }

    public TaskStatus getTaskStatus(String id) {
        return statuses.get(id);
    }

}
