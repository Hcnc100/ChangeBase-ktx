package changeBase

/**
 *  class that save one string formatted
 *  for the use in core.ChangeBase class
 *
 *  @param signed signed of the number, cant be empty space
 *  @param partInteger string that contain the part integer
 *  @param partFractional string that contain the part fractional
 *  @see ChangeBase
 * */

data class SubsStringFormatBase(
    val hasPartFractional: Boolean,
    val signed: String,
    val partInteger: String,
    val partFractional: String
)