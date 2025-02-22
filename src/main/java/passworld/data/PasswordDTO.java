package passworld.data;

public class PasswordDTO {
    private int id;
    private String description;
    private String username;
    private String url;
    private String password;

    public PasswordDTO(String description, String username, String url, String password) {
        this.description = description;
        this.username = username;
        this.url = url;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

