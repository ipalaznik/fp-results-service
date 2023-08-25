package by.formula1.fpieramohi

import by.formula1.fpieramohi.telegram.dto.ResultRow
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.litote.kmongo.sort
import org.litote.kmongo.withDocumentClass

//private val client = KMongo.createClient()
//private val database: MongoDatabase = client.getDatabase("formulaPieramohi")
//private val resultRowsCollection = database.getCollection<Result>("resultRows")
//
//data class Result(
//    val resultRows: List<ResultRow>
//)
//
//fun saveResultRows(resultRows: List<ResultRow>) {
//    resultRowsCollection.insertOne(Result(resultRows))
//}
//
//fun findLatestResultRows(): List<ResultRow>? = resultRowsCollection.withDocumentClass<Result>()
//    .find()
//    .sort("{_id:-1}")
//    .limit(1)
//    .first()
//    ?.resultRows