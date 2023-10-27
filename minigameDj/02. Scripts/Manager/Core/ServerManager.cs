
using UnityEngine;
using WebSocketSharp;

public class ServerManager
{
    private bool _init = false;
    private bool _isFirstTurn = false;
    private bool _isStart = false;
    private bool _isOppOut = false;
    private bool _isGameEnd = false;
    private string _prevServer = string.Empty;
    private string _prevGame = string.Empty;
    private string _resultGame = string.Empty;
    private WebSocket _ws = null;
    private ServerStatus _serverStatus = new ServerStatus();
    private GameStatus _gameStatus = new GameStatus();    

    public bool IsInit { get { return _init; } }
    public bool IsFirstTurn { get { return _isFirstTurn; } }
    public bool IsStart { get { return _isStart; } }
    public bool IsOppOut { get { return _isOppOut; } }
    public bool IsGameEnd {  get { return _isGameEnd; } }
    public string PrevServer { get { return _prevServer; } }
    public string PrevGame { get { return _prevGame; } }
    public string ResultGame { get { return _resultGame; } }
    public WebSocket WebSocket { get { return _ws; } }
    public ServerStatus ServerStatus { get { return _serverStatus; } } 
    public GameStatus GameStatus { get { return _gameStatus; } }
    
    public void Init()
    {
        //if (_ws == null) 
        _ws = new WebSocket("ws://52.78.178.113:8080/yachtWS");

        _ws.OnMessage += WS_ServerStatus;        
        _ws.OnOpen += WS_OnOpen;
        _ws.OnClose += WS_OnClose;


        _resultGame = "You Win";

        _ws.Connect();
    }

    public void OnUpdate()
    {
        if (_ws.ReadyState != WebSocketState.Closed)
        {
            _ws.Send("server_status@");
            if (Input.GetKeyDown(KeyCode.A))
                _ws.Close();
        }
    }

    public void WS_ServerStatus(object send, MessageEventArgs e)
    {
        //Debug.Log(e.Data);
        string header = e.Data.Split('@')[0];
        string status = e.Data.Split('@')[1];
        if (header == "server_status" && _prevServer.Equals(status) == false)
        {
            _prevServer = status;
            _serverStatus = JsonUtility.FromJson<ServerStatus>(status);
            //Debug.Log($"서버:{status}");
        }

        if (header == "no_room")
        {

        }

        if (header == "first")
        {
            _isFirstTurn = status.Equals("true");            
        }

        if (header == "game_status")
        {
            _prevGame = status;            
            _gameStatus = JsonUtility.FromJson<GameStatus>(status);
            _isStart = _gameStatus.active.Equals("true");
            //Debug.Log($"게임:{_isStart}");
        }

        if (header == "result")
        {
            _isGameEnd = true;
            _resultGame = status;
        }

        if (header == "opp_disconnected")
        {
            _isOppOut = true;
        }
        _init = true;
    }

    public void WS_OnOpen(object send, System.EventArgs e)
    {
        Debug.Log("Open");
    }

    public void WS_OnClose(object send, CloseEventArgs e)
    {
        Debug.Log("Close");
    }
}
