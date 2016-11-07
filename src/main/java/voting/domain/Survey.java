package voting.domain;

import java.util.Date;
import java.util.UUID;

public class Survey {
    private String id = UUID.randomUUID().toString();
    private String title;
    private String description;
    private Date createdTime = new Date();

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedTime(){
        return createdTime;
    }
}
