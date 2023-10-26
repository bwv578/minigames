[System.Serializable]
public class ServerStatus
{
    public string myName;
    public int playerNum;
    public string[] connectedPlayers;
    public RoomInfo[] rooms;
}

[System.Serializable]
public class RoomInfo
{
    public string gameID;
    public string[] players;
    public string title;
}

[System.Serializable]
public class GameStatus
{
    public string gameID;
    public string title;
    public string active;
    public int turn;
    public int remaining;
    public PlayerInfo[] players;
    public int[] dice;
}

[System.Serializable]
public class PlayerInfo
{
    public string name;
    public RecordBoard status;
}

[System.Serializable]
public class RecordBoard
{
    public string aces;
    public string twos;
    public string threes;
    public string fours;
    public string fives;
    public string sixes;
    public string bonus;
    public string fourofakind;
    public string fullhouse;
    public string smallstr;
    public string largestr;
    public string choice;
    public string yacht;
    public string total;
}
