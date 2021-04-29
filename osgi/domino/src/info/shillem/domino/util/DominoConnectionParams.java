package info.shillem.domino.util;

import info.shillem.util.ConnectionParams;

public class DominoConnectionParams extends ConnectionParams {

    public static class Builder extends ConnectionParams.Builder {

        private boolean remote;

        @Override
        public ConnectionParams build() {
            return new DominoConnectionParams(this);
        }

        public void setRemote(boolean remote) {
            this.remote = remote;
        }

    }

    private static final long serialVersionUID = 1L;

    private final boolean remote;

    private DominoConnectionParams(Builder builder) {
        super(builder);

        remote = builder.remote;
    }
    
    public boolean isRemote() {
        return remote;
    }

}
