using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

public class UI_GamePopup : UI_Popup
{
    enum GameObjects
    {
        pnlResult,
    }

    enum Buttons
    {
        btnaces,
        btntwos,
        btnthrees,
        btnfours,
        btnfives,
        btnsixes,
        btnchoice,
        btnfourofakind,
        btnfullhouse,
        btnsmallstr,
        btnlargestr,
        btnyacht,

        btnDice1,
        btnDice2,
        btnDice3,
        btnDice4,
        btnDice5,

        btnRoll,
        btnRecord,

        btnExit,
        btnResultExit,
    }

    enum Images
    {
        imgaces,
        imgtwos,
        imgthrees,
        imgfours,
        imgfives,
        imgsixes,
        imgchoice,
        imgfourofakind,
        imgfullhouse,
        imgsmallstr,
        imglargestr,
        imgyacht,
    }

    enum Texts
    {
        txtp1Aces,
        txtp1Twos,
        txtp1Threes,
        txtp1Fours,
        txtp1Fives,
        txtp1Sixes,
        txtp1Choice,
        txtp14Kind,
        txtp1FullHouse,
        txtp1SStraight,
        txtp1LStraight,
        txtp1Yacht,
        txtp1Bonus,
        txtp1Total,

        txtp2Aces,
        txtp2Twos,
        txtp2Threes,
        txtp2Fours,
        txtp2Fives,
        txtp2Sixes,
        txtp2Choice,
        txtp24Kind,
        txtp2FullHouse,
        txtp2SStraight,
        txtp2LStraight,
        txtp2Yacht,
        txtp2Bonus,
        txtp2Total,

        txtRollCount,

        txtResult,
    }

    enum DiceRecords
    {
        aces,
        twos,
        threes,
        fours,
        fives,
        sixes,
        choice,
        fourofakind,
        fullhouse,
        smallstr,
        largestr,
        yacht,
        bonus,
        total,

        Count,
    }

    private Sprite[] _imgDice;
    private Sprite _imgWhite;

    private bool _isBoolTemp;
    private bool _isFirstTurn;
    private bool _myTurn;
    private int _player;
    private int _rerollCount = 0;
    private int _turnCount = 0;
    private int[] _dices = { 0, 0, 0, 0, 0 };
    private string _recordName = string.Empty;
    private string _oldGame = string.Empty;
    private string _gameId = string.Empty;
    private string _oldCheck = null;

    private PlayerInfo[] _playersInfo = null;
    private Button[] _btnRerollDice = { null, null, null, null, null };
    private List<int> _listDice = new List<int>();
    private Dictionary<string, bool> _dicRecorded = new Dictionary<string, bool>();
    private Dictionary<string, int> _dicGhostResult = new Dictionary<string, int>();
    private Dictionary<string, bool> _dicLockDice = new Dictionary<string, bool>()
    {
        { Utils.GetEnumName(typeof(Buttons), (int)Buttons.btnDice1), false },
        { Utils.GetEnumName(typeof(Buttons), (int)Buttons.btnDice2), false },
        { Utils.GetEnumName(typeof(Buttons), (int)Buttons.btnDice3), false },
        { Utils.GetEnumName(typeof(Buttons), (int)Buttons.btnDice4), false },
        { Utils.GetEnumName(typeof(Buttons), (int)Buttons.btnDice5), false },
    };

