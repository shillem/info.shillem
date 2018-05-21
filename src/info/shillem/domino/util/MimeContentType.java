package info.shillem.domino.util;

public enum MimeContentType {

	ATTACHMENT {
		@Override
		public boolean matches(String[] headers) {
			for (String header : headers) {
				if (header.startsWith("Content-Disposition")
						&& header.contains("attachment")
						&& header.contains("filename")) {
					return true;
				}
			}

			return false;
		}
	},
	TEXT {
		@Override
		public boolean matches(String[] headers) {
			for (String header : headers) {
				if (header.startsWith("Content-Type") && header.contains("text")) {
					return true;
				}
			}

			return false;
		}
	},
	TEXT_HTML {
		@Override
		public boolean matches(String[] headers) {
			for (String header : headers) {
				if (header.startsWith("Content-Type") && header.contains("text/html")) {
					return true;
				}
			}

			return false;
		}
	},
	TEXT_PLAIN {
		@Override
		public boolean matches(String[] headers) {
			for (String header : headers) {
				if (header.startsWith("Content-Type") && header.contains("text/plain")) {
					return true;
				}
			}

			return false;
		}
	};

	public abstract boolean matches(String[] headers);

}
