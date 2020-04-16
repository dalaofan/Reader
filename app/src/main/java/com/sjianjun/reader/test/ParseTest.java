package com.sjianjun.reader.test;

import com.sjianjun.reader.bean.Book;
import com.sjianjun.reader.bean.Chapter;
import com.sjianjun.reader.bean.SearchResult;
import com.sjianjun.reader.http.Http;
import com.sjianjun.reader.http.HttpKt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import sjj.alog.Log;

public final class ParseTest {
    private static String source = "起点中文网";

    public static List<SearchResult> search(Http http, String query) throws Exception {
        List<SearchResult> searchResults = new ArrayList<>();
        String baseUrl = "https://www.qidian.com/";
        String html = http.get(baseUrl + "search?kw=" + URLEncoder.encode(query, "utf-8"));
        Document parse = Jsoup.parse(html, baseUrl);
        Elements bookElItemList = parse.select(".res-book-item");
        Element bookElItm = bookElItemList.get(0);
        SearchResult result = new SearchResult();
        result.source = source;
        result.bookTitle = bookElItm.select(".book-mid-info h4").text();
        result.bookUrl = bookElItm.select(".book-mid-info h4 a").get(0).absUrl("href");
        result.bookAuthor = bookElItm.select(".author a").get(0).text();
        result.latestChapter = bookElItm.select(".update a").get(0).text();
        result.bookCover = bookElItm.select("img").get(0).absUrl("src");
        searchResults.add(result);
        return searchResults;
    }

    public static Book getBookDetails(Http http, String url) {
        Book book = new Book();
        book.url = url;
        book.source = source;
        Document parse = Jsoup.parse(http.get(url), url);
        Element bookInfoEl = parse.select(".book-info").get(0);
        book.title = bookInfoEl.select("h1 em").text();
        book.author = bookInfoEl.select("h1 span a").text();
        book.intro = parse.select(".book-intro").text();
        book.cover = parse.select("#bookImg > img").get(0).absUrl("src");
        List<Chapter> chapterList = new ArrayList<>();

        Elements chapterListEl = parse.select(".volume-wrap > .volume > .cf li a");
        if (chapterListEl.isEmpty()) {
            String bookId = bookInfoEl.select("#addBookBtn").attr("data-bookid");
            String chapterListHtml = http.get("https://m.qidian.com/book/" + bookId + "/catalog");
            Elements phoneChapterListEl = Jsoup.parse(chapterListHtml).select(".chapter-li-a");
            for (int i = 0; i < phoneChapterListEl.size(); i++) {
                Element chapterEl = phoneChapterListEl.get(i);
                Chapter chapter = new Chapter();
                chapter.bookUrl = book.url;
                chapter.title = chapterEl.select("span").text();
                chapter.url = chapterEl.absUrl("href");
                chapterList.add(chapter);
            }

        } else {
            for (int i = 0; i < chapterListEl.size(); i++) {
                Element chapterEl = chapterListEl.get(i);
                Chapter chapter = new Chapter();
                chapter.bookUrl = book.url;
                chapter.title = chapterEl.text();
                chapter.url = chapterEl.absUrl("href");
                chapterList.add(chapter);
            }
        }
        book.chapterList = chapterList;
        return book;
    }

    public static String getBookChapterContent(Http http, String url) {
        String html = http.get(url);
        String chapterContent = Jsoup.parse(html).select(".main-text-wrap  div.read-content").html();
        return chapterContent;
    }


    public static void test() throws Exception {
        List<SearchResult> searchResults = search(HttpKt.getHttp(), "哈利波特之学霸无敌");
        Log.e(searchResults);
        if (searchResults.isEmpty()) {
            return;
        }
        Book book = getBookDetails(HttpKt.getHttp(), searchResults.get(0).bookUrl);
        Log.e(book);
        if (book == null || book.chapterList.isEmpty()) {
            return;
        }
        String content = getBookChapterContent(HttpKt.getHttp(), book.chapterList.get(book.chapterList.size() - 1).url);
        Log.e(content);
    }

}
