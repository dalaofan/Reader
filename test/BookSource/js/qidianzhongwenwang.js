function search(query){
    var baseUrl = "https://www.qidian.com/";
    var url = "soushu/" + URLEncoder.encode(query, "utf-8")+".html";
    var doc = get({baseUrl:baseUrl,url:url})

    var bookListEl = doc.select(".res-book-item");
    var results = new ArrayList();
    for (var i=0;i<bookListEl.size();i++){
        var bookEl = bookListEl.get(i);
        var result = new SearchResult();
        result.bookTitle = bookEl.select(".book-info-title").get(0).select("a").get(0).text();
        result.bookUrl = "https://m.qidian.com/book/"+(bookEl.select("a[data-bid]").attr("data-bid"));
        result.bookAuthor = bookEl.select(".author").select(".author > :nth-child(2)").get(0).text();
        results.add(result);
    }
    return results;
}

function getDetails(url){
    var doc = get({url:url});
    var book = new Book();
    book.url = url;
    book.title = doc.select("meta[property=\"og:novel:book_name\"]").attr("content");
    book.author = doc.select("meta[property=\"og:novel:author\"]").attr("content");
    book.intro = doc.select("meta[property=\"og:description\"]").attr("content");
    book.cover = doc.select("meta[property=\"og:image\"]").attr("content");
    var chapterListUrl = doc.select("#details-menu").get(0).absUrl("href");
    var docChapterList = get({url:chapterListUrl});
    var elements = docChapterList.select("script");
    var chapterList = new ArrayList();
    var bookId = url.match(/\d+/)
    for (i = 0; i < elements.size(); i++) {
        var data = elements.get(i).data();
        if (data.contains("g_data.volumes")) {
            try{
                data = eval(data+"\n"+"g_data.volumes");
                Log.e(data)
                for (i = 0; i < data.length; i++) {
                    var chapterListJson = data[i]["cs"]
                    for (j = 0; j < chapterListJson.length; j++) {
                        var chapterJson = chapterListJson[j];
                        var chapter = new Chapter();
                        chapter.title = chapterJson["cN"];
                        chapter.url = "https://vipreader.qidian.com/chapter/"+bookId+"/"+chapterJson["id"];
                        chapterList.add(chapter);
                    }
                }
                break;
            }catch(error){
                Log.e(source+"解析章节列表出错，"+error)
                break;
            }
        }
    }
    book.chapterList = chapterList;
    return book;
}

function getChapterContent(url){
    var doc = get({url:url});
    return doc.select(".read-content").html()
}