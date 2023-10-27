
using UnityEngine;

public class BaseScene : MonoBehaviour
{

    protected bool _init = false;

    void Start()
    {
        Init();
    }

    protected virtual bool Init()
    {
        if (_init)
            return false;

        _init = true;

        return true;
    }
}
