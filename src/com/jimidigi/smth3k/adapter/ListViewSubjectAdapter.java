package com.jimidigi.smth3k.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Subject;
import com.jimidigi.smth3k.common.DateUtils;
import com.jimidigi.smth3k.common.UIHelper;
import net.youmi.android.diy.banner.DiyAdSize;
import net.youmi.android.diy.banner.DiyBanner;

import java.util.List;

/**
 * 新闻资讯Adapter类
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewSubjectAdapter extends BaseAdapter {
    private Context context;//运行上下文
    private List<Subject> listItems;//数据集合
    private LayoutInflater listContainer;//视图容器
    private int itemViewResource;//自定义项视图源

    static class ListItemView {                //自定义控件集合
        public TextView board;
        public TextView title;
        public TextView author;
        public TextView postAt;
        public TextView date;
        public TextView replyer;
        public TextView replyAt;
        public TextView replyDate;
        public ImageView commentIco;
        public TextView count;
        public TextView floor;
        public LinearLayout meta;
        public RelativeLayout ad;

    }

    /**
     * 实例化Adapter
     *
     * @param context
     * @param data
     * @param resource
     */
    public ListViewSubjectAdapter(Context context, List<Subject> data, int resource) {
        this.context = context;
        this.listContainer = LayoutInflater.from(context);    //创建视图容器并设置上下文
        this.itemViewResource = resource;
        this.listItems = data;
    }

    public int getCount() {
        return listItems.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    /**
     * ListView Item设置
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.d("method", "getView");
        //设置文字和图片
        Subject subject = listItems.get(position);
        //自定义视图
        ListItemView listItemView = null;

        if (convertView == null) {
            //获取list_item布局文件的视图
            convertView = listContainer.inflate(this.itemViewResource, null);

            listItemView = new ListItemView();
            //获取控件对象

            listItemView.board = (TextView) convertView.findViewById(R.id.subject_listitem_board);
            listItemView.title = (TextView) convertView.findViewById(R.id.subject_listitem_title);
            listItemView.floor = (TextView) convertView.findViewById(R.id.subject_listitem_floor);

            listItemView.meta = (LinearLayout) convertView.findViewById(R.id.subject_listitem_meta);

            listItemView.author = (TextView) convertView.findViewById(R.id.subject_listitem_author);
            listItemView.postAt = (TextView) convertView.findViewById(R.id.subject_listitem_post_at);
            listItemView.date = (TextView) convertView.findViewById(R.id.subject_listitem_date);
            listItemView.replyer = (TextView) convertView.findViewById(R.id.subject_listitem_replyer);
            listItemView.replyAt = (TextView) convertView.findViewById(R.id.subject_listitem_reply_at);
            listItemView.replyDate = (TextView) convertView.findViewById(R.id.subject_listitem_replydate);
            listItemView.commentIco = (ImageView) convertView.findViewById(R.id.subject_listitem_comment_ico);
            listItemView.count = (TextView) convertView.findViewById(R.id.subject_listitem_commentCount);
            //设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }


        switch (subject.getDatatype()) {
            case UIHelper.LISTVIEW_DATATYPE_SUBJECT_HOT:
                listItemView.board.setText("[" + subject.getBoardChsName() + "]");
                listItemView.board.setVisibility(View.VISIBLE);

                listItemView.board.setOnClickListener(boardClickListener);
                listItemView.board.setTag(subject);
                listItemView.meta.setVisibility(View.GONE);
                break;
            case UIHelper.LISTVIEW_DATATYPE_SUBJECT_BOARD:
                listItemView.meta.setVisibility(View.VISIBLE);
                listItemView.author.setText(subject.getAuthor());
                listItemView.replyer.setText(subject.getLastAuthor());
                listItemView.replyDate.setText(DateUtils.niceDay(subject.getLastDate()));
                listItemView.date.setText(DateUtils.niceDay(subject.getDate()));
                listItemView.count.setText(subject.getReplys() + "");
                break;

            case UIHelper.LISTVIEW_DATATYPE_POST_BOARD:
                listItemView.meta.setVisibility(View.VISIBLE);
                listItemView.replyer.setVisibility(View.GONE);
                listItemView.replyDate.setVisibility(View.GONE);
                listItemView.author.setText(subject.getAuthor());
                listItemView.date.setText(DateUtils.niceDay(subject.getDate()));
                break;

        }
        if (subject.isTop()) {
            listItemView.title.setTextColor(Color.RED);
        }

        listItemView.floor.setText(subject.getFloor());
        listItemView.title.setText(subject.getTitle());
        listItemView.title.setTag(subject);//设置隐藏参数(实体类)

        listItemView.title.setOnClickListener(subjectClickListener);
        LinearLayout sub = (LinearLayout) convertView.findViewById(R.id.subject_listitem);

        if (position % 2 == 0) {
            sub.setBackgroundColor(Color.parseColor("#DDDDDD"));
        } else {
            sub.setBackgroundColor(Color.parseColor("#eeeeee"));
        }

        if (position == 1) {

            RelativeLayout adLayout = (RelativeLayout) convertView.findViewById(R.id.AdLayout);
            //demo 1 迷你Banner : 宽满屏，高32dp
            DiyBanner banner = new DiyBanner(convertView.getContext(), DiyAdSize.SIZE_MATCH_SCREENx32);//传入高度为32dp的AdSize来定义迷你Banner
            //将积分Banner加入到布局中
            adLayout.addView(banner);
        }
        return convertView;
    }

    private View.OnClickListener boardClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Subject msg = (Subject) v.getTag();
            UIHelper.showBoardDetail(v.getContext(), msg.getBoardEngName(), UIHelper.LISTVIEW_BOARD_TYPE_SUBJECT, 1);
        }
    };

    private View.OnClickListener subjectClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Subject msg = (Subject) v.getTag();
            if (msg.isAsPost()) {
                UIHelper.showPostDetail(v.getContext(), msg.getBoardEngName(), msg.getSubjectID());
            } else {
                UIHelper.showSubjectDetail(v.getContext(), msg.getBoardEngName(), msg.getSubjectID());
            }
        }
    };


}