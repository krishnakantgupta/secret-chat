package com.kk.secretchat.chat;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kk.secretchat.R;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnCreateContextMenuListener {

    private Context context;
    private ArrayList<ModelChat> list;
    private LayoutInflater mLayoutInflater;
    private int IS_INCOMMING_TYPE = 0;
    private int IS_GOING_TYPE = 1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
    private int position = -1;

    public ChatRecyclerAdapter(Context context) {
        this.context = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setData(ArrayList<ModelChat> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ArrayList<ModelChat> getList() {
        return list == null ? new ArrayList<ModelChat>() : list;
    }

    public void updatePosition(int position, ModelChat modelChat) {
        list.set(position, modelChat);
        notifyItemChanged(position);
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == IS_INCOMMING_TYPE) {
            itemView = mLayoutInflater.inflate(R.layout.view_from, parent, false);
        } else {
            itemView = mLayoutInflater.inflate(R.layout.view_to, parent, false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).bind(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public int getItemViewType(int position) {
        ModelChat modelChat = list.get(position);
        if (modelChat.isIncomming()) {
            return IS_INCOMMING_TYPE;
        }
        return IS_GOING_TYPE;
    }

    public void addNew(@NotNull ModelChat modelChat) {
        if (list == null) {
            list = new ArrayList();
        }
        list.add(0, modelChat);
        notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, 0,
                Menu.NONE, "Copy");
        menu.add(Menu.NONE, 1,
                Menu.NONE, "Reply");
    }

    public void readChat(@NotNull List<String> ids) {
        if (list != null) {
            for(ModelChat chat : list){
                if(!ids.isEmpty() && ids.contains(chat.getChatID())){
                    chat.setRead(true);
                    ids.remove(chat.getChatID());
                }
            }
            notifyDataSetChanged();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMessage, tvUser, tvReplyMsg;
        private TextView tvMessageDate;
        private ImageView messageStatus;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvMessageDate = itemView.findViewById(R.id.tvMessageDate);
            messageStatus = itemView.findViewById(R.id.imgStatus);
            tvReplyMsg = itemView.findViewById(R.id.tvReplyMsg);
        }

        public void bind(int position) {
            ModelChat chat = list.get(position);
            tvUser.setText(!chat.isIncomming() ? "You" : chat.getName());
            tvMessage.setText(chat.getMessage());
            tvMessageDate.setText(dateFormat.format(new Date(chat.getTimestamp())));
            if (!chat.isIncomming()) {
                messageStatus.setVisibility(View.VISIBLE);
                if (chat.isRead()) {
                    messageStatus.setBackgroundResource(R.drawable.ic_read);
                } else if (chat.isDeliver()) {
                    messageStatus.setBackgroundResource(R.drawable.ic_delivered);
                } else if (chat.isSend()) {
                    messageStatus.setBackgroundResource(R.drawable.ic_sent);
                } else {
                    messageStatus.setBackgroundResource(R.drawable.ic_clock);
                }
            } else {
                messageStatus.setVisibility(View.GONE);
            }
            if (chat.getRepliedID() != null && !chat.getRepliedID().startsWith("null")) {
                tvReplyMsg.setText(chat.getRepliedID().substring(chat.getRepliedID().indexOf("##")+2));
                tvReplyMsg.setVisibility(View.VISIBLE);
            } else {
                tvReplyMsg.setVisibility(View.GONE);
            }
        }
    }

}
