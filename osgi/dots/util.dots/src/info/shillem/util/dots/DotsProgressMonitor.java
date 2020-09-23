package info.shillem.util.dots;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;

import info.shillem.util.ProgressMonitor;

public class DotsProgressMonitor extends ProgressMonitor {

    private IProgressMonitor progressMonitor;

    public DotsProgressMonitor(IProgressMonitor progressMonitor) {
        super();

        this.progressMonitor =
                Objects.requireNonNull(progressMonitor, "Progress monitor cannot be null");
    }

    @Override
    public boolean isRequestCanceled() {
        return progressMonitor.isCanceled();
    }

    @Override
    public void setRequestCanceled() {
        progressMonitor.setCanceled(true);
    }

}