    public override bool Init()
    {
        if (base.Init() == false)
            return false;

        BindObject(typeof(GameObjects));
        BindButton(typeof(Buttons));
        BindImage(typeof(Images));
        BindText(typeof(Texts));
        
        GetObject((int)GameObjects.pnlResult).SetActive(false);

        // 리롤 주사위 흰색으로 초기화
        _imgDice = Managers.Resource.LoadAll<Sprite>("Arts/Dice/dices");
        _imgWhite = Managers.Resource.Load<Sprite>("Arts/Dice/dice");

        // 체크 이미지 비활성화
        for (int i = (int)Images.imgaces; i < (int)Images.imgyacht + 1; i++)
            GetImage(i).enabled = false;

        // 점수판 초기화
        Dictionary<string, bool> dicTemp = new Dictionary<string, bool>();
        for (int i = 0; i < (int)DiceRecords.Count; i++)
        {
            string key = Utils.GetEnumName(typeof(DiceRecords), (int)DiceRecords.aces + i);
            _dicRecorded.Add(key, false);
            _dicGhostResult.Add(key, 0);
        }

        // 굴릴 주사위 배열 적용
        int length = (int)Buttons.btnDice5 - (int)Buttons.btnDice1 + 1;
        for (int i = 0; i < length; i++)
            _btnRerollDice[i] = GetButton((int)Buttons.btnDice1 + i);

        // 버튼 이벤트 적용
        for (int i = 0; i < _btnRerollDice.Length; i++)
        {
            _btnRerollDice[i].gameObject.BindEvent(OnClickLockDice);
            _btnRerollDice[i].GetComponent<Image>().sprite = _imgWhite;
        }

        for (int i = (int)Buttons.btnaces; i < (int)Buttons.btnyacht + 1; i++)
            GetButton((int)Buttons.btnaces + i).gameObject.BindEvent(OnClickBoard);
        GetButton((int)Buttons.btnExit).gameObject.BindEvent(OnClickExit);
        GetButton((int)Buttons.btnRoll).gameObject.BindEvent(OnClickReRoll);
        GetButton((int)Buttons.btnRecord).gameObject.BindEvent(OnClickRecord);
        GetButton((int)Buttons.btnResultExit).gameObject.BindEvent(OnClickResultExit);

        return true;
    }

    private void Update()
    {
        if (_isBoolTemp == false && (Managers.Server.IsOppOut || Managers.Server.IsGameEnd))
        {
            _isBoolTemp = true;
            ShowResult();            
        }
        if (Managers.Server.IsInit)
        {
            UpdateGameStatus();
        }
    }

    #region 서버 통신
    private void UpdateGameStatus()
    {
        if (_oldGame.Equals(Managers.Server.PrevGame) == false && Managers.Server.IsStart)
        {
            _oldGame = Managers.Server.PrevGame;
            _gameId = Managers.Server.GameStatus.gameID;
            _playersInfo = Managers.Server.GameStatus.players;
            _rerollCount = Managers.Server.GameStatus.remaining;
            _isFirstTurn = Managers.Server.IsFirstTurn;
            _turnCount = Managers.Server.GameStatus.turn;
            _dices = Managers.Server.GameStatus.dice;

            ClearReroll();
            UpdateTurnCheck();
            UpdateRecordBoard();

            // 주사위 이미지 갱신
            for (int i = 0; i < _dices.Length; i++)
            {
                _btnRerollDice[i].GetComponent<Image>().sprite = _imgDice[_dices[i] - 1];
                _listDice.Add(_dices[i]);
            }

            PrevShowBoard();

            GetText((int)Texts.txtRollCount).text = $"Roll : {(_rerollCount)}";
        }
    }

    // 턴 체크
    private void UpdateTurnCheck()
    {
        if (_isFirstTurn == true && (_turnCount % 2) == 1)
        {
            _player = 0;
            _myTurn = true;
        }
        else if (_isFirstTurn == false && (_turnCount % 2) == 0)
        {
            _player = 1;
            _myTurn = true;
        }
        else
            _myTurn = false;
    }

