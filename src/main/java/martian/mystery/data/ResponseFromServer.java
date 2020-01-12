package martian.mystery.data;

public class ResponseFromServer { // ответ от сервера заворачивается в этот класс-обертку

    private String winner;
    private String prize;
    private String season;
    private String email;
    private int result;
    private int existwinner;
    private int updateapp;
    private int updateforce;

    public int getUpdate() {
        return updateapp;
    }

    public void setUpdate(int update) {
        this.updateapp = update;
    }

    public int getForceUpdate() {
        return updateforce;
    }

    public void setForceUpdate(int forceUpdate) {
        this.updateforce = forceUpdate;
    }

    public int getExistWinner() {
        return existwinner;
    }

    public void setExistWinner(int existWinner) {
        this.existwinner = existWinner;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private int place;

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}
