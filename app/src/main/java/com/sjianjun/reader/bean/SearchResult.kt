package com.sjianjun.reader.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 搜索结果 bookinfo不完整
 */
@Entity
class SearchResult {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var query: String = ""
}