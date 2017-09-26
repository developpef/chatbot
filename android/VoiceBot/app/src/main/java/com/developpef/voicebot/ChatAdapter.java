package com.developpef.voicebot;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;
import java.util.TreeSet;

/**
 * Created by frup43821 on 26/09/2017.
 */

public class ChatAdapter extends BaseAdapter {

    private final List<ChatMessage> chatMessages;
    private Activity context;
    private LayoutInflater vi;
    private static final int TYPE_TXT = 0;
    private static final int TYPE_MAP = 1;
    private TreeSet mMapsSet = new TreeSet();

    public ChatAdapter(Activity context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMapsSet.contains(position) ? TYPE_MAP : TYPE_TXT;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        /*ChatMessage chatMessage = getItem(position);
        int type = getItemViewType(position);
        if(type==TYPE_TXT) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = vi.inflate(R.layout.list_item_chat_message, null, false);
                holder = createViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            boolean myMsg = chatMessage.getIsme() ;//Just a dummy check
            //to simulate whether it me or other sender
            setAlignment(holder, myMsg);
            holder.txtMessage.setText(chatMessage.getMessage());
            holder.txtInfo.setText(chatMessage.getDate());
        } else {
            convertView = vi.inflate(R.layout.map_chat_message, null, false);
            Location loc= new Location("GPS_PROVIDER");
            try {
                loc.setLatitude(chatMessage.getData().getDouble("lat"));
                loc.setLongitude(chatMessage.getData().getDouble("lng"));
            } catch(JSONException e) {
                loc.setLatitude(47.113329);
                loc.setLongitude(1.077769);
            }
            MapHolder holder = createMapHolder(convertView, loc);
            holder.txtInfo.setText(chatMessage.getDate());
            convertView.setTag(holder);
        }
        return convertView;*/

        ChatMessage chatMessage = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            if(chatMessage.isMap()) {
                convertView = vi.inflate(R.layout.map_chat_message, null, false);
                Location loc= new Location("GPS_PROVIDER");
                try {
                    loc.setLatitude(chatMessage.getData().getDouble("lat"));
                    loc.setLongitude(chatMessage.getData().getDouble("lng"));
                } catch(JSONException e) {
                    loc.setLatitude(47.113329);
                    loc.setLongitude(1.077769);
                }
                holder = createMapHolder(convertView, loc);
            } else {
                convertView = vi.inflate(R.layout.list_item_chat_message, null, false);
                holder = createViewHolder(convertView);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txtMessage.setText(chatMessage.getMessage());
        holder.txtInfo.setText(chatMessage.getDate());
        if(chatMessage.isMap()) {
            Location loc= new Location("GPS_PROVIDER");
            try {
                loc.setLatitude(chatMessage.getData().getDouble("lat"));
                loc.setLongitude(chatMessage.getData().getDouble("lng"));
            } catch(JSONException e) {
                loc.setLatitude(47.113329);
                loc.setLongitude(1.077769);
            }
            ((MapHolder)holder).mapView = new MapView(this.context,convertView, loc);
        } else if(holder instanceof MapHolder) {
            ((MapHolder)holder).mapView = null;
        }
        setAlignment(holder, chatMessage.getIsme());
        return convertView;
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    public void addMap(ChatMessage message) {
        chatMessages.add(message);
        mMapsSet.add(chatMessages.size() - 1);
    }

    public void add(List<ChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isMe) {
        if (!isMe) {
            holder.contentWithBG.setBackgroundResource(R.drawable.in_msg_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.out_msg_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }

    private MapHolder createMapHolder(View v, Location loc) {
        MapHolder holder = new MapHolder();
        holder.mapView = new MapView(this.context,v, loc);
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }

    private static class MapHolder extends ViewHolder {
        public MapView mapView;
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}
