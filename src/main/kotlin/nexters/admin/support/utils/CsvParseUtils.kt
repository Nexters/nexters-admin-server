package nexters.admin.support.utils

import com.opencsv.CSVReader
import nexters.admin.domain.user.member.EMAIL
import nexters.admin.exception.BadRequestException
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader

fun parseCsvFileToMap(file: MultipartFile): Map<String, List<String>> {
    val columnValues: MutableMap<String, MutableList<String>> = mutableMapOf()
    val reader = CSVReader(InputStreamReader(file.inputStream))
    reader.use { csvReader ->
        val lines = csvReader.readAll()
        val rowNum = lines.size
        val colNum = lines.firstOrNull()?.size ?: return columnValues
        for (colIdx in 0 until colNum) {
            val colName: String = lines.firstOrNull()?.get(colIdx) ?: return columnValues
            val values = mutableListOf<String>()
            for (rowIdx in 1 until rowNum) {
                val value = lines[rowIdx]?.get(colIdx) ?: return columnValues
                values.add(value)
            }
            columnValues[colName] = values
        }
        return columnValues
    }
}

fun validateCsvColumns(members: Map<String, List<String>>, requiredKeys: List<String>) {
    if (members.isEmpty()) {
        throw BadRequestException.wrongCsvFile()
    }
    if (!members.keys.containsAll(requiredKeys)) {
        throw BadRequestException.wrongCsvFile()
    }
}

fun hasNoDuplicates(members: Map<String, List<String>>, uniqueKey: String): Boolean {
    return members[uniqueKey]!!.size != setOf(members[uniqueKey]).size
}
