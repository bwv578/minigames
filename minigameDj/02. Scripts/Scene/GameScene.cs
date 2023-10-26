
public class GameScene : BaseScene
{
    protected override bool Init()
    {
        if (base.Init() == false)
            return false;

        Managers.UI.ShowPopupUI<UI_MainPopup>();

        return true;
    }
}