    // 점수판 갱신
    private void UpdateRecordBoard()
    {
        int diceRecord = 0;
        for (int p = 0; p < _playersInfo.Length; p++)
        {
            Dictionary<string, string> dicTemp = new Dictionary<string, string>
                {
                    { "aces", _playersInfo[p].status.aces },
                    { "twos", _playersInfo[p].status.twos },
                    { "threes", _playersInfo[p].status.threes },
                    { "fours", _playersInfo[p].status.fours },
                    { "fives", _playersInfo[p].status.fives },
                    { "sixes", _playersInfo[p].status.sixes },
                    { "choice", _playersInfo[p].status.choice },
                    { "fourofakind", _playersInfo[p].status.fourofakind },
                    { "fullhouse", _playersInfo[p].status.fullhouse },
                    { "smallstr", _playersInfo[p].status.smallstr },
                    { "largestr", _playersInfo[p].status.largestr },
                    { "yacht", _playersInfo[p].status.yacht },
                    { "bonus", _playersInfo[p].status.bonus },
                    { "total", _playersInfo[p].status.total }
                };
            if (p == 1) diceRecord = (int)DiceRecords.Count;
            for (int i = 0; i < (int)DiceRecords.Count; i++)
            {
                string key = Utils.GetEnumName(typeof(DiceRecords), i);
                dicTemp.TryGetValue(key, out string temp);
                if (temp == null || temp == "")
                    GetText((int)Texts.txtp1Aces + i + diceRecord).text = $"";
                else
                {
                    GetText((int)Texts.txtp1Aces + i + diceRecord).text = $"{temp}";
                    if (_player == 0)
                    {
                        if (p == 0) { GetText((int)Texts.txtp1Aces + i + diceRecord).color = Color.green; _dicRecorded[key] = true; }
                        else GetText((int)Texts.txtp1Aces + i + diceRecord).color = Color.red;
                    }
                    else
                    {
                        if (p == 0) GetText((int)Texts.txtp1Aces + i + diceRecord).color = Color.red;
                        else { GetText((int)Texts.txtp1Aces + i + diceRecord).color = Color.green; _dicRecorded[key] = true; }
                    }
                }
            }
        }
    }
    #endregion

    #region 버튼 이벤트
    // 방 퇴장
    private void OnClickExit()
    {
        Managers.Server.WebSocket.Send("exit@");
        ClosePopupUI();
    }

    private void OnClickResultExit()
    {
        ClosePopupUI();
    }

    // 주사위 리롤
    private void OnClickReRoll()
    {
        if (Managers.Server.IsStart && _rerollCount.Equals(0) == false && _myTurn)
        {
            // 주사위 잠금 체크
            string reroll = string.Empty;
            for (int i = 0; i < _btnRerollDice.Length; i++)
            {
                _dicLockDice.TryGetValue(_btnRerollDice[i].name, out bool value);
                if (value == true)
                    reroll += $"{i}/";
            }

            // 서버에 리롤 요청
            Managers.Server.WebSocket.Send($"gameID@{_gameId}@reroll@{reroll}");
        }
    }

    // 주사위 리롤 잠금
    private void OnClickLockDice()
    {
        if (Managers.Server.IsStart && _myTurn)
        {
            GameObject go = EventSystem.current.currentSelectedGameObject;

            _dicLockDice.TryGetValue(go.name, out bool value);

            if (value)
            {
                _dicLockDice[go.name] = false;
                go.GetComponent<Button>().image.color = Color.white;
            }
            else
            {
                _dicLockDice[go.name] = true;
                go.GetComponent<Button>().image.color = Color.gray;
            }
        }
    }

    // 기록 선택
    private void OnClickBoard()
    {
        if (Managers.Server.IsStart && _myTurn && _rerollCount != 3)
        {
            GameObject go = EventSystem.current.currentSelectedGameObject;
            // aces, twos ....
            string goName = go.name.ToLower().Replace("btn", string.Empty);
            int imgIndex = (int)Utils.GetEnumParse(typeof(DiceRecords), goName);

            if (_oldCheck != null)
            {
                int imgOld = (int)Utils.GetEnumParse(typeof(DiceRecords), _oldCheck);
                // 이전 체크 fasle
                if (_oldCheck != goName)
                    GetImage(imgOld).enabled = false;
            }

            _oldCheck = goName;
            _recordName = goName;
            GetImage(imgIndex).enabled = true;

        }
    }

    // 기록 저장
    private void OnClickRecord()
    {
        if (Managers.Server.IsStart && _recordName != null && _myTurn)
        {
            int imgIndex = (int)Utils.GetEnumParse(typeof(DiceRecords), _oldCheck);
            GetImage(imgIndex).enabled = false;
            Managers.Server.WebSocket.Send($"gameID@{_gameId}@select@{_recordName}");

        }
    }
    #endregion

    private void ShowResult()
    {
        GetObject((int)GameObjects.pnlResult).SetActive(true);
        GetText((int)Texts.txtResult).text = Managers.Server.ResultGame;
    }

