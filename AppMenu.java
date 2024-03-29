package com.phonegap.menu;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ListIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

class MenuInfo
{
  public String label = "";
  public Drawable icon;
  public String callback;
  public boolean disabled;
}

public class AppMenu extends Plugin {

    
    private Menu appMenu;
    private ArrayList <MenuInfo> items;
    private boolean menuChanged = false;
    
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        // TODO Auto-generated method stub
        if(action.equals("create"))
        {
            return this.createMenu(args);
        }
        if(action.equals("update"))
        {
            return this.updateMenu(args);
        }
        else if(action.equals("refresh"))
        {
            return this.refresh(args);
        }
        else
        {
            return new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
    }
  
    private PluginResult refresh(JSONArray args) {
        // TODO Auto-generated method stub
        this.menuChanged = true;
        return new PluginResult(PluginResult.Status.OK);
    }

    private PluginResult createMenu(JSONArray args) 
    {
        PluginResult goodResult = updateMenu(args);
    /*
    This should only be done if we are going to be using an action bar
    if(android.os.Build.VERSION.RELEASE.startsWith("3."))
      {
        appMenu = ctx.dMenu;
        buildHoneycombMenu(appMenu);
      }
    */
    //We should do something here if Honeycomb fails!
        return goodResult;
    }
  
    private PluginResult updateMenu(JSONArray args)
    {
        //Toss out all the items, and create a new list
        items = new ArrayList<MenuInfo>();
    
        try {
            String menu = args.getString(0);
            JSONArray menuArr = new JSONArray(menu);
            for(int i = 0; i < menuArr.length(); ++i)
            {
                JSONObject mObject = menuArr.getJSONObject(i);
                MenuInfo info = parseInfo(mObject);
                items.add(info);
            }
            return new PluginResult(PluginResult.Status.OK);
        } catch (JSONException e) {
            //e.printStackTrace();      
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        } 
    }
  
    private MenuInfo parseInfo(JSONObject mObject) throws JSONException
    {
        MenuInfo info = new MenuInfo();
        info.label = mObject.getString("label");
    info.callback = mObject.getString("action");
    if(mObject.has("icon"))
    {
      String tmp_uri = mObject.getString("icon");
      //I don't expect this to work at all  
      try {
        info.icon = getIcon(tmp_uri);
      } catch (IOException e) {
        //DO NOTHING, we just don't have a file here!
      }
    }
    try
    {
      info.disabled = mObject.getBoolean("disabled");
    }
    //Catch the case when "enabled" is not defined
    catch(JSONException e)
    {
      Log.d("AppMenuPlugin", "DISABLED");
      info.disabled = false;
    }
    return info;    
  }
  
  private Drawable getIcon(String tmp_uri) throws IOException {
    AssetManager mgr = this.ctx.getAssets();
    String fileName = "www/" + tmp_uri;
    InputStream image = mgr.open(fileName);
    Drawable icon = Drawable.createFromStream(image, tmp_uri);
    return icon;
  }
  
  public boolean isMenuChanged()
  {
    return menuChanged;
  }
  
    /**
     * Call to build the menu
     * 
     * @param menu
     * @return
     */
    public boolean buildMenu(Menu menu)
    {
      appMenu = menu;
      if(appMenu.size() > 0)
        appMenu.clear();
      ListIterator<MenuInfo> iter = items.listIterator();     
      while(iter.hasNext())
      {
        int itemId = iter.nextIndex();
        MenuInfo item = iter.next();
        appMenu.add(Menu.NONE, itemId, Menu.NONE, item.label);
        if(item.icon != null)
        {
          MenuItem currentItem = menu.getItem(itemId);
          currentItem.setIcon(item.icon);
        }
        if(item.disabled == true) {
          MenuItem currentItem = menu.getItem(itemId);
          currentItem.setEnabled(false);
        }
      }
      menuChanged = false;      
      return true;
    }
    
    public boolean buildHoneycombMenu(final Menu menu)
    {
      final AppMenu that = this;
      ctx.runOnUiThread(new Runnable()
      {
      public void run() {
        menu.clear();
        that.buildMenu(menu);
      }
      });
      return true;
    }
    
    /**
     * Call your receive when menuItem is selected.
     * 
     * @param item
     * @return
     */
    public boolean onMenuItemSelected(MenuItem item)
    {    
      //This is where everything tends to fall down, we should instead something else
      webView.loadUrl("javascript:window.plugins.SimpleMenu.fireCallback(" + item.getItemId() + ")");
      return true;
    }


    @Override
    public void onMessage(String id, Object data) {

        if(id.equals("onPrepareOptionsMenu"))
        {
            Menu menu = (Menu) data;
            menu.clear();
            buildMenu(menu);
        }
        else if(id.equals("onCreateOptionsMenu") && isMenuChanged())
        {
            Menu menu = (Menu) data;
            buildMenu(menu);
        }
        else if(id.equals("onOptionsItemSelected"))
        {
            MenuItem item = (MenuItem) data;
            onMenuItemSelected(item);
        }
        
    }
    
}
