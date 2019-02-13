package info.shillem.domino.util;

import lotus.domino.Base;
import lotus.domino.NotesException;

abstract class AbstractLooperDriver<T extends Base> {

    private boolean aborted;

    private T base;

    public void abort() {
        aborted = true;
    }

    public T getBase() {
        return base;
    }

    boolean isAborted() {
        return aborted;
    }

    public T moveTo(T base) throws NotesException {
        recycle();

        this.base = base;

        return base;
    }

    void recycle() {
        DominoUtil.recycle(base);
    }

}
