package financialmanager.objectFolder.responseFolder;

import lombok.Getter;

@Getter
public class Response {
    private final AlertType alertType;
    private final String message;
    private Object data;

    public Response(AlertType alertType, String message) {
        this.alertType = alertType;
        this.message = message;
    }

    public Response(AlertType alertType, String message, Object data) {
        this.alertType = alertType;
        this.message = message;
        this.data = data;
    }

}
