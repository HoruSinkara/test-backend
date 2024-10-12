package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetPostDto): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                body.authorId?.let {
                    this.author = AuthorEntity.findById(it)
                }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            var query = BudgetTable
                .leftJoin(AuthorTable)
                .select { BudgetTable.year eq param.year }

            val total = query.count()
            var data = BudgetEntity.wrapRows(query).map { it.toResponse() }
            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            query = query
                .limit(param.limit, param.offset)

            param.authorFio?.let { fioFilter ->
                query = query.andWhere {
                    AuthorTable.fio.lowerCase() like "%${fioFilter.toLowerCase()}%"
                }
            }

            data = BudgetEntity.wrapRows(query).map { it.toResponse() }
            val sortedData = data.sortedWith(compareBy({ it.month }, { -it.amount }))

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = sortedData
            )
        }
    }
}