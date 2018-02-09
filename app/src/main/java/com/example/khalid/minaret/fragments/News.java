package com.example.khalid.minaret.fragments;import android.content.Context;import android.os.Bundle;import android.support.annotation.Nullable;import android.support.design.widget.BottomSheetBehavior;import android.support.v4.app.Fragment;import android.support.v7.widget.LinearLayoutManager;import android.support.v7.widget.RecyclerView;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.EditText;import android.widget.ImageView;import android.widget.LinearLayout;import android.widget.ProgressBar;import com.android.volley.DefaultRetryPolicy;import com.android.volley.Request;import com.android.volley.Response;import com.android.volley.VolleyError;import com.android.volley.toolbox.StringRequest;import com.android.volley.toolbox.Volley;import com.example.khalid.minaret.Adapter.CommentAdapter;import com.example.khalid.minaret.Adapter.NewsAdapter;import com.example.khalid.minaret.OnItemClickListener;import com.example.khalid.minaret.R;import com.example.khalid.minaret.models.Comment;import com.example.khalid.minaret.models.Post;import com.example.khalid.minaret.utils.Database;import com.example.khalid.minaret.utils.EndlessRecyclerOnScrollListener;import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;import org.json.JSONArray;import org.json.JSONException;import org.json.JSONObject;import java.text.ParseException;import java.text.SimpleDateFormat;import java.util.ArrayList;import java.util.Date;import static com.example.khalid.minaret.activities.MainActivity.title;import static com.example.khalid.minaret.utils.Utils.base_url;import static com.example.khalid.minaret.utils.Utils.encodeString;import static com.example.khalid.minaret.utils.Utils.get;import static com.example.khalid.minaret.utils.Utils.html2text;import static com.example.khalid.minaret.utils.Utils.isInternetAvailable;/** * Created by khalid on 12/22/2017. */public class News extends Fragment implements OnItemClickListener {    RecyclerView recyclerView;    ArrayList<Post> news;    DilatingDotsProgressBar dilatingDotsProgressBar;    ProgressBar progressBar;    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");    BottomSheetBehavior sheetBehavior;    LinearLayout layoutBottomSheet;    RecyclerView comment_recycler;    ArrayList<Comment> comments;    Context context;    EditText editText;    ImageView send;    int position = 0;    Database database;    private int currentPage = 1;    public static News newInstance() {        News news = new News();        return news;    }    @Nullable    @Override    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {        View view = inflater.inflate(R.layout.news, container, false);        title.setText("نشاطات واخبار");        context = getActivity();        database = new Database(getActivity());        editText = view.findViewById(R.id.edit);        send = view.findViewById(R.id.send);        recyclerView = view.findViewById(R.id.recycler);        comment_recycler = view.findViewById(R.id.comment_recycler);        comment_recycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));        layoutBottomSheet = view.findViewById(R.id.comment);        LinearLayoutManager mLayoutManager =                new LinearLayoutManager(getActivity());        recyclerView.setLayoutManager(mLayoutManager);        news = new ArrayList<>();        comments = new ArrayList<>();        dilatingDotsProgressBar = view.findViewById(R.id.progress);        progressBar = view.findViewById(R.id.progressBar);        if (!isInternetAvailable(getActivity())) {            progressBar.setVisibility(View.GONE);            news = database.getNews();            recyclerView.setAdapter(new NewsAdapter(getActivity(), news, News.this));        } else {            recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {                @Override                public void onLoadMore(int current) {                    currentPage++;                    dilatingDotsProgressBar.show();                    getNews(currentPage);                }            });            getNews(currentPage);        }        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);        send.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View view) {                insertComment(news.get(position).getId(), encodeString(editText.getText().toString()));                comments.add(new Comment(editText.getText().toString(), format.format(new Date())));                comment_recycler.getAdapter().notifyDataSetChanged();                editText.setText("");            }        });        return view;    }    private void getNews(int page) {        String url = base_url + "api/get_posts?page=" + page;        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url,                new Response.Listener<String>() {                    @Override                    public void onResponse(String response) {                        String id, title, content, date, comment_count, image, favorite_count = "";                        Date date1;                        try {                            JSONObject jsonObject = new JSONObject(response);                            JSONArray jsonArray = jsonObject.getJSONArray("posts");                            for (int i = 0; i < jsonArray.length(); i++) {                                id = jsonArray.getJSONObject(i).getString("id");                                if (!id.equals("42")) {                                    title = jsonArray.getJSONObject(i).getString("title");                                    content = jsonArray.getJSONObject(i).getString("content");                                    try {                                        image = jsonArray.getJSONObject(i).getJSONObject("thumbnail_images").getJSONObject("full").getString("url");                                    } catch (JSONException e) {                                        image = "";                                    }                                    date = jsonArray.getJSONObject(i).getString("date");                                    date1 = format.parse(date);                                    comment_count = jsonArray.getJSONObject(i).getString("comment_count");                                    if (jsonArray.getJSONObject(i).getJSONObject("custom_fields").toString().contains("simplefavorites_count"))                                        favorite_count = jsonArray.getJSONObject(i).getJSONObject("custom_fields").getJSONArray("simplefavorites_count").getString(0);                                    news.add(new Post(id, html2text(title), html2text(content), format.format(date1), image, "", comment_count, favorite_count));                                    database.addNews(new Post(id, html2text(title), html2text(content), format.format(date1), image, "", comment_count, favorite_count));                                }                            }                            recyclerView.setAdapter(new NewsAdapter(getActivity(), news, News.this));                            progressBar.setVisibility(View.GONE);                            dilatingDotsProgressBar.setVisibility(View.GONE);                        } catch (JSONException e) {                            e.printStackTrace();                        } catch (ParseException e) {                            e.printStackTrace();                        }                    }                }, new Response.ErrorListener() {            @Override            public void onErrorResponse(VolleyError error) {            }        });        Volley.newRequestQueue(context).add(stringRequest2).                setRetryPolicy(new DefaultRetryPolicy(                        10000,                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));    }    private void getComment(String id) {        String url = base_url + "api/get_post/?id=" + id;        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url,                new Response.Listener<String>() {                    @Override                    public void onResponse(String response) {                        String content, date;                        Date date1 = null;                        comments = new ArrayList<>();                        try {                            JSONObject jsonObject = new JSONObject(response);                            JSONObject jsonObject1 = jsonObject.getJSONObject("post");                            JSONArray jsonArray = jsonObject1.getJSONArray("comments");                            for (int i = 0; i < jsonArray.length(); i++) {                                content = jsonArray.getJSONObject(i).getString("content");                                date1 = format.parse(jsonArray.getJSONObject(i).getString("date"));                                comments.add(new Comment(html2text(content), format.format(date1)));                            }                            comment_recycler.setAdapter(new CommentAdapter(context, comments));                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);                            dilatingDotsProgressBar.setVisibility(View.GONE);                        } catch (JSONException e) {                            e.printStackTrace();                        } catch (ParseException e) {                            e.printStackTrace();                        }                    }                }, new Response.ErrorListener() {            @Override            public void onErrorResponse(VolleyError error) {            }        });        Volley.newRequestQueue(getActivity()).add(stringRequest2).                setRetryPolicy(new DefaultRetryPolicy(                        0,                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));    }    private void insertComment(String post_id, String content) {        String url = base_url + "api/user/post_comment/?cookie=" + get(context, "cookie") +                "&post_id=" + post_id +                "&content=" + content +                "&comment_status=1";        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url,                new Response.Listener<String>() {                    @Override                    public void onResponse(String response) {                    }                }, new Response.ErrorListener() {            @Override            public void onErrorResponse(VolleyError error) {            }        });        Volley.newRequestQueue(context).add(stringRequest2).                setRetryPolicy(new DefaultRetryPolicy(                        0,                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));    }    @Override    public void onClick(View view, int position) {        getComment(news.get(position).getId());        dilatingDotsProgressBar.setVisibility(View.VISIBLE);        this.position = position;    }}