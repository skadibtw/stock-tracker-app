package com.example.stockexchange.data

import com.example.stockexchange.data.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MoexStockApi(
    private val baseUrl: String = "https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities/"
) {

    suspend fun fetchQuote(symbol: String): StockQuote = withContext(Dispatchers.IO) {
        val url = URL("${baseUrl}${symbol}.json")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
        }

        val responseText: String = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        val root = JSONObject(responseText)

        val securitiesObj = root.getJSONObject("securities")
        val marketDataObj = root.getJSONObject("marketdata")

        val secColumns = securitiesObj.getJSONArray("columns")
        val secData = securitiesObj.getJSONArray("data")
        val secIdx = columnsToIndex(secColumns)

        val boardIdIdxSec = secIdx["BOARDID"] ?: return@withContext StockQuote(
            symbol,
            symbol,
            0.0,
            0.0,
            0.0
        )
        val shortNameIdx = secIdx["SHORTNAME"] ?: return@withContext StockQuote(
            symbol,
            symbol,
            0.0,
            0.0,
            0.0
        )
        val prevPriceIdx = secIdx["PREVPRICE"] ?: return@withContext StockQuote(
            symbol,
            symbol,
            0.0,
            0.0,
            0.0
        )

        val prevRow = findRowWithBoardAndPrevPrice(secData, boardIdIdxSec, prevPriceIdx)
        val prevPrice = prevRow?.let { safeGetDoubleOrNull(it, prevPriceIdx) } ?: 0.0
        val name = prevRow?.let { safeGetString(it, shortNameIdx) } ?: symbol

        val lastColumns = marketDataObj.getJSONArray("columns")
        val lastData = marketDataObj.getJSONArray("data")
        val mdIdx = columnsToIndex(lastColumns)

        val boardIdIdxMd = mdIdx["BOARDID"]
        val lastIdx = mdIdx["LAST"]
        if (boardIdIdxMd == null || lastIdx == null) {
            return@withContext StockQuote(symbol, name, 0.0, 0.0, 0.0)
        }

        val lastRow = findRowWithBoardId(lastData, boardIdIdxMd, "TQBR")
        val lastPrice = lastRow?.let { safeGetDoubleOrNull(it, lastIdx) } ?: 0.0

        // LASTCHANGE and LASTCHANGEPRCNT can be null depending on the trading state.
        val lastChangeIdx = mdIdx["LASTCHANGE"]
        val lastChangePctIdx = mdIdx["LASTCHANGEPRCNT"]

        val changeAbsFromApi = lastChangeIdx?.let { idx ->
            lastRow?.let { safeGetDoubleOrNull(it, idx) }
        }

        val changeAbs = when {
            changeAbsFromApi != null -> changeAbsFromApi
            prevPrice > 0.0 -> lastPrice - prevPrice
            else -> 0.0
        }

        val changePctFromApi = lastChangePctIdx?.let { idx -> lastRow?.let { safeGetDoubleOrNull(it, idx) } }

        val changePct = when {
            changePctFromApi != null -> changePctFromApi
            prevPrice > 0.0 -> (changeAbs / prevPrice) * 100.0
            else -> 0.0
        }

        StockQuote(
            symbol = symbol,
            name = name,
            price = lastPrice,
            changeAbs = changeAbs,
            changePct = changePct
        )
    }

    private fun columnsToIndex(columns: JSONArray): Map<String, Int> {
        val result = HashMap<String, Int>(columns.length())
        for (i in 0 until columns.length()) {
            result[columns.getString(i)] = i
        }
        return result
    }

    private fun findRowWithBoardAndPrevPrice(
        rows: JSONArray,
        boardIdIdx: Int,
        prevPriceIdx: Int
    ): JSONArray? {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONArray(i)
            val boardId = safeGetStringOrNull(row, boardIdIdx)
            if (boardId == "TQBR" && !row.isNull(prevPriceIdx)) {
                return row
            }
        }
        // Fallback: first row with a non-null PREVPRICE.
        for (i in 0 until rows.length()) {
            val row = rows.getJSONArray(i)
            if (!row.isNull(prevPriceIdx)) return row
        }
        return null
    }

    private fun findRowWithBoardId(rows: JSONArray, boardIdIdx: Int, boardId: String): JSONArray? {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONArray(i)
            val bid = safeGetStringOrNull(row, boardIdIdx)
            if (bid == boardId) return row
        }
        return null
    }

    private fun safeGetDoubleOrNull(row: JSONArray, idx: Int): Double? {
        return if (row.isNull(idx)) null else row.getDouble(idx)
    }

    private fun safeGetString(row: JSONArray, idx: Int): String {
        return if (row.isNull(idx)) "" else row.getString(idx)
    }

    private fun safeGetStringOrNull(row: JSONArray, idx: Int): String? {
        return if (row.isNull(idx)) null else row.getString(idx)
    }
}

