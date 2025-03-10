function search(query){
    var baseUrl = "http://www.shushulou.com/fuighdfiuhgdfoi.php";
    var html = http.get(baseUrl+"?ie=gbk&q=" + URLEncoder.encode(query, "gbk")).body;
    var parse = Jsoup.parse(html, baseUrl);
    var bookListEl = parse.select(".bookbox");
    var results = new ArrayList();
    for (var i=0;i<bookListEl.size();i++){
        var bookEl = bookListEl.get(i);
        var result = new SearchResult();
        result.bookTitle = bookEl.selectFirst(".bookname > a").text();
        result.bookUrl = bookEl.selectFirst(".bookname > a").absUrl("href");
        result.bookAuthor = bookEl.selectFirst(".author").text().replace("作者：","");
        result.latestChapter = bookEl.selectFirst(".update > a").text();
        result.bookCover = bookEl.selectFirst(".bookimg img").absUrl("src");
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
    book.title = parse.selectFirst("#info > h1").text();
    book.author = parse.getElementsByTag("head").get(0).getElementsByAttributeValue("property","og:novel:author").attr("content");
    book.intro = parse.selectFirst("#intro").outerHtml();
    book.cover = parse.selectFirst("#fmimg > img").absUrl("src");
    //加载章节列表
    var chapterList = new ArrayList();
    var chapterListEl = parse.select(".listmain a");
    for(i=chapterListEl.size() - 1; i>= 0;i--){
        var chapterEl = chapterListEl.get(i);
        var chapterA = chapterEl.selectFirst("a");
        var chapter = new Chapter();
        chapter.title = chapterA.text();
        chapter.url = chapterA.absUrl("href");
        chapterList.add(0,chapter);
    }

    book.chapterList = chapterList;
    return book;
}

function getChapterContent(url){
    var html = http.get(url).body;
    return Jsoup.parse(html).selectFirst("#content").outerHtml();
}