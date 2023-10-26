
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class UIManager
{
    private int _order;

    private Stack<UI_Popup> _popupStack = new Stack<UI_Popup>();

    public GameObject Root
    {
        get
        {
            GameObject root = GameObject.Find("@UI_Root");
            if (root == null)
                root = new GameObject { name = "@UI_Root" };

            return root;
        }
    }

    public void SetCanvas(GameObject go, bool sort = true)
    {
        Canvas canvas = Utils.GetOrAddComponent<Canvas>(go);
        canvas.renderMode = RenderMode.ScreenSpaceOverlay;
        canvas.overrideSorting = true;

        if (sort)
        {
            canvas.sortingOrder = _order;
            _order++;
        }
        else
            canvas.sortingOrder = 0;
    }

    public T ShowPopupUI<T>(string name = null, Transform parent = null) where T : UI_Popup
    {
        if (string.IsNullOrEmpty(name))
            name = typeof(T).Name;

        GameObject prefab = Managers.Resource.Load<GameObject>($"Prefabs/UI/Popup/{name}");

        GameObject go = Managers.Resource.Instantiate($"UI/Popup/{name}");
        T popup = go.GetComponent<T>();
        _popupStack.Push(popup);

        if (parent != null)
            go.transform.SetParent(parent);
        else
            go.transform.SetParent(Root.transform);

        return popup;
    }


    public void ClosePopupUI(UI_Popup popup)
    {
        if (_popupStack.Count == 0)
            return;

        if (_popupStack.Peek() != popup)                    
            return;

        ClosePopupUI();
    }

    public void ClosePopupUI()
    {
        if (_popupStack.Count == 0)
            return;
        
        UI_Popup popup = _popupStack.Pop();
        Object.Destroy(popup.gameObject);
        popup = null;
        _order--;
    }

    public void ClosePopupUIAll()
    {
        while (_popupStack.Count > 0)
        {
            ClosePopupUI();        
        }
    }
}
