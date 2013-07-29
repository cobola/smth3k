package com.jimidigi.smth3k.adapter;

import java.util.List;

import android.graphics.Color;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Post;
import com.jimidigi.smth3k.common.BitmapManager;
import com.jimidigi.smth3k.common.DateUtils;
import com.jimidigi.smth3k.common.StringUtility;
import com.jimidigi.smth3k.common.UIHelper;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.jimidigi.smth3k.widget.LinkView;

/**
 * 用户评论Adapter类
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewCommentAdapter extends BaseAdapter {
    private Context context;//运行上下文
    private List<Post> listItems;//数据集合
    private LayoutInflater listContainer;//视图容器
    private int itemViewResource;//自定义项视图源
    private BitmapManager bmpManager;

    static class ListItemView {                //自定义控件集合
        public TextView title;
        public TextView plant;
        public TextView name;
        public TextView date;
        public LinkView content;
        public FrameLayout frameLayout;
    }

    /**
     * 实例化Adapter
     *
     * @param context
     * @param data
     * @param resource
     */
    public ListViewCommentAdapter(Context context, List<Post> data, int resource) {
        this.context = context;
        this.listContainer = LayoutInflater.from(context);    //创建视图容器并设置上下文
        this.itemViewResource = resource;
        this.listItems = data;
        this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(), R.drawable.widget_dface_loading));
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

        //自定义视图
        ListItemView listItemView = null;

        if (convertView == null) {
            //获取list_item布局文件的视图
            convertView = listContainer.inflate(this.itemViewResource, null);

            listItemView = new ListItemView();
            //获取控件对象
            listItemView.title = (TextView) convertView.findViewById(R.id.comment_listitem_title);
            listItemView.plant = (TextView) convertView.findViewById(R.id.comment_listitem_plant);
            listItemView.name = (TextView) convertView.findViewById(R.id.comment_listitem_username);
            listItemView.date = (TextView) convertView.findViewById(R.id.comment_listitem_date);

            listItemView.frameLayout = (FrameLayout) convertView.findViewById(R.id.comment_listitem_att);
            listItemView.content = (LinkView) convertView.findViewById(R.id.comment_listitem_content);

            //设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }

        //设置文字和图片
        Post post = listItems.get(position);
        listItemView.name.setText(post.getAuthor());
        listItemView.date.setText(DateUtils.niceDay(post.getDate()));
        listItemView.plant.setText(post.getFloor());

        listItemView.content.setLinkText(post.getContent());
        if (StringUtility.isNotEmpty(post.getFloor()) && "楼主".equals(post.getFloor())) {
            listItemView.title.setText(post.getTitle());
            listItemView.title.setVisibility(View.VISIBLE);
        } else {
            listItemView.title.setVisibility(View.GONE);
        }

        if (position % 2 == 0) {
            LinearLayout comm = (LinearLayout) convertView.findViewById(R.id.comment_listitem);
            comm.setBackgroundColor(Color.parseColor("#dfdfdf"));
        }
        return convertView;
    }

    private View.OnClickListener faceClickListener = new View.OnClickListener() {
        public void onClick(View v) {
//			Comment comment = (Comment)v.getTag();
//			UIHelper.showUserCenter(v.getContext(), comment.getAuthorId(), comment.getAuthor());
        }
    };
}