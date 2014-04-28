package test;

import com.jimidigi.smth3k.common.StringUtility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12-12-24
 * Time: 下午10:41
 * To change this template use File | Settings | File Templates.
 */
public class TestSmth {


    @Test
    public void test1PostList() throws Exception {

//        String html = SmthSupport.getInstance().getUrlContent("http://m.newsmth.net");

//        PostList postList = PostList.parseHot(html, 0);
//
//        for (Post p : postList.getPostlist()) {
//            System.out.println(p.getTitle());
//        }

        String href="bbstcon.php?board=Movie&gid=2259526";
        System.out.println(href.indexOf("board="));
        System.out.println(href.indexOf("&"));

        System.out.println(StringUtility.substring(href, href.indexOf("board=")+6, href.indexOf("&")));
    }

    @Test
    public void test1PostDetail() throws Exception {


        Document doc = Jsoup.connect("http://www.baidu.com/").get();

        int total = 50000;

        long start = System.currentTimeMillis();
        int i = 0;
        while (i < total) {
            for (Element e : doc.select("a[href]")) {
                System.out.print(e.text());
                break;
            }
            i++;
        }
        System.out.println("");
        System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        i = 0;
        while (i < total) {
            Elements tmp = doc.select("a[href]");

            if (tmp!= null && tmp.size()>0) {
                System.out.print(tmp.first().text());
            }
            i++;
        }

        System.out.println("");
        System.out.println(System.currentTimeMillis() - start);


    }


    @Test
    public void test() {
        System.out.println(StringUtility.parseDate("asdfjh23 12:34:23 al_ksdf").toLocaleString());
    }
}

