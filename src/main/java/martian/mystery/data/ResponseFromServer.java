package martian.mystery.data;

public class ResponseFromServer { // ответ от сервера заворачивается в этот класс-обертку

    private String winner; // имя победителя
    private String prize; // приз
    private String season; // сезон
    private String email; // контакт для связи
    private String linkwinner; // ссылка на соц сеть победителя
    private String leaders; // инфа о каждом уровне записывается в таком виде: 'level'-'countPlayersOnThisLevel'-'firstPlayer'
    private String riddle;
    private int result; // результат запроса
    private int existwinner; // наличие победителя
    private int updating; // процесс обновления
    private int updateforce; // принудительное обновление приложения
    private int place; // место в игре

    public String getLinktowinner() {
        return linkwinner;
    }

    public void setLinktowinner(String linktowinner) {
        this.linkwinner = linktowinner;
    }

    public String getRiddle() {
        return riddle;
    }

    public void setRiddle(String riddle) {
        this.riddle = riddle;
    }

    public int getUpdating() {
        return updating;
    }

    public void setUpdating(int updating) {
        this.updating = updating;
    }

    public int getUpdateforce() {
        return updateforce;
    }

    public void setUpdateforce(int updateforce) {
        this.updateforce = updateforce;
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

    public String getLeaders() {
        return leaders;
    }

    public void setLeaders(String leaders) {
        this.leaders = leaders;
    }
}
