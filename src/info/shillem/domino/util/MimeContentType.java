package info.shillem.domino.util;

public enum MimeContentType {

    ATTACHMENT("attachment") {

        @Override
        public boolean matches(String[] headers) {
            int score = 0;

            for (String header : headers) {
                if (header.startsWith("Content-Disposition")) {
                    score++;
                }

                if (header.contains("attachment")) {
                    score++;
                }

                if (header.contains("filename")) {
                    score++;
                }

                if (score >= 3) {
                    return true;
                }
            }

            return false;
        }

    },
    TEXT("text"),
    TEXT_HTML("text/html"),
    TEXT_PLAIN("text/plain");

    private final String type;

    private MimeContentType(String type) {
        this.type = type;
    }

    public boolean matches(String[] headers) {
        for (String header : headers) {
            if (header.startsWith("Content-Type") && header.contains(type)) {
                return true;
            }
        }

        return false;
    }

}
