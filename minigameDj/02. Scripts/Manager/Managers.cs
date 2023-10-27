
using UnityEngine;

public class Managers : MonoBehaviour
{
    private static Managers s_instance = null;
    public static Managers Instance { get { return s_instance; } }

    #region Core
    private static ResourceManager s_resource = new ResourceManager();
    private static ServerManager s_server = new ServerManager();
    private static UIManager s_ui = new UIManager();

    public static ResourceManager Resource { get { return s_resource; } }
    public static ServerManager Server { get {  return s_server; } }
    public static UIManager UI { get { return s_ui; } }

    #endregion

    private void Awake()
    {
        
    }

    private void Start()
    {
        Init();
    }

    private void Update()
    {
        s_server.OnUpdate();
    }

    private static void Init()
    {
        if (s_instance == null)
        {
            GameObject manager = GameObject.Find("@Managers");
            if (manager == null)             
                manager = new GameObject { name = "@Managers" };
                        
            s_instance = Utils.GetOrAddComponent<Managers>(manager);
            DontDestroyOnLoad(manager);

            s_server.Init();           
        }
    }
}
