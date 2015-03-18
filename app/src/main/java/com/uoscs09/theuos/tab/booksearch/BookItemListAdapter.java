package com.uoscs09.theuos.tab.booksearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.UOSApplication;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.JerichoParse;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class BookItemListAdapter extends AbsArrayAdapter<BookItem, GroupHolder> {
    private View.OnLongClickListener ll;
    private ImageLoader imageLoader;

    public BookItemListAdapter(Context context, int layout, List<BookItem> list, View.OnLongClickListener ll) {
        super(context, layout, list);
        imageLoader = ((UOSApplication) ((Activity) context).getApplication()).getImageLoader();

        this.ll = ll;
    }

    private View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BookItem item;
            Object o = v.getTag();
            if (o != null && o instanceof BookItem) {
                item = (BookItem) o;
                if (v instanceof ImageView) {
                    Intent i = AppUtil.setWebPageIntent("http://mlibrary.uos.ac.kr" + item.url);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(i);

                } else if (v instanceof TextView) {
                    if (item.site.startsWith("http")) {
                        Intent i = AppUtil.setWebPageIntent(item.site);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(i);
                    }

                }
            }
        }
    };

    @Override
    public View setView(int position, final View convertView, final GroupHolder holder) {
        final BookItem item = getItem(position);

        if(holder.imageContainer != null)
            holder.imageContainer.cancelRequest();

        holder.coverImg.setImageResource(R.drawable.noimg_en);
        if (!item.coverSrc.equals(StringUtil.NULL))
            holder.imageContainer = imageLoader.get(item.coverSrc, holder);

        holder.coverImg.setOnClickListener(l);
        holder.coverImg.setTag(item);
        holder.title.setText(item.title);
        holder.title.setOnLongClickListener(ll);
        holder.writer.setText(item.writer);
        holder.writer.setOnLongClickListener(ll);
        holder.publish_year.setText(item.bookInfo);
        holder.publish_year.setOnLongClickListener(ll);
        holder.bookState.setText(setSpannableText(item.bookState, 2));
        holder.location.setText(setSpannableText(item.site, 1));
        holder.location.setOnClickListener(l);
        holder.location.setTag(item);

        if (item.bookStateInfoList != null)
            setBookStateLayout(holder.stateInfoLayout, item.bookStateInfoList);
        holder.stateInfoLayout.setVisibility(View.GONE);
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // 설정된 데이터가 없다면
                // 해당 아이템을 처음 터치하는 것 이므로 데이터를 불러옴
                if (item.bookStateInfoList == null) {
                    AsyncLoader.excute(new Callable<List<BookStateInfo>>() {

                        @Override
                        public List<BookStateInfo> call() throws Exception {
                            if (item.infoUrl.equals(StringUtil.NULL)) {
                                return null;
                            } else {
                                return new ParseBookInfo(HttpRequest.getBody(item.infoUrl)).parse();
                            }
                        }
                    }, new AsyncLoader.OnTaskFinishedListener() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public void onTaskFinished(boolean isExceptionOccurred,Object data) {
                            if (!isExceptionOccurred && data != null) {
                                item.bookStateInfoList = (List<BookStateInfo>) data;
                                setBookStateLayout(holder.stateInfoLayout, item.bookStateInfoList);
                                holder.stateInfoLayout.setVisibility(View.VISIBLE);
                                v.requestLayout();
                            }
                        }
                    });
                } else {
                    if (holder.stateInfoLayout.getVisibility() == View.GONE && !item.bookStateInfoList.isEmpty())
                        holder.stateInfoLayout.setVisibility(View.VISIBLE);
                    else
                        holder.stateInfoLayout.setVisibility(View.GONE);
                    v.requestLayout();
                }
            }
        });
        return convertView;
    }

    void setBookStateLayout(LinearLayout layout, List<BookStateInfo> list) {
        final int attachingViewsSize = list.size();
        int childCount = layout.getChildCount();
        if (attachingViewsSize < childCount) {
            // bookStateInfo의 갯수가 LinearLayout의 childView의 갯수보다 적은 경우
            // LinearLayout에 그 차이 만큼 View를 삭제하고, childCount를 변경한다.
            layout.removeViews(attachingViewsSize, childCount - attachingViewsSize);
            childCount = layout.getChildCount();

        } else if (attachingViewsSize > childCount) {
            // bookStateInfo의 갯수가 LinearLayout의 childView의 갯수보다 많은 경우
            // LinearLayout에 그 차이 만큼 View를 추가한다.
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (int i = childCount; i < attachingViewsSize; i++) {
                View v = inflater.inflate(R.layout.list_layout_book_state, layout, false);
                setChildViewContent(v, list.get(i));
                layout.addView(v);
            }
        }

        for (int i = 0; i < childCount; i++) {
            setChildViewContent(layout.getChildAt(i), list.get(i));
        }
    }

    private void setChildViewContent(View v, BookStateInfo info) {
        ChildHolder h = (ChildHolder) v.getTag();
        if (h == null || !(h instanceof ChildHolder)) {
            h = new ChildHolder(v);
            v.setTag(h);
        }

        h.code.setText(info.infoArray[0]);
        h.location.setText(info.infoArray[1]);
        h.state.setText(setSpannableText(info.infoArray[2], 2));
    }

    @Override
    public GroupHolder getViewHolder(View convertView) {
        return new GroupHolder(convertView);
    }


    protected Spannable setSpannableText(final String title, int which) {
        Spannable styledText = new Spannable.Factory().newSpannable(title);
        int last_length = styledText.length();
        switch (which) {
            case 1:
                if (title.startsWith("http")) {
                    styledText = new Spannable.Factory().newSpannable("URL");
                    styledText.setSpan(new URLSpan(title), 0, 3,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    styledText.setSpan(new StyleSpan(Typeface.ITALIC), 0, 3,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
                break;
            case 2:
                if (title.contains("대출가능") || title.contains("온라인")) {
                    styledText.setSpan(
                            new ForegroundColorSpan(getContext().getResources()
                                    .getColor(android.R.color.holo_green_light)),
                            0, last_length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    styledText.setSpan(new ForegroundColorSpan(getContext()
                                    .getResources()
                                    .getColor(android.R.color.holo_red_light)), 0,
                            last_length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
            default:
                break;
        }
        return styledText;
    }
}

class ParseBookInfo extends JerichoParse<BookStateInfo> {
    private static final String[] BOOK_STATE_XML_TAGS = {"call_no", "place_name", "book_state"};

    protected ParseBookInfo(String htmlBody) {
        super(htmlBody);
    }

    @Override
    protected ArrayList<BookStateInfo> parseHttpBody(Source source)
            throws IOException {
        ArrayList<BookStateInfo> bookStateInfoList = new ArrayList<>();
        List<Element> itemList = source.getAllElements("item");
        final int size = itemList.size();

        for (int n = 0; n < size; n++) {
            Element infoItem = itemList.get(n);
            BookStateInfo stateInfo = new BookStateInfo();
            for (int i = 0; i < BOOK_STATE_XML_TAGS.length; i++) {
                Element element = infoItem.getFirstElement(BOOK_STATE_XML_TAGS[i]);

                if (element != null) {
                    stateInfo.infoArray[i] = removeExtra(element.getContent().toString());

                } else {
                    stateInfo.infoArray[i] = StringUtil.NULL;
                    if (i == 1) {
                        element = infoItem.getFirstElement("shelf");
                        if (element != null) {
                            stateInfo.infoArray[i] = removeExtra(element.getContent().toString());
                        }
                    }
                }
            }

            bookStateInfoList.add(stateInfo);
        }

        return bookStateInfoList;
    }

    private String removeExtra(String str) {
        return str.substring(9, str.length() - 3);
    }
}

class GroupHolder implements AbsArrayAdapter.ViewHolder, ImageLoader.ImageListener {
    public TextView title;
    public TextView writer;
    public TextView publish_year;
    public TextView location;
    public TextView bookState;
    public ImageView coverImg;
    public LinearLayout stateInfoLayout;
    ImageLoader.ImageContainer imageContainer;

    public GroupHolder(View v) {
        bookState = (TextView) v.findViewById(R.id.tab_booksearch_list_text_book_state);
        coverImg = (ImageView) v.findViewById(R.id.tab_booksearch_list_image_book_image);
        location = (TextView) v.findViewById(R.id.tab_booksearch_list_text_book_site);
        title = (TextView) v.findViewById(R.id.tab_booksearch_list_text_book_title);
        publish_year = (TextView) v.findViewById(R.id.tab_booksearch_list_text_book_publish_and_year);
        writer = (TextView) v.findViewById(R.id.tab_booksearch_list_text_book_writer);
        stateInfoLayout = (LinearLayout) v.findViewById(R.id.tab_booksearch_layout_book_state);
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
        if(imageContainer.getBitmap() != null){
            coverImg.setImageBitmap(imageContainer.getBitmap());
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }
}

class ChildHolder {
    public TextView location;
    public TextView code;
    public TextView state;

    public ChildHolder(View v) {
        location = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_location);
        code = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_code);
        state = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_state);
    }
}
