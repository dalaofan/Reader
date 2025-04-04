package com.sjianjun.reader.bean

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.sjianjun.coroutine.withIo
import com.sjianjun.reader.http.CookieMgr
import com.sjianjun.reader.http.http
import com.sjianjun.reader.rhino.ContextWrap
import com.sjianjun.reader.rhino.importClassCode
import com.sjianjun.reader.rhino.js
import com.sjianjun.reader.utils.md5
import okhttp3.HttpUrl
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import sjj.alog.Log

@Entity
class BookSource {

    @PrimaryKey
    var id: String = ""
        get() {
            if (field.isEmpty()) {
                field = "${group.trim()}:${name.trim()}"
            }
            return field
        }

    /**
     * 来源 与书籍[Book.source]对应。例如：笔趣阁
     */
    var name: String = ""

    /**
     * 分组名称
     */
    var group: String = ""

    /**
     * - js 脚本内容
     * - 多余的参数从arguments取
     */
    var js: String = ""
    var version: Int = -1
    var enable: Boolean = true
    var requestDelay: Long = 1000L

    /**
     * 书源校验结果
     */
    var checkResult: String? = null

    var checkErrorMsg: String? = null
    @Ignore
    val jsProps = mutableListOf<Pair<String, Any>>()

    /**
     * 书源管理页面是否被选中
     */
    @Ignore
    var selected = false


    inline fun <reified T> execute(func: Func, vararg params: String?): T? {
        Log.i("调用脚本方法：${func}")
        return execute(func.name, *params)
    }

    inline fun <reified T> execute(functionName: String, vararg params: String?): T? {
        return execute {
            val paramList = params.filter { it?.isNotEmpty() == true }
            val result = if (paramList.isEmpty()) {
                eval("${functionName}()","call_func0")
            } else {
                val param = paramList.map { "\"$it\"" }.reduce { acc, s -> "$acc,$s" }
                eval("${functionName}(${param})","call_func0")
            }
            jsToJava<T>(result)
        }
    }


    inline fun <reified T> execute(runner: ContextWrap.() -> T?): T? {
        return js {
            //以前的脚本使用了这个值。以后应该删除这个属性
            putProperty("source", javaToJS(name))
            putProperty("http", javaToJS(http))
            jsProps.forEach {
                putProperty(it.first, javaToJS(it.second))
            }
//            putProperty("context", this)

            eval(headerScript,"headerScript")
            eval(js,"BookSource_${name}")
            runner()
        }
    }

    suspend fun search(query: String): List<SearchResult>? {
        return withIo {
            execute<List<SearchResult>>(Func.search, query)?.also {
                it.forEach {
                    it.bookSource = this@BookSource
                }
            }
        }
    }

    suspend fun getDetails(bookUrl: String): Book? {
        return withIo {
            execute<Book>(Func.getDetails, bookUrl)?.also {
                it.bookSourceId = id
            }
        }
    }

    suspend fun getChapterContent(chapterUrl: String): String? {
        return withIo {
            try {
                execute<String>(Func.getChapterContent, chapterUrl)
            } catch (t: Throwable) {
                Log.e("$name 加载章节内容出错：$chapterUrl", t)
                null
            }
        }
    }



    override fun toString(): String {
        return "BookSource(name='$name', group='$group', version=$version, enable=$enable)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookSource

        if (id != other.id) return false
        if (name != other.name) return false
        if (group != other.group) return false
        if (js != other.js) return false
        if (version != other.version) return false
        if (enable != other.enable) return false
        if (requestDelay != other.requestDelay) return false
        if (checkResult != other.checkResult) return false
        if (checkErrorMsg != other.checkErrorMsg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + js.hashCode()
        result = 31 * result + version
        result = 31 * result + enable.hashCode()
        result = 31 * result + requestDelay.hashCode()
        result = 31 * result + (checkResult?.hashCode() ?: 0)
        result = 31 * result + (checkErrorMsg?.hashCode() ?: 0)
        return result
    }


    enum class Func {
        search, getDetails, getChapterContent
    }

}

val headerScript = """
        ${importClassCode<Jsoup>()}
        ${importClassCode<Log>()}
        ${importClassCode<CookieMgr>()}
        ${importClassCode<SearchResult>()}
        ${importClassCode<Chapter>()}
        ${importClassCode<Book>()}
        ${importClassCode<StringUtil>()}
        ${importClassCode<HttpUrl>()}

        importClass(Packages.java.util.ArrayList)
        importClass(Packages.java.util.HashMap)
        importClass(Packages.java.net.URLEncoder)
        importClass(Packages.java.net.URLDecoder)
        var requestUrl;
        function request(params){
            // url type header data enc
            var header = new HashMap();
            var hd = params.header || {}
            for(k in hd){
                header.put(k,hd[k])
            }
            
            var query = new HashMap();
            var data = params.data || {}
            var enc = params.enc || "utf-8"
            for(k in data){
                query.put(k,URLEncoder.encode(data[k],enc))
            }
    
            if(params.baseUrl==undefined){
                requestUrl = HttpUrl.get(params.url).url().toString()
            }else{
                requestUrl = HttpUrl.get(params.baseUrl).newBuilder(params.url).build().url().toString()
            }
            
            var resp;
            if(params.type=="post"){
                resp = http.post(requestUrl,query,header)
            }else{
                resp = http.get(requestUrl,query,header)
            }
            return resp;
        }
        
        function get(params){
            params.type = "get"
            return Jsoup.parse(request(params).body,requestUrl)
        }
        
        function post(params){
            params.type = "post"
            return Jsoup.parse(request(params).body,requestUrl)
        }
        
        function encode(s,enc){
            return URLEncoder.encode(s,enc||"utf-8")
        }
        
        function decode(s,enc){
            return URLEncoder.decode(s,enc||"utf-8")
        }
        
        function getCookie(url,name){
            return CookieMgr.getCookie(url,name)
        }
        
    """.trimIndent()