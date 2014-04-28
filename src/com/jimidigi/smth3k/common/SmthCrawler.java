package com.jimidigi.smth3k.common;

import android.util.Log;
import com.jimidigi.smth3k.bean.Post;
import com.jimidigi.smth3k.bean.Result;
import com.jimidigi.smth3k.bean.Subject;
import com.jimidigi.smth3k.bean.User;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class SmthCrawler {
    public static final String UTF8 = "utf-8";
    public static final String GBK = "gbk";
    public static String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.4) Gecko/20091016 Firefox/3.5.4";
    final static String TAG = "SmthCrawler";
    private int threadNum;
    private ExecutorService execService;

    private boolean destroy;

    private static class Holder {
        static SmthCrawler instance = new SmthCrawler();
    }

    private DefaultHttpClient httpClient;

    public static SmthCrawler getIntance() {
        return Holder.instance;
    }

    private SmthCrawler() {
        init();
    }

    public void init() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 10);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
                schemeRegistry);
        httpClient = new DefaultHttpClient(cm, params);
        // 重试
        // httpClient.setHttpRequestRetryHandler(new
        // DefaultHttpRequestRetryHandler(3, false));
        // 超时设置
        // httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
        // 10000);
        // httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT,
        // 10000);
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
                true);

        threadNum = 10;
        execService = Executors.newFixedThreadPool(threadNum);
        destroy = false;
    }

    public List login(String userid, String passwd) {
        String url = "http://m.newsmth.net/user/login";
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("id", userid));
        formparams.add(new BasicNameValuePair("passwd", passwd));
        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(formparams, "GBK");
        } catch (UnsupportedEncodingException e1) {
            return Collections.emptyList();
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("User-Agent", userAgent);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity e = response.getEntity();
            String content = EntityUtils.toString(e, UTF8);
            if (content.contains("你登录的窗口过多")) {
                formparams.add(new BasicNameValuePair("kick_multi", "1"));
                UrlEncodedFormEntity entity2;
                entity2 = new UrlEncodedFormEntity(formparams, UTF8);
                httpPost = new HttpPost(
                        "http://m.newsmth.net/user/login");
                httpPost.setHeader("User-Agent", userAgent);
                httpPost.setEntity(entity2);
                httpClient.execute(httpPost);
            } else if (content.contains("您的用户名并不存在，或者您的密码错误")) {

                return Collections.emptyList();
            } else if (content.contains("用户密码错误")) {
                return Collections.emptyList();
            }
            org.jsoup.nodes.Document doc = Jsoup.parse(content);

            User user = new User();
            List logined = new ArrayList();

            Element tmp = doc.select("[class=sec slist]").first();

            if (tmp != null) {

                Elements a = tmp.getElementsByTag("li");
                if (a != null && a.size() == 3) {
                    user.setUserID(a.get(0).text().replace("当前用户:", ""));
                    user.setLevel(a.get(1).text().replace("等级:", ""));
                    user.setPostNumber(StringUtility.filterUnNumber(a.get(2).text()));
                }
            }

            Result result = new Result();
            result.parseNotice(doc);
            user.setResult(result);

            logined.add(0, user);


            Elements newsList = doc.select("[class=slist sec]").first().getElementsByTag("li");

            List<Subject> slist = new ArrayList<Subject>();
            for (Element e1 : newsList) {
                Subject subject = new Subject();
                subject.setAsPost(false);
                Element title = e1.select("a").first();
                if (title != null) {
                    subject.setTitle(title.text());

                    try {
                        subject.setReplys(Integer.parseInt(title.getElementsByTag("span").text()));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    String href = e1.select("a").attr("href");
                    String[] tmp2 = href.split("/");
                    if (tmp2 != null && tmp2.length == 4) {
                        subject.setSubjectID(tmp2[3]);
                        subject.setBoardEngName(tmp2[2]);
                    }
                    subject.setFloor(StringUtility.substring(e1.text(), e1.text().indexOf("\"") + 1, e1.text().indexOf("|")));

                    slist.add(subject);
                }
            }


            logined.add(1, slist);
            return logined;

        } catch (IOException e2) {
            return Collections.emptyList();
        }

    }

    public Result sendPost(String postUrl, String postTitle, String postContent) {
        HttpPost httpPost = new HttpPost(postUrl);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("title", postTitle));
        formparams.add(new BasicNameValuePair("text", postContent));
        formparams.add(new BasicNameValuePair("signature", "0"));
        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(formparams, UTF8);
        } catch (UnsupportedEncodingException e1) {
            return Result.getError(0, e1.toString());
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("User-Agent", userAgent);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity e = response.getEntity();
            String content = EntityUtils.toString(e, UTF8);

            Result result = new Result();
            if (content.contains("发文成功")) {
                result.setOk(true);
                result.parseNotice(content);
                return result;
            }

        } catch (Exception e) {

        }
        return Result.getError(1, "");
    }

    public Result sendQuickReply(String postUrl, String postTitle, String postContent) {
        HttpPost httpPost = new HttpPost(postUrl);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("subject", "Re%3A%20" + URLEncoder.encode(postTitle)));
        formparams.add(new BasicNameValuePair("content", postContent+"\r\n\r\n --来自 水木三千"));
        formparams.add(new BasicNameValuePair("submit", "快速回复"));


        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(formparams, UTF8);
        } catch (UnsupportedEncodingException e1) {
            return Result.getError(0, e1.toString());
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("User-Agent", userAgent);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity e = response.getEntity();
            String content = EntityUtils.toString(e, UTF8);

            Result result = new Result();
            if (!content.contains("发文成功")) {
                result.setOk(true);
                result.parseNotice(content);
                return result;
            }
        } catch (Exception e) {

        }
        return Result.getError(1, "");
    }

    public Result sendMail(String mailUrl, String mailTitle, String userid, String backup, String mailContent) {
        HttpPost httpPost = new HttpPost(mailUrl);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("title", mailTitle));
        formparams.add(new BasicNameValuePair("backup", "true"));
        formparams.add(new BasicNameValuePair("content", mailContent));
        formparams.add(new BasicNameValuePair("id", userid));


        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(formparams, UTF8);
        } catch (UnsupportedEncodingException e1) {
            return Result.getError(1, e1.toString());
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("User-Agent", userAgent);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity e = response.getEntity();
            String content = EntityUtils.toString(e, UTF8);
            Result result = new Result();
            if (!content.contains("发文成功")) {
                result.setOk(true);
                result.parseNotice(content);
                return result;
            }
        } catch (IOException e) {
            return Result.getError(1, e.toString());
        }
        return Result.getError(1, "");
    }

    public String getRedirectUrl(String url) {
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("User-Agent", userAgent);
        httpget.addHeader("Accept-Encoding", "gzip, deflate");
        String newUrl;
        try {
            HttpResponse response = httpClient.execute(httpget);
            Header locationHeader = response.getLastHeader("Location");
            newUrl = locationHeader.getValue();
        } catch (IOException e) {
            Log.d(TAG, "get url failed,", e);
            newUrl = null;
        }
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
        return newUrl;
    }


    public String getUrlContent(String url) {
        if (StringUtility.isEmpty(url) || !url.startsWith("http")) {
            return "";
        }
        return getUrlContent(url, UTF8);
    }

    public String getUrlContent(String url, String encoding) {
        Log.d(TAG, url);
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("User-Agent", userAgent);
        httpget.addHeader("Accept-Encoding", "gzip, deflate");
        String content;
        try {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            Header[] headers = response.getHeaders("Content-Encoding");
            boolean isgzip = false;
            if (headers != null && headers.length != 0) {
                for (Header header : headers) {
                    String s = header.getValue();
                    if (s.contains("gzip")) {
                        isgzip = true;
                    }
                }
            }
            if (isgzip) {
                InputStream is = entity.getContent();
                BufferedReader br = new java.io.BufferedReader(
                        new InputStreamReader(new GZIPInputStream(is),
                                encoding));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                br.close();
                content = sb.toString();
            } else {
                content = EntityUtils.toString(entity, encoding);
            }
        } catch (IOException e) {
            Log.d(TAG, "get url failed,", e);
            content = null;
        }

        return content;
    }

    public void getPostList(List<Post> postList) {
        if (postList == null)
            return;
        Pattern contentPattern = Pattern.compile("prints\\('(.*?)'\\);",
                Pattern.DOTALL);
        Pattern infoPattern = Pattern.compile("conWriter\\(\\d+, '[^']+', \\d+, (\\d+), (\\d+), (\\d+), '[^']+', (\\d+), \\d+,'([^']+)'\\);");
        List<Future<?>> futureList = new ArrayList<Future<?>>(postList.size());
//		for (Post post : postList) {
//			Future<?> future = execService.submit(new PostContentCrawler(post,
//					contentPattern, infoPattern));
//			futureList.add(future);
//		}
        for (Future<?> future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Log.e(TAG, "excute error", e);
            } catch (ExecutionException e) {
                Log.e(TAG, "excute error", e);
            }
        }
    }

    public void excuteMethod() {

    }

    public void destroy() {
        httpClient.getConnectionManager().shutdown();
        execService.shutdown();
        destroy = true;
    }

    public boolean isDestroy() {
        return destroy;
    }

