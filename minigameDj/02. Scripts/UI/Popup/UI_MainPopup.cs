using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

public class UI_MainPopup : UI_Popup
{
    enum Texts
    {
        txtRoomName1,
        txtRoomName2,
        txtRoomName3,
        txtRoomName4,
        txtRoomName5,
        txtPeople1,
        txtPeople2,
        txtPeople3,
        txtPeople4,
        txtPeople5,

        txtServerConnect,
    }

    enum Buttons
    {
        btnEnter1,
        btnEnter2,
        btnEnter3,
        btnEnter4,
        btnEnter5,
        btnCreateRoom,
        btnMatchingRoom,
    }

    private int _playerNum = 0;
    private string _oldServer = string.Empty;
    private RoomInfo[] _roomsInfo = null;
    private Dictionary<string, int> _dicRoomId = new Dictionary<string, int>();

    public override bool Init()
    {
        if (base.Init() == false)
            return false;

        BindText(typeof(Texts));
        BindButton(typeof(Buttons));

        // 텍스트 초기화
        GetText((int)Texts.txtRoomName1).text = string.Empty;
        GetText((int)Texts.txtRoomName2).text = string.Empty;
        GetText((int)Texts.txtRoomName3).text = string.Empty;
        GetText((int)Texts.txtRoomName4).text = string.Empty;
        GetText((int)Texts.txtRoomName5).text = string.Empty;

        GetText((int)Texts.txtPeople1).text = string.Empty;
        GetText((int)Texts.txtPeople2).text = string.Empty;
        GetText((int)Texts.txtPeople3).text = string.Empty;
        GetText((int)Texts.txtPeople4).text = string.Empty;
        GetText((int)Texts.txtPeople5).text = string.Empty;

        GetButton((int)Buttons.btnCreateRoom).gameObject.BindEvent(OnClickCreateRoom);
        GetButton((int)Buttons.btnMatchingRoom).gameObject.BindEvent(OnClickMatching);

        for (int i = (int)Buttons.btnEnter1; i < (int)Buttons.btnEnter5 + 1; i++)
        {
            GetButton((int)Buttons.btnEnter1 + i).gameObject.BindEvent(OnClickEnterRoom);
            _dicRoomId.Add(GetButton((int)Buttons.btnEnter1 + i).gameObject.name, i);
        }

        //RoomInit();

        return true;
    }

    private void Update()
    {
        if (Managers.Server.IsInit)
        {
            UpdateServerStatus();
        }
    }

    // 서버 통신
    private void UpdateServerStatus()
    {
        if (_oldServer.Equals(Managers.Server.PrevServer) == false)
        {
            _oldServer = Managers.Server.PrevServer;
            _playerNum = Managers.Server.ServerStatus.playerNum;
            _roomsInfo = Managers.Server.ServerStatus.rooms;

            ConnectedPlayers();
            RoomList();
        }
    }

    #region 버튼 이벤트
    // 방 입장    
    private void OnClickEnterRoom()
    {
        GameObject go = EventSystem.current.currentSelectedGameObject;

        if (go == null)
            return;

        _dicRoomId.TryGetValue(go.name, out int value);

        Managers.Server.WebSocket.Send($"enter@{Managers.Server.ServerStatus.rooms[value].gameID}");
        Managers.UI.ShowPopupUI<UI_GamePopup>();
    }

    // 방 생성
    private void OnClickCreateRoom()
    {
        string roomName = $"Room {Random.Range(0, 999)}";
        // TODO : 원하는 방 제목 설정 코드 필요

        Managers.Server.WebSocket.Send($"create_room@{roomName}");

        Managers.UI.ShowPopupUI<UI_GamePopup>();
    }

    // 랜덤 매칭
    private void OnClickMatching()
    {
        if (_roomsInfo.Length == 0)
            return;
        else
        {
            while(true)
            {
                int random = Random.Range(0, _roomsInfo.Length) / 1;
                if (_roomsInfo[random].players.Length != 2)
                {
                    Managers.Server.WebSocket.Send($"enter@{_roomsInfo[random].gameID}");
                    Managers.UI.ShowPopupUI<UI_GamePopup>();
                    break;
                }
            }            
        }
    }
    #endregion

    // 연결된 플레이어 수
    private void ConnectedPlayers()
    {
        GetText((int)Texts.txtServerConnect).text = $"Players :{_playerNum}";
    }

    // 방 리스트 정보
    private void RoomList()
    {
        RoomInit();

        ColorBlock colorBlock;
        for (int i = 0; i < _roomsInfo.Length; i++)
        {
            int players = _roomsInfo[i].players.Length;
            GetText((int)Texts.txtRoomName1 + i).text = _roomsInfo[i].title;
            GetText((int)Texts.txtPeople1 + i).text = $"{players}/2";
            if (players == 1)
            {
                colorBlock = GetButton((int)Buttons.btnEnter1 + i).colors;
                colorBlock.normalColor = Color.white;
                GetButton((int)Buttons.btnEnter1 + i).colors = colorBlock;
                GetButton((int)Buttons.btnEnter1 + i).interactable = true;
            }
        }
    }

    // 방 리스트 초기화
    private void RoomInit()
    {
        ColorBlock colorBlock;

        for (int i = (int)Buttons.btnEnter1; i < (int)Buttons.btnEnter5 + 1; i++)
        {
            colorBlock = GetButton((int)Buttons.btnEnter1 + i).colors;
            GetText((int)Texts.txtRoomName1 + i).text = string.Empty;
            GetText((int)Texts.txtPeople1 + i).text = string.Empty;

            colorBlock.normalColor = Color.gray;
            GetButton((int)Buttons.btnEnter1 + i).colors = colorBlock;
            GetButton((int)Buttons.btnEnter1 + i).interactable = false;
        }
    }


}
