package info.shillem.domino.util;

public enum MimeContentType {

    ATTACHMENT("attachment") {

        @Override
        public boolean matches(String[] properties) {
            int score = 0;

            for (String property : properties) {
                if (property.startsWith("Content-Disposition")) {
                    score++;
                }

                if (property.contains("attachment")) {
                    score++;
                }

                if (property.contains("filename")) {
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

    public boolean matches(String[] properties) {
        for (String property : properties) {
            if (property.startsWith("Content-Type") && property.contains(type)) {
                return true;
            }
        }

        return false;
    }

}
