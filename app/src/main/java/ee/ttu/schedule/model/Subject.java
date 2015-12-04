package ee.ttu.schedule.model;

import java.io.Serializable;

public class Subject implements Serializable {

    private int ID;
    private Long dateStart;
    private Long dateEnd;
    private String description;
    private String location;
    private String summary;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Long getDateStart() {
        return dateStart;
    }

    public void setDateStart(Long dateStart) {
        this.dateStart = dateStart;
    }

    public Long getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Long dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

}
