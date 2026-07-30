package persistence

import models._
import scala.util.Try

// ai-assisted: #3
// why: Generic CSV serialization interface for parsing and serializing domain models safely.
trait CSVSerializer[T] {
  def toCSV(item: T): String
  def fromCSV(row: String): Try[T]
}

object CSVSerializer {
  
  given BorrowerSerializer: CSVSerializer[Borrower] with {
    override def toCSV(borrower: Borrower): String = {
      s"${escape(borrower.nationalId)},${escape(borrower.name)},${escape(borrower.village)},${borrower.monthlyIncome},${borrower.dependents}"
    }
    
    override def fromCSV(row: String): Try[Borrower] = Try {
      val columns = splitCSV(row)
      require(columns.length == 5, s"Invalid borrower CSV row: $row")
      Borrower(
        unescape(columns(0)), 
        unescape(columns(1)), 
        unescape(columns(2)), 
        columns(3).toDouble, 
        columns(4).toInt
      )
    }
  }
  
  given LoanSerializer: CSVSerializer[Loan] with {
    override def toCSV(loan: Loan): String = loan match {
      case standard: StandardMicroLoan =>
        s"STANDARD,${escape(standard.loanId)},${escape(standard.borrowerId)},${standard.principal},${standard.termMonths},${standard.interestRate}"
      case emergency: EmergencyInterestFreeLoan =>
        s"EMERGENCY,${escape(emergency.loanId)},${escape(emergency.borrowerId)},${emergency.principal},${emergency.termMonths},${emergency.adminFee}"
    }
    
    override def fromCSV(row: String): Try[Loan] = Try {
      val columns = splitCSV(row)
      require(columns.length >= 6, s"Invalid loan CSV row: $row")
      val loanType = columns(0)
      loanType match {
        case "STANDARD" =>
          StandardMicroLoan(
            unescape(columns(1)), 
            unescape(columns(2)), 
            columns(3).toDouble, 
            columns(4).toInt, 
            columns(5).toDouble
          )
        case "EMERGENCY" =>
          EmergencyInterestFreeLoan(
            unescape(columns(1)), 
            unescape(columns(2)), 
            columns(3).toDouble, 
            columns(4).toInt, 
            columns(5).toDouble
          )
        case otherType =>
          throw new IllegalArgumentException(s"Unknown loan type: $otherType")
      }
    }
  }
  
  given RepaymentSerializer: CSVSerializer[Repayment] with {
    override def toCSV(repayment: Repayment): String = {
      s"${escape(repayment.repaymentId)},${escape(repayment.loanId)},${repayment.amountPaid},${escape(repayment.dateString)}"
    }
    
    override def fromCSV(row: String): Try[Repayment] = Try {
      val columns = splitCSV(row)
      require(columns.length == 4, s"Invalid repayment CSV row: $row")
      Repayment(
        unescape(columns(0)), 
        unescape(columns(1)), 
        columns(2).toDouble, 
        unescape(columns(3))
      )
    }
  }
  
  private def escape(field: String): String = {
    field.replace(",", "\\,")
  }
  
  private def unescape(field: String): String = {
    field.replace("\\,", ",")
  }
  
  private def splitCSV(row: String): Array[String] = {
    val parts = row.split("(?<!\\\\),")
    parts.map(part => part.replace("\\,", ","))
  }
}
