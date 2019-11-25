/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.sqlite.databaseConnection.jdbc

import com.android.tools.idea.concurrency.FutureCallbackExecutor
import com.android.tools.idea.sqlite.databaseConnection.DatabaseConnection
import com.android.tools.idea.sqlite.databaseConnection.SqliteResultSet
import com.android.tools.idea.sqlite.model.SqliteColumn
import com.android.tools.idea.sqlite.model.SqliteSchema
import com.android.tools.idea.sqlite.model.SqliteStatement
import com.android.tools.idea.sqlite.model.SqliteTable
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.SequentialTaskExecutor
import java.sql.Connection
import java.sql.JDBCType
import java.util.concurrent.Executor

/**
 * Implementation of [DatabaseConnection] for a local Sqlite file using the JDBC driver.
 *
 * This class has a [SequentialTaskExecutor] with one thread, that should be used to make sure that
 * operations are executed sequentially, to avoid concurrency issues with the JDBC objects.
 */
class JdbcDatabaseConnection(
  private val connection: Connection,
  private val sqliteFile: VirtualFile,
  pooledExecutor: Executor
) : DatabaseConnection {
  companion object {
    private val logger: Logger = Logger.getInstance(JdbcDatabaseConnection::class.java)
  }

  val sequentialTaskExecutor = FutureCallbackExecutor.wrap(
    SequentialTaskExecutor.createSequentialApplicationPoolExecutor("Sqlite JDBC service", pooledExecutor)
  )

  override fun close(): ListenableFuture<Unit> = sequentialTaskExecutor.executeAsync {
    connection.close()
    logger.info("Successfully closed database: ${sqliteFile.path}")
  }

  override fun readSchema(): ListenableFuture<SqliteSchema> = sequentialTaskExecutor.executeAsync {
    val tables = connection.metaData.getTables(null, null, null, null)
    val sqliteTables = mutableListOf<SqliteTable>()
    while (tables.next()) {
      sqliteTables.add(
        SqliteTable(
          tables.getString("TABLE_NAME"),
          readColumnDefinitions(connection, tables.getString("TABLE_NAME")),
          isView = tables.getString("TABLE_TYPE") == "VIEW"
        )
      )
    }

    SqliteSchema(sqliteTables).apply { logger.info("Successfully read database schema: ${sqliteFile.path}") }
  }

  private fun readColumnDefinitions(connection: Connection, tableName: String?): ArrayList<SqliteColumn> {
    val columnsSet = connection.metaData.getColumns(null, null, tableName, null)
    val columns = ArrayList<SqliteColumn>()
    while (columnsSet.next()) {
      if (logger.isDebugEnabled) {
        logger.debug("Table \"$tableName\" metadata:")
        for (i in 1..columnsSet.metaData.columnCount) {
          logger.debug("  Column \"${columnsSet.metaData.getColumnName(i)}\" = ${columnsSet.getString(i)}")
        }
      }
      columns.add(
        SqliteColumn(
          columnsSet.getString("COLUMN_NAME"),
          JDBCType.valueOf(columnsSet.getInt("DATA_TYPE"))
        )
      )
    }
    return columns
  }

  override fun executeQuery(sqLiteStatement: SqliteStatement): ListenableFuture<SqliteResultSet> {
    val newSqliteResultSet = JdbcSqliteResultSet(this, connection, sqLiteStatement)
    return Futures.immediateFuture(newSqliteResultSet)
  }

  override fun executeUpdate(sqLiteStatement: SqliteStatement): ListenableFuture<Int> {
    return sequentialTaskExecutor.executeAsync {
      val preparedStatement = connection.resolvePreparedStatement(sqLiteStatement)
      return@executeAsync preparedStatement.executeUpdate().also {
        logger.info("SQL statement \"${sqLiteStatement.sqliteStatementText}\" executed with success.")
      }
    }
  }
}
