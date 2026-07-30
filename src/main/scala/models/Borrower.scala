package models

import scala.util.Try

case class Borrower(
  nationalId: String,
  name: String,
  village: String,
  monthlyIncome: Double,
  dependents: Int
)

object Borrower {
  // ai-assisted: #15
  // why: Enforces data validation rules on borrower records before constructing Borrower objects.
  def validate(
    nationalId: String,
    name: String,
    village: String,
    monthlyIncomeStr: String,
    dependentsStr: String
  ): Try[Borrower] = Try {
    val cleanId = nationalId.trim
    val cleanName = name.trim
    val cleanVillage = village.trim
    
    require(cleanId.nonEmpty, "National ID cannot be empty.")
    require(cleanId.forall(character => character.isLetterOrDigit), "National ID must be alphanumeric.")
    require(cleanName.nonEmpty, "Name cannot be empty.")
    require(cleanName.forall(character => character.isLetter || character.isSpaceChar), "Name must only contain letters and spaces.")
    require(cleanVillage.nonEmpty, "Village cannot be empty.")
    
    val incomeValue = monthlyIncomeStr.toDouble
    require(incomeValue >= 0.0, "Monthly income cannot be negative.")
    
    val dependentsCount = dependentsStr.toInt
    require(dependentsCount >= 0, "Dependents count cannot be negative.")
    
    Borrower(cleanId, cleanName, cleanVillage, incomeValue, dependentsCount)
  }
}
