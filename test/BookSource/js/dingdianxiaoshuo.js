//请求延迟的时间ms，如果时长小于0将会并发
var hostUrl = "https://www.230book.net/";
function search(query){
    var baseUrl = hostUrl;
    var map = new HashMap();
    map.put("searchkey",URLEncoder.encode(query,"gbk"))
    var html = http.post(baseUrl+"modules/article/search.php",map).body;
    var parse = Jsoup.parse(html,baseUrl);
    try{
        var bookInfo = parse.select(".box_con").get(0);
        var results = new ArrayList();
        var result = new SearchResult();
        result.bookTitle = bookInfo.select("#info").get(0).child(0).text();
        result.bookUrl = parse.getElementsByAttributeValue("property","og:novel:read_url").attr("content").replace("www.230book.com","www.230book.net");
        result.bookAuthor = bookInfo.select("#info").get(0).child(1).text().replace("作 者：","");
        result.bookCover = bookInfo.select("#fmimg").select("img").get(0).absUrl("src");
        result.latestChapter = bookInfo.select("#info").get(0).child(4).select("a").text();
        results.add(result);
        return results;
    }catch(error){
        var bookList = parse.select("tbody").select("tr");
        var results = new ArrayList();
        for (var i=1;i<bookList.size();i++){
            var bookElement = bookList.get(i);
            var result = new SearchResult();
            result.bookTitle = bookElement.select(".odd a").text();
            result.bookUrl = bookElement.select(".odd a").get(0).absUrl("href");
            result.bookAuthor = bookElement.child(2).text();
            //result.bookCover = bookElement.getElementsByClass("c").get(0).getElementsByTag("img").get(0).absUrl("src");
            result.latestChapter = bookElement.select(".even a").text();
            results.add(result);
        }
        return results;

    }
}

/**
 * 书籍详情[JavaScript.source]
 */
function getDetails(url){
    var parse = Jsoup.parse(http.get(url).body,url);
    var book = new Book();
    //书籍信息
    var bookInfo = parse.select(".box_con").get(0);
    book.url = url;
    book.title = bookInfo.select("#info").get(0).child(0).text();
    book.author = bookInfo.select("#info").get(0).child(1).text().replace("作 者：","");
    book.intro = bookInfo.select("#intro").html();
    book.cover = bookInfo.select("#fmimg img").get(0).absUrl("src");
    var children = parse.select("._chapter").select("a");
    var chapterList = new ArrayList();
    for(i=0; i<children.size();i++){
        var chapterEl = children.get(i);
        var chapter = new Chapter();
        chapter.title = chapterEl.text();
        chapter.url = chapterEl.absUrl("href");
        chapterList.add(chapter);
    }
    book.chapterList = chapterList;
    return book;
}

function getChapterContent(url){
    var parse = Jsoup.parse(http.get(url).body,url);
    var content = parse.getElementById("content").html();
    return content;
}