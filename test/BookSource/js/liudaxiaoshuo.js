var hostUrl = "http://liudatxt.com/";
function search(query){
    var baseUrl = "http://www.txtshuku.org/";
    var queryMap = new HashMap();
    queryMap.put("searchkey", URLEncoder.encode(query, "utf-8"));
    var html = http.post(baseUrl + "search.php",queryMap).body;
    var parse = Jsoup.parse(html, baseUrl);
    var bookListEl = parse.select("#sitembox > dl");
    var results = new ArrayList();
    for (var i=0;i<bookListEl.size();i++){
        var bookEl = bookListEl.get(i);
        var result = new SearchResult();
        result.bookTitle = bookEl.select("> dd:nth-child(2) > h3 > a").text();
        result.bookUrl = bookEl.select("> dt > a").get(0).absUrl("href");
        result.bookAuthor = bookEl.select("> dd:nth-child(3) > span:nth-child(1)").text();
        result.bookCover = bookEl.select("> dt > a > img").get(0).absUrl("src");
        result.latestChapter = bookEl.select("> dd:nth-child(5) > a").get(0).text();
        results.add(result);
    }
    return results;
}

/**
 * 书籍详情[JavaScript.source]
 */
function getDetails(url){
    var parse = Jsoup.parse(http.get(url).body,url);
    var book = new Book();
    book.url = url;
    book.title = parse.select("#bookinfo > div.bookright > div.booktitle > h1").get(0).text();
    book.author = parse.select("#author > a").text();
    book.intro = parse.select("#bookintro").html();
    book.cover = parse.select("#bookimg > img").get(0).absUrl("src");
    //加载章节列表
    var chapterList = new ArrayList();
    var chapterListUrl = parse.select("#newlist > div > strong > a").get(0).absUrl("href");
    var chapterListHtml = Jsoup.parse(http.get(chapterListUrl).body, chapterListUrl);
    var chapterListEl = chapterListHtml.select("#readerlist a");
    for(i=0; i<chapterListEl.size();i++){
        var chapterEl = chapterListEl.get(i);
        var chapter = new Chapter();
        chapter.title = chapterEl.text();
        chapter.url = chapterEl.absUrl("href");
        chapterList.add(chapter);
    }

    book.chapterList = chapterList;
    return book;
}

function getChapterContent(url){
    var html = http.get(url).body;
    return Jsoup.parse(html).select("#content").outerHtml();
}