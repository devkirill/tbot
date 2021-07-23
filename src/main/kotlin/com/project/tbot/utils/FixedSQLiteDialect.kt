package com.project.tbot.utils

import org.hibernate.dialect.SQLiteDialect

class FixedSQLiteDialect : SQLiteDialect() {
    override fun getAddColumnString(): String {
        return "add column"
    }
}