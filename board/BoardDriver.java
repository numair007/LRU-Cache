package board;

import java.util.*;




class LeaderBoard{
    static LeaderBoard leaderBoardInstance;
    DisplayBoard displayBoard;
   // static int id;

    private LeaderBoard(){
        displayBoard = new DisplayBoard();
    }

    public static LeaderBoard getLeaderBoardInstance(){
       // id = 0;
        if(leaderBoardInstance == null) leaderBoardInstance = new LeaderBoard();
        return leaderBoardInstance;
        
    }

    public void addPlayer(String name, int score, int id){
        displayBoard.addPlayer(new Player(name, score, id));
    }
    public void addPlayer(Player p){
        displayBoard.addPlayer(new Player(p.name, p.score, p.id));
    }

    public void updatePlayerScore(int playerId, int score){
        displayBoard.updatePlayerScore(playerId, score);
    }
    public List<Player> getTopKPlayers(int k){
        long currentTime = System.currentTimeMillis()/1000l;
        return displayBoard.getTopKPlayers(k, currentTime);
    }

}

class DisplayBoard{
    TreeSet<Player> players;
    Map<Integer, Player> map;

    List<Player> topPlayers;
    long lastTimeComputed;
    long threshold;
    DisplayBoard(){
        players = new TreeSet<>(new comparePlayer());
        map = new HashMap<>();
        topPlayers = new ArrayList<>();
        lastTimeComputed = 0;
        threshold = 100l;
    }

    public synchronized void addPlayer(Player player){
        players.add(player);
        map.put(player.id, player);
    }

    public synchronized void updatePlayerScore(int playerId, int score){
        if(!map.containsKey(playerId)) return;
       Player player = map.get(playerId);
       players.remove(player);
       Player updatedPlayer = new Player(player.name, score, playerId);
       map.put(playerId, updatedPlayer);
       players.add(updatedPlayer);
    }

    public synchronized List<Player> getTopKPlayers(int k, long currentTime){

        if((currentTime - lastTimeComputed) < threshold) return topPlayers;
        TreeSet<Player> result = new TreeSet<>(new comparePlayer());

        for(Player p: players){
            if(result.size() < k){
                result.add(p);
            }
            else{
                Player out = result.last();
                if(out.score < p.score){
                    result.remove(out);
                    result.add(p);
                }
            }
        }

        lastTimeComputed = System.currentTimeMillis()/1000l;
        topPlayers  = new ArrayList<>(result);
        return topPlayers;
        
    }
}

class Player{
    String name;
    int score;
    int id;

    Player(String name, int score, int id){
        this.name = name;
        this.score = score;
        this.id = id;
    }
    public void updateScore(int score){
        this.score = score;
    }

    @Override
    public boolean equals(Object p){
        if(p == this) return true;
        if(p == null || (this.getClass() != p.getClass())) return false;
        
        Player player = (Player)(p);
        
        return player.id == this.id;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.id);
    }
}
class comparePlayer implements Comparator<Player>{
    public int compare(Player p1, Player p2){
        return -p1.score + p2.score;
    }
}

public class BoardDriver {
   

    public static void main(String[] args) {
        LeaderBoard leaderBoard = LeaderBoard.getLeaderBoardInstance();

        Player p1 = new Player("A", 5, 0);
        Player p2 = new Player("B", 1, 1);
        Player p3 = new Player("C", 20, 2);
        Player p4 = new Player("D",70, 3);
        Player p5 = new Player("E", 5, 4);

        leaderBoard.addPlayer(p1);
        leaderBoard.addPlayer(p2);
        leaderBoard.addPlayer(p3);
        leaderBoard.addPlayer(p4);
        leaderBoard.addPlayer(p5);

        print(leaderBoard.getTopKPlayers(3));
        leaderBoard.updatePlayerScore(3, 1);
        System.out.println("Division");
        print(leaderBoard.getTopKPlayers(3));



        
       
    }

    public static void print(List<Player> players){
        for(Player p : players){
            System.out.println(p.id+" "+p.name+" "+p.score);
        }
    }
}