package com.sjianjun.reader.utils

import com.sjianjun.reader.bean.Book

val bookComparator = BookComparator()

class BookComparator : Comparator<Book> {
    override fun compare(o1: Book, o2: Book): Int {
        val compareTo = o1.unreadChapterCount.compareTo(o2.unreadChapterCount)
        if (compareTo != 0) {
            return -compareTo
        }
        val compareTitle = o1.title.compareTo(o2.title)
        if (compareTitle != 0) {
            return -compareTitle
        }
        return o1.source.compareTo(o2.source)
    }
}