    // 리롤 초기화
    private void ClearReroll()
    {
        for (int i = 0; i < _btnRerollDice.Length; i++)
        {
            _dicLockDice[Utils.GetEnumName(typeof(Buttons), (int)Buttons.btnDice1 + i)] = false;
            _btnRerollDice[i].image.color = Color.white;
            _btnRerollDice[i].GetComponent<Image>().sprite = _imgWhite;
        }
        for (int i = 0; i < (int)DiceRecords.Count; i++)
        {
            string key = Utils.GetEnumName(typeof(DiceRecords), (int)DiceRecords.aces + i);
            _dicGhostResult[key] = 0;
        }
        if (_oldCheck != null)
        {
            int imgIndex = (int)Utils.GetEnumParse(typeof(DiceRecords), _oldCheck);
            GetImage(imgIndex).enabled = false;
        }
        _listDice.Clear();
        _recordName = null;
        _oldCheck = null;
    }

    // 미리보기 판
    private void PrevShowBoard()
    {
        if (_dices.Length < 5 || _myTurn == false)
            return;

        GhostScore();

        int player = 0;
        if (_player.Equals(0))
            player = (int)Texts.txtp1Aces;
        else if (_player.Equals(1))
            player = (int)Texts.txtp2Aces;

        for (int i = 0; i < (int)DiceRecords.bonus; i++)
        {
            string key = Utils.GetEnumName(typeof(DiceRecords), i);
            _dicRecorded.TryGetValue(key, out bool value);
            if (value == false)
                GetText(player + i).text = $"{_dicGhostResult[key]}";
        }
    }

    // 미리보기 점수 계산
    private void GhostScore()
    {
        _listDice.Sort();
        var groupBy = _listDice.GroupBy(x => x);

        for (int i = 0; i <= (int)DiceRecords.sixes; i++)
        {
            string key = Utils.GetEnumName(typeof(DiceRecords), i);
            if (groupBy.Any(g => g.Key == (i + 1)))
                _dicGhostResult[key] = groupBy.Where(g => g.Key == (i + 1)).Select(x => x.Count()).First() * (i + 1);
            else
                _dicGhostResult[key] = 0;
        }

        // choice
        {
            _dicGhostResult["choice"] = _listDice.Sum();
        }
        // fourofakind
        {
            int result = 0;
            if (groupBy.Any(g => g.Count() == 4))
                result = groupBy.Where(g => g.Count() == 4).Select(x => x.Key).First() * 4;
            else if (groupBy.Any(g => g.Count() == 5))
                result = groupBy.Where(g => g.Count() == 5).Select(x => x.Key).First() * 5;

            _dicGhostResult["fourofakind"] = result;
        }
        // fullhouse
        {
            int result = 0;

            if ((groupBy.Any(g => g.Count() == 2) &&
                groupBy.Any(g => g.Count() == 3)))
            {
                int num1 = groupBy.Where(g => g.Count() == 2).Select(x => x.Key).First();
                int num2 = groupBy.Where(g => g.Count() == 3).Select(x => x.Key).First();
                result = (num1 * 2) + (num2 * 3);
            }
            else if (groupBy.Any(g => g.Count() == 5))
                result = groupBy.Where(g => g.Count() == 5).Select(x => x.Key).First() * 5;

            _dicGhostResult["fullhouse"] = result;
        }
        // smallstr
        {
            int result = 0;
            int pass = 0;
            for (int i = 0; i < _listDice.Count - 1; i++)
            {
                if (_listDice[i] + 1 == _listDice[i + 1])
                    pass++;
                if (pass == 3)
                {
                    result = 15;
                    break;
                }
            }

            _dicGhostResult["smallstr"] = result;
        }
        // largestr
        {
            int result = 30;

            for (int i = 0; i < _listDice.Count - 1; i++)
            {
                if (_listDice[i] + 1 != _listDice[i + 1])
                {
                    result = 0;
                    break;
                }
            }

            _dicGhostResult["largestr"] = result;
        }
        // yacht
        {
            int result = 0;

            if (groupBy.Any(g => g.Count() == 5))
                result = 50;

            _dicGhostResult["yacht"] = result;
        }
    }

}
