package lt.gama.api.response;

import java.util.List;

/**
 * Writes an error in the expected form for JSON-REST:
 * <p>
 * {
 *   "error": {
 *     "errors: [
 *       {
 *         "domain: "global",
 *         "reason": "backendError",
 *         "message: "..."
 *       }
 *     ],
 *     "code": 503,
 *     "message": "..."
 *   }
 * }
 */

public class ApiErrorResponse {

    private Error error;

    public static class Error {
        private List<ErrorMessage> errors;
        private int code;
        private String message;

        public List<ErrorMessage> getErrors() {
            return errors;
        }

        public void setErrors(List<ErrorMessage> errors) {
            this.errors = errors;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "errors=" + errors +
                    ", code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public static class ErrorMessage {
        private String domain;
        private String reason;
        private String message;

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "ErrorMessage{" +
                    "domain='" + domain + '\'' +
                    ", reason='" + reason + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
