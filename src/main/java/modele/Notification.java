/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;

/**
 *
 * @author Glamor Store
 */
import java.time.LocalDateTime;

public class Notification {
    private int id;
    private String message;
    private LocalDateTime dateEnvoi;
    private boolean lue;

    public Notification(int id, String message, LocalDateTime dateEnvoi, boolean lue) {
        this.id = id;
        this.message = message;
        this.dateEnvoi = dateEnvoi;
        this.lue = lue;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public boolean isLue() {
        return lue;
    }
}
