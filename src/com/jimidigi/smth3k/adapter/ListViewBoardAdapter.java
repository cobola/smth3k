package com.jimidigi.smth3k.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jimidigi.smth3k.R;
import com.jimidigi.smth3k.bean.Board;

import java.util.List;

/**
 * 软件分类Adapter类
 *
 * @author liux (http://my.jimidigi.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewBoardAdapter extends BaseAdapter {
    private Context context;//运行上下文
    private List<Board> listItems;//数据集合
    private LayoutInflater listContainer;//视图容器
    private int itemViewResource;//自定义项视图源

    static class ListItemView {                //自定义控件集合
        public ImageView flag;
        public TextView name;
    }

    /**
     * 实例化Adapter
     *
     * @param context
     * @param data
     * @param resource
     */
    public ListViewBoardAdapter(Context context, List<Board> data, int resource) {
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

        //自定义视图
        ListItemView listItemView = null;

        if (convertView == null) {
            //获取list_item布局文件的视图
            convertView = listContainer.inflate(this.itemViewResource, null);

            listItemView = new ListItemView();
            //获取控件对象
            listItemView.name = (TextView) convertView.findViewById(R.id.board_listitem_name);
            listItemView.flag = (ImageView) convertView.findViewById(R.id.board_listitem_flag);

            //设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }

        //设置文字和图片
        Board board = listItems.get(position);

        listItemView.name.setText(board.getChsName());
        listItemView.name.setTag(board);//设置隐藏参数(实体类)
        if (board.isDirectory()) {
            listItemView.flag.setImageResource(R.drawable.widget_directory_icon);
        } else {
            listItemView.flag.setImageResource(R.drawable.widget_board_icon);
        }
        return convertView;
    }
}