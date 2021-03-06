package com.inin.gearphoneapp.app.icws;

//import com.inin.phoneapplication.app.IWatchService;

import com.inin.gearphoneapp.app.GearAccessoryProviderService;
import com.inin.gearphoneapp.app.util.AppLog;

import org.json.JSONArray;
import org.json.JSONObject;
/**
 * Created by kevin.glinski on 3/25/14.
 */
public class AlertWatchingService implements IMessageReceiver {
    //IWatchService _watchService;
    IcwsClient _icwsClient;
    AlertCatalog _alertCatalog;
    QueueWatcher _queueWatcher;

    public AlertWatchingService(AlertCatalog alertCatalog, QueueWatcher queueWatcher, IcwsClient icwsClient){
       // _watchService = watchService;
        _icwsClient = icwsClient;
        _alertCatalog = alertCatalog;
        _queueWatcher = queueWatcher;

        try {
            JSONArray categories = new JSONArray(); //new int[] {2,3,4,5}
            categories.put(2);
            categories.put(3);
            categories.put(4);
            categories.put(5);
            JSONObject obj = new JSONObject();
            obj.put("alertSetCategories", categories);

            _icwsClient.put("/messaging/subscriptions/alerts/alert-catalog", obj);
        }
        catch(Exception ex){}
    }

    public String messageId(){
        return "urn:inin.com:alerts:alertNotificationMessage";
    }

    public void MessageReceived(JSONObject data){
        try {
            JSONArray alertSets = data.getJSONArray("alertNotificationList");

            for (int x = 0; x < alertSets.length(); x++) {
                JSONObject alert = alertSets.getJSONObject(x);
                String ruleId = alert.getString("alertRuleId");

                AlertAction action = _alertCatalog.getAlertAction(ruleId);

                if(action != null && alert.getBoolean("cleared") != true)
                {
                    AppLog.d("AlertWatchingService","Got Alert " + action.getText());

                    if(action.getText().equalsIgnoreCase("WorstActiveCustomerKwScore")) {
                        String interactionId = _queueWatcher.findCallWithLowestCustomerScore();

                        GearAccessoryProviderService.instance.newAlert("Negative Sentiment", "Agent: " + _queueWatcher.getUserName(interactionId), "Workgroup: " + _queueWatcher.getWorkgroup(interactionId), interactionId);
                    }
                }
                else
                {
                    AppLog.d("AlertWatchingService", "got alert " + action.getText() + " but no action");
                }
            }
        }
        catch(Exception ex){}
    }
}