//	class PostContentCrawler implements Runnable {
//
//		private Post post;
//		private Pattern contentPattern;
//		private Pattern infoPattern;
//
//		public PostContentCrawler(Post post, Pattern contentPattern,
//				Pattern infoPattern) {
//			this.post = post;
//			this.contentPattern = contentPattern;
//			this.infoPattern = infoPattern;
//		}
//
//		@Override
//		public void run() {
//			String url = "http://www.newsmth.net/bbscon.php?bid="
//					+ post.getBoardID() + "&id=" + post.getSubjectID();
//			HttpGet httpget = new HttpGet(url);
//			httpget.setHeader("User-Agent", SmthCrawler.userAgent);
//			httpget.addHeader("Accept-Encoding", "gzip, deflate");
//			String content;
//			try {
//				HttpResponse response = httpClient.execute(httpget);
//				HttpEntity entity = response.getEntity();
//				Header[] headers = response.getHeaders("Content-Encoding");
//				boolean isgzip = false;
//				if (headers != null && headers.length != 0) {
//					for (Header header : headers) {
//						String s = header.getValue();
//						if (s.contains("gzip")) {
//							isgzip = true;
//						}
//					}
//				}
//				if (isgzip) {
//					InputStream is = entity.getContent();
//					BufferedReader br = new java.io.BufferedReader(
//							new InputStreamReader(new GZIPInputStream(is),
//									SmthCrawler.smthEncoding));
//					String line;
//					StringBuilder sb = new StringBuilder();
//					while ((line = br.readLine()) != null) {
//						sb.append(line);
//						sb.append("\n");
//					}
//					br.close();
//					content = sb.toString();
//				} else {
//					content = EntityUtils.toString(entity,
//                            SmthCrawler.smthEncoding);
//				}
//			} catch (IOException e) {
//				Log.d(TAG, "get url failed,", e);
//				return;
//			}
//			Matcher contentMatcher = contentPattern.matcher(content);
//			if (contentMatcher.find()) {
//				String contentString = contentMatcher.group(1);
//				Object[] objects = StringUtility
//						.parsePostContent(contentString);
//				post.setContent((String) objects[0]);
////				post.setDate((java.util.Date) objects[1]);
//			}
//
//			Matcher infoMatcher = infoPattern.matcher(content);
//			if (infoMatcher.find()) {
//				post.setSubjectID(infoMatcher.group(1));
//				post.setTopicSubjectID(infoMatcher.group(2));
//				post.setTitle(infoMatcher.group(5));
//			}
//
//			String bid = null, id = null, ftype = null, num = null, cacheable = null;
//			Matcher attachPartOneMatcher = Pattern.compile(
//                    "attWriter\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+)").matcher(
//					content);
//			if (attachPartOneMatcher.find()) {
//				bid = attachPartOneMatcher.group(1);
//				id = attachPartOneMatcher.group(2);
//				ftype = attachPartOneMatcher.group(3);
//				num = attachPartOneMatcher.group(4);
//				cacheable = attachPartOneMatcher.group(5);
//			}
//
//			ArrayList<Attachment> attachFiles = new ArrayList<Attachment>();
//			Matcher attachPartTwoMatcher = Pattern.compile(
//                    "attach\\('([^']+)', (\\d+), (\\d+)\\)").matcher(content);
//			while (attachPartTwoMatcher.find()) {
//				Attachment innerAtt = new Attachment();
//				innerAtt.setBid(bid);
//				innerAtt.setId(id);
//				innerAtt.setFtype(ftype);
//				innerAtt.setNum(num);
//				innerAtt.setCacheable(cacheable);
//				String name = attachPartTwoMatcher.group(1);
//				String len = attachPartTwoMatcher.group(2);
//				String pos = attachPartTwoMatcher.group(3);
//				innerAtt.setName(name);
//				innerAtt.setLen(len);
//				innerAtt.setPos(pos);
//				attachFiles.add(innerAtt);
//			}
//			post.setAttachFiles(attachFiles);
//		}
//
//	}
}